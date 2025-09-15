"""
Test script for chat document upload functionality
"""

import asyncio
import aiohttp
import json
from pathlib import Path

async def test_document_upload():
    """Test document upload to chat session"""
    
    base_url = "http://localhost:8000/api/v1"
    
    # Test data
    test_pdf_content = b"%PDF-1.4\n1 0 obj\n<<\n/Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n2 0 obj\n<<\n/Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n3 0 obj\n<<\n/Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 612 792]\n/Contents 4 0 R\n>>\nendobj\n4 0 obj\n<<\n/Length 44\n>>\nstream\nBT\n/F1 12 Tf\n72 720 Td\n(Test Document) Tj\nET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f \n0000000010 00000 n \n0000000053 00000 n \n0000000125 00000 n \n0000000185 00000 n \ntrailer\n<<\n/Size 5\n/Root 1 0 R\n>>\nstartxref\n238\n%%EOF"
    
    headers = {
        "Authorization": "Bearer test_token"  # You may need a valid JWT token
    }
    
    async with aiohttp.ClientSession() as session:
        # Step 1: Create a new chat session
        print("1. Creating new chat session...")
        async with session.post(f"{base_url}/sessions", headers=headers, json={"title": "Test Session"}) as resp:
            if resp.status == 200:
                session_data = await resp.json()
                session_id = session_data["session_id"]
                print(f"✅ Session created: {session_id}")
            else:
                print(f"❌ Failed to create session: {resp.status}")
                return
        
        # Step 2: Upload a document to the session
        print("2. Uploading document...")
        data = aiohttp.FormData()
        data.add_field('file', test_pdf_content, filename='test_document.pdf', content_type='application/pdf')
        
        async with session.post(f"{base_url}/sessions/{session_id}/upload-document", headers=headers, data=data) as resp:
            if resp.status == 200:
                upload_result = await resp.json()
                print(f"✅ Document uploaded: {upload_result}")
            else:
                error_text = await resp.text()
                print(f"❌ Failed to upload document: {resp.status} - {error_text}")
                return
        
        # Step 3: List session documents
        print("3. Listing session documents...")
        async with session.get(f"{base_url}/sessions/{session_id}/documents", headers=headers) as resp:
            if resp.status == 200:
                documents = await resp.json()
                print(f"✅ Session documents: {json.dumps(documents, indent=2)}")
            else:
                print(f"❌ Failed to list documents: {resp.status}")
        
        # Step 4: Send a chat message to test RAG with uploaded document
        print("4. Testing chat with uploaded document...")
        chat_data = {
            "message": "What does this document say?",
            "session_id": session_id
        }
        
        async with session.post(f"{base_url}/chat", headers=headers, json=chat_data) as resp:
            if resp.status == 200:
                chat_response = await resp.json()
                print(f"✅ Chat response: {chat_response['response']}")
            else:
                error_text = await resp.text()
                print(f"❌ Failed to send chat message: {resp.status} - {error_text}")

async def test_health():
    """Test health endpoint"""
    async with aiohttp.ClientSession() as session:
        async with session.get("http://localhost:8000/health") as resp:
            if resp.status == 200:
                health_data = await resp.json()
                print(f"✅ Health check: {json.dumps(health_data, indent=2)}")
            else:
                print(f"❌ Health check failed: {resp.status}")

if __name__ == "__main__":
    print("🧪 Testing Chat Document Upload Functionality")
    print("=" * 50)
    
    # Test health first
    asyncio.run(test_health())
    print()
    
    # Test document upload
    # asyncio.run(test_document_upload())
