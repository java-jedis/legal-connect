"""
Configuration settings for the RAG Chatbot API
"""
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

class Config:
    # Database Configuration
    DB_HOST = os.getenv('DB_HOST', 'localhost')
    DB_PORT = int(os.getenv('DB_PORT', 5432))
    DB_NAME = os.getenv('DB_NAME', 'rag_app')
    DB_USER = os.getenv('DB_USER', 'postgres')
    DB_PASSWORD = os.getenv('DB_PASSWORD', 'password')
    
    # Google Gemini API Configuration
    GOOGLE_API_KEY = os.getenv('GOOGLE_API_KEY')
    
    # Application Configuration
    DEBUG = os.getenv('DEBUG', 'false').lower() == 'true'
    HOST = os.getenv('HOST', '0.0.0.0')
    PORT = int(os.getenv('PORT', 8000))
    
    # File Upload Configuration
    MAX_FILE_SIZE = 10 * 1024 * 1024  # 10MB
    ALLOWED_EXTENSIONS = ['.pdf', '.docx', '.html', '.txt']
    
    # Chroma Configuration
    CHROMA_PERSIST_DIR = os.getenv('CHROMA_PERSIST_DIR', './chroma_db')
    
    # Text Splitting Configuration
    CHUNK_SIZE = int(os.getenv('CHUNK_SIZE', 1000))
    CHUNK_OVERLAP = int(os.getenv('CHUNK_OVERLAP', 200))
    
    # Retrieval Configuration
    RETRIEVAL_K = int(os.getenv('RETRIEVAL_K', 2))
    
    @classmethod
    def validate_config(cls):
        """Validate that all required configuration is present"""
        errors = []
        
        if not cls.GOOGLE_API_KEY:
            errors.append("GOOGLE_API_KEY is required")
        
        if not cls.DB_PASSWORD or cls.DB_PASSWORD == 'password':
            errors.append("DB_PASSWORD should be set to a secure value")
            
        return errors

# Create a global config instance
config = Config()