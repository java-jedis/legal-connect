"""
Test if Qdrant vector data still exists
"""
import os
from dotenv import load_dotenv

load_dotenv()

def test_qdrant_connection():
    """Test Qdrant connection and data"""
    try:
        from qdrant_client import QdrantClient
        
        # Connect to Qdrant
        client = QdrantClient(
            url=os.getenv("QDRANT_URL"),
            api_key=os.getenv("QDRANT_API_KEY"),
        )
        
        collection_name = os.getenv("QDRANT_COLLECTION_NAME", "legal_qd_db")
        
        # Check collection info
        collection_info = client.get_collection(collection_name)
        print(f"🔍 Collection: {collection_name}")
        print(f"📊 Vector count: {collection_info.points_count}")
        print(f"📏 Vector size: {collection_info.config.params.vectors.size}")
        
        # Get a sample of points
        points = client.scroll(
            collection_name=collection_name,
            limit=5,
            with_payload=True
        )[0]
        
        print(f"\n📋 Sample documents found:")
        for i, point in enumerate(points, 1):
            payload = point.payload or {}
            print(f"   {i}. Document: {payload.get('document_name', 'Unknown')}")
            print(f"      Content preview: {str(payload.get('content', ''))[:100]}...")
            print(f"      Chunk: {payload.get('chunk_index', 'N/A')}")
            print()
        
        return True, collection_info.points_count
        
    except Exception as e:
        print(f"❌ Qdrant test failed: {e}")
        return False, 0

async def test_search_functionality():
    """Test if search still works"""
    try:
        # Test a simple search
        from app.services.vectordb import QdrantService
        from app.services.embeddings import EmbeddingService
        
        # Initialize services
        embedding_service = EmbeddingService()
        vectordb_service = QdrantService()
        
        # Test search
        query = "legal contract"
        print(f"\n🔍 Testing search with query: '{query}'")
        
        # Get embedding for query
        query_embedding = await embedding_service.generate_query_embedding(query)
        
        # Search in Qdrant
        results = await vectordb_service.search_similar(query_embedding, top_k=3)
        
        print(f"✅ Search returned {len(results)} results")
        for i, result in enumerate(results, 1):
            print(f"   {i}. Score: {result.get('score', 'N/A'):.3f}")
            print(f"      Content: {str(result.get('content', ''))[:100]}...")
        
        return True
        
    except Exception as e:
        print(f"❌ Search test failed: {e}")
        import traceback
        traceback.print_exc()
        return False

async def main():
    """Main test function"""
    print("🧪 Testing Qdrant Vector Data")
    print("=" * 40)
    
    # Test connection and count
    success, count = test_qdrant_connection()
    
    if success:
        if count > 0:
            print(f"\n✅ Good news! আপনার {count} vectors still আছে Qdrant এ!")
            print("🔍 Your embeddings are safe in the cloud cluster")
            
            # Test search functionality
            print("\n🔍 Testing search functionality...")
            search_success = await test_search_functionality()
            
            if search_success:
                print("✅ Search functionality working!")
                print("\n📋 Summary:")
                print("   ✅ Vector embeddings: Safe in Qdrant Cloud")
                print("   ✅ Search functionality: Working")
                print("   ❌ Document metadata: Lost (need to re-upload)")
                print("   ❌ PostgreSQL records: Lost (need to recreate)")
                
                print("\n🔧 What you need to do:")
                print("   1. Re-upload documents to recreate PostgreSQL metadata")
                print("   2. Or sync existing Qdrant data back to PostgreSQL")
            else:
                print("❌ Search functionality has issues")
        else:
            print("❌ No vectors found in Qdrant!")
            print("🔧 You'll need to re-run preprocessing to recreate embeddings")
    else:
        print("❌ Cannot connect to Qdrant!")

if __name__ == "__main__":
    import asyncio
    asyncio.run(main())