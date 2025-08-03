"""
Test the RAG system with sample queries
"""

import asyncio
import os
import sys
from pathlib import Path

# Add project root to path
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from app.services.embeddings import EmbeddingService
from app.services.vectordb import QdrantService
from app.services.llm import GeminiService
from app.services.rag import RAGService

async def test_rag_system():
    """Test the RAG system with sample queries"""
    
    # Load environment
    from dotenv import load_dotenv
    load_dotenv()
    
    # Initialize services
    print("🔧 Initializing services...")
    embedding_service = EmbeddingService()
    vectordb_service = QdrantService()
    llm_service = GeminiService()
    
    # Initialize vector database
    await vectordb_service.initialize()
    
    # Create RAG service
    rag_service = RAGService(embedding_service, vectordb_service, llm_service)
    
    # Test queries in both Bengali and English
    test_queries = [
        "পার্বত্য চট্টগ্রাম আঞ্চলিক পরিষদের ক্ষমতা কী কী?",
        "What are the powers of Chittagong Hill Tracts Regional Council?",
        "সালিস আইন সম্পর্কে বলুন",
        "What is arbitration law in Bangladesh?",
        "নির্বাচন কমিশনের দায়িত্ব কী?"
    ]
    
    print("\n🧪 Testing RAG System with sample queries...\n")
    
    for i, query in enumerate(test_queries, 1):
        print(f"🔍 Query {i}: {query}")
        print("-" * 60)
        
        try:
            # Process query through RAG pipeline
            response = await rag_service.process_query(query)
            
            print(f"📝 Response: {response['answer'][:300]}...")
            print(f"📚 Sources: {len(response['sources'])} documents")
            print(f"📊 Avg Similarity: {response['metadata']['avg_similarity']}")
            
            # Show top source
            if response['sources']:
                top_source = response['sources'][0]
                print(f"🔗 Top Source: {top_source['document_name']}")
                print(f"   Page: {top_source.get('page_number', 'N/A')}")
                print(f"   Score: {top_source['similarity_score']}")
            
        except Exception as e:
            print(f"❌ Error: {e}")
        
        print("\n" + "="*80 + "\n")
    
    # Test search functionality
    print("🔍 Testing document search...")
    search_results = await rag_service.search_documents(
        query="আইন", 
        top_k=3
    )
    
    print(f"📄 Found {len(search_results)} documents")
    for i, result in enumerate(search_results, 1):
        print(f"   {i}. {result['document_name']} (Score: {result['similarity_score']})")
    
    # Health check
    print("\n🏥 Running health check...")
    health = await rag_service.health_check()
    print(f"   Overall Health: {'✅ Healthy' if health['overall'] else '❌ Unhealthy'}")
    print(f"   Embedding Service: {'✅' if health['embedding_service'] else '❌'}")
    print(f"   Vector Database: {'✅' if health['vector_database'] else '❌'}")
    print(f"   LLM Service: {'✅' if health['llm_service'] else '❌'}")
    
    # Close connections
    await vectordb_service.close()
    
    print("\n🎉 RAG System test completed!")

if __name__ == "__main__":
    asyncio.run(test_rag_system())
