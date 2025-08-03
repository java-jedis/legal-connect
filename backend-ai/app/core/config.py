"""
Application configuration and settings
"""

import os
from typing import List
from pydantic import BaseSettings

class Settings(BaseSettings):
    """Application settings from environment variables"""
    
    # Application
    app_name: str = "Legal Connect RAG"
    app_version: str = "1.0.0"
    debug: bool = False
    
    # Database
    database_url: str = "postgresql://username:password@localhost:5432/legal_connect_db"
    
    # Vector Database
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    qdrant_api_key: str = ""
    qdrant_collection_name: str = "legal_documents"
    
    # Google AI
    google_api_key: str = ""
    gemini_model: str = "gemini-2.0-flash-exp"
    embedding_model: str = "models/embedding-001"
    
    # Redis
    redis_url: str = "redis://localhost:6379/0"
    
    # JWT
    jwt_secret_key: str = "your_secret_key_here"
    jwt_algorithm: str = "HS256"
    jwt_access_token_expire_minutes: int = 30
    
    # CORS
    cors_origins: List[str] = ["http://localhost:3000", "http://localhost:8080"]
    
    # RAG Configuration
    chunk_size: int = 1000
    chunk_overlap: int = 200
    top_k_results: int = 5
    similarity_threshold: float = 0.7
    max_context_length: int = 4000
    
    # File paths
    json_documents_path: str = "bdcode_json"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

# Global settings instance
settings = Settings()
