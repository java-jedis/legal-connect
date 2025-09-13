"""
RAG Configuration Settings for Optimal Legal Document Retrieval
"""

from typing import Dict, Any

class RAGConfig:
    """Configuration class for RAG system parameters"""
    
    # Chat endpoint optimized parameters
    CHAT_DEFAULT_CONTEXT_LIMIT = 8
    CHAT_DEFAULT_TOP_K = 8
    CHAT_DEFAULT_THRESHOLD = 0.5

    # Search endpoint optimized parameters
    SEARCH_DEFAULT_TOP_K = 8
    SEARCH_DEFAULT_THRESHOLD = 0.5
    
    # RAG service parameters
    RAG_DEFAULT_TOP_K = 8
    RAG_DEFAULT_THRESHOLD = 0.25
    
    @classmethod
    def get_chat_defaults(cls) -> Dict[str, Any]:
        """Get default configuration for chat endpoint"""
        return {
            "context_limit": cls.CHAT_DEFAULT_CONTEXT_LIMIT,
            "top_k": cls.CHAT_DEFAULT_TOP_K,
            "threshold": cls.CHAT_DEFAULT_THRESHOLD
        }
    
    @classmethod
    def get_search_defaults(cls) -> Dict[str, Any]:
        """Get default configuration for search endpoint"""
        return {
            "top_k": cls.SEARCH_DEFAULT_TOP_K,
            "threshold": cls.SEARCH_DEFAULT_THRESHOLD
        }
