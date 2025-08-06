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
    user_id: Optional[str] = None

@router.get("/sessions")
async def list_chat_sessions(
    user_id: Optional[str] = None,
    limit: int = 50,
    offset: int = 0,
    db: Session = Depends(get_db)
):
    """
    List chat sessions for a user
    """
    try:
        from app.services.chat_service import chat_service
        
        if user_id:
            sessions = chat_service.get_user_sessions(db=db, user_id=user_id, limit=limit)
            return {
                "sessions": sessions,
                "total": len(sessions),
                "limit": limit,
                "offset": offset
            }
        else:
            # If no user_id provided, return empty for security
            return {
                "sessions": [],
                "total": 0,
                "limit": limit,
                "offset": offset,
                "message": "User ID required for session listing"
            }
        
    except Exception as e:
        logger.error(f"Error listing chat sessions: {e}")
        raise HTTPException(status_code=500, detail=f"Listing error: {str(e)}")

@router.post("/sessions")
async def create_chat_session(request: CreateSessionRequest, db: Session = Depends(get_db)):
    """
    Create a new chat session
    """
    try:
        from app.services.chat_service import chat_service
        
        session_id = chat_service.create_session(
            db=db, 
            user_id=request.user_id, 
            title=request.title
        )
        
        return {
            "session_id": session_id,
            "title": request.title or "New Chat",
            "created_at": datetime.utcnow(),
            "message": "Session created successfully"
        }
        
    except Exception as e:
        logger.error(f"Error creating chat session: {e}")
        raise HTTPException(status_code=500, detail=f"Creation error: {str(e)}")

@router.get("/sessions/{session_id}/messages")
async def get_session_messages(
    session_id: str,
    limit: int = 100,
    offset: int = 0,
    db: Session = Depends(get_db)
):
    """
    Get messages for a specific session
    """
    try:
        from app.services.chat_service import chat_service
        
        # Verify session exists
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        # Get messages
        messages = chat_service.get_chat_history(db=db, session_id=session_id, limit=limit)
        
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
        logger.error(f"Error getting session messages: {e}")
        raise HTTPException(status_code=500, detail=f"Retrieval error: {str(e)}")

@router.post("/sessions/{session_id}/messages")
async def add_message_to_session(
    session_id: str,
    role: str,
    content: str,
    metadata: Optional[Dict[str, Any]] = None
):
    """
    Add a message to a chat session
    """
    try:
        # TODO: Implement message saving to database
        return {
            "session_id": session_id,
            "message_id": "temp_message_id",
            "role": role,
            "content": content,
            "timestamp": datetime.utcnow(),
            "message": "Message saving not yet implemented"
        }
        
    except Exception as e:
        logger.error(f"Error adding message to session: {e}")
        raise HTTPException(status_code=500, detail=f"Save error: {str(e)}")

@router.put("/sessions/{session_id}")
async def update_session(
    session_id: str,
    title: Optional[str] = None
):
    """
    Update a chat session
    """
    try:
        # TODO: Implement session update in database
        return {
            "session_id": session_id,
            "title": title,
            "updated_at": datetime.utcnow(),
            "message": "Session update not yet implemented"
        }
        
    except Exception as e:
        logger.error(f"Error updating session: {e}")
        raise HTTPException(status_code=500, detail=f"Update error: {str(e)}")

@router.delete("/sessions/{session_id}")
async def delete_session(session_id: str, db: Session = Depends(get_db)):
    """
    Delete a chat session and all its messages
    """
    try:
        from app.services.chat_service import chat_service
        
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
        logger.error(f"Error deleting session: {e}")
        raise HTTPException(status_code=500, detail=f"Deletion error: {str(e)}")

@router.get("/sessions/{session_id}")
async def get_session_details(session_id: str, db: Session = Depends(get_db)):
    """
    Get details of a specific chat session
    """
    try:
        from app.services.chat_service import chat_service
        
        session_info = chat_service.get_session_info(db=db, session_id=session_id)
        if not session_info:
            raise HTTPException(status_code=404, detail=f"Session {session_id} not found")
        
        return session_info
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting session details: {e}")
        raise HTTPException(status_code=500, detail=f"Retrieval error: {str(e)}")

@router.get("/sessions/{session_id}/export")
async def export_session(session_id: str, format: str = "json"):
    """
    Export chat session history
    """
    try:
        # TODO: Implement session export functionality
        return {
            "session_id": session_id,
            "format": format,
            "message": "Session export not yet implemented"
        }
        
    except Exception as e:
        logger.error(f"Error exporting session: {e}")
        raise HTTPException(status_code=500, detail=f"Export error: {str(e)}")

@router.get("/health")
async def history_health_check():
    """Health check for history service"""
    return {
        "status": "healthy",
        "service": "history",
        "endpoints": ["/sessions", "/sessions/{session_id}/messages"]
    }
