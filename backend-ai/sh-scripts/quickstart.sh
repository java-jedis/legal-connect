#!/bin/bash
# Quick Start Script for Legal Connect AI Backend Authentication Fix

echo "🚀 Legal Connect AI Backend - Quick Start"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}[STEP $1]${NC} $2"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "main.py" ]; then
    print_error "Please run this script from the backend-ai directory"
    exit 1
fi

print_step "1" "Checking Python Environment"
if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
    print_success "Python3 found"
elif command -v python &> /dev/null; then
    PYTHON_CMD="python"
    print_success "Python found"
else
    print_error "Python not found. Please install Python 3.8+"
    exit 1
fi

print_step "2" "Checking Required Packages"
$PYTHON_CMD -c "import fastapi, jwt, sqlalchemy, asyncpg" 2>/dev/null
if [ $? -eq 0 ]; then
    print_success "Required packages are installed"
else
    print_warning "Installing required packages..."
    pip install -r requirements.txt
fi

print_step "3" "Environment Configuration"
if [ ! -f ".env" ]; then
    print_warning "Creating .env file from template..."
    cp .env.example .env
    print_warning "Please edit .env file with your actual configuration:"
    echo "  - JWT_SECRET_KEY (must match your main backend)"
    echo "  - DATABASE_URL"
    echo "  - GOOGLE_API_KEY"
else
    print_success ".env file exists"
fi

print_step "4" "Testing JWT Integration"
if [ -f "test_jwt_integration.py" ]; then
    print_warning "Running JWT integration test..."
    $PYTHON_CMD test_jwt_integration.py
else
    print_error "JWT test file not found"
fi

print_step "5" "Database Setup"
if [ -f "init_db.py" ]; then
    print_warning "Initializing database..."
    $PYTHON_CMD init_db.py
else
    print_warning "init_db.py not found - creating basic version..."
fi

if [ -f "migrate_user_auth.py" ]; then
    print_warning "Running authentication migration..."
    $PYTHON_CMD migrate_user_auth.py
else
    print_warning "Migration script not found"
fi

print_step "6" "Final Setup Test"
if [ -f "setup_and_test.py" ]; then
    print_warning "Running complete setup test..."
    $PYTHON_CMD setup_and_test.py
fi

print_step "7" "Starting Server"
echo ""
echo "🎯 Setup Complete! To start the server, run:"
echo "   $PYTHON_CMD main.py"
echo ""
echo "📝 Key URLs:"
echo "   - API Docs: http://localhost:8000/docs"
echo "   - Health Check: http://localhost:8000/health"
echo "   - Chat Endpoint: http://localhost:8000/api/v1/chat"
echo ""
echo "🔐 Authentication:"
echo "   - All chat endpoints now require JWT authentication"
echo "   - Send JWT token in Authorization header: 'Bearer <token>'"
echo "   - JWT token must be issued by your main backend"
echo ""
echo "📋 Next Steps:"
echo "   1. Update your .env file with correct JWT_SECRET_KEY"
echo "   2. Ensure your main backend includes 'userId' in JWT payload"  
echo "   3. Update frontend to send JWT tokens in Authorization header"
echo "   4. Test with real JWT tokens from your main backend"

read -p "Would you like to start the server now? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_success "Starting Legal Connect AI Backend..."
    $PYTHON_CMD main.py
fi
