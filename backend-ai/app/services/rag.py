"""
RAG Service - Main service that coordinates the RAG pipeline
"""

import logging
from typing import List, Dict, Any, Optional

from app.services.embeddings import EmbeddingService
from app.services.vectordb import QdrantService
from app.services.llm import GeminiService

logger = logging.getLogger(__name__)

class RAGService:
    """Main RAG service that coordinates query processing"""
    
    def __init__(
        self,
        embedding_service: EmbeddingService,
        vectordb_service: QdrantService,
        llm_service: GeminiService
    ):
        self.embedding_service = embedding_service
        self.vectordb_service = vectordb_service
        self.llm_service = llm_service
        
        logger.info("Initialized RAGService")
    
    async def process_query(
        self,
        query: str,
        top_k: int = 5,
        score_threshold: float = 0.7,
        chat_history: List[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """
        Process a user query through the complete RAG pipeline
        
        Args:
            query: User's question
            top_k: Number of documents to retrieve
            score_threshold: Minimum similarity score
            chat_history: Previous conversation context
        
        Returns:
            Complete response with sources and metadata
        """
        try:
            # Step 1: Generate query embedding
            logger.info(f"Processing query: {query[:100]}...")
            query_embedding = await self.embedding_service.generate_query_embedding(query)
            
            # Step 2: Retrieve relevant documents
            similar_docs = await self.vectordb_service.search_similar(
                query_embedding=query_embedding,
                top_k=top_k,
                score_threshold=score_threshold
            )
            
            logger.info(f"Retrieved {len(similar_docs)} relevant documents")
            
            # Step 3: Generate response using LLM
            llm_response = await self.llm_service.generate_response(
                query=query,
                context_documents=similar_docs,
                chat_history=chat_history
            )
            
            # Step 4: Enhance response with additional metadata
            response = {
                "answer": llm_response["response"],
                "sources": self._format_sources(similar_docs),
                "metadata": {
                    **llm_response["metadata"],
                    "query": query,
                    "retrieval_count": len(similar_docs),
                    "avg_similarity": self._calculate_avg_similarity(similar_docs)
                }
            }
            
            logger.info("Successfully processed query")
            return response
            
        except Exception as e:
            logger.error(f"Error processing query: {e}")
            return {
                "answer": "I apologize, but I encountered an error while processing your question. Please try again.",
                "sources": [],
                "metadata": {"error": str(e)}
            }
    
    async def search_documents(
        self,
        query: str,
        top_k: int = 10,
        score_threshold: float = 0.5,
        filters: Optional[Dict] = None
    ) -> List[Dict[str, Any]]:
        """
        Search for documents without generating a response
        
        Args:
            query: Search query
            top_k: Number of results
            score_threshold: Minimum similarity
            filters: Additional filters
        
        Returns:
            List of matching documents
        """
        try:
            # Generate query embedding
            query_embedding = await self.embedding_service.generate_query_embedding(query)
            
            # Search documents
            results = await self.vectordb_service.search_similar(
                query_embedding=query_embedding,
                top_k=top_k,
                score_threshold=score_threshold,
                filter_conditions=filters
            )
            
            return self._format_search_results(results)
            
        except Exception as e:
            logger.error(f"Error searching documents: {e}")
            return []
    
    async def get_document_summary(self, document_id: str) -> Dict[str, Any]:
        """
        Get a summary of a specific document
        
        Args:
            document_id: Document identifier
        
        Returns:
            Document summary
        """
        try:
            # TODO: Implement document retrieval by ID
            # For now, return placeholder
            return {
                "document_id": document_id,
                "summary": "Document summary not yet implemented",
                "key_points": [],
                "metadata": {}
            }
            
        except Exception as e:
            logger.error(f"Error getting document summary: {e}")
            return {"error": str(e)}
    
    async def explain_legal_concept(self, concept: str) -> Dict[str, Any]:
        """
        Explain a legal concept using retrieved context
        
        Args:
            concept: Legal concept to explain
        
        Returns:
            Explanation with sources
        """
        try:
            # Create a query for the concept
            query = f"What is {concept}? Please explain this legal concept in detail."
            
            # Process through RAG pipeline
            response = await self.process_query(
                query=query,
                top_k=3,
                score_threshold=0.6
            )
            
            return {
                "concept": concept,
                "explanation": response["answer"],
                "sources": response["sources"],
                "metadata": response["metadata"]
            }
            
        except Exception as e:
            logger.error(f"Error explaining concept: {e}")
            return {"error": str(e)}
    
    def _format_sources(self, documents: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Format source documents for response"""
        sources = []
        
        for i, doc in enumerate(documents, 1):
            source = {
                "id": i,
                "document_name": doc.get("document_name", "Unknown"),
                "filename": doc.get("filename", ""),
                "page_number": doc.get("page_number"),
                "similarity_score": round(doc.get("score", 0.0), 3),
                "snippet": doc.get("text", "")[:200] + "..." if len(doc.get("text", "")) > 200 else doc.get("text", "")
            }
            sources.append(source)
        
        return sources
    
    def _format_search_results(self, documents: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Format search results"""
        results = []
        
        for doc in documents:
            result = {
                "document_id": doc.get("document_id"),
                "chunk_id": doc.get("chunk_id"),
                "document_name": doc.get("document_name", "Unknown"),
                "filename": doc.get("filename", ""),
                "page_number": doc.get("page_number"),
                "similarity_score": round(doc.get("score", 0.0), 3),
                "text": doc.get("text", ""),
                "metadata": doc.get("metadata", {})
            }
            results.append(result)
        
        return results
    
    def _calculate_avg_similarity(self, documents: List[Dict[str, Any]]) -> float:
        """Calculate average similarity score"""
        if not documents:
            return 0.0
        
        scores = [doc.get("score", 0.0) for doc in documents]
        return round(sum(scores) / len(scores), 3)
    
    async def health_check(self) -> Dict[str, Any]:
        """Check the health of all RAG components"""
        try:
            health_status = {
                "embedding_service": True,
                "vector_database": True,
                "llm_service": True,
                "overall": True
            }
            
            # Test embedding service
            try:
                test_embedding = await self.embedding_service.generate_embedding("test")
                health_status["embedding_service"] = len(test_embedding) > 0
            except Exception:
                health_status["embedding_service"] = False
            
            # Test vector database
            try:
                vector_info = await self.vectordb_service.get_collection_info()
                health_status["vector_database"] = "name" in vector_info
            except Exception:
                health_status["vector_database"] = False
            
            # Test LLM service
            try:
                test_response = await self.llm_service.generate_response(
                    query="test",
                    context_documents=[]
                )
                health_status["llm_service"] = "response" in test_response
            except Exception:
                health_status["llm_service"] = False
            
            # Overall health
            health_status["overall"] = all([
                health_status["embedding_service"],
                health_status["vector_database"],
                health_status["llm_service"]
            ])
            
            return health_status
            
        except Exception as e:
            logger.error(f"Error in health check: {e}")
            return {
                "embedding_service": False,
                "vector_database": False,
                "llm_service": False,
                "overall": False,
                "error": str(e)
            }
