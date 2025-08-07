"""
Chat service for managing chat sessions and messages
"""

from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
from sqlalchemy import desc
from app.db.models import ChatSession, ChatMessage
from app.db.database import get_db
import uuid
import logging

logger = logging.getLogger(__name__)

class ChatService:
    """Service for managing chat sessions and messages"""
    
    def __init__(self):
        pass
    
    def create_session(self, db: Session, user_id: str, title: Optional[str] = None) -> str:
        """
        Create a new chat session
        
        Args:
            db: Database session
            user_id: User ID (REQUIRED - extracted from JWT token)
            title: Optional session title
            
        Returns:
            Session ID
            
        Raises:
            ValueError: If user_id is None
        """
        try:
            if not user_id:
                raise ValueError("user_id is required - extracted from JWT token")
            
            # Note: Don't verify user exists in AI backend database
            # User ID comes from authenticated JWT token from main backend
            # This allows main backend users to use AI chat without separate registration
            
            session_id = str(uuid.uuid4())
            
            session = ChatSession(
                id=session_id,
                user_id=user_id,  # ✅ Always required now from JWT
                title=title or "New Chat Session"
            )
            
            db.add(session)
            db.commit()
            db.refresh(session)
            
            logger.info(f"Created chat session {session_id} for user {user_id}")
            return session_id
            
        except Exception as e:
            logger.error(f"Error creating chat session: {e}")
            db.rollback()
            raise e
    
    def get_or_create_session(self, db: Session, session_id: Optional[str] = None, 
                             user_id: str = None) -> str:
        """
        Get existing session or create a new one
        
        Args:
            db: Database session
            session_id: Optional existing session ID
            user_id: User ID (REQUIRED)
            
        Returns:
            Session ID
            
        Raises:
            ValueError: If user_id is None
        """
        if not user_id:
            raise ValueError("user_id is required")
            
        if session_id:
            # Check if session exists and belongs to user
            session = db.query(ChatSession).filter(
                ChatSession.id == session_id,
                ChatSession.user_id == user_id
            ).first()
            if session:
                return session_id
            else:
                logger.warning(f"Session {session_id} not found for user {user_id}, creating new session")
        
        # Create new session
        return self.create_session(db, user_id)
    
    def add_message(self, db: Session, session_id: str, role: str, content: str, 
                   user_id: str, metadata: Optional[Dict[str, Any]] = None) -> str:
        """
        Add a message to a chat session
        
        Args:
            db: Database session
            session_id: Chat session ID
            role: Message role ('user' or 'assistant')
            content: Message content
            user_id: User ID (for ownership verification)
            metadata: Optional metadata (sources, confidence, etc.)
            
        Returns:
            Message ID
            
        Raises:
            ValueError: If session doesn't exist or doesn't belong to user
        """
        try:
            # Verify session exists and belongs to user
            session = db.query(ChatSession).filter(
                ChatSession.id == session_id,
                ChatSession.user_id == user_id
            ).first()
            if not session:
                raise ValueError(f"Session {session_id} not found or not owned by user {user_id}")
            
            message_id = str(uuid.uuid4())
            message = ChatMessage(
                id=message_id,
                session_id=session_id,
                role=role,
                content=content,
                message_metadata=metadata or {}
            )
            
            db.add(message)
            db.commit()
            db.refresh(message)
            
            logger.info(f"Added {role} message to session {session_id}")
            return message_id
            
        except Exception as e:
            logger.error(f"Error adding message: {e}")
            db.rollback()
            raise e
    
    def get_chat_history(self, db: Session, session_id: str, user_id: str,
                        limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        Get chat history for a session
        
        Args:
            db: Database session
            session_id: Chat session ID
            user_id: User ID (for ownership verification)
            limit: Optional limit on number of messages to return
            
        Returns:
            List of messages with metadata
            
        Raises:
            ValueError: If session doesn't exist or doesn't belong to user
        """
        try:
            # Verify session exists and belongs to user
            session = db.query(ChatSession).filter(
                ChatSession.id == session_id,
                ChatSession.user_id == user_id
            ).first()
            if not session:
                raise ValueError(f"Session {session_id} not found or not owned by user {user_id}")
                
            query = db.query(ChatMessage).filter(
                ChatMessage.session_id == session_id
            ).order_by(ChatMessage.created_at)
            
            if limit:
                # Get the most recent messages
                query = query.order_by(desc(ChatMessage.created_at)).limit(limit)
                messages = query.all()
                messages.reverse()  # Return in chronological order
            else:
                messages = query.all()
            
            history = []
            for message in messages:
                history.append({
                    "id": message.id,
                    "role": message.role,
                    "content": message.content,
                    "metadata": message.message_metadata or {},
                    "created_at": message.created_at.isoformat()
                })
            
            logger.info(f"Retrieved {len(history)} messages for session {session_id}")
            return history
            
        except Exception as e:
            logger.error(f"Error retrieving chat history: {e}")
            raise e
    
    def get_session_info(self, db: Session, session_id: str) -> Optional[Dict[str, Any]]:
        """
        Get session information
        
        Args:
            db: Database session
            session_id: Chat session ID
            
        Returns:
            Session information or None if not found
        """
        try:
            session = db.query(ChatSession).filter(ChatSession.id == session_id).first()
            if not session:
                return None
            
            message_count = db.query(ChatMessage).filter(
                ChatMessage.session_id == session_id
            ).count()
            
            return {
                "id": session.id,
                "user_id": session.user_id,
                "title": session.title,
                "message_count": message_count,
                "created_at": session.created_at.isoformat(),
                "updated_at": session.updated_at.isoformat()
            }
            
        except Exception as e:
            logger.error(f"Error getting session info: {e}")
            raise e
    
    def delete_session(self, db: Session, session_id: str) -> bool:
        """
        Delete a chat session and all its messages
        
        Args:
            db: Database session
            session_id: Chat session ID
            
        Returns:
            True if deleted successfully, False if session not found
        """
        try:
            # Delete all messages first
            db.query(ChatMessage).filter(ChatMessage.session_id == session_id).delete()
            
            # Delete the session
            deleted_count = db.query(ChatSession).filter(ChatSession.id == session_id).delete()
            
            db.commit()
            
            if deleted_count > 0:
                logger.info(f"Deleted session {session_id}")
                return True
            else:
                logger.warning(f"Session {session_id} not found for deletion")
                return False
                
        except Exception as e:
            logger.error(f"Error deleting session: {e}")
            db.rollback()
            raise e
    
    def get_user_sessions(self, db: Session, user_id: str, limit: Optional[int] = 20) -> List[Dict[str, Any]]:
        """
        Get all sessions for a user
        
        Args:
            db: Database session
            user_id: User ID
            limit: Optional limit on number of sessions
            
        Returns:
            List of session information
        """
        try:
            query = db.query(ChatSession).filter(
                ChatSession.user_id == user_id
            ).order_by(desc(ChatSession.updated_at))
            
            if limit:
                query = query.limit(limit)
                
            sessions = query.all()
            
            result = []
            for session in sessions:
                message_count = db.query(ChatMessage).filter(
                    ChatMessage.session_id == session.id
                ).count()
                
                result.append({
                    "id": session.id,
                    "title": session.title,
                    "message_count": message_count,
                    "created_at": session.created_at.isoformat(),
                    "updated_at": session.updated_at.isoformat()
                })
            
            return result
            
        except Exception as e:
            logger.error(f"Error getting user sessions: {e}")
            raise e

# Global instance
chat_service = ChatService()
