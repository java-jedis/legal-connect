"""
Simple FastAPI test to verify the setup works
"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse
import os
from dotenv import load_dotenv
import google.generativeai as genai

# Load environment variables
load_dotenv()

app = FastAPI(title="Legal Connect RAG - Basic Test")

@app.get("/")
async def root():
    return {"message": "Legal Connect RAG System is running!", "status": "active"}

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    checks = {
        "fastapi": "✅ OK",
        "environment": "✅ OK",
        "google_api": "❌ Not tested"
    }
    
    # Test Google API connection
    try:
        api_key = os.getenv("GOOGLE_API_KEY")
        if api_key:
            genai.configure(api_key=api_key)
            # Simple test
            model = genai.GenerativeModel('gemini-2.0-flash-exp')
            response = model.generate_content("Hello", request_options={"timeout": 5})
            checks["google_api"] = "✅ OK"
        else:
            checks["google_api"] = "❌ No API key"
    except Exception as e:
        checks["google_api"] = f"❌ Error: {str(e)[:50]}"
    
    # Test database connection
    try:
        database_url = os.getenv("DATABASE_URL")
        if database_url:
            checks["database"] = "✅ Config OK"
        else:
            checks["database"] = "❌ No DATABASE_URL"
    except Exception as e:
        checks["database"] = f"❌ Error: {str(e)[:50]}"
    
    return {
        "status": "Legal Connect RAG System Health Check",
        "checks": checks,
        "environment": {
            "has_google_api_key": bool(os.getenv("GOOGLE_API_KEY")),
            "has_database_url": bool(os.getenv("DATABASE_URL")),
            "has_qdrant_url": bool(os.getenv("QDRANT_URL")),
        }
    }

@app.post("/test-gemini")
async def test_gemini(message: str = "Test message"):
    """Test Gemini AI API"""
    try:
        api_key = os.getenv("GOOGLE_API_KEY")
        if not api_key:
            return JSONResponse(
                status_code=500,
                content={"error": "GOOGLE_API_KEY not set"}
            )
        
        genai.configure(api_key=api_key)
        model = genai.GenerativeModel('gemini-2.0-flash-exp')
        response = model.generate_content(f"Legal AI Test: {message}")
        
        return {
            "success": True,
            "message": message,
            "response": response.text,
            "model": "gemini-2.0-flash-exp"
        }
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={
                "error": str(e),
                "message": message
            }
        )

@app.get("/config")
async def get_config():
    """Get current configuration (without sensitive data)"""
    return {
        "app_name": os.getenv("APP_NAME", "Legal Connect RAG"),
        "version": os.getenv("APP_VERSION", "1.0.0"),
        "environment": os.getenv("NODE_ENV", "development"),
        "debug": os.getenv("DEBUG", "False").lower() == "true",
        "embedding_model": os.getenv("EMBEDDING_MODEL", "models/embedding-001"),
        "gemini_model": os.getenv("GEMINI_MODEL", "gemini-2.0-flash-exp"),
        "chunk_size": int(os.getenv("CHUNK_SIZE", "1000")),
        "chunk_overlap": int(os.getenv("CHUNK_OVERLAP", "200")),
        "top_k_results": int(os.getenv("TOP_K_RESULTS", "5")),
        "similarity_threshold": float(os.getenv("SIMILARITY_THRESHOLD", "0.7")),
        "max_context_length": int(os.getenv("MAX_CONTEXT_LENGTH", "4000")),
        "has_qdrant_url": bool(os.getenv("QDRANT_URL")),
        "has_database_url": bool(os.getenv("DATABASE_URL")),
        "has_google_api_key": bool(os.getenv("GOOGLE_API_KEY")),
        "qdrant_collection": os.getenv("QDRANT_COLLECTION_NAME", "legal_documents"),
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main_simple:app", 
        host="0.0.0.0", 
        port=8000, 
        reload=True
    )
