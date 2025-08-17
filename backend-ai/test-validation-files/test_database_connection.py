"""
Database Connection Test Script
"""
import sys
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

def test_database_connection():
    """Test database connection and configuration"""
    print("🧪 Database Connection Test")
    print("=" * 30)
    
    try:
        # Test environment variable loading
        database_url = os.getenv("DATABASE_URL")
        if not database_url:
            print("❌ DATABASE_URL not found in environment!")
            print("💡 Check your .env file")
            return False
        
        # Hide password in display
        display_url = database_url
        if ':' in database_url and '@' in database_url:
            parts = database_url.split(':')
            if len(parts) >= 3:
                password_part = parts[2].split('@')[0]
                display_url = database_url.replace(password_part, '****')
        
        print(f"✅ DATABASE_URL loaded: {display_url}")
        
        # Import and test database module
        print("\n🔗 Testing database module import...")
        from app.db.database import engine, DATABASE_URL, check_db_health
        print("✅ Database module imported successfully!")
        
        # Test database connection
        print("\n🔌 Testing database connection...")
        import asyncio
        
        async def test_connection():
            is_healthy = await check_db_health()
            return is_healthy
        
        is_connected = asyncio.run(test_connection())
        
        if is_connected:
            print("✅ Database connection successful!")
            
            # Test table creation
            print("\n📊 Testing table creation...")
            from app.db.models import Base
            Base.metadata.create_all(bind=engine)
            print("✅ Database tables verified/created!")
            
            # Show tables
            from sqlalchemy import text
            with engine.connect() as conn:
                if DATABASE_URL.startswith("postgresql"):
                    result = conn.execute(text("""
                        SELECT table_name 
                        FROM information_schema.tables 
                        WHERE table_schema = 'public'
                    """))
                    tables = [row[0] for row in result]
                    print(f"📋 Tables found: {', '.join(tables)}")
                else:
                    print("📋 SQLite database tables created")
            
            print("\n🎉 All tests passed!")
            return True
        else:
            print("❌ Database connection failed!")
            return False
            
    except ImportError as e:
        print(f"❌ Import error: {e}")
        print("💡 Check if all dependencies are installed")
        return False
    except Exception as e:
        print(f"❌ Database test failed: {e}")
        print("\n🔧 Common issues:")
        print("- PostgreSQL not running: sudo systemctl start postgresql")
        print("- Wrong credentials in .env file")
        print("- Database doesn't exist: createdb legal_connect_db")
        return False

if __name__ == "__main__":
    success = test_database_connection()
    if not success:
        sys.exit(1)
    print("\n✅ Database is ready for use!")
