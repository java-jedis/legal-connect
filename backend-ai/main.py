"""
Legal Connect RAG Backend - Main FastAPI Application
Author: Backend Developer
Description: Agentic RAG system for legal document search and analysis
"""

from fastapi import FastAPI, HTTPException, Depends, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
import uvicorn
import os
from dotenv import load_dotenv

# Load environment variables FIRST before any imports
load_dotenv()

# Import our modules
from app.api.endpoints import chat, documents, history
from app.services.embeddings import EmbeddingService
from app.services.vectordb import QdrantService
from app.services.llm import GeminiService
from app.db.database import init_db

# Global services
embedding_service = None
vectordb_service = None
llm_service = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan manager"""
    global embedding_service, vectordb_service, llm_service
    
    # Startup
    print("🚀 Starting Legal Connect RAG API...")
    
    # Debug: Print DATABASE_URL (masked)
    db_url = os.getenv("DATABASE_URL", "NOT_SET")
    if db_url != "NOT_SET":
        # Mask password for security
        masked_url = db_url.split('://')[0] + '://' + db_url.split('://')[1].split(':')[0] + ':****@' + db_url.split('@')[1] if '@' in db_url else db_url
        print(f"📊 Database URL: {masked_url}")
    else:
        print("❌ DATABASE_URL not found in environment variables!")
    
    # Initialize database
    await init_db()
    
    # Initialize services
    embedding_service = EmbeddingService()
    vectordb_service = QdrantService()
    llm_service = GeminiService()
    
    # Initialize vector database
    await vectordb_service.initialize()
    
    print("✅ Legal Connect RAG API started successfully!")
    
    yield
    
    # Shutdown
    if vectordb_service:
        await vectordb_service.close()
    print("👋 Legal Connect RAG API shutdown complete!")

# Initialize FastAPI
app = FastAPI(
    title="Legal Connect RAG API",
    description="Agentic RAG system for Bangladesh legal documents",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure as needed
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Health check endpoint
@app.get("/")
async def root():
    return {
        "message": "Legal Connect RAG API",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs"
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "services": {
            "embedding": embedding_service is not None,
            "vectordb": vectordb_service is not None,
            "llm": llm_service is not None
        }
    }

# Include API routers
app.include_router(chat.router, prefix="/api/v1", tags=["chat"])
app.include_router(documents.router, prefix="/api/v1", tags=["documents"])
app.include_router(history.router, prefix="/api/v1", tags=["history"])

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
