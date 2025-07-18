# Database utilities for document storage and management
import sqlite3
from typing import List, Dict, Optional
import json
from datetime import datetime

class DatabaseManager:
    def __init__(self, db_path: str = "legal_documents.db"):
        self.db_path = db_path
        self.init_database()
    
    def init_database(self):
        """Initialize the database with required tables"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            
            # Documents table
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    file_path TEXT,
                    document_type TEXT,
                    metadata TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            ''')
            
            # Document chunks table (for RAG)
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS document_chunks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    document_id INTEGER,
                    chunk_text TEXT NOT NULL,
                    chunk_index INTEGER,
                    metadata TEXT,
                    FOREIGN KEY (document_id) REFERENCES documents (id)
                )
            ''')
            
            conn.commit()
    
    def add_document(self, title: str, content: str, file_path: str = None, 
                    document_type: str = None, metadata: dict = None) -> int:
        """Add a new document to the database"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            
            metadata_json = json.dumps(metadata) if metadata else None
            
            cursor.execute('''
                INSERT INTO documents (title, content, file_path, document_type, metadata)
                VALUES (?, ?, ?, ?, ?)
            ''', (title, content, file_path, document_type, metadata_json))
            
            conn.commit()
            return cursor.lastrowid
    
    def get_document(self, document_id: int) -> Optional[Dict]:
        """Get a document by ID"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute('SELECT * FROM documents WHERE id = ?', (document_id,))
            row = cursor.fetchone()
            
            if row:
                return {
                    'id': row[0],
                    'title': row[1],
                    'content': row[2],
                    'file_path': row[3],
                    'document_type': row[4],
                    'metadata': json.loads(row[5]) if row[5] else None,
                    'created_at': row[6],
                    'updated_at': row[7]
                }
            return None
    
    def get_all_documents(self) -> List[Dict]:
        """Get all documents"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute('SELECT * FROM documents ORDER BY created_at DESC')
            rows = cursor.fetchall()
            
            documents = []
            for row in rows:
                documents.append({
                    'id': row[0],
                    'title': row[1],
                    'content': row[2],
                    'file_path': row[3],
                    'document_type': row[4],
                    'metadata': json.loads(row[5]) if row[5] else None,
                    'created_at': row[6],
                    'updated_at': row[7]
                })
            
            return documents
    
    def delete_document(self, document_id: int) -> bool:
        """Delete a document"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute('DELETE FROM documents WHERE id = ?', (document_id,))
            cursor.execute('DELETE FROM document_chunks WHERE document_id = ?', (document_id,))
            conn.commit()
            return cursor.rowcount > 0
