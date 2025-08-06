"""
Document processing service for loading and indexing legal documents
"""

import os
import json
import asyncio
from pathlib import Path
from typing import List, Dict, Any, Optional
import logging
from sqlalchemy.orm import Session

from app.utils.text_processing import TextProcessor
from app.services.embeddings import EmbeddingService
from app.services.vectordb import QdrantService
from app.db.models import Document, DocumentChunk
from app.db.database import get_db

logger = logging.getLogger(__name__)

class DocumentProcessor:
    """Service for processing and indexing legal documents"""
    
    def __init__(
        self,
        embedding_service: EmbeddingService,
        vectordb_service: QdrantService
    ):
        self.embedding_service = embedding_service
        self.vectordb_service = vectordb_service
        self.text_processor = TextProcessor()
        
        self.json_directory = os.getenv("JSON_DOCUMENTS_PATH", "bdcode_json")
        
        logger.info("Initialized DocumentProcessor")
    
    async def process_all_documents(self) -> Dict[str, Any]:
        """
        Process all JSON documents in the directory
        
        Returns:
            Processing results summary
        """
        json_files = self._find_json_files()
        
        logger.info(f"Found {len(json_files)} JSON files to process")
        
        results = {
            "total_files": len(json_files),
            "processed_files": 0,
            "failed_files": 0,
            "total_chunks": 0,
            "total_embeddings": 0,
            "errors": []
        }
        
        # Process files in batches to avoid memory issues
        batch_size = 10
        for i in range(0, len(json_files), batch_size):
            batch = json_files[i:i + batch_size]
            
            for json_file in batch:
                try:
                    result = await self.process_single_document(json_file)
                    if result["success"]:
                        results["processed_files"] += 1
                        results["total_chunks"] += result["chunks_created"]
                        results["total_embeddings"] += result["embeddings_created"]
                    else:
                        results["failed_files"] += 1
                        results["errors"].append({
                            "file": str(json_file),
                            "error": result["error"]
                        })
                    
                    logger.info(f"Processed {json_file.name}: {result}")
                    
                except Exception as e:
                    logger.error(f"Error processing {json_file}: {e}")
                    results["failed_files"] += 1
                    results["errors"].append({
                        "file": str(json_file),
                        "error": str(e)
                    })
            
            # Small delay between batches
            await asyncio.sleep(1)
        
        return results
    
    async def process_single_document(self, json_file_path: Path) -> Dict[str, Any]:
        """
        Process a single JSON document
        
        Args:
            json_file_path: Path to the JSON file
        
        Returns:
            Processing result
        """
        db = None
        try:
            # Load JSON content
            with open(json_file_path, 'r', encoding='utf-8') as f:
                json_content = json.load(f)
            
            # Extract metadata
            metadata = self.text_processor.extract_metadata(json_content)
            
            # Get database session
            db = next(get_db())
            
            # Check if document already exists
            existing_doc = db.query(Document).filter(
                Document.filename == json_content.get("filename", "")
            ).first()
            
            if existing_doc:
                logger.info(f"Document {json_file_path.name} already exists, skipping")
                db.close()
                return {
                    "success": True,
                    "chunks_created": 0,
                    "embeddings_created": 0,
                    "message": "Document already exists"
                }
            
            # Create document record
            document = Document(
                name=json_content.get("name", ""),
                filename=json_content.get("filename", ""),
                url=json_content.get("url", ""),
                pdf_path=json_content.get("pdf_path", ""),
                extraction_date=json_content.get("extraction_date"),
                ocr_language=json_content.get("ocr_language", "ben"),
                text=self.text_processor.combine_full_text(json_content.get("pages", [])),
                document_metadata=json_content.get("metadata", {}),
                total_pages=len(json_content.get("pages", [])),
                language=json_content.get("metadata", {}).get("language", "ben"),
                extraction_method=json_content.get("metadata", {}).get("extraction_method", "OCR")
            )
            
            db.add(document)
            db.commit()
            db.refresh(document)
            
            # Store document ID for later use
            document_id = document.id
            document_name = document.name
            document_filename = document.filename
            
            # Process pages into chunks
            pages = json_content.get("pages", [])
            all_chunks = self.text_processor.process_pages(pages)
            
            if not all_chunks:
                logger.warning(f"No text chunks created for {json_file_path.name}")
                db.close()
                return {
                    "success": True,
                    "chunks_created": 0,
                    "embeddings_created": 0,
                    "message": "No text content found"
                }
            
            # Create embeddings for chunks
            chunk_texts = [chunk["text"] for chunk in all_chunks]
            embeddings = await self.embedding_service.generate_embeddings_batch(chunk_texts)
            
            # Prepare documents for vector database
            vector_documents = []
            chunk_records = []
            
            for i, (chunk, embedding) in enumerate(zip(all_chunks, embeddings)):
                # Create document chunk record
                chunk_record = DocumentChunk(
                    document_id=document_id,
                    chunk_text=chunk["text"],
                    chunk_index=chunk["chunk_index"],
                    page_number=chunk["metadata"].get("page_number")
                )
                chunk_records.append(chunk_record)
                
                # Prepare for vector database (use stored values instead of document object)
                vector_doc = {
                    "document_id": document_id,
                    "chunk_id": None,  # Will be updated after database commit
                    "text": chunk["text"],
                    "page_number": chunk["metadata"].get("page_number"),
                    "document_name": document_name,
                    "filename": document_filename,
                    "metadata": {
                        "chunk_index": chunk["chunk_index"],
                        "length": chunk["length"],
                        **metadata
                    }
                }
                vector_documents.append(vector_doc)
            
            # Add chunk records to database
            db.add_all(chunk_records)
            db.commit()
            
            # Refresh all chunk records to get their IDs
            for chunk_record in chunk_records:
                db.refresh(chunk_record)
            
            # Update vector document IDs
            for vector_doc, chunk_record in zip(vector_documents, chunk_records):
                vector_doc["chunk_id"] = chunk_record.id
            
            # Add to vector database
            vector_ids = await self.vectordb_service.add_documents(vector_documents, embeddings)
            
            # Update chunk records with vector IDs
            for chunk_record, vector_id in zip(chunk_records, vector_ids):
                chunk_record.vector_id = vector_id
            
            # Final commit and close
            db.commit()
            db.close()
            
            return {
                "success": True,
                "chunks_created": len(all_chunks),
                "embeddings_created": len(embeddings),
                "document_id": document_id
            }
            
        except Exception as e:
            logger.error(f"Error processing document {json_file_path}: {e}")
            if db:
                db.rollback()
                db.close()
            return {
                "success": False,
                "error": str(e)
            }
    
    def _find_json_files(self) -> List[Path]:
        """Find all JSON files in the directory"""
        json_path = Path(self.json_directory)
        
        if not json_path.exists():
            logger.error(f"JSON directory not found: {json_path}")
            return []
        
        json_files = []
        
        # Recursively find all JSON files
        for root, dirs, files in os.walk(json_path):
            for file in files:
                if file.endswith('.json'):
                    json_files.append(Path(root) / file)
        
        return sorted(json_files)
    
    async def reindex_document(self, document_id: str) -> Dict[str, Any]:
        """
        Reindex a specific document
        
        Args:
            document_id: Document ID to reindex
        
        Returns:
            Reindexing result
        """
        try:
            db = next(get_db())
            
            # Get document
            document = db.query(Document).filter(Document.id == document_id).first()
            if not document:
                return {"success": False, "error": "Document not found"}
            
            # Delete existing chunks and vectors
            chunks = db.query(DocumentChunk).filter(DocumentChunk.document_id == document_id).all()
            vector_ids = [chunk.vector_id for chunk in chunks if chunk.vector_id]
            
            if vector_ids:
                await self.vectordb_service.delete_documents(vector_ids)
            
            # Delete chunk records
            db.query(DocumentChunk).filter(DocumentChunk.document_id == document_id).delete()
            db.commit()
            
            # Find the original JSON file
            json_files = self._find_json_files()
            target_file = None
            
            for json_file in json_files:
                with open(json_file, 'r', encoding='utf-8') as f:
                    content = json.load(f)
                    if content.get("filename") == document.filename:
                        target_file = json_file
                        break
            
            if not target_file:
                return {"success": False, "error": "Original JSON file not found"}
            
            # Delete document record
            db.delete(document)
            db.commit()
            db.close()
            
            # Reprocess the document
            result = await self.process_single_document(target_file)
            
            return result
            
        except Exception as e:
            logger.error(f"Error reindexing document {document_id}: {e}")
            return {"success": False, "error": str(e)}
    
    async def get_processing_stats(self) -> Dict[str, Any]:
        """Get processing statistics"""
        try:
            db = next(get_db())
            
            total_documents = db.query(Document).count()
            total_chunks = db.query(DocumentChunk).count()
            
            # Get vector database stats
            vector_stats = await self.vectordb_service.get_collection_info()
            
            db.close()
            
            return {
                "total_documents": total_documents,
                "total_chunks": total_chunks,
                "vector_database": vector_stats
            }
            
        except Exception as e:
            logger.error(f"Error getting processing stats: {e}")
            return {"error": str(e)}
