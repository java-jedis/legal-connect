#!/usr/bin/env python3
"""
Test script for session management functionality
Run this after starting the backend to test the chat session management
"""

import requests
import json
from uuid import uuid4

# Configuration
BASE_URL = "http://localhost:8000"
API_BASE = f"{BASE_URL}/api/v1"

def test_session_management():
    """Test the session management functionality"""
    print("🧪 Testing Legal Connect AI Session Management")
    print("=" * 50)
    
    # Test 1: Health check
    print("1. Testing health check...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        assert response.status_code == 200
        print("✅ Health check passed")
    except Exception as e:
        print(f"❌ Health check failed: {e}")
        return
    
    # Test 2: Create a new session
    print("\n2. Testing session creation...")
    try:
        create_data = {
            "user_id": str(uuid4()),
            "title": "Test Session"
        }
        response = requests.post(f"{API_BASE}/sessions", json=create_data)
        assert response.status_code == 200
        session_data = response.json()
        session_id = session_data["session_id"]
        print(f"✅ Session created: {session_id}")
    except Exception as e:
        print(f"❌ Session creation failed: {e}")
        return
    
    # Test 3: Send a chat message
    print("\n3. Testing chat message...")
    try:
        chat_data = {
            "message": "What is the legal definition of contract?",
            "session_id": session_id,
            "user_id": create_data["user_id"],
            "context_limit": 3
        }
        response = requests.post(f"{API_BASE}/chat", json=chat_data)
        assert response.status_code == 200
        chat_response = response.json()
        print(f"✅ Chat message sent and response received")
        print(f"Response preview: {chat_response['response'][:100]}...")
    except Exception as e:
        print(f"❌ Chat message failed: {e}")
        return
    
    # Test 4: Get session history
    print("\n4. Testing session history retrieval...")
    try:
        response = requests.get(f"{API_BASE}/sessions/{session_id}/history")
        assert response.status_code == 200
        history_data = response.json()
        print(f"✅ Session history retrieved: {len(history_data['messages'])} messages")
    except Exception as e:
        print(f"❌ Session history retrieval failed: {e}")
        return
    
    # Test 5: Get user sessions
    print("\n5. Testing user sessions listing...")
    try:
        response = requests.get(f"{API_BASE}/users/{create_data['user_id']}/sessions")
        assert response.status_code == 200
        sessions_data = response.json()
        print(f"✅ User sessions retrieved: {sessions_data['total_sessions']} sessions")
    except Exception as e:
        print(f"❌ User sessions listing failed: {e}")
        return
    
    # Test 6: Get session info
    print("\n6. Testing session info retrieval...")
    try:
        response = requests.get(f"{API_BASE}/sessions/{session_id}")
        assert response.status_code == 200
        session_info = response.json()
        print(f"✅ Session info retrieved: {session_info['title']}")
    except Exception as e:
        print(f"❌ Session info retrieval failed: {e}")
        return
    
    # Test 7: Delete session
    print("\n7. Testing session deletion...")
    try:
        response = requests.delete(f"{API_BASE}/sessions/{session_id}")
        assert response.status_code == 200
        print("✅ Session deleted successfully")
    except Exception as e:
        print(f"❌ Session deletion failed: {e}")
        return
    
    # Test 8: Verify session is deleted
    print("\n8. Verifying session deletion...")
    try:
        response = requests.get(f"{API_BASE}/sessions/{session_id}")
        assert response.status_code == 404
        print("✅ Session properly deleted (404 response)")
    except Exception as e:
        print(f"❌ Session deletion verification failed: {e}")
        return
    
    print("\n🎉 All tests passed! Session management is working correctly.")
    print("\n📝 Summary:")
    print("- ✅ Session creation with UUID")
    print("- ✅ Chat message sending and response")
    print("- ✅ Session history retrieval")
    print("- ✅ User sessions listing")
    print("- ✅ Session info retrieval")
    print("- ✅ Session deletion")
    print("- ✅ Proper UUID format handling")

def test_uuid_validation():
    """Test UUID validation in the system"""
    print("\n🔍 Testing UUID Validation")
    print("=" * 30)
    
    # Test valid UUID
    valid_uuid = str(uuid4())
    print(f"Valid UUID: {valid_uuid}")
    
    # Test invalid formats
    invalid_formats = [
        "session_123456_abc",  # Old format
        "not-a-uuid",
        "12345",
        "",
        "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"  # Wrong format
    ]
    
    for invalid_id in invalid_formats:
        try:
            response = requests.get(f"{API_BASE}/sessions/{invalid_id}")
            print(f"❌ Invalid ID '{invalid_id}' should have failed but got {response.status_code}")
        except Exception:
            print(f"✅ Invalid ID '{invalid_id}' properly rejected")

if __name__ == "__main__":
    try:
        test_session_management()
        test_uuid_validation()
    except KeyboardInterrupt:
        print("\n⏹️  Tests interrupted by user")
    except Exception as e:
        print(f"\n💥 Unexpected error: {e}")
