"""
Chat API endpoints for the RAG system
"""

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import logging

logger = logging.getLogger(__name__)

router = APIRouter()

# We'll import these here to avoid circular imports
def get_embedding_service():
    from main import embedding_service
    if embedding_service is None:
        raise HTTPException(status_code=503, detail="Embedding service not initialized")
    return embedding_service

def get_vectordb_service():
    from main import vectordb_service
    if vectordb_service is None:
        raise HTTPException(status_code=503, detail="Vector database service not initialized")
    return vectordb_service

def get_llm_service():
    from main import llm_service
    if llm_service is None:
        raise HTTPException(status_code=503, detail="LLM service not initialized")
    return llm_service

# Request/Response models
class ChatRequest(BaseModel):
    """Chat request model"""
    message: str
    session_id: Optional[str] = None
    user_id: Optional[str] = None
    context_limit: Optional[int] = 5

class ChatResponse(BaseModel):
    """Chat response model"""
    response: str
    session_id: str
    sources: List[Dict[str, Any]]
    metadata: Dict[str, Any]

class SearchRequest(BaseModel):
    """Search request model"""
    query: str
    top_k: Optional[int] = 5
    threshold: Optional[float] = 0.7

class SearchResponse(BaseModel):
    """Search response model"""
    results: List[Dict[str, Any]]
    query: str
    total_found: int

@router.post("/chat", response_model=ChatResponse)
async def chat_endpoint(
    request: ChatRequest,
    embedding_service = Depends(get_embedding_service),
    vectordb_service = Depends(get_vectordb_service),
    llm_service = Depends(get_llm_service)
):
    """
    Main chat endpoint for RAG-based legal assistance
    """
    try:
        # Generate query embedding
        query_embedding = await embedding_service.generate_query_embedding(request.message)
        
        # Search for relevant documents
        similar_docs = await vectordb_service.search_similar(
            query_embedding=query_embedding,
            top_k=request.context_limit,
            score_threshold=0.7
        )
        
        # Get chat history (placeholder - implement with database)
        chat_history = []  # TODO: Implement chat history retrieval
        
        # Generate response
        llm_response = await llm_service.generate_response(
            query=request.message,
            context_documents=similar_docs,
            chat_history=chat_history
        )
        
        # TODO: Save chat message to database
        session_id = request.session_id or "default_session"
        
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
async def get_chat_history(session_id: str):
    """
    Get chat history for a session
    """
    try:
        # TODO: Implement chat history retrieval from database
        return {
            "session_id": session_id,
            "messages": [],
            "message": "Chat history retrieval not yet implemented"
        }
        
    except Exception as e:
        logger.error(f"Error getting chat history: {e}")
        raise HTTPException(status_code=500, detail=f"Error retrieving history: {str(e)}")

@router.delete("/sessions/{session_id}")
async def delete_chat_session(session_id: str):
    """
    Delete a chat session and its history
    """
    try:
        # TODO: Implement session deletion
        return {
            "session_id": session_id,
            "message": "Session deletion not yet implemented"
        }
        
    except Exception as e:
        logger.error(f"Error deleting session: {e}")
        raise HTTPException(status_code=500, detail=f"Error deleting session: {str(e)}")

@router.get("/health")
async def chat_health_check():
    """Health check for chat service"""
    return {
        "status": "healthy",
        "service": "chat",
        "endpoints": ["/chat", "/search", "/sessions/{session_id}/history"]
    }
