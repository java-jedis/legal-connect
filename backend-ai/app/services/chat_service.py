"""
Chat service for managing chat sessions and messages
"""

from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
from sqlalchemy import desc
from app.db.models import ChatSession, ChatMessage, User
from app.db.database import get_db
import uuid
import logging

logger = logging.getLogger(__name__)

class ChatService:
    """Service for managing chat sessions and messages"""
    
    def __init__(self):
        pass
    
    def create_session(self, db: Session, user_id: Optional[str] = None, title: Optional[str] = None) -> str:
        """
        Create a new chat session
        
        Args:
            db: Database session
            user_id: Optional user ID (can be None for anonymous sessions)
            title: Optional session title
            
        Returns:
            Session ID
        """
        try:
            session_id = str(uuid.uuid4())
            
            # If no user_id provided, create anonymous session without user reference
            if user_id:
                # Verify user exists
                user = db.query(User).filter(User.id == user_id).first()
                if not user:
                    logger.warning(f"User {user_id} not found, creating anonymous session")
                    user_id = None
            
            session = ChatSession(
                id=session_id,
                user_id=user_id,
                title=title or "New Chat Session"
            )
            
            db.add(session)
            db.commit()
            db.refresh(session)
            
            logger.info(f"Created chat session {session_id}")
            return session_id
            
        except Exception as e:
            logger.error(f"Error creating chat session: {e}")
            db.rollback()
            raise e
    
    def get_or_create_session(self, db: Session, session_id: Optional[str] = None, 
                             user_id: Optional[str] = None) -> str:
        """
        Get existing session or create a new one
        
        Args:
            db: Database session
            session_id: Optional existing session ID
            user_id: Optional user ID
            
        Returns:
            Session ID
        """
        if session_id:
            # Check if session exists
            session = db.query(ChatSession).filter(ChatSession.id == session_id).first()
            if session:
                return session_id
            else:
                logger.warning(f"Session {session_id} not found, creating new session")
        
        # Create new session
        return self.create_session(db, user_id)
    
    def add_message(self, db: Session, session_id: str, role: str, content: str, 
                   metadata: Optional[Dict[str, Any]] = None) -> str:
        """
        Add a message to a chat session
        
        Args:
            db: Database session
            session_id: Chat session ID
            role: Message role ('user' or 'assistant')
            content: Message content
            metadata: Optional metadata (sources, confidence, etc.)
            
        Returns:
            Message ID
        """
        try:
            # Verify session exists
            session = db.query(ChatSession).filter(ChatSession.id == session_id).first()
            if not session:
                raise ValueError(f"Session {session_id} not found")
            
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
    
    def get_chat_history(self, db: Session, session_id: str, 
                        limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        Get chat history for a session
        
        Args:
            db: Database session
            session_id: Chat session ID
            limit: Optional limit on number of messages to return
            
        Returns:
            List of messages with metadata
        """
        try:
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
