"""
Config.py Test Script - Environment Variables Loading Test
"""
import sys
import os
from dotenv import load_dotenv

# Load environment variables first
load_dotenv()

def test_config_loading():
    """Test if config.py properly loads environment variables"""
    print("🧪 Config.py Test")
    print("=" * 40)
    
    try:
        # Import config
        print("📂 Importing config module...")
        from app.core.config import settings
        print("✅ Config module imported successfully!")
        
        # Test critical settings
        print("\n🔍 Testing critical settings:")
        
        # Database URL
        if settings.database_url:
            display_db = settings.database_url
            if ':' in settings.database_url and '@' in settings.database_url:
                parts = settings.database_url.split(':')
                if len(parts) >= 3:
                    password_part = parts[2].split('@')[0]
                    display_db = settings.database_url.replace(password_part, '****')
            print(f"✅ DATABASE_URL: {display_db}")
        else:
            print("❌ DATABASE_URL: NOT SET!")
            
        # Google API Key
        if settings.google_api_key:
            print(f"✅ GOOGLE_API_KEY: {settings.google_api_key[:20]}...")
        else:
            print("❌ GOOGLE_API_KEY: NOT SET!")
            
        # Qdrant settings
        if settings.qdrant_url:
            print(f"✅ QDRANT_URL: {settings.qdrant_url}")
        else:
            print("❌ QDRANT_URL: NOT SET!")
            
        if settings.qdrant_api_key:
            print(f"✅ QDRANT_API_KEY: {settings.qdrant_api_key[:20]}...")
        else:
            print("❌ QDRANT_API_KEY: NOT SET!")
            
        # JWT Secret
        if settings.jwt_secret_key:
            print(f"✅ JWT_SECRET_KEY: {settings.jwt_secret_key[:10]}...")
        else:
            print("❌ JWT_SECRET_KEY: NOT SET!")
        
        # Test other settings
        print(f"\n📊 Other settings:")
        print(f"   App Name: {settings.app_name}")
        print(f"   App Version: {settings.app_version}")
        print(f"   Debug: {settings.debug}")
        print(f"   Gemini Model: {settings.gemini_model}")
        print(f"   Embedding Model: {settings.embedding_model}")
        print(f"   Collection Name: {settings.qdrant_collection_name}")
        print(f"   Top K Results: {settings.top_k_results}")
        print(f"   Chunk Size: {settings.chunk_size}")
        print(f"   CORS Origins: {settings.cors_origins}")
        
        # Test environment variables directly
        print(f"\n🔄 Direct environment variable check:")
        direct_db_url = os.getenv("DATABASE_URL")
        direct_google_key = os.getenv("GOOGLE_API_KEY")
        direct_qdrant_url = os.getenv("QDRANT_URL")
        
        print(f"   Direct DATABASE_URL: {'SET' if direct_db_url else 'NOT SET'}")
        print(f"   Direct GOOGLE_API_KEY: {'SET' if direct_google_key else 'NOT SET'}")
        print(f"   Direct QDRANT_URL: {'SET' if direct_qdrant_url else 'NOT SET'}")
        
        # Validation
        missing_critical = []
        if not settings.database_url:
            missing_critical.append("DATABASE_URL")
        if not settings.google_api_key:
            missing_critical.append("GOOGLE_API_KEY")
        if not settings.qdrant_url:
            missing_critical.append("QDRANT_URL")
        if not settings.qdrant_api_key:
            missing_critical.append("QDRANT_API_KEY")
        if not settings.jwt_secret_key:
            missing_critical.append("JWT_SECRET_KEY")
            
        if missing_critical:
            print(f"\n❌ Missing critical environment variables:")
            for var in missing_critical:
                print(f"   - {var}")
            print("\n💡 Please check your .env file!")
            return False
        else:
            print(f"\n🎉 All critical environment variables are set!")
            return True
            
    except ImportError as e:
        print(f"❌ Import error: {e}")
        return False
    except Exception as e:
        print(f"❌ Config test failed: {e}")
        return False

def test_config_vs_direct():
    """Compare config.py values with direct os.getenv()"""
    print(f"\n🔀 Comparing config.py vs direct environment access:")
    print("-" * 50)
    
    try:
        from app.core.config import settings
        
        comparisons = [
            ("DATABASE_URL", settings.database_url, os.getenv("DATABASE_URL")),
            ("GOOGLE_API_KEY", settings.google_api_key, os.getenv("GOOGLE_API_KEY")),
            ("QDRANT_URL", settings.qdrant_url, os.getenv("QDRANT_URL")),
            ("JWT_SECRET_KEY", settings.jwt_secret_key, os.getenv("JWT_SECRET_KEY")),
            ("GEMINI_MODEL", settings.gemini_model, os.getenv("GEMINI_MODEL"))
        ]
        
        for var_name, config_val, direct_val in comparisons:
            config_status = "SET" if config_val else "EMPTY"
            direct_status = "SET" if direct_val else "EMPTY"
            match_status = "✅ MATCH" if bool(config_val) == bool(direct_val) else "❌ MISMATCH"
            
            print(f"   {var_name}:")
            print(f"     Config: {config_status}")
            print(f"     Direct: {direct_status}")
            print(f"     Status: {match_status}")
            print()
            
    except Exception as e:
        print(f"❌ Comparison failed: {e}")

if __name__ == "__main__":
    print("🧪 Testing Config.py Environment Variable Loading")
    print("=" * 60)
    
    success = test_config_loading()
    test_config_vs_direct()
    
    print("\n" + "=" * 60)
    if success:
        print("🎉 Config.py is working properly!")
        print("✅ All environment variables loaded correctly")
        print("🚀 Ready to start FastAPI server!")
    else:
        print("❌ Config.py has issues!")
        print("🔧 Please fix the missing environment variables")
        sys.exit(1)
