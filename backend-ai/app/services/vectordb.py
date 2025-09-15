"""
Qdrant vector database service for storing and retrieving document embeddings
"""

import os
from typing import List, Dict, Any, Optional, Tuple
from qdrant_client import QdrantClient
from qdrant_client.http import models
from qdrant_client.http.models import Distance, VectorParams, PointStruct
import uuid
import logging
import asyncio

logger = logging.getLogger(__name__)

class QdrantService:
    """Service for managing document embeddings in Qdrant vector database"""
    
    def __init__(self):
        self.url = os.getenv("QDRANT_URL")
        self.host = os.getenv("QDRANT_HOST", "localhost")
        self.port = int(os.getenv("QDRANT_PORT", "6333"))
        self.api_key = os.getenv("QDRANT_API_KEY")
        self.collection_name = os.getenv("QDRANT_COLLECTION_NAME", "legal_documents")
        
        # Initialize client - prefer URL if available
        if self.url:
            self.client = QdrantClient(
                url=self.url,
                api_key=self.api_key
            )
        else:
            self.client = QdrantClient(host=self.host, port=self.port)
        
        self.vector_size = 768  # Gemini embedding dimension
        
        logger.info(f"Initialized QdrantService with collection: {self.collection_name}")
    
    async def initialize(self):
        """Initialize the vector database collection"""
        try:
            # Check if collection exists
            collections = self.client.get_collections()
            collection_names = [col.name for col in collections.collections]
            
            if self.collection_name not in collection_names:
                # Create collection
                self.client.create_collection(
                    collection_name=self.collection_name,
                    vectors_config=VectorParams(
                        size=self.vector_size,
                        distance=Distance.COSINE
                    )
                )
                logger.info(f"Created collection: {self.collection_name}")
            else:
                logger.info(f"Collection {self.collection_name} already exists")
            
            # Create indexes for filtering capabilities
            await self.ensure_required_indexes()
            
            # Get collection info
            info = self.client.get_collection(self.collection_name)
            logger.info(f"Collection info: {info.points_count} points, {info.vectors_count} vectors")
            
        except Exception as e:
            logger.error(f"Error initializing Qdrant: {e}")
            raise e
    
    async def add_documents(
        self, 
        documents: List[Dict[str, Any]], 
        embeddings: List[List[float]]
    ) -> List[str]:
        """
        Add documents with their embeddings to the vector database
        
        Args:
            documents: List of document metadata
            embeddings: List of corresponding embeddings
        
        Returns:
            List of vector IDs
        """
        if len(documents) != len(embeddings):
            raise ValueError("Number of documents must match number of embeddings")
        
        points = []
        vector_ids = []
        
        for doc, embedding in zip(documents, embeddings):
            vector_id = str(uuid.uuid4())
            vector_ids.append(vector_id)
            
            point = PointStruct(
                id=vector_id,
                vector=embedding,
                payload={
                    "document_id": doc.get("document_id"),
                    "chunk_id": doc.get("chunk_id"),
                    "text": doc.get("text", ""),
                    "page_number": doc.get("page_number"),
                    "document_name": doc.get("document_name", ""),
                    "filename": doc.get("filename", ""),
                    "session_id": doc.get("session_id"),  # Add session_id to payload
                    "document_type": doc.get("document_type", ""),
                    "metadata": doc.get("metadata", {})
                }
            )
            points.append(point)
        
        try:
            # Upload points in batches
            batch_size = 100
            for i in range(0, len(points), batch_size):
                batch = points[i:i + batch_size]
                
                self.client.upsert(
                    collection_name=self.collection_name,
                    points=batch
                )
                
                logger.info(f"Uploaded batch {i//batch_size + 1}/{(len(points) + batch_size - 1)//batch_size}")
            
            logger.info(f"Successfully added {len(points)} documents to vector database")
            return vector_ids
            
        except Exception as e:
            logger.error(f"Error adding documents to Qdrant: {e}")
            raise e
    
    async def search_similar(
        self,
        query_embedding: List[float],
        top_k: int = 5,
        score_threshold: float = 0.7,
        filter_conditions: Optional[Dict] = None
    ) -> List[Dict[str, Any]]:
        """
        Search for similar documents using vector similarity
        
        Args:
            query_embedding: Query embedding vector
            top_k: Number of top results to return
            score_threshold: Minimum similarity score threshold
            filter_conditions: Optional filter conditions
        
        Returns:
            List of similar documents with metadata and scores
        """
        try:
            # Convert filter conditions to Qdrant format
            query_filter = None
            if filter_conditions:
                must_conditions = []
                for key, value in filter_conditions.items():
                    must_conditions.append(
                        models.FieldCondition(
                            key=key,
                            match=models.MatchValue(value=value)
                        )
                    )
                
                if must_conditions:
                    query_filter = models.Filter(
                        must=must_conditions
                    )
            
            # Perform search
            search_result = self.client.search(
                collection_name=self.collection_name,
                query_vector=query_embedding,
                limit=top_k,
                score_threshold=score_threshold,
                query_filter=query_filter
            )
            
            results = []
            for hit in search_result:
                result = {
                    "id": hit.id,
                    "score": hit.score,
                    "document_id": hit.payload.get("document_id"),
                    "chunk_id": hit.payload.get("chunk_id"),
                    "text": hit.payload.get("text", ""),
                    "page_number": hit.payload.get("page_number"),
                    "document_name": hit.payload.get("document_name", ""),
                    "filename": hit.payload.get("filename", ""),
                    "session_id": hit.payload.get("session_id"),  # Include session_id in results
                    "document_type": hit.payload.get("document_type", ""),
                    "metadata": hit.payload.get("metadata", {})
                }
                results.append(result)
            
            logger.info(f"Found {len(results)} similar documents")
            return results
            
        except Exception as e:
            logger.error(f"Error searching in Qdrant: {e}")
            raise e
    
    async def get_document_by_id(self, vector_id: str) -> Optional[Dict[str, Any]]:
        """
        Get a specific document by its vector ID
        
        Args:
            vector_id: Vector ID in the database
        
        Returns:
            Document data or None if not found
        """
        try:
            result = self.client.retrieve(
                collection_name=self.collection_name,
                ids=[vector_id]
            )
            
            if result:
                hit = result[0]
                return {
                    "id": hit.id,
                    "document_id": hit.payload.get("document_id"),
                    "chunk_id": hit.payload.get("chunk_id"),
                    "text": hit.payload.get("text", ""),
                    "page_number": hit.payload.get("page_number"),
                    "document_name": hit.payload.get("document_name", ""),
                    "filename": hit.payload.get("filename", ""),
                    "metadata": hit.payload.get("metadata", {})
                }
            
            return None
            
        except Exception as e:
            logger.error(f"Error retrieving document from Qdrant: {e}")
            return None
    
    async def delete_documents(self, vector_ids: List[str]) -> bool:
        """
        Delete documents by their vector IDs
        
        Args:
            vector_ids: List of vector IDs to delete
        
        Returns:
            True if successful, False otherwise
        """
        try:
            self.client.delete(
                collection_name=self.collection_name,
                points_selector=models.PointIdsList(points=vector_ids)
            )
            
            logger.info(f"Deleted {len(vector_ids)} documents from vector database")
            return True
            
        except Exception as e:
            logger.error(f"Error deleting documents from Qdrant: {e}")
            return False
    
    async def get_collection_info(self) -> Dict[str, Any]:
        """
        Get information about the collection
        
        Returns:
            Collection information
        """
        try:
            info = self.client.get_collection(self.collection_name)
            
            return {
                "name": self.collection_name,
                "points_count": info.points_count,
                "vectors_count": info.vectors_count,
                "status": info.status,
                "config": {
                    "vector_size": info.config.params.vectors.size,
                    "distance": info.config.params.vectors.distance.value
                }
            }
            
        except Exception as e:
            logger.error(f"Error getting collection info: {e}")
            return {}
    
    async def close(self):
        """Close the Qdrant client connection"""
        try:
            if hasattr(self.client, 'close'):
                self.client.close()
            logger.info("Closed Qdrant client connection")
        except Exception as e:
            logger.error(f"Error closing Qdrant connection: {e}")
    
    async def create_index(self, field_name: str, field_type: str = "keyword"):
        """
        Create an index on a payload field for faster filtering
        
        Args:
            field_name: Name of the field to index
            field_type: Type of the field ('keyword', 'integer', 'geo', etc.)
        """
        try:
            self.client.create_payload_index(
                collection_name=self.collection_name,
                field_name=field_name,
                field_schema=field_type
            )
            
            logger.info(f"Created index on field: {field_name}")
            
        except Exception as e:
            # Check if it's just that the index already exists
            if "already exists" in str(e).lower():
                logger.info(f"Index for {field_name} already exists")
            else:
                logger.error(f"Error creating index: {e}")
                raise e
    
    async def ensure_required_indexes(self):
        """Ensure all required indexes exist for filtering"""
        try:
            # Create session_id index
            await self.create_index("session_id", "keyword")
            
            # Create document_type index  
            await self.create_index("document_type", "keyword")
            
            # Create document_id index
            await self.create_index("document_id", "keyword")
            
            logger.info("All required indexes ensured")
            
        except Exception as e:
            logger.error(f"Error ensuring indexes: {e}")
            # Don't raise - this shouldn't stop the application
