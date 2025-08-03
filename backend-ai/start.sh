#!/bin/bash
set -e

echo "🚀 Starting Legal Connect Application..."

# Function to kill all background processes on exit
cleanup() {
    echo "🛑 Shutting down services..."
    pkill -f "python.*main.py" || true
    pkill -f "npm.*run.*dev" || true
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Check if we're in the right directory
if [[ ! -f "main.py" ]]; then
    echo "❌ Error: Please run this script from the legal-connect root directory"
    exit 1
fi

# Check if backend dependencies are installed
if [[ ! -d "venv" && ! -d ".venv" ]]; then
    echo "⚠️  Warning: No virtual environment found. Make sure Python dependencies are installed."
fi

# Check if frontend dependencies are installed
if [[ ! -d "frontend/node_modules" ]]; then
    echo "📦 Installing frontend dependencies..."
    cd frontend
    npm install
    cd ..
fi

echo "🐍 Starting backend server..."
# Start backend in background
python main.py &
BACKEND_PID=$!

# Wait a moment for backend to start
sleep 3

echo "⚛️  Starting frontend development server..."
# Start frontend in background
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "🎉 Application is starting up!"
echo ""
echo "📋 Service URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend API: http://localhost:8000"
echo "   API Docs: http://localhost:8000/docs"
echo ""
echo "💡 Press Ctrl+C to stop all services"
echo ""

# Wait for any process to exit
wait $BACKEND_PID $FRONTEND_PID
