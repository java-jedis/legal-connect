#run after preprocessing done
#run also when database document_chunk and documents are empty
"""
Sync Qdrant Vector Data to PostgreSQL
"""
import os
import sys
from datetime import datetime

# Add the parent directory to the path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Load environment variables manually
def load_env_file():
    """Load environment variables from .env file"""
    env_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), '.env')
    if os.path.exists(env_path):
        with open(env_path, 'r') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    os.environ[key] = value

load_env_file()

def sync_qdrant_to_postgres():
    """Sync existing Qdrant vectors to PostgreSQL tables"""
    try:
        print("🔗 Connecting to Qdrant and PostgreSQL...")
        
        # Import required modules
        from qdrant_client import QdrantClient
        from app.db.database import SessionLocal
        from app.db.models import Document, DocumentChunk
        
        # Connect to Qdrant
        client = QdrantClient(
            url=os.getenv("QDRANT_URL"),
            api_key=os.getenv("QDRANT_API_KEY"),
        )
        
        collection_name = os.getenv("QDRANT_COLLECTION_NAME", "legal-vector-db")
        print(f"📊 Syncing from collection: {collection_name}")
        
        # Get database session
        db = SessionLocal()
        
        try:
            # Get all points from Qdrant
            print("📥 Fetching vectors from Qdrant...")
            
            # Scroll through all vectors in batches
            offset = None
            total_synced = 0
            documents_created = {}  # Track created documents
            
            while True:
                # Get batch of vectors
                points, next_offset = client.scroll(
                    collection_name=collection_name,
                    limit=100,  # Process in batches
                    offset=offset,
                    with_payload=True
                )
                
                if not points:
                    break
                
                print(f"📦 Processing batch of {len(points)} vectors...")
                
                for point in points:
                    try:
                        payload = point.payload or {}
                        
                        # Extract information from payload
                        content = payload.get('content', payload.get('text', ''))
                        doc_name = payload.get('document_name', payload.get('source', f'document_{point.id}'))
                        doc_type = payload.get('document_type', 'pdf')
                        
                        # Extract chunk index from metadata
                        metadata = payload.get('metadata', {})
                        chunk_index = metadata.get('chunk_index', 0)  # Get from metadata field
                        
                        page_num = payload.get('page_number', payload.get('page', 1))
                        
                        # Create document if not exists
                        if doc_name not in documents_created:
                            existing_doc = db.query(Document).filter(Document.filename == doc_name).first()
                            
                            if not existing_doc:
                                document = Document(
                                    name=doc_name,  # Use 'name' field
                                    filename=doc_name,  # Keep filename
                                    text=content[:2000] + "..." if len(content) > 2000 else content,  # Store preview in 'text' field
                                    extraction_date=datetime.utcnow(),
                                    language='ben',  # Default Bengali
                                    extraction_method='qdrant_sync',
                                    document_metadata={'type': doc_type, 'synced_from_qdrant': True}
                                )
                                db.add(document)
                                db.flush()  # Get the ID
                                documents_created[doc_name] = document.id
                                print(f"📄 Created document: {doc_name}")
                            else:
                                documents_created[doc_name] = existing_doc.id
                        
                        # Create document chunk
                        chunk = DocumentChunk(
                            document_id=documents_created[doc_name],
                            chunk_text=content,  # Use 'chunk_text' field instead of 'content'
                            chunk_index=chunk_index,
                            page_number=page_num,
                            vector_id=str(point.id)  # Store Qdrant vector ID
                        )
                        db.add(chunk)
                        total_synced += 1
                        
                    except Exception as e:
                        print(f"⚠️  Error processing point {point.id}: {e}")
                        continue
                
                # Commit batch
                db.commit()
                print(f"✅ Synced {total_synced} chunks so far...")
                
                # Check if we have more data
                offset = next_offset
                if offset is None:
                    break
            
            print(f"\n🎉 Sync completed!")
            print(f"📊 Total documents created: {len(documents_created)}")
            print(f"📊 Total chunks synced: {total_synced}")
            
            # Verify the sync
            doc_count = db.query(Document).count()
            chunk_count = db.query(DocumentChunk).count()
            
            print(f"\n📋 Database verification:")
            print(f"   Documents in PostgreSQL: {doc_count}")
            print(f"   Chunks in PostgreSQL: {chunk_count}")
            
            return True
            
        except Exception as e:
            db.rollback()
            print(f"❌ Error during sync: {e}")
            return False
        finally:
            db.close()
            
    except Exception as e:
        print(f"❌ Connection error: {e}")
        return False

def verify_sync():
    """Verify the sync was successful"""
    try:
        from app.db.database import SessionLocal
        from app.db.models import Document, DocumentChunk
        
        db = SessionLocal()
        
        try:
            # Count records
            doc_count = db.query(Document).count()
            chunk_count = db.query(DocumentChunk).count()
            
            print(f"\n📊 Verification Results:")
            print(f"   Documents: {doc_count}")
            print(f"   Chunks: {chunk_count}")
            
            if chunk_count > 0:
                # Show sample data
                sample_chunk = db.query(DocumentChunk).first()
                print(f"\n📋 Sample chunk:")
                print(f"   Document ID: {sample_chunk.document_id}")
                print(f"   Vector ID: {sample_chunk.vector_id}")
                print(f"   Content: {sample_chunk.chunk_text[:100]}...")
                print(f"   Page: {sample_chunk.page_number}")
                
                return True
            else:
                print("❌ No chunks found in database")
                return False
                
        finally:
            db.close()
            
    except Exception as e:
        print(f"❌ Verification error: {e}")
        return False

def main():
    """Main function"""
    print("🔄 Qdrant to PostgreSQL Sync")
    print("=" * 35)
    print("📋 Purpose: Sync your 32,000+ Qdrant vectors to PostgreSQL")
    print("💡 This will populate document_chunks table with metadata")
    print()
    
    success = sync_qdrant_to_postgres()
    
    if success:
        print("\n🔍 Verifying sync...")
        verify_sync()
        
        print("\n" + "=" * 35)
        print("🎉 SUCCESS: Qdrant data synced to PostgreSQL!")
        print("\n📋 Now you have:")
        print("   ✅ 32,000+ vectors in Qdrant (for similarity search)")
        print("   ✅ Corresponding metadata in PostgreSQL (for chat context)")
        print("   ✅ Ready for chat functionality!")
        print("\n🚀 Next step: Start FastAPI server and test chat")
    else:
        print("\n❌ FAILED: Could not sync data")
        print("🔧 Check database connections and try again")

if __name__ == "__main__":
    main()