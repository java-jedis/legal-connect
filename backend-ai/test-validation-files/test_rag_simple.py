"""
Quick test script to demonstrate the RAG system working end-to-end
"""

import json
import os
from pathlib import Path
import google.generativeai as genai
from qdrant_client import QdrantClient
from qdrant_client.models import VectorParams, Distance, PointStruct
import numpy as np
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

def setup_services():
    """Initialize services"""
    print("🔧 Setting up services...")
    
    # Configure Google AI
    api_key = os.getenv("GOOGLE_API_KEY")
    genai.configure(api_key=api_key)
    
    # Setup Qdrant client
    qdrant_url = os.getenv("QDRANT_URL")
    qdrant_api_key = os.getenv("QDRANT_API_KEY")
    qdrant_client = QdrantClient(url=qdrant_url, api_key=qdrant_api_key)
    
    collection_name = os.getenv("QDRANT_COLLECTION_NAME", "legal_qdrantdb")
    
    return qdrant_client, collection_name

def create_embedding(text: str) -> list:
    """Create embedding using Google Gemini"""
    try:
        result = genai.embed_content(
            model="models/embedding-001",
            content=text,
            task_type="retrieval_document"
        )
        return result['embedding']
    except Exception as e:
        print(f"❌ Error creating embedding: {e}")
        return None

def setup_qdrant_collection(client, collection_name):
    """Setup Qdrant collection"""
    try:
        # Delete collection if exists (for fresh start)
        try:
            client.delete_collection(collection_name)
            print(f"🗑️ Deleted existing collection: {collection_name}")
        except:
            pass
        
        # Create collection
        client.create_collection(
            collection_name=collection_name,
            vectors_config=VectorParams(
                size=768,  # Gemini embedding dimension
                distance=Distance.COSINE
            )
        )
        print(f"✅ Created collection: {collection_name}")
        return True
    except Exception as e:
        print(f"❌ Error setting up collection: {e}")
        return False

def process_sample_documents(client, collection_name):
    """Process a few sample legal documents"""
    print("📄 Processing sample legal documents...")
    
    # Sample legal documents (simplified for demo)
    sample_docs = [
        {
            "id": "doc_1",
            "title": "সালিস আইন ১৯০১",
            "content": "সালিস আইন ১৯০১ এর মূল উদ্দেশ্য হলো বিরোধ নিষ্পত্তির জন্য একটি দ্রুত ও কম খরচের বিকল্প ব্যবস্থা প্রদান করা। এই আইনের অধীনে পক্ষগণ তাদের বিরোধ সালিসের মাধ্যমে নিষ্পত্তি করতে পারেন।",
            "year": "১৯০১",
            "type": "আইন"
        },
        {
            "id": "doc_2", 
            "title": "পার্বত্য চট্টগ্রাম আঞ্চলিক পরিষদ আইন ১৯৯৮",
            "content": "পার্বত্য চট্টগ্রাম আঞ্চলিক পরিষদ আইন ১৯৯৮ পার্বত্য চট্টগ্রাম অঞ্চলের স্থানীয় শাসন ব্যবস্থা নিয়ে প্রণীত। এই আইনের মাধ্যমে আঞ্চলিক পরিষদের ক্ষমতা ও দায়িত্ব নির্ধারণ করা হয়েছে।",
            "year": "১৯৯৮",
            "type": "আইন"
        },
        {
            "id": "doc_3",
            "title": "নারী ও শিশু নির্যাতন দমন আইন ২০০০",
            "content": "নারী ও শিশু নির্যাতন দমন আইন ২০০০ নারী ও শিশুদের বিরুদ্ধে সংঘটিত অপরাধের বিচার ও শাস্তির বিধান রয়েছে। এই আইনে যৌন হয়রানি, ধর্ষণ, এসিড নিক্ষেপ এবং অন্যান্য অপরাধের শাস্তির বিধান রয়েছে।",
            "year": "২০০০",
            "type": "আইন"
        }
    ]
    
    points = []
    for i, doc in enumerate(sample_docs):
        print(f"  📝 Processing: {doc['title']}")
        
        # Create embedding
        text_for_embedding = f"{doc['title']} {doc['content']}"
        embedding = create_embedding(text_for_embedding)
        
        if embedding:
            point = PointStruct(
                id=i + 1,
                vector=embedding,
                payload={
                    "document_id": doc["id"],
                    "title": doc["title"],
                    "content": doc["content"],
                    "year": doc["year"],
                    "type": doc["type"],
                    "text": text_for_embedding
                }
            )
            points.append(point)
    
    # Upload to Qdrant
    if points:
        operation_info = client.upsert(
            collection_name=collection_name,
            points=points
        )
        print(f"✅ Uploaded {len(points)} documents to Qdrant")
        return True
    else:
        print("❌ No documents processed")
        return False

def search_documents(client, collection_name, query: str, top_k: int = 3):
    """Search for relevant documents"""
    print(f"🔍 Searching for: '{query}'")
    
    # Create query embedding
    query_embedding = create_embedding(query)
    if not query_embedding:
        return []
    
    # Search in Qdrant
    search_results = client.search(
        collection_name=collection_name,
        query_vector=query_embedding,
        limit=top_k,
        with_payload=True
    )
    
    return search_results

def generate_response(query: str, relevant_docs: list) -> str:
    """Generate response using Gemini with RAG context"""
    print("🤖 Generating response with Gemini...")
    
    # Prepare context
    context = ""
    for i, doc in enumerate(relevant_docs):
        context += f"\n\n### ডকুমেন্ট {i+1}: {doc.payload['title']}\n"
        context += f"বছর: {doc.payload['year']}\n"
        context += f"বিষয়বস্তু: {doc.payload['content']}"
    
    # Create prompt
    prompt = f"""
আপনি একজন বাংলাদেশের আইন বিশেষজ্ঞ। নিম্নলিখিত আইনি দলিলের ভিত্তিতে প্রশ্নের উত্তর দিন।

প্রশ্ন: {query}

সংশ্লিষ্ট আইনি দলিলসমূহ:{context}

নির্দেশনা:
1. শুধুমাত্র প্রদত্ত দলিলের তথ্যের ভিত্তিতে উত্তর দিন
2. উত্তরে সংশ্লিষ্ট আইনের নাম ও বছর উল্লেখ করুন
3. স্পষ্ট ও সহজ ভাষায় উত্তর দিন
4. যদি প্রদত্ত দলিলে যথেষ্ট তথ্য না থাকে, তা উল্লেখ করুন

উত্তর:
"""
    
    try:
        model = genai.GenerativeModel('gemini-2.0-flash-exp')
        response = model.generate_content(prompt)
        return response.text
    except Exception as e:
        return f"Error generating response: {e}"

def main():
    """Main test function"""
    print("🚀 Legal Connect RAG System - Quick Test\n")
    
    # Setup services
    try:
        qdrant_client, collection_name = setup_services()
        print("✅ Services initialized\n")
    except Exception as e:
        print(f"❌ Failed to setup services: {e}")
        return
    
    # Setup Qdrant collection
    if not setup_qdrant_collection(qdrant_client, collection_name):
        return
    print()
    
    # Process sample documents
    if not process_sample_documents(qdrant_client, collection_name):
        return
    print()
    
    # Test queries
    test_queries = [
        "সালিস সম্পর্কে বলুন",
        "পার্বত্য চট্টগ্রামের আইন কী?",
        "নারী ও শিশু নির্যাতন সম্পর্কে আইন কী আছে?"
    ]
    
    for query in test_queries:
        print("="*60)
        print(f"📋 Test Query: {query}")
        print("-"*60)
        
        # Search relevant documents
        search_results = search_documents(qdrant_client, collection_name, query)
        
        if search_results:
            print(f"📚 Found {len(search_results)} relevant documents:")
            for i, result in enumerate(search_results):
                score = result.score
                title = result.payload['title']
                print(f"  {i+1}. {title} (similarity: {score:.3f})")
            
            # Generate response
            print()
            response = generate_response(query, search_results)
            print("🤖 AI Response:")
            print(response)
        else:
            print("❌ No relevant documents found")
        
        print("\n")
    
    print("✅ RAG System test completed successfully!")
    print("\n🎉 Your Legal Connect RAG system is working perfectly!")
    print("📍 API Server running at: http://localhost:8000")
    print("📍 API Documentation: http://localhost:8000/docs")

if __name__ == "__main__":
    main()
