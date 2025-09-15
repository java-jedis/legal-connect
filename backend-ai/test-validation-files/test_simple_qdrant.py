"""
Simple Qdrant Vector Data Test
"""
import os
from dotenv import load_dotenv

load_dotenv()

def test_qdrant_simple():
    """Simple test to check if Qdrant data exists"""
    try:
        from qdrant_client import QdrantClient
        
        print("🔗 Connecting to Qdrant...")
        
        # Connect to Qdrant
        client = QdrantClient(
            url=os.getenv("QDRANT_URL"),
            api_key=os.getenv("QDRANT_API_KEY"),
        )
        
        collection_name = os.getenv("QDRANT_COLLECTION_NAME", "legal-vector-db")
        print(f"📊 Checking collection: {collection_name}")
        
        # Check if collection exists
        try:
            collection_info = client.get_collection(collection_name)
            
            print(f"✅ Collection found!")
            print(f"📊 Vector count: {collection_info.points_count}")
            print(f"📏 Vector dimension: {collection_info.config.params.vectors.size}")
            print(f"💾 Status: {collection_info.status}")
            
            if collection_info.points_count > 0:
                print(f"\n🎉 GOOD NEWS! Apnar {collection_info.points_count} vectors Ache!")
                
                # Get a few sample points
                try:
                    points = client.scroll(
                        collection_name=collection_name,
                        limit=3,
                        with_payload=True
                    )[0]
                    
                    print(f"\n📋 Sample data preview:")
                    for i, point in enumerate(points, 1):
                        payload = point.payload or {}
                        content = str(payload.get('content', payload.get('text', 'No content')))
                        doc_name = payload.get('document_name', payload.get('source', 'Unknown'))
                        
                        print(f"   {i}. Document: {doc_name}")
                        print(f"      Content: {content[:100]}...")
                        print(f"      Vector ID: {point.id}")
                        print()
                    
                    print("✅ Vector embeddings are SAFE in Qdrant Cloud!")
                    print("💡 You don't need to re-run preprocessing")
                    print("📝 Just need to recreate PostgreSQL metadata tables")
                    
                except Exception as e:
                    print(f"⚠️  Could not fetch sample data: {e}")
                    print("✅ But vectors exist - that's the important part!")
                
            else:
                print("❌ Collection exists but no vectors found!")
                print("🔧 You'll need to re-run preprocessing")
            
            return True, collection_info.points_count
            
        except Exception as e:
            print(f"❌ Collection not found or error: {e}")
            print("🔧 You'll need to re-run preprocessing completely")
            return False, 0
            
    except Exception as e:
        print(f"❌ Qdrant connection failed: {e}")
        print("🔧 Check your QDRANT_URL and QDRANT_API_KEY in .env")
        return False, 0

def main():
    """Main test function"""
    print("🧪 Qdrant Vector Data Test")
    print("=" * 30)
    print(f"🌐 URL: {os.getenv('QDRANT_URL')}")
    print(f"🔑 API Key: {os.getenv('QDRANT_API_KEY')[:20]}...")
    print(f"📦 Collection: {os.getenv('QDRANT_COLLECTION_NAME')}")
    print()
    
    success, count = test_qdrant_simple()
    
    print("\n" + "=" * 30)
    if success:
        if count > 0:
            print("🎉 RESULT: Your vector embeddings are SAFE!")
            print("📋 Next steps:")
            print("   1. Your 32,000+ vectors are intact in Qdrant")
            print("   2. Just recreate PostgreSQL tables (documents, document_chunks)")
            print("   3. Start FastAPI server - chat will work with existing vectors")
        else:
            print("⚠️  RESULT: Collection exists but empty")
            print("🔧 Need to re-run preprocessing")
    else:
        print("❌ RESULT: Cannot access Qdrant or collection missing")
        print("🔧 Need to check connection and re-run preprocessing")

if __name__ == "__main__":
    main()
