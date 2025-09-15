"""
Chat API endpoints for the RAG system
"""

from fastapi import APIRouter, HTTPException, Depends, UploadFile, File
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
import logging

from app.db.database import get_db
from app.services.chat_service import chat_service
from app.services.chat_document_processor import ChatDocumentProcessor
from app.core.auth import get_current_user_id, verify_session_ownership
from app.core.rag_config import RAGConfig

logger = logging.getLogger(__name__)

router = APIRouter()



def get_chat_document_processor():
    from main import embedding_service, vectordb_service
    if embedding_service is None or vectordb_service is None:
        raise HTTPException(status_code=503, detail="Document processing services not initialized")
    return ChatDocumentProcessor(embedding_service, vectordb_service)

def get_embedding_service():
    from main import embedding_service
    if embedding_service is None:
        raise HTTPException(status_code=503, detail="LegalConnect Embedding service not initialized")
    return embedding_service

def get_vectordb_service():
    from main import vectordb_service
    if vectordb_service is None:
        raise HTTPException(status_code=503, detail="LegalConnect Vector database service not initialized")
    return vectordb_service

def get_llm_service():
    from main import llm_service
    if llm_service is None:
        raise HTTPException(status_code=503, detail="LegalConnect LLM service not initialized")
    return llm_service




# Request/Response models
class ChatRequest(BaseModel):
    """Chat request model"""
    message: str
    session_id: Optional[str] = None
    context_limit: Optional[int] = RAGConfig.CHAT_DEFAULT_CONTEXT_LIMIT

class ChatResponse(BaseModel):
    """Chat response model"""
    response: str
    session_id: str
    sources: List[Dict[str, Any]]
    metadata: Dict[str, Any]

class SearchRequest(BaseModel):
    """Search request model"""
    query: str
    top_k: Optional[int] = RAGConfig.SEARCH_DEFAULT_TOP_K
    threshold: Optional[float] = RAGConfig.SEARCH_DEFAULT_THRESHOLD

class SearchResponse(BaseModel):
    """Search response model"""
    results: List[Dict[str, Any]]
    query: str
    total_found: int

class SessionCreateRequest(BaseModel):
    """Session creation request model"""
    title: Optional[str] = None

class DocumentUploadResponse(BaseModel):
    """Document upload response model"""
    success: bool
    document_id: Optional[str] = None
    filename: str
    processing_result: Optional[Dict[str, Any]] = None
    error: Optional[str] = None

class SessionResponse(BaseModel):
    """Session response model"""
    session_id: str
    message: str



# POST endpoint to handle chat requests and return a structured ChatResponse
@router.post("/chat", response_model=ChatResponse)
async def chat_endpoint(
    request: ChatRequest,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
    embedding_service = Depends(get_embedding_service),
    vectordb_service = Depends(get_vectordb_service),
    llm_service = Depends(get_llm_service)
):
    """Legal AI chat endpoint with document retrieval"""
    try:
        session_id = chat_service.get_or_create_session(
            db=db, 
            session_id=request.session_id,
            user_id=current_user_id
        )
        
        if request.session_id:
            session_info = chat_service.get_session_info(db=db, session_id=session_id)
            if session_info and session_info.get("user_id"):
                verify_session_ownership(session_info["user_id"], current_user_id)
        
        # Store user message
        user_message_id = chat_service.add_message(
            db=db,
            session_id=session_id,
            role="user",
            content=request.message,
            user_id=current_user_id
        )
        
        chat_history = chat_service.get_chat_history(
            db=db,
            session_id=session_id,
            user_id=current_user_id,  
            limit=request.context_limit * 2  # Get more history for better context
        )
        
        # Convert chat history to format expected by LLM service
        formatted_history = []
        for msg in chat_history[:-1]:
            formatted_history.append({
                "role": msg["role"],
                "content": msg["content"],
                "timestamp": msg["created_at"]
            })
        
        # Get session documents to filter search
        session_documents = []
        if request.session_id:
            chat_doc_processor = get_chat_document_processor()
            try:
                session_documents = await chat_doc_processor.search_session_documents(
                    query=request.message,
                    session_id=session_id,
                    user_id=current_user_id,
                    top_k=request.context_limit // 2,  # Reserve half for session docs
                    db=db
                )
            except Exception as e:
                logger.warning(f"Could not search session documents: {e}")
        
        # Generate query embedding
        query_embedding = await embedding_service.generate_query_embedding(request.message)
        
        # Search for relevant documents with optimized parameters
        # Reduce general search if have session-specific documents
        general_search_limit = request.context_limit - len(session_documents) if session_documents else request.context_limit
        similar_docs = await vectordb_service.search_similar(
            query_embedding=query_embedding,
            top_k=max(1, general_search_limit),
            score_threshold=RAGConfig.CHAT_DEFAULT_THRESHOLD
        )
        
        all_docs = session_documents + similar_docs
        context_documents = all_docs[:request.context_limit]
        
        # Generate response with chat history context
        llm_response = await llm_service.generate_response(
            query=request.message,
            context_documents=context_documents,
            chat_history=formatted_history
        )
        
        # Save assistant response to database
        chat_service.add_message(
            db=db,
            session_id=session_id,
            role="assistant",
            content=llm_response["response"],
            user_id=current_user_id,
            metadata={
                "sources": llm_response["metadata"]["sources"],
                "model_metadata": llm_response["metadata"]
            }
        )
        
        return ChatResponse(
            response=llm_response["response"],
            session_id=session_id,
            sources=llm_response["metadata"]["sources"],
            metadata=llm_response["metadata"]
        )
        
    except Exception as e:
        logger.error(f"Error in chat endpoint: {e}")
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

@router.post("/search", response_model=SearchResponse)
async def search_endpoint(
    request: SearchRequest,
    embedding_service = Depends(get_embedding_service),
    vectordb_service = Depends(get_vectordb_service)
):
    """
    Search endpoint for finding relevant legal documents
    """
    try:
        # Generate query embedding
        query_embedding = await embedding_service.generate_query_embedding(request.query)
        
        # Search for relevant documents
        results = await vectordb_service.search_similar(
            query_embedding=query_embedding,
            top_k=request.top_k,
            score_threshold=request.threshold
        )
        
        return SearchResponse(
            results=results,
            query=request.query,
            total_found=len(results)
        )
        
    except Exception as e:
        logger.error(f"Error in search endpoint: {e}")
        raise HTTPException(status_code=500, detail=f"Search error: {str(e)}")

@router.get("/sessions/{session_id}/history")
async def get_chat_history(
    session_id: str, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Get chat history for a session"""
    try:
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        # Verify session ownership
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        # Get chat history
        messages = chat_service.get_chat_history(db=db, session_id=session_id, user_id=current_user_id)
        
        return {
            "session_id": session_id,
            "session_info": session_info,
            "messages": messages,
            "total_messages": len(messages)
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting chat history for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Error retrieving history: {str(e)}")

@router.delete("/sessions/{session_id}")
async def delete_chat_session(
    session_id: str, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Delete a chat session and its history"""
    try:
        # Verify session ownership before deletion
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        # Attempt to delete the session
        deleted = chat_service.delete_session(db=db, session_id=session_id)
        
        if not deleted:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        return {
            "session_id": session_id,
            "message": "Session deleted successfully"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error deleting session for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Error deleting session: {str(e)}")






@router.post("/sessions/{session_id}/upload-document", response_model=DocumentUploadResponse)
async def upload_document_to_session(
    session_id: str,
    file: UploadFile = File(...),
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
    chat_doc_processor: ChatDocumentProcessor = Depends(get_chat_document_processor)
):
    """Upload a document to a chat session for RAG processing"""
    try:
        # Validate file type
        allowed_types = ['pdf', 'txt', 'docx', 'doc']
        file_extension = file.filename.split('.')[-1].lower() if '.' in file.filename else ''
        
        if file_extension not in allowed_types:
            raise HTTPException(
                status_code=400, 
                detail=f"Unsupported file type. Allowed: {', '.join(allowed_types)}"
            )
        
        # Validate file size (max 10MB)
        content = await file.read()
        if len(content) > 10 * 1024 * 1024:  # 10MB
            raise HTTPException(status_code=400, detail="File size exceeds 10MB limit")
        
        if len(content) == 0:
            raise HTTPException(status_code=400, detail="Empty file uploaded")
        
        # Process the document
        result = await chat_doc_processor.upload_and_process_document(
            file_content=content,
            filename=file.filename,
            file_type=file_extension,
            session_id=session_id,
            user_id=current_user_id,
            db=db
        )
        
        if result["success"]:
            return DocumentUploadResponse(
                success=True,
                document_id=result["document_id"],
                filename=result["filename"],
                processing_result=result["processing_result"]
            )
        else:
            raise HTTPException(status_code=500, detail=result["error"])
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error uploading document to session {session_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Document upload failed: {str(e)}")

@router.get("/sessions/{session_id}/documents")
async def get_session_documents(
    session_id: str,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
    chat_doc_processor: ChatDocumentProcessor = Depends(get_chat_document_processor)
):
    """Get all documents uploaded to a chat session"""
    try:
        documents = await chat_doc_processor.get_session_documents(
            session_id=session_id,
            user_id=current_user_id,
            db=db
        )
        
        return {
            "session_id": session_id,
            "documents": documents,
            "total_documents": len(documents)
        }
        
    except Exception as e:
        logger.error(f"Error getting session documents: {e}")
        raise HTTPException(status_code=500, detail=f"Error retrieving documents: {str(e)}")

@router.delete("/sessions/{session_id}/documents/{document_id}")
async def delete_session_document(
    session_id: str,
    document_id: str,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
    chat_doc_processor: ChatDocumentProcessor = Depends(get_chat_document_processor)
):
    """Delete a document from a chat session"""
    try:
        deleted = await chat_doc_processor.delete_session_document(
            document_id=document_id,
            session_id=session_id,
            user_id=current_user_id,
            db=db
        )
        
        if deleted:
            return {
                "success": True,
                "message": f"Document {document_id} deleted successfully"
            }
        else:
            raise HTTPException(status_code=404, detail="Document not found")
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error deleting document: {e}")
        raise HTTPException(status_code=500, detail=f"Error deleting document: {str(e)}")




@router.post("/sessions/{session_id}/search-documents")
async def search_session_documents(
    session_id: str,
    request: SearchRequest,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
    chat_doc_processor: ChatDocumentProcessor = Depends(get_chat_document_processor)
):
    """Search within documents uploaded to a specific chat session"""
    try:
        results = await chat_doc_processor.search_session_documents(
            query=request.query,
            session_id=session_id,
            user_id=current_user_id,
            top_k=request.top_k or 5,
            db=db
        )
        
        return {
            "session_id": session_id,
            "query": request.query,
            "results": results,
            "total_found": len(results)
        }
        
    except Exception as e:
        logger.error(f"Error searching session documents: {e}")
        raise HTTPException(status_code=500, detail=f"Search error: {str(e)}")







# Health Check
@router.get("/health")
async def chat_health_check():
    """Health check for chat service"""
    return {
        "status": "healthy",
        "service": "chat",
        "endpoints": [
            "/chat", 
            "/search", 
            "/sessions/{session_id}/history", 
            "/sessions/{session_id}", 
            "/users/{user_id}/sessions",
            "/sessions/{session_id}/upload-document",
            "/sessions/{session_id}/documents",
            "/sessions/{session_id}/documents/{document_id}",
            "/sessions/{session_id}/search-documents"
        ]
    }

@router.get("/users/{user_id}/sessions")
async def get_user_sessions(
    user_id: str, 
    current_user_id: str = Depends(get_current_user_id),
    limit: int = 20, 
    db: Session = Depends(get_db)
):
    try:
        if user_id != current_user_id:
            raise HTTPException(
                status_code=403, 
                detail="Access denied: You can only access your own sessions"
            )
        
        sessions = chat_service.get_user_sessions(db=db, user_id=current_user_id, limit=limit)
        
        return {
            "user_id": current_user_id,
            "sessions": sessions,
            "total_sessions": len(sessions)
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting user sessions for {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Error retrieving user sessions: {str(e)}")

@router.get("/sessions/{session_id}")
async def get_session_info(
    session_id: str, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    
    try:
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")

        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        return session_info
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting session info for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Error retrieving session info: {str(e)}")

@router.post("/sessions", response_model=SessionResponse)
async def create_chat_session(
    request: SessionCreateRequest, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Create a new chat session for the authenticated user
    """
    try:
        session_id = chat_service.create_session(
            db=db, 
            user_id=current_user_id,
            title=request.title
        )
        
        return SessionResponse(
            session_id=session_id,
            message="Session created successfully"
        )
        
    except Exception as e:
        logger.error(f"Error creating session for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Error creating session: {str(e)}")
