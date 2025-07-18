# Pydantic models for data validation and API schemas
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime

class DocumentBase(BaseModel):
    title: str = Field(..., description="Title of the document")
    content: str = Field(..., description="Content of the document")
    document_type: Optional[str] = Field(None, description="Type of document (e.g., contract, policy, etc.)")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Additional metadata")

class DocumentCreate(DocumentBase):
    file_path: Optional[str] = Field(None, description="Path to the original file")

class DocumentResponse(DocumentBase):
    id: int = Field(..., description="Unique identifier for the document")
    file_path: Optional[str] = Field(None, description="Path to the original file")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Last update timestamp")

    class Config:
        from_attributes = True

class QuestionRequest(BaseModel):
    question: str = Field(..., description="Question to ask about the documents")
    collection_name: Optional[str] = Field("default", description="Collection to search in")
    max_results: Optional[int] = Field(5, description="Maximum number of results to return")

class SourceDocument(BaseModel):
    content: str = Field(..., description="Content of the source document")
    metadata: Dict[str, Any] = Field(..., description="Metadata of the source document")

class QuestionResponse(BaseModel):
    answer: str = Field(..., description="Generated answer to the question")
    source_documents: List[SourceDocument] = Field(..., description="Source documents used for the answer")
    confidence_score: Optional[float] = Field(None, description="Confidence score of the answer")

class FileUploadRequest(BaseModel):
    file_name: str = Field(..., description="Name of the uploaded file")
    document_type: Optional[str] = Field(None, description="Type of document")
    collection_name: Optional[str] = Field("default", description="Collection to add the document to")

class FileUploadResponse(BaseModel):
    message: str = Field(..., description="Upload status message")
    document_id: int = Field(..., description="ID of the created document")
    chunks_created: int = Field(..., description="Number of chunks created")

class SummaryRequest(BaseModel):
    document_id: int = Field(..., description="ID of the document to summarize")

class SummaryResponse(BaseModel):
    document_id: int = Field(..., description="ID of the summarized document")
    summary: str = Field(..., description="Generated summary")

class KeyPointsRequest(BaseModel):
    document_id: int = Field(..., description="ID of the document to extract key points from")

class KeyPointsResponse(BaseModel):
    document_id: int = Field(..., description="ID of the document")
    key_points: List[str] = Field(..., description="Extracted key points")

class CollectionInfo(BaseModel):
    name: str = Field(..., description="Name of the collection")
    document_count: Optional[int] = Field(None, description="Number of documents in the collection")

class CollectionListResponse(BaseModel):
    collections: List[CollectionInfo] = Field(..., description="List of available collections")

class ErrorResponse(BaseModel):
    error: str = Field(..., description="Error message")
    detail: Optional[str] = Field(None, description="Detailed error information")

class HealthCheckResponse(BaseModel):
    status: str = Field(..., description="Service status")
    version: str = Field(..., description="API version")
    timestamp: datetime = Field(..., description="Current timestamp")
