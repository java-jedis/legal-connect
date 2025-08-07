"""
Direct table creation using existing models
"""

import sys
import os

# Add the app directory to Python path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

def create_tables():
    """Create tables using existing models and database config"""
    try:
        print("🏗️  Creating tables using existing models...")
        
        # Import your existing database components
        from app.db.database import engine, Base
        # Import models directly to ensure they're registered
        from app.db.models import Document, DocumentChunk, ChatSession, ChatMessage, ProcessingJob
        
        print("📋 Models imported:")
        print("   ✅ Document")
        print("   ✅ DocumentChunk") 
        print("   ✅ ChatSession")
        print("   ✅ ChatMessage")
        print("   ✅ ProcessingJob")
        
        # Debug: Check what tables SQLAlchemy knows about
        print(f"\n🔍 SQLAlchemy metadata tables: {list(Base.metadata.tables.keys())}")
        
        # Test database connection first
        print("🧪 Testing database connection...")
        from sqlalchemy import text
        with engine.connect() as conn:
            result = conn.execute(text("SELECT 1 as test"))
            test_value = result.fetchone()[0]
            if test_value == 1:
                print("✅ Database connection successful!")
            else:
                print("❌ Database connection test failed!")
                return False
        
        # Create all tables - this uses your models.py
        print("🏗️  Creating tables in database...")
        Base.metadata.create_all(bind=engine)
        
        print("✅ All tables created successfully!")
        return True
        
    except Exception as e:
        print(f"❌ Error creating tables: {e}")
        import traceback
        traceback.print_exc()
        return False

def verify_tables():
    """Verify tables were created"""
    try:
        from app.db.database import engine
        from sqlalchemy import inspect
        
        inspector = inspect(engine)
        tables = inspector.get_table_names()
        
        print(f"\n📋 Created {len(tables)} tables:")
        for table in sorted(tables):
            print(f"   ✅ {table}")
            
        # Check specific columns in chat_sessions
        columns = inspector.get_columns('chat_sessions')
        print(f"\n🏗️  chat_sessions columns:")
        for col in columns:
            nullable = "NULL" if col['nullable'] else "NOT NULL"
            print(f"   📌 {col['name']} ({col['type']}) {nullable}")
        
        return True
        
    except Exception as e:
        print(f"❌ Error verifying tables: {e}")
        return False

if __name__ == "__main__":
    print("🚀 Direct Table Creation")
    print("=" * 30)
    
    if create_tables():
        print("\n🔍 Verifying created tables...")
        if verify_tables():
            print("\n🎉 Database setup completed!")
            print("✅ All tables ready for FastAPI!")
        else:
            print("❌ Table verification failed!")
    else:
        print("❌ Table creation failed!")