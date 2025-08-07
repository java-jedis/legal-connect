#!/usr/bin/env python3
"""
Check Qdrant collection status
"""
import asyncio
import os
import sys
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Add project root to path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.services.vectordb import QdrantService

async def check_qdrant_status():
    """Check Qdrant collection status"""
    try:
        print("🔍 Checking Qdrant collection status...")
        
        vectordb = QdrantService()
        await vectordb.initialize()
        
        # Get collection info
        stats = await vectordb.get_collection_info()
        
        print(f"📊 Collection Information:")
        print(f"   - Collection Name: {stats.get('name', 'N/A')}")
        print(f"   - Points Count: {stats.get('points_count', 0)}")
        print(f"   - Vectors Count: {stats.get('vectors_count', 0)}")
        print(f"   - Status: {stats.get('status', 'Unknown')}")
        
        if stats.get('points_count', 0) == 0:
            print("\n❌ Your Qdrant collection is EMPTY!")
            print("This is why you're getting dummy search results.")
            print("\n🔧 To fix this, you need to:")
            print("1. Add your legal documents (JSON format)")
            print("2. Run the preprocessing script")
        else:
            print(f"\n✅ Your collection has {stats.get('points_count')} documents")
            
            # Test a search to see what kind of data is there
            print("\n🧪 Testing search functionality...")
            try:
                from app.services.embeddings import EmbeddingService
                embedding_service = EmbeddingService()
                
                # Generate test query embedding
                query_embedding = await embedding_service.generate_query_embedding("marriage law")
                
                # Search
                results = await vectordb.search_similar(
                    query_embedding=query_embedding,
                    top_k=3,
                    score_threshold=0.3
                )
                
                print(f"📋 Sample search results ({len(results)} found):")
                for i, result in enumerate(results[:2]):
                    print(f"   {i+1}. Score: {result.get('score', 0):.3f}")
                    print(f"      Document: {result.get('document_name', 'N/A')}")
                    print(f"      Text: {result.get('text', '')[:100]}...")
                    print()
                    
            except Exception as search_error:
                print(f"❌ Search test failed: {search_error}")
        
        await vectordb.close()
        
    except Exception as e:
        print(f"❌ Error checking Qdrant: {e}")
        print("\nPossible issues:")
        print("- Qdrant URL/API key incorrect")
        print("- Network connectivity problems")
        print("- Collection doesn't exist")

if __name__ == "__main__":
    asyncio.run(check_qdrant_status())
