#!/usr/bin/env python3
"""
Test the RAG search functionality with optimized parameters
"""
import asyncio
import aiohttp
import json

async def test_search_with_better_params():
    """Test search with lower similarity threshold and better queries"""
    
    # Test queries in both Bengali and English
    test_queries = [
        "বিবাহ আইন",  # Marriage law in Bengali
        "marriage law",  # Marriage law in English
        "divorce আইন",  # Divorce law (mixed)
        "কোম্পানি আইন",  # Company law in Bengali
        "company law",  # Company law in English
        "সম্পত্তি আইন",  # Property law in Bengali
        "property law"  # Property law in English
    ]
    
    base_url = "http://localhost:8000"
    
    async with aiohttp.ClientSession() as session:
        print("🔍 Testing RAG search with optimized parameters")
        print("=" * 60)
        
        for i, query in enumerate(test_queries, 1):
            print(f"\n📝 Test {i}: '{query}'")
            print("-" * 40)
            
            # Test with different similarity thresholds
            for threshold in [0.3, 0.5, 0.7]:
                search_data = {
                    "query": query,
                    "top_k": 5,
                    "similarity_threshold": threshold
                }
                
                try:
                    async with session.post(
                        f"{base_url}/api/v1/search/",
                        json=search_data,
                        headers={"Content-Type": "application/json"}
                    ) as response:
                        
                        if response.status == 200:
                            result = await response.json()
                            documents = result.get('documents', [])
                            
                            print(f"  Threshold {threshold}: {len(documents)} results")
                            
                            if documents:
                                for j, doc in enumerate(documents[:2], 1):  # Show top 2
                                    doc_name = doc.get('document_name', 'Unknown')
                                    similarity = doc.get('similarity_score', 0)
                                    text_preview = doc.get('text', '')[:100] + "..." if len(doc.get('text', '')) > 100 else doc.get('text', '')
                                    
                                    print(f"    {j}. {doc_name} (Score: {similarity:.3f})")
                                    print(f"       {text_preview}")
                            else:
                                print("    No relevant documents found")
                        else:
                            print(f"  Threshold {threshold}: Error {response.status}")
                            
                except Exception as e:
                    print(f"  Threshold {threshold}: Exception - {e}")
        
        print("\n" + "=" * 60)
        print("✅ RAG search testing completed")

if __name__ == "__main__":
    asyncio.run(test_search_with_better_params())
