"""
Data preprocessing and indexing script
This script processes all JSON files and creates embeddings for the vector database
"""

import asyncio
import os
import sys
import logging
from pathlib import Path

# Load environment variables first, before any other imports
from dotenv import load_dotenv
load_dotenv()

# Add the project root to Python path
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from app.services.embeddings import EmbeddingService
from app.services.vectordb import QdrantService
from app.services.document_processor import DocumentProcessor
from app.db.database import init_db

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('preprocessing.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

async def main():
    """Main preprocessing function"""
    try:
        logger.info("🚀 Starting Legal Connect RAG preprocessing...")
        
        # Initialize database
        logger.info("📊 Initializing database...")
        await init_db()
        
        # Initialize services
        logger.info("🔧 Initializing services...")
        embedding_service = EmbeddingService()
        vectordb_service = QdrantService()
        
        # Initialize vector database
        await vectordb_service.initialize()
        
        # Create document processor
        processor = DocumentProcessor(embedding_service, vectordb_service)
        
        # Process all documents
        logger.info("📚 Starting document processing...")
        results = await processor.process_all_documents()
        
        # Print results
        logger.info("✅ Preprocessing completed!")
        logger.info(f"📈 Results Summary:")
        logger.info(f"   - Total files: {results['total_files']}")
        logger.info(f"   - Processed files: {results['processed_files']}")
        logger.info(f"   - Failed files: {results['failed_files']}")
        logger.info(f"   - Total chunks: {results['total_chunks']}")
        logger.info(f"   - Total embeddings: {results['total_embeddings']}")
        
        if results['errors']:
            logger.warning(f"❌ {len(results['errors'])} files had errors:")
            for error in results['errors']:
                logger.warning(f"   - {error['file']}: {error['error']}")
        
        # Get final stats
        stats = await processor.get_processing_stats()
        logger.info(f"📊 Final Statistics:")
        logger.info(f"   - Documents in database: {stats.get('total_documents', 0)}")
        logger.info(f"   - Chunks in database: {stats.get('total_chunks', 0)}")
        logger.info(f"   - Vectors in Qdrant: {stats.get('vector_database', {}).get('points_count', 0)}")
        
        # Close connections
        await vectordb_service.close()
        
        logger.info("🎉 Preprocessing pipeline completed successfully!")
        
    except Exception as e:
        logger.error(f"💥 Error in preprocessing pipeline: {e}")
        raise e

if __name__ == "__main__":
    # Check if .env file exists
    env_file = Path(".env")
    if not env_file.exists():
        logger.error("❌ .env file not found. Please create one based on .env.example")
        sys.exit(1)
    
    # Check required environment variables
    required_vars = ["GOOGLE_API_KEY", "DATABASE_URL"]
    missing_vars = [var for var in required_vars if not os.getenv(var)]
    
    # Check for either QDRANT_URL or QDRANT_HOST
    if not os.getenv("QDRANT_URL") and not os.getenv("QDRANT_HOST"):
        missing_vars.append("QDRANT_URL or QDRANT_HOST")
    
    if missing_vars:
        logger.error(f"❌ Missing required environment variables: {', '.join(missing_vars)}")
        sys.exit(1)
    
    # Run the preprocessing
    asyncio.run(main())
