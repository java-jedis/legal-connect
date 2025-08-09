"""
JWT Authentication middleware for backend-ai service
Integrates with main legal-connect backend authentication system
"""

import jwt
import os
import logging
from datetime import datetime, timedelta
from typing import Optional
from fastapi import HTTPException, Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.core.config import settings

logger = logging.getLogger(__name__)

# Security scheme for JWT tokens
security = HTTPBearer()

class AuthenticationError(Exception):
    """Custom authentication error"""
    pass

def get_jwt_secret() -> str:
    """
    Get JWT secret key from environment or config
    Should match the main legal-connect backend JWT secret
    """
    jwt_secret = os.getenv("JWT_SECRET_KEY") or settings.jwt_secret_key
    if jwt_secret == "your_secret_key_here":
        logger.warning("Using default JWT secret key. Please set JWT_SECRET_KEY environment variable.")
    return jwt_secret

def decode_jwt_token(token: str) -> dict:
    """
    Decode and validate JWT token from main legal-connect backend
    
    Args:
        token: JWT token string
        
    Returns:
        Decoded token payload
        
    Raises:
        AuthenticationError: If token is invalid
    """
    try:
        # Decode with audience and issuer validation to match main backend
        payload = jwt.decode(
            token, 
            get_jwt_secret(), 
            algorithms=[settings.jwt_algorithm],
            audience="LegalConnect-API",  # ✅ Match main backend audience
            issuer="LegalConnect"         # ✅ Match main backend issuer
        )
        return payload
        
    except jwt.ExpiredSignatureError:
        raise AuthenticationError("Token has expired")
    except jwt.InvalidTokenError as e:
        raise AuthenticationError(f"Invalid token: {str(e)}")
    except Exception as e:
        raise AuthenticationError(f"Token validation failed: {str(e)}")

def extract_user_id_from_token(payload: dict) -> str:
    """
    Extract user ID from JWT payload (main backend format)
    
    Args:
        payload: Decoded JWT payload
        
    Returns:
        User ID as string UUID
        
    Raises:
        AuthenticationError: If user ID not found in token
    """
    # Main backend stores user ID in "userId" field as UUID string
    user_id = payload.get("userId")
    
    if not user_id:
        available_fields = list(payload.keys())
        logger.error(f"No userId found in JWT payload. Available fields: {available_fields}")
        raise AuthenticationError("User ID not found in token")
    
    return str(user_id)

def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> str:
    """
    Extract and validate current user ID from JWT token
    
    Args:
        credentials: HTTP Bearer token credentials
        
    Returns:
        Current user ID as string
        
    Raises:
        HTTPException: If authentication fails
    """
    try:
        token = credentials.credentials
        payload = decode_jwt_token(token)
        user_id = extract_user_id_from_token(payload)
        
        logger.info(f"✅ Authenticated user: {user_id}")
        return user_id
        
    except AuthenticationError as e:
        logger.error(f"❌ Authentication failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication failed: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )
    except Exception as e:
        logger.error(f"❌ Unexpected authentication error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed",
            headers={"WWW-Authenticate": "Bearer"},
        )

# Note: We don't need get_current_user() since we don't replicate user data
# User information comes from main backend database via JWT tokens

def verify_session_ownership(session_user_id: str, current_user_id: str) -> bool:
    """
    Verify that the current user owns the session
    
    Args:
        session_user_id: User ID from session
        current_user_id: Current authenticated user ID
        
    Returns:
        True if user owns session
        
    Raises:
        HTTPException: If user doesn't own session
    """
    if session_user_id != current_user_id:
        logger.warning(f"User {current_user_id} attempted to access session owned by {session_user_id}")
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied: You don't own this session"
        )
    return True

# Optional: Create JWT token for testing
def create_access_token(user_id: str, expires_delta: Optional[timedelta] = None) -> str:
    """
    Create JWT access token for testing purposes
    
    Args:
        user_id: User ID to encode
        expires_delta: Token expiration time
        
    Returns:
        JWT token string
    """
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.jwt_access_token_expire_minutes)
    
    to_encode = {
        "sub": user_id,
        "user_id": user_id,
        "exp": expire,
        "iat": datetime.utcnow()
    }
    
    encoded_jwt = jwt.encode(to_encode, get_jwt_secret(), algorithm=settings.jwt_algorithm)
    return encoded_jwt
