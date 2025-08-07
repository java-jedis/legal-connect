"""
Database initialization script
Run this script to create database tables and initial setup
"""

import asyncio
import sys
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Add the project root to Python path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def check_database_config():
    """Check if DATABASE_URL is properly configured"""
    database_url = os.getenv("DATABASE_URL")
    
    if not database_url:
        print("❌ DATABASE_URL environment variable is not set!")
        print("\n📝 Please set your database connection string:")
        print("For PostgreSQL:")
        print("  export DATABASE_URL='postgresql://your_username:your_password@localhost:5432/your_database'")
        print("\nFor SQLite (development only):")
        print("  export DATABASE_URL='sqlite:///./legal_connect.db'")
        return False
    
    if database_url == "postgresql://username:password@localhost:5432/legal_connect_db":
        print("❌ DATABASE_URL is using placeholder credentials!")
        print("\n📝 Please update DATABASE_URL with real credentials:")
        print("  export DATABASE_URL='postgresql://your_real_username:your_real_password@localhost:5432/your_database'")
        return False
    
    print(f"✅ DATABASE_URL configured: {database_url.replace(':' + database_url.split(':')[2].split('@')[0], ':****')}")
    return True

async def main():
    """Initialize the database"""
    print("🚀 Starting database initialization...")
    
    # Check database configuration first
    if not check_database_config():
        print("\n� Quick setup options:")
        print("1. Use SQLite for development (no PostgreSQL needed):")
        print("   export DATABASE_URL='sqlite:///./legal_connect.db'")
        print("   python init_db.py")
        print("\n2. Set up PostgreSQL:")
        print("   - Install PostgreSQL")
        print("   - Create database: createdb legal_connect_db")
        print("   - Set DATABASE_URL with your credentials")
        sys.exit(1)
    
    try:
        from app.db.database import init_db
        from app.db.models import Base, ChatSession, ChatMessage, Document, DocumentChunk
        
        # Initialize database tables
        await init_db()
        
        print("✅ Database initialization completed successfully!")
        print("\nCreated tables:")
        print("- chat_sessions") 
        print("- chat_messages")
        print("- documents")
        print("- document_chunks")
        print("- processing_jobs")
        print("\n📝 Note: User data comes from main Spring Boot backend via JWT tokens")
        
        print("\n🎉 Your database is ready!")
        print("You can now run: python test_chat_history.py")
        
    except Exception as e:
        print(f"❌ Database initialization failed: {e}")
        print("\n🔧 Troubleshooting:")
        
        if "password authentication failed" in str(e):
            print("- Check your database username and password")
            print("- Verify the user has permissions to create tables")
            
        elif "database" in str(e) and "does not exist" in str(e):
            print("- Create the database first:")
            print("  createdb legal_connect_db")
            
        elif "connection" in str(e) and "refused" in str(e):
            print("- Check if PostgreSQL is running:")
            print("  sudo systemctl status postgresql")
            print("  sudo systemctl start postgresql")
            
        elif "could not translate host name" in str(e):
            print("- Check your database host/port settings")
            
        print(f"\n💡 For SQLite (easier setup): export DATABASE_URL='sqlite:///./legal_connect.db'")
        sys.exit(1)

if __name__ == "__main__":
    asyncio.run(main())
