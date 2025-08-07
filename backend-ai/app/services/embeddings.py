"""
Embedding service using Google's Gemini embedding model
"""

import os
import google.generativeai as genai
from typing import List, Optional, Dict, Any
import numpy as np
from tenacity import retry, stop_after_attempt, wait_exponential
import asyncio
import logging

logger = logging.getLogger(__name__)

class EmbeddingService:
    """Service for generating embeddings using Google's Gemini embedding model"""
    
    def __init__(self):
        self.api_key = os.getenv("GOOGLE_API_KEY")
        self.model_name = os.getenv("EMBEDDING_MODEL", "models/embedding-001")
        
        if not self.api_key:
            raise ValueError("GOOGLE_API_KEY environment variable is required")
        
        # Configure Gemini
        genai.configure(api_key=self.api_key)
        
        logger.info(f"Initialized EmbeddingService with model: {self.model_name}")
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=4, max=10)
    )
    async def generate_embedding(self, text: str, task_type: str = "retrieval_document") -> List[float]:
        """
        Generate embedding for a single text
        
        Args:
            text: Input text to embed
            task_type: Type of task - 'retrieval_document', 'retrieval_query', 'semantic_similarity', etc.
        
        Returns:
            List of float values representing the embedding
        """
        try:
            # Clean and prepare text
            cleaned_text = self._clean_text(text)
            
            if not cleaned_text.strip():
                logger.warning("Empty text provided for embedding")
                return [0.0] * 768  # Return zero vector for empty text
            
            # Generate embedding
            result = genai.embed_content(
                model=self.model_name,
                content=cleaned_text,
                task_type=task_type
            )
            
            embedding = result['embedding']
            logger.debug(f"Generated embedding of dimension: {len(embedding)}")
            
            return embedding
            
        except Exception as e:
            logger.error(f"Error generating embedding: {e}")
            raise e
    
    async def generate_embeddings_batch(
        self, 
        texts: List[str], 
        task_type: str = "retrieval_document",
        batch_size: int = 10
    ) -> List[List[float]]:
        """
        Generate embeddings for multiple texts in batches
        
        Args:
            texts: List of texts to embed
            task_type: Type of task
            batch_size: Number of texts to process in each batch
        
        Returns:
            List of embeddings
        """
        embeddings = []
        
        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]
            batch_embeddings = []
            
            for text in batch:
                try:
                    embedding = await self.generate_embedding(text, task_type)
                    batch_embeddings.append(embedding)
                    
                    # Small delay between requests to avoid rate limiting
                    await asyncio.sleep(0.1)
                    
                except Exception as e:
                    logger.error(f"Error processing text in batch: {e}")
                    # Use zero vector for failed embeddings
                    batch_embeddings.append([0.0] * 768)
            
            embeddings.extend(batch_embeddings)
            logger.info(f"Processed batch {i//batch_size + 1}/{(len(texts) + batch_size - 1)//batch_size}")
        
        return embeddings
    
    async def generate_query_embedding(self, query: str) -> List[float]:
        """
        Generate embedding specifically for query text
        
        Args:
            query: User query text
        
        Returns:
            Query embedding
        """
        return await self.generate_embedding(query, task_type="retrieval_query")
    
    def _clean_text(self, text: str) -> str:
        """
        Clean and preprocess text for embedding
        
        Args:
            text: Raw text input
        
        Returns:
            Cleaned text
        """
        if not text:
            return ""
        
        # Remove excessive whitespace
        text = " ".join(text.split())
        
        # Truncate if too long (Gemini has token limits)
        max_length = 8000  # Conservative limit
        if len(text) > max_length:
            text = text[:max_length]
            logger.warning(f"Text truncated to {max_length} characters")
        
        return text
    
    def cosine_similarity(self, vec1: List[float], vec2: List[float]) -> float:
        """
        Calculate cosine similarity between two vectors
        
        Args:
            vec1: First vector
            vec2: Second vector
        
        Returns:
            Cosine similarity score
        """
        try:
            v1 = np.array(vec1)
            v2 = np.array(vec2)
            
            # Calculate cosine similarity
            dot_product = np.dot(v1, v2)
            norm_v1 = np.linalg.norm(v1)
            norm_v2 = np.linalg.norm(v2)
            
            if norm_v1 == 0 or norm_v2 == 0:
                return 0.0
            
            similarity = dot_product / (norm_v1 * norm_v2)
            return float(similarity)
            
        except Exception as e:
            logger.error(f"Error calculating cosine similarity: {e}")
            return 0.0
    
    async def find_most_similar(
        self, 
        query_embedding: List[float], 
        document_embeddings: List[List[float]], 
        top_k: int = 5
    ) -> List[Dict[str, Any]]:
        """
        Find most similar documents to a query
        
        Args:
            query_embedding: Query embedding vector
            document_embeddings: List of document embedding vectors
            top_k: Number of top results to return
        
        Returns:
            List of similarity results with scores and indices
        """
        similarities = []
        
        for i, doc_embedding in enumerate(document_embeddings):
            similarity = self.cosine_similarity(query_embedding, doc_embedding)
            similarities.append({
                'index': i,
                'similarity': similarity
            })
        
        # Sort by similarity score (descending)
        similarities.sort(key=lambda x: x['similarity'], reverse=True)
        
        return similarities[:top_k]
