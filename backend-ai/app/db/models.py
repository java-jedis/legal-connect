"""
Database models for the Legal Connect RAG system
"""

from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, ForeignKey, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime
import uuid

Base = declarative_base()


class Document(Base):
    """Document model for storing legal document metadata"""
    __tablename__ = "documents"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(255), nullable=False)
    filename = Column(String(255), nullable=False)
    url = Column(String(500))
    pdf_path = Column(String(500))
    extraction_date = Column(DateTime)
    ocr_language = Column(String(10), default='ben')
    text = Column(Text)  # Full text content
    document_metadata = Column(JSON)
    total_pages = Column(Integer)
    language = Column(String(10))
    extraction_method = Column(String(50))
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    document_chunks = relationship("DocumentChunk", back_populates="document")

class DocumentChunk(Base):
    """Document chunks for vector storage"""
    __tablename__ = "document_chunks"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    document_id = Column(String, ForeignKey("documents.id"), nullable=False)
    chunk_text = Column(Text, nullable=False)
    chunk_index = Column(Integer, nullable=False)
    page_number = Column(Integer)
    vector_id = Column(String)  # ID in vector database
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # Relationships
    document = relationship("Document", back_populates="document_chunks")

class ChatSession(Base):
    """Chat session model"""
    __tablename__ = "chat_sessions"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, nullable=False)
    title = Column(String(255))
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    messages = relationship("ChatMessage", back_populates="session")
    documents = relationship("ChatDocument", back_populates="session")

class ChatMessage(Base):
    """Chat message model"""
    __tablename__ = "chat_messages"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    session_id = Column(String, ForeignKey("chat_sessions.id"), nullable=False)
    role = Column(String(20), nullable=False)  # 'user' or 'assistant'
    content = Column(Text, nullable=False)
    message_metadata = Column(JSON)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # Relationships
    session = relationship("ChatSession", back_populates="messages")

class ChatDocument(Base):
    """Documents uploaded during chat sessions"""
    __tablename__ = "chat_documents"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    session_id = Column(String, ForeignKey("chat_sessions.id"), nullable=False)
    user_id = Column(String, nullable=False)  # User who uploaded the document
    filename = Column(String(255), nullable=False)
    original_filename = Column(String(255), nullable=False)
    file_size = Column(Integer, nullable=False)
    file_type = Column(String(50), nullable=False)  # 'pdf', 'txt', 'docx'
    file_path = Column(String(500))  # Local file storage path
    text_content = Column(Text)  # Extracted text content
    total_chunks = Column(Integer, default=0)
    processing_status = Column(String(20), default='pending')  # 'pending', 'processing', 'completed', 'failed'
    processing_error = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    session = relationship("ChatSession", back_populates="documents")
    chunks = relationship("ChatDocumentChunk", back_populates="chat_document")

class ChatDocumentChunk(Base):
    """Chunks of chat documents for vector storage"""
    __tablename__ = "chat_document_chunks"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    chat_document_id = Column(String, ForeignKey("chat_documents.id"), nullable=False)
    chunk_text = Column(Text, nullable=False)
    chunk_index = Column(Integer, nullable=False)
    page_number = Column(Integer)
    vector_id = Column(String)  # ID in vector database
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # Relationships
    chat_document = relationship("ChatDocument", back_populates="chunks")

class ProcessingJob(Base):
    """Background job tracking for document processing"""
    __tablename__ = "processing_jobs"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    job_type = Column(String(50), nullable=False)  # 'embedding', 'indexing', etc.
    status = Column(String(20), default='pending')  # 'pending', 'processing', 'completed', 'failed'
    progress = Column(Integer, default=0)  # 0-100
    result = Column(JSON)
    error_message = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    completed_at = Column(DateTime)
