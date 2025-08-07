#!/bin/bash
set -e

echo "🚀 Starting Legal Connect RAG Backend..."

# Function to kill all background processes on exit
cleanup() {
    echo "🛑 Shutting down backend services..."
    pkill -f "python.*main.py" || true
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Check if we're in the right directory
if [[ ! -f "main.py" ]]; then
    echo "❌ Error: Please run this script from the backend-ai directory"
    exit 1
fi

# Check for .env file
if [[ ! -f ".env" ]]; then
    echo "⚠️  Warning: .env file not found. Please copy .env.example to .env and configure it."
    echo "   cp .env.example .env"
    echo ""
fi

# Check if virtual environment exists
if [[ -d "venv" ]]; then
    echo "🐍 Activating virtual environment..."
    source venv/bin/activate
elif [[ -d ".venv" ]]; then
    echo "🐍 Activating virtual environment..."
    source .venv/bin/activate
else
    echo "⚠️  Warning: No virtual environment found. Make sure Python dependencies are installed."
fi

# Check if requirements are installed
echo "📦 Checking dependencies..."
if ! python -c "import fastapi, uvicorn, google.generativeai, qdrant_client" 2>/dev/null; then
    echo "❌ Missing dependencies. Installing..."
    pip install -r requirements.txt
fi

# Validate environment
echo "� Validating environment configuration..."
if [[ -f "validate_env.py" ]]; then
    python validate_env.py
    if [[ $? -ne 0 ]]; then
        echo "❌ Environment validation failed. Please check your .env configuration."
        exit 1
    fi
fi

echo ""
echo "🐍 Starting Legal Connect RAG Backend..."
echo ""

# Start the main application
python main.py

echo ""
echo "📋 Backend URLs:"
echo "   API: http://localhost:8000"
echo "   Swagger UI: http://localhost:8000/docs"
echo "   ReDoc: http://localhost:8000/redoc"
echo "   Health Check: http://localhost:8000/health"
echo ""
echo "💡 Press Ctrl+C to stop the server"
echo ""
