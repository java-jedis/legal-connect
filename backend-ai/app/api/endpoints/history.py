"""
Chat history API endpoints
"""

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from datetime import datetime
from sqlalchemy.orm import Session
import logging

from app.db.database import get_db
from app.core.auth import get_current_user_id, verify_session_ownership

logger = logging.getLogger(__name__)

router = APIRouter()

# Request/Response models
class ChatHistoryItem(BaseModel):
    """Chat history item model"""
    id: str
    role: str
    content: str
    timestamp: datetime
    metadata: Optional[Dict[str, Any]] = None

class ChatSession(BaseModel):
    """Chat session model"""
    id: str
    title: str
    created_at: datetime
    updated_at: datetime
    message_count: int

class CreateSessionRequest(BaseModel):
    """Create session request"""
    title: Optional[str] = None

@router.get("/sessions")
async def list_chat_sessions(
    current_user_id: str = Depends(get_current_user_id),
    limit: int = 50,
    offset: int = 0,
    db: Session = Depends(get_db)
):
    """
    List chat sessions for authenticated user
    """
    try:
        from app.services.chat_service import chat_service
        
        sessions = chat_service.get_user_sessions(
            db=db, 
            user_id=current_user_id,
            limit=limit
        )
        
        return {
            "sessions": sessions,
            "total": len(sessions),
            "user_id": current_user_id,
            "limit": limit,
            "offset": offset
        }
        
    except Exception as e:
        logger.error(f"Error listing sessions for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Listing error: {str(e)}")

@router.post("/sessions")
async def create_chat_session(
    request: CreateSessionRequest, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Create a new chat session for authenticated user
    """
    try:
        from app.services.chat_service import chat_service
        
        session_id = chat_service.create_session(
            db=db, 
            user_id=current_user_id,
            title=request.title
        )
        
        return {
            "session_id": session_id,
            "title": request.title or "New Chat",
            "user_id": current_user_id,
            "created_at": datetime.utcnow(),
            "message": "Session created successfully"
        }
        
    except Exception as e:
        logger.error(f"Error creating session for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Creation error: {str(e)}")

@router.get("/sessions/{session_id}/messages")
async def get_session_messages(
    session_id: str,
    current_user_id: str = Depends(get_current_user_id),
    limit: int = 100,
    offset: int = 0,
    db: Session = Depends(get_db)
):
    """
    Get messages for a specific session (user must own the session)
    """
    try:
        from app.services.chat_service import chat_service
        
        # Verify session exists and belongs to user
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        # Get messages
        messages = chat_service.get_chat_history(
            db=db, 
            session_id=session_id, 
            user_id=current_user_id,
            limit=limit
        )
        
        return {
            "session_id": session_id,
            "messages": messages,
            "total": len(messages),
            "limit": limit,
            "offset": offset
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting session messages for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Retrieval error: {str(e)}")

@router.post("/sessions/{session_id}/messages")
async def add_message_to_session(
    session_id: str,
    role: str,
    content: str,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
    metadata: Optional[Dict[str, Any]] = None
):
    """
    Add a message to a chat session
    """
    try:
        from app.services.chat_service import chat_service
        
        # Verify session ownership
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        # Add message using chat service
        message_id = chat_service.add_message(
            db=db,
            session_id=session_id,
            role=role,
            content=content,
            user_id=current_user_id,
            metadata=metadata
        )
        
        return {
            "session_id": session_id,
            "message_id": message_id,
            "role": role,
            "content": content,
            "timestamp": datetime.utcnow(),
            "message": "Message saved successfully"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error adding message to session: {e}")
        raise HTTPException(status_code=500, detail=f"Save error: {str(e)}")

@router.put("/sessions/{session_id}")
async def update_session(
    session_id: str,
    title: Optional[str] = None,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Update a chat session
    """
    try:
        from app.services.chat_service import chat_service
        
        # Verify session ownership
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        # Update session using chat service
        updated = chat_service.update_session(
            db=db,
            session_id=session_id,
            user_id=current_user_id,
            title=title
        )
        
        if not updated:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        return {
            "session_id": session_id,
            "title": title,
            "updated_at": datetime.utcnow(),
            "message": "Session updated successfully"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error updating session: {e}")
        raise HTTPException(status_code=500, detail=f"Update error: {str(e)}")

@router.delete("/sessions/{session_id}")
async def delete_session(
    session_id: str, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Delete a chat session and all its messages"""
    try:
        from app.services.chat_service import chat_service
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
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
        raise HTTPException(status_code=500, detail=f"Deletion error: {str(e)}")

@router.get("/sessions/{session_id}")
async def get_session_details(
    session_id: str, 
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """Get details of a specific chat session"""
    try:
        from app.services.chat_service import chat_service
        
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        if session_info.get("user_id"):
            verify_session_ownership(session_info["user_id"], current_user_id)
        
        return session_info
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting session details for user {current_user_id}: {e}")
        raise HTTPException(status_code=500, detail=f"Retrieval error: {str(e)}")

@router.get("/health")
async def history_health_check():
    """Health check for history service"""
    return {
        "status": "healthy",
        "service": "history",
        "endpoints": ["/sessions", "/sessions/{session_id}/messages"]
    }
