import psycopg2
import psycopg2.extras
import os
from datetime import datetime
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# PostgreSQL connection parameters
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 5432)),
    'database': os.getenv('DB_NAME', 'rag_app'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', 'password')
}

def get_db_connection():
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except psycopg2.Error as e:
        print(f"Database connection error: {e}")
        raise


def create_application_logs():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''CREATE TABLE IF NOT EXISTS application_logs
                    (id SERIAL PRIMARY KEY,
                     session_id TEXT,
                     user_query TEXT,
                     gpt_response TEXT,
                     model TEXT,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)''')
    conn.commit()
    conn.close()

def create_document_store():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''CREATE TABLE IF NOT EXISTS document_store
                    (id SERIAL PRIMARY KEY,
                     filename TEXT,
                     upload_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)''')
    conn.commit()
    conn.close()



def insert_application_logs(session_id, user_query, gpt_response, model):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('INSERT INTO application_logs (session_id, user_query, gpt_response, model) VALUES (%s, %s, %s, %s)',
                 (session_id, user_query, gpt_response, model))
    conn.commit()
    conn.close()

def get_chat_history(session_id):
    conn = get_db_connection()
    cursor = conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
    cursor.execute('SELECT user_query, gpt_response FROM application_logs WHERE session_id = %s ORDER BY created_at', (session_id,))
    messages = []
    for row in cursor.fetchall():
        messages.extend([
            {"role": "human", "content": row['user_query']},
            {"role": "ai", "content": row['gpt_response']}
        ])
    conn.close()
    return messages



def insert_document_record(filename):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('INSERT INTO document_store (filename) VALUES (%s) RETURNING id', (filename,))
    file_id = cursor.fetchone()[0]
    conn.commit()
    conn.close()
    return file_id

def delete_document_record(file_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('DELETE FROM document_store WHERE id = %s', (file_id,))
    conn.commit()
    conn.close()
    return True

def get_all_documents():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('SELECT id, filename, upload_timestamp FROM document_store ORDER BY upload_timestamp DESC')
    documents = cursor.fetchall()
    conn.close()
    return [dict(doc) for doc in documents]

# Initialize database tables safely
def initialize_database():
    """Initialize database tables. Call this during app startup."""
    try:
        create_application_logs()
        create_document_store()
        print("Database tables initialized successfully")
    except Exception as e:
        print(f"Error initializing database: {e}")
        raise

# Only initialize if this module is run directly (for testing)
if __name__ == "__main__":
    initialize_database()
