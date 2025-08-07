#!/usr/bin/env python3
"""
JWT Token Validation Test Script
Tests JWT token decoding between main backend and AI backend
"""

import os
import jwt
import json
from datetime import datetime, timedelta
from app.core.auth import decode_jwt_token, extract_user_id_from_token, AuthenticationError
from app.core.config import settings

def test_jwt_integration():
    """Test JWT token integration with main backend"""
    
    print("🔧 Testing JWT Integration between backends...")
    print(f"📊 Current JWT Secret: {settings.jwt_secret_key[:10]}...")
    print(f"🔑 JWT Algorithm: {settings.jwt_algorithm}")
    
    # Sample JWT payload matching main backend format
    test_user_id = "123e4567-e89b-12d3-a456-426614174000"  # UUID format
    test_payload = {
        "userId": test_user_id,
        "sub": "user@example.com",
        "role": "USER", 
        "firstName": "John",
        "lastName": "Doe",
        "emailVerified": True,
        "tokenType": "ACCESS",
        "iss": "LegalConnect",
        "aud": "LegalConnect-API",
        "exp": int((datetime.utcnow() + timedelta(hours=1)).timestamp()),
        "iat": int(datetime.utcnow().timestamp())
    }
    
    try:
        # Create test JWT token (simulating main backend)
        test_token = jwt.encode(
            test_payload, 
            settings.jwt_secret_key, 
            algorithm=settings.jwt_algorithm
        )
        
        print(f"✅ Generated test token: {test_token[:50]}...")
        
        # Test token decoding (AI backend)
        decoded_payload = decode_jwt_token(test_token)
        print(f"✅ Token decoded successfully")
        print(f"📋 Decoded payload keys: {list(decoded_payload.keys())}")
        
        # Test user ID extraction
        extracted_user_id = extract_user_id_from_token(decoded_payload)
        print(f"✅ User ID extracted: {extracted_user_id}")
        
        # Verify user ID matches
        if extracted_user_id == test_user_id:
            print("🎉 JWT Integration Test PASSED!")
            return True
        else:
            print(f"❌ User ID mismatch: expected {test_user_id}, got {extracted_user_id}")
            return False
            
    except AuthenticationError as e:
        print(f"❌ Authentication Error: {e}")
        return False
    except Exception as e:
        print(f"❌ Unexpected Error: {e}")
        return False

def test_invalid_token():
    """Test handling of invalid tokens"""
    print("\n🔧 Testing invalid token handling...")
    
    try:
        # Test with invalid token
        decode_jwt_token("invalid.token.here")
        print("❌ Should have failed with invalid token")
        return False
    except AuthenticationError as e:
        print(f"✅ Invalid token correctly rejected: {e}")
        return True
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False

def test_missing_user_id():
    """Test handling of tokens without userId"""
    print("\n🔧 Testing token without userId...")
    
    test_payload = {
        "sub": "user@example.com",
        "role": "USER",
        "exp": int((datetime.utcnow() + timedelta(hours=1)).timestamp()),
        "iat": int(datetime.utcnow().timestamp())
        # Missing userId field
    }
    
    try:
        test_token = jwt.encode(
            test_payload, 
            settings.jwt_secret_key, 
            algorithm=settings.jwt_algorithm
        )
        
        decoded_payload = decode_jwt_token(test_token)
        extract_user_id_from_token(decoded_payload)
        
        print("❌ Should have failed with missing userId")
        return False
        
    except AuthenticationError as e:
        print(f"✅ Missing userId correctly detected: {e}")
        return True
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False

def main():
    """Run all JWT tests"""
    print("🚀 Starting JWT Integration Tests...\n")
    
    # Check environment configuration
    if settings.jwt_secret_key == "your_secret_key_here":
        print("⚠️  WARNING: Using default JWT secret key!")
        print("   Please set JWT_SECRET_KEY environment variable to match your main backend")
    
    tests = [
        test_jwt_integration,
        test_invalid_token,
        test_missing_user_id
    ]
    
    passed = 0
    total = len(tests)
    
    for test in tests:
        if test():
            passed += 1
    
    print(f"\n📊 Test Results: {passed}/{total} tests passed")
    
    if passed == total:
        print("🎉 All JWT integration tests PASSED!")
        print("\n✅ Your backend-ai is ready to authenticate users from main backend!")
    else:
        print("❌ Some tests failed. Please check your JWT configuration.")
        print("\n🔧 Make sure to:")
        print("   1. Set JWT_SECRET_KEY to match your main backend")
        print("   2. Set JWT_ALGORITHM to match your main backend")
        print("   3. Ensure main backend includes 'userId' in JWT payload")

if __name__ == "__main__":
    main()
