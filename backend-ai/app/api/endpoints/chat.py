"""
Chat API endpoints for the RAG system
"""

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
import logging

from app.db.database import get_db
from app.services.chat_service import chat_service
from app.core.auth import get_current_user_id, verify_session_ownership
from app.core.rag_config import RAGConfig

logger = logging.getLogger(__name__)

router = APIRouter()



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
    title: Optional[str] = None  #extracted from JWT token

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
        for msg in chat_history[:-1]:  # Exclude the current message we just added
            formatted_history.append({
                "role": msg["role"],
                "content": msg["content"],
                "timestamp": msg["created_at"]
            })
        
        # Generate query embedding
        query_embedding = await embedding_service.generate_query_embedding(request.message)
        
        # Search for relevant documents with optimized parameters
        similar_docs = await vectordb_service.search_similar(
            query_embedding=query_embedding,
            top_k=request.context_limit,
            score_threshold=RAGConfig.CHAT_DEFAULT_THRESHOLD
        )
        
        # Generate response with chat history context
        llm_response = await llm_service.generate_response(
            query=request.message,
            context_documents=similar_docs,
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
        # Get session info to verify it exists and ownership
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






# Health Check
@router.get("/health")
async def chat_health_check():
    """Health check for chat service"""
    return {
        "status": "healthy",
        "service": "chat",
        "endpoints": ["/chat", "/search", "/sessions/{session_id}/history", "/sessions/{session_id}", "/users/{user_id}/sessions"]
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
