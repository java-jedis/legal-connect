# Chroma database utilities for vector storage and retrieval
import chromadb
from langchain_chroma import Chroma
from langchain_openai import OpenAIEmbeddings
from langchain.text_splitter import RecursiveCharacterTextSplitter
from typing import List, Optional
import os

class ChromaManager:
    def __init__(self, persist_directory: str = "./chroma_db"):
        self.persist_directory = persist_directory
        self.client = chromadb.PersistentClient(path=persist_directory)
        self.embeddings = OpenAIEmbeddings()
        
    def create_collection(self, collection_name: str):
        """Create a new Chroma collection"""
        return Chroma(
            client=self.client,
            collection_name=collection_name,
            embedding_function=self.embeddings,
            persist_directory=self.persist_directory
        )
    
    def add_documents(self, collection_name: str, documents: List[str], metadatas: Optional[List[dict]] = None):
        """Add documents to a collection"""
        vectorstore = self.create_collection(collection_name)
        
        # Split documents into chunks
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=1000,
            chunk_overlap=200
        )
        
        texts = []
        for doc in documents:
            chunks = text_splitter.split_text(doc)
            texts.extend(chunks)
        
        # Add to vectorstore
        vectorstore.add_texts(texts, metadatas=metadatas)
        return vectorstore
    
    def search_documents(self, collection_name: str, query: str, k: int = 5):
        """Search for similar documents"""
        vectorstore = self.create_collection(collection_name)
        return vectorstore.similarity_search(query, k=k)
    
    def get_collection_names(self):
        """Get all collection names"""
        return [collection.name for collection in self.client.list_collections()]
