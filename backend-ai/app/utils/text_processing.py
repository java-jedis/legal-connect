"""
Text processing utilities for document chunking and cleaning
"""

import re
import json
from typing import List, Dict, Any, Tuple
import logging

logger = logging.getLogger(__name__)

class TextProcessor:
    """Utility class for text processing and chunking"""
    
    def __init__(self, chunk_size: int = 1000, chunk_overlap: int = 200):
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap
    
    def chunk_text(self, text: str, metadata: Dict[str, Any] = None) -> List[Dict[str, Any]]:
        """
        Split text into overlapping chunks for embedding
        
        Args:
            text: Input text to chunk
            metadata: Additional metadata to attach to chunks
        
        Returns:
            List of text chunks with metadata
        """
        if not text or not text.strip():
            return []
        
        # Clean the text first
        cleaned_text = self.clean_text(text)
        
        # Split into sentences for better chunk boundaries
        sentences = self._split_into_sentences(cleaned_text)
        
        chunks = []
        current_chunk = ""
        current_length = 0
        chunk_index = 0
        
        for sentence in sentences:
            sentence_length = len(sentence)
            
            # If adding this sentence would exceed chunk size, create a new chunk
            if current_length + sentence_length > self.chunk_size and current_chunk:
                chunk_data = {
                    "text": current_chunk.strip(),
                    "chunk_index": chunk_index,
                    "length": len(current_chunk.strip()),
                    "metadata": metadata or {}
                }
                chunks.append(chunk_data)
                
                # Start new chunk with overlap
                overlap_text = self._get_overlap(current_chunk, self.chunk_overlap)
                current_chunk = overlap_text + " " + sentence
                current_length = len(current_chunk)
                chunk_index += 1
            else:
                current_chunk += " " + sentence if current_chunk else sentence
                current_length = len(current_chunk)
        
        # Add the final chunk
        if current_chunk.strip():
            chunk_data = {
                "text": current_chunk.strip(),
                "chunk_index": chunk_index,
                "length": len(current_chunk.strip()),
                "metadata": metadata or {}
            }
            chunks.append(chunk_data)
        
        logger.info(f"Created {len(chunks)} chunks from text of length {len(text)}")
        return chunks
    
    def clean_text(self, text: str) -> str:
        """
        Clean and normalize text
        
        Args:
            text: Raw text input
        
        Returns:
            Cleaned text
        """
        if not text:
            return ""
        
        # Remove excessive whitespace
        text = re.sub(r'\s+', ' ', text)
        
        # Remove special characters but keep Bengali characters
        text = re.sub(r'[^\w\s\u0980-\u09FF\u0660-\u06FF\u0750-\u077F.,;:!?()-]', ' ', text)
        
        # Remove extra spaces
        text = re.sub(r' +', ' ', text)
        
        # Strip leading/trailing whitespace
        text = text.strip()
        
        return text
    
    def _split_into_sentences(self, text: str) -> List[str]:
        """Split text into sentences"""
        # Bengali and English sentence endings
        sentence_endings = r'[।.!?]+'
        sentences = re.split(sentence_endings, text)
        
        # Filter out empty sentences and clean
        sentences = [s.strip() for s in sentences if s.strip()]
        
        return sentences
    
    def _get_overlap(self, text: str, overlap_size: int) -> str:
        """Get overlap text from the end of current chunk"""
        if len(text) <= overlap_size:
            return text
        
        # Try to find a good break point (sentence boundary)
        overlap_text = text[-overlap_size:]
        
        # Find the first sentence boundary in the overlap
        sentence_endings = r'[।.!?]+'
        sentences = re.split(sentence_endings, overlap_text)
        
        if len(sentences) > 1:
            # Start from the second sentence to avoid partial sentences
            return " ".join(sentences[1:]).strip()
        
        return overlap_text
    
    def extract_metadata(self, json_content: Dict[str, Any]) -> Dict[str, Any]:
        """
        Extract metadata from JSON document structure
        
        Args:
            json_content: Parsed JSON content
        
        Returns:
            Extracted metadata
        """
        metadata = {}
        
        # Basic document info
        metadata["name"] = json_content.get("name", "")
        metadata["filename"] = json_content.get("filename", "")
        metadata["url"] = json_content.get("url", "")
        metadata["pdf_path"] = json_content.get("pdf_path", "")
        metadata["extraction_date"] = json_content.get("extraction_date", "")
        metadata["ocr_language"] = json_content.get("ocr_language", "")
        
        # Document metadata
        doc_metadata = json_content.get("metadata", {})
        metadata["total_pages"] = doc_metadata.get("total_pages", 0)
        metadata["language"] = doc_metadata.get("language", "")
        metadata["extraction_method"] = doc_metadata.get("extraction_method", "")
        
        return metadata
    
    def process_pages(self, pages: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Process pages from JSON document
        
        Args:
            pages: List of page dictionaries
        
        Returns:
            List of processed page chunks
        """
        all_chunks = []
        
        for page in pages:
            page_number = page.get("page_number", 0)
            page_text = page.get("text", "")
            
            if not page_text.strip():
                continue
            
            # Create chunks for this page
            page_metadata = {"page_number": page_number}
            chunks = self.chunk_text(page_text, page_metadata)
            
            all_chunks.extend(chunks)
        
        return all_chunks
    
    def combine_full_text(self, pages: List[Dict[str, Any]]) -> str:
        """
        Combine all pages into full document text
        
        Args:
            pages: List of page dictionaries
        
        Returns:
            Combined text
        """
        full_text_parts = []
        
        for page in sorted(pages, key=lambda x: x.get("page_number", 0)):
            page_text = page.get("text", "").strip()
            if page_text:
                full_text_parts.append(page_text)
        
        return "\n\n".join(full_text_parts)
    
    def extract_key_terms(self, text: str) -> List[str]:
        """
        Extract key legal terms from text
        
        Args:
            text: Input text
        
        Returns:
            List of key terms
        """
        # Common legal terms in Bengali and English
        legal_terms = [
            "আইন", "law", "আদালত", "court", "বিচার", "judgment",
            "ধারা", "section", "নিয়ম", "rule", "বিধান", "provision",
            "চুক্তি", "contract", "সংবিধান", "constitution", "কানুন", "statute"
        ]
        
        found_terms = []
        text_lower = text.lower()
        
        for term in legal_terms:
            if term.lower() in text_lower:
                found_terms.append(term)
        
        return list(set(found_terms))
    
    def get_document_summary_stats(self, json_content: Dict[str, Any]) -> Dict[str, Any]:
        """
        Get summary statistics for a document
        
        Args:
            json_content: Parsed JSON content
        
        Returns:
            Summary statistics
        """
        pages = json_content.get("pages", [])
        full_text = self.combine_full_text(pages)
        
        stats = {
            "total_pages": len(pages),
            "total_characters": len(full_text),
            "total_words": len(full_text.split()) if full_text else 0,
            "average_page_length": 0,
            "key_terms": self.extract_key_terms(full_text)
        }
        
        if pages:
            page_lengths = [len(page.get("text", "")) for page in pages]
            stats["average_page_length"] = sum(page_lengths) / len(page_lengths)
        
        return stats
