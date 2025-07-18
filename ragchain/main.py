from fastapi import FastAPI, File, UploadFile, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic_models import QueryInput, QueryResponse, DocumentInfo, DeleteFileRequest
from langchain_utils import get_rag_chain
from db_utils import insert_application_logs, get_chat_history, get_all_documents, insert_document_record, delete_document_record
from chroma_utils import index_document_to_chroma, delete_doc_from_chroma
import os
import uuid
import logging
import shutil
from dotenv import load_dotenv
from contextlib import asynccontextmanager

# Load environment variables
load_dotenv()

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('app.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Lifespan context manager for startup/shutdown events
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Starting up RAG Chatbot API...")
    
    # Verify environment variables
    required_env_vars = ['GOOGLE_API_KEY', 'DB_HOST', 'DB_NAME', 'DB_USER', 'DB_PASSWORD']
    missing_vars = [var for var in required_env_vars if not os.getenv(var)]
    if missing_vars:
        logger.error(f"Missing required environment variables: {missing_vars}")
        raise ValueError(f"Missing required environment variables: {missing_vars}")
    
    # Test database connection
    try:
        from db_utils import get_db_connection
        conn = get_db_connection()
        conn.close()
        logger.info("Database connection successful")
    except Exception as e:
        logger.error(f"Database connection failed: {e}")
        raise
    
    logger.info("RAG Chatbot API started successfully")
    yield
    
    # Shutdown
    logger.info("Shutting down RAG Chatbot API...")

# Initialize FastAPI app
app = FastAPI(
    title="Legal Connect RAG API",
    description="A RAG-based chatbot for legal document Q&A",
    version="1.0.0",
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure this properly for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    return {"message": "Legal Connect RAG API is running", "status": "healthy"}

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "rag-chatbot"}

@app.post("/chat", response_model=QueryResponse)
async def chat(query_input: QueryInput):
    try:
        session_id = query_input.session_id or str(uuid.uuid4())
        logger.info(f"Session ID: {session_id}, User Query: {query_input.question}, Model: {query_input.model.value}")

        chat_history = get_chat_history(session_id)
        rag_chain = get_rag_chain(query_input.model.value)
        
        result = rag_chain.invoke({
            "input": query_input.question,
            "chat_history": chat_history
        })
        
        answer = result['answer']

        insert_application_logs(session_id, query_input.question, answer, query_input.model.value)
        logger.info(f"Session ID: {session_id}, AI Response generated successfully")
        
        return QueryResponse(answer=answer, session_id=session_id, model=query_input.model)
    
    except Exception as e:
        logger.error(f"Error in chat endpoint: {e}")
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

@app.post("/upload-doc")
async def upload_and_index_document(file: UploadFile = File(...)):
    allowed_extensions = ['.pdf', '.docx', '.html']
    file_extension = os.path.splitext(file.filename)[1].lower()

    if file_extension not in allowed_extensions:
        raise HTTPException(
            status_code=400, 
            detail=f"Unsupported file type. Allowed types are: {', '.join(allowed_extensions)}"
        )

    temp_file_path = f"temp_{uuid.uuid4()}_{file.filename}"

    try:
        logger.info(f"Uploading file: {file.filename}")
        
        # Save the uploaded file to a temporary file
        with open(temp_file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        file_id = insert_document_record(file.filename)
        success = index_document_to_chroma(temp_file_path, file_id)

        if success:
            logger.info(f"Successfully indexed file: {file.filename} with ID: {file_id}")
            return {
                "message": f"File {file.filename} has been successfully uploaded and indexed.", 
                "file_id": file_id
            }
        else:
            delete_document_record(file_id)
            raise HTTPException(status_code=500, detail=f"Failed to index {file.filename}.")
            
    except Exception as e:
        logger.error(f"Error uploading file {file.filename}: {e}")
        # Clean up file record if it was created
        try:
            if 'file_id' in locals():
                delete_document_record(file_id)
        except:
            pass
        raise HTTPException(status_code=500, detail=f"Error processing file: {str(e)}")
    finally:
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)

@app.get("/list-docs", response_model=list[DocumentInfo])
async def list_documents():
    try:
        documents = get_all_documents()
        return documents
    except Exception as e:
        logger.error(f"Error listing documents: {e}")
        raise HTTPException(status_code=500, detail=f"Error retrieving documents: {str(e)}")

@app.post("/delete-doc")
async def delete_document(request: DeleteFileRequest):
    try:
        logger.info(f"Deleting document with file_id: {request.file_id}")
        
        chroma_delete_success = delete_doc_from_chroma(request.file_id)

        if chroma_delete_success:
            db_delete_success = delete_document_record(request.file_id)
            if db_delete_success:
                logger.info(f"Successfully deleted document with file_id: {request.file_id}")
                return {"message": f"Successfully deleted document with file_id {request.file_id} from the system."}
            else:
                logger.warning(f"Deleted from Chroma but failed to delete from database: {request.file_id}")
                return {
                    "error": f"Deleted from Chroma but failed to delete document with file_id {request.file_id} from the database."
                }
        else:
            logger.error(f"Failed to delete document from Chroma: {request.file_id}")
            return {"error": f"Failed to delete document with file_id {request.file_id} from Chroma."}
            
    except Exception as e:
        logger.error(f"Error deleting document {request.file_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Error deleting document: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app", 
        host=os.getenv("HOST", "0.0.0.0"), 
        port=int(os.getenv("PORT", 8000)), 
        reload=os.getenv("DEBUG", "false").lower() == "true"
    )

