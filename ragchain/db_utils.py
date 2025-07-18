# Database utilities for document storage and management
import psycopg2
import psycopg2.extras
from typing import List, Dict, Optional
import json
from datetime import datetime
import os

class DatabaseManager:
    def __init__(self, db_config: Dict = None):
        """
        Initialize DatabaseManager with PostgreSQL configuration
        
        Args:
            db_config: Dictionary containing database connection parameters
                      If None, will use environment variables
        """
        if db_config is None:
            self.db_config = {
                'host': os.getenv('DB_HOST', 'localhost'),
                'port': os.getenv('DB_PORT', 5432),
                'database': os.getenv('DB_NAME', 'legal_documents'),
                'user': os.getenv('DB_USER', 'postgres'),
                'password': os.getenv('DB_PASSWORD', 'password')
            }
        else:
            self.db_config = db_config
        
        self.init_database()
    
    def get_connection(self):
        """Get a database connection"""
        return psycopg2.connect(**self.db_config)
    
    def init_database(self):
        """Initialize the database with required tables"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    
                    # Documents table
                    cursor.execute('''
                        CREATE TABLE IF NOT EXISTS documents (
                            id SERIAL PRIMARY KEY,
                            title TEXT NOT NULL,
                            content TEXT NOT NULL,
                            file_path TEXT,
                            document_type TEXT,
                            metadata JSONB,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    ''')
                    
                    # Document chunks table (for RAG)
                    cursor.execute('''
                        CREATE TABLE IF NOT EXISTS document_chunks (
                            id SERIAL PRIMARY KEY,
                            document_id INTEGER REFERENCES documents(id) ON DELETE CASCADE,
                            chunk_text TEXT NOT NULL,
                            chunk_index INTEGER,
                            metadata JSONB
                        )
                    ''')
                    
                    # Create indexes for better performance
                    cursor.execute('''
                        CREATE INDEX IF NOT EXISTS idx_documents_type 
                        ON documents(document_type)
                    ''')
                    
                    cursor.execute('''
                        CREATE INDEX IF NOT EXISTS idx_documents_created 
                        ON documents(created_at)
                    ''')
                    
                    cursor.execute('''
                        CREATE INDEX IF NOT EXISTS idx_chunks_document_id 
                        ON document_chunks(document_id)
                    ''')
                    
                    conn.commit()
        except psycopg2.Error as e:
            print(f"Database initialization error: {e}")
            raise
    
    def add_document(self, title: str, content: str, file_path: str = None, 
                    document_type: str = None, metadata: dict = None) -> int:
        """Add a new document to the database"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    
                    cursor.execute('''
                        INSERT INTO documents (title, content, file_path, document_type, metadata)
                        VALUES (%s, %s, %s, %s, %s)
                        RETURNING id
                    ''', (title, content, file_path, document_type, json.dumps(metadata) if metadata else None))
                    
                    document_id = cursor.fetchone()[0]
                    conn.commit()
                    return document_id
        except psycopg2.Error as e:
            print(f"Error adding document: {e}")
            raise
    
    def get_document(self, document_id: int) -> Optional[Dict]:
        """Get a document by ID"""
        try:
            with self.get_connection() as conn:
                with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
                    cursor.execute('SELECT * FROM documents WHERE id = %s', (document_id,))
                    row = cursor.fetchone()
                    
                    if row:
                        document = dict(row)
                        # Parse metadata if it exists
                        if document['metadata']:
                            document['metadata'] = json.loads(document['metadata']) if isinstance(document['metadata'], str) else document['metadata']
                        return document
                    return None
        except psycopg2.Error as e:
            print(f"Error getting document: {e}")
            return None
    
    def get_all_documents(self) -> List[Dict]:
        """Get all documents"""
        try:
            with self.get_connection() as conn:
                with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
                    cursor.execute('SELECT * FROM documents ORDER BY created_at DESC')
                    rows = cursor.fetchall()
                    
                    documents = []
                    for row in rows:
                        document = dict(row)
                        # Parse metadata if it exists
                        if document['metadata']:
                            document['metadata'] = json.loads(document['metadata']) if isinstance(document['metadata'], str) else document['metadata']
                        documents.append(document)
                    
                    return documents
        except psycopg2.Error as e:
            print(f"Error getting all documents: {e}")
            return []
    
    def update_document(self, document_id: int, title: str = None, content: str = None, 
                       file_path: str = None, document_type: str = None, metadata: dict = None) -> bool:
        """Update a document"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    # Build dynamic update query
                    update_fields = []
                    values = []
                    
                    if title is not None:
                        update_fields.append("title = %s")
                        values.append(title)
                    if content is not None:
                        update_fields.append("content = %s")
                        values.append(content)
                    if file_path is not None:
                        update_fields.append("file_path = %s")
                        values.append(file_path)
                    if document_type is not None:
                        update_fields.append("document_type = %s")
                        values.append(document_type)
                    if metadata is not None:
                        update_fields.append("metadata = %s")
                        values.append(json.dumps(metadata))
                    
                    if not update_fields:
                        return False
                    
                    # Add updated_at timestamp
                    update_fields.append("updated_at = CURRENT_TIMESTAMP")
                    values.append(document_id)
                    
                    query = f"UPDATE documents SET {', '.join(update_fields)} WHERE id = %s"
                    cursor.execute(query, values)
                    
                    conn.commit()
                    return cursor.rowcount > 0
        except psycopg2.Error as e:
            print(f"Error updating document: {e}")
            return False
    
    def delete_document(self, document_id: int) -> bool:
        """Delete a document and its chunks (CASCADE will handle chunks automatically)"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute('DELETE FROM documents WHERE id = %s', (document_id,))
                    conn.commit()
                    return cursor.rowcount > 0
        except psycopg2.Error as e:
            print(f"Error deleting document: {e}")
            return False
    
    def add_document_chunk(self, document_id: int, chunk_text: str, 
                          chunk_index: int = None, metadata: dict = None) -> int:
        """Add a document chunk"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute('''
                        INSERT INTO document_chunks (document_id, chunk_text, chunk_index, metadata)
                        VALUES (%s, %s, %s, %s)
                        RETURNING id
                    ''', (document_id, chunk_text, chunk_index, json.dumps(metadata) if metadata else None))
                    
                    chunk_id = cursor.fetchone()[0]
                    conn.commit()
                    return chunk_id
        except psycopg2.Error as e:
            print(f"Error adding document chunk: {e}")
            raise
    
    def get_document_chunks(self, document_id: int) -> List[Dict]:
        """Get all chunks for a document"""
        try:
            with self.get_connection() as conn:
                with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
                    cursor.execute('''
                        SELECT * FROM document_chunks 
                        WHERE document_id = %s 
                        ORDER BY chunk_index
                    ''', (document_id,))
                    
                    chunks = []
                    for row in cursor.fetchall():
                        chunk = dict(row)
                        if chunk['metadata']:
                            chunk['metadata'] = json.loads(chunk['metadata']) if isinstance(chunk['metadata'], str) else chunk['metadata']
                        chunks.append(chunk)
                    
                    return chunks
        except psycopg2.Error as e:
            print(f"Error getting document chunks: {e}")
            return []
    
    def search_documents(self, query: str, document_type: str = None) -> List[Dict]:
        """Search documents by content or title"""
        try:
            with self.get_connection() as conn:
                with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
                    if document_type:
                        cursor.execute('''
                            SELECT * FROM documents 
                            WHERE (title ILIKE %s OR content ILIKE %s) 
                            AND document_type = %s
                            ORDER BY created_at DESC
                        ''', (f'%{query}%', f'%{query}%', document_type))
                    else:
                        cursor.execute('''
                            SELECT * FROM documents 
                            WHERE title ILIKE %s OR content ILIKE %s
                            ORDER BY created_at DESC
                        ''', (f'%{query}%', f'%{query}%'))
                    
                    documents = []
                    for row in cursor.fetchall():
                        document = dict(row)
                        if document['metadata']:
                            document['metadata'] = json.loads(document['metadata']) if isinstance(document['metadata'], str) else document['metadata']
                        documents.append(document)
                    
                    return documents
        except psycopg2.Error as e:
            print(f"Error searching documents: {e}")
            return []
    
    def get_document_count(self) -> int:
        """Get total number of documents"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute('SELECT COUNT(*) FROM documents')
                    return cursor.fetchone()[0]
        except psycopg2.Error as e:
            print(f"Error getting document count: {e}")
            return 0
    
    def close(self):
        """Close database connections (handled automatically by context managers)"""
        pass