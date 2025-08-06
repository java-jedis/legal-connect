#!/bin/bash

# Database Setup Script for Legal Connect
echo "🏗️  Legal Connect Database Setup"
echo "=================================="

# Check if DATABASE_URL is already set
if [ ! -z "$DATABASE_URL" ]; then
    echo "✅ DATABASE_URL is already set: $DATABASE_URL"
    echo "Proceeding with initialization..."
    python init_db.py
    exit 0
fi

echo "Choose your database setup:"
echo "1. SQLite (Recommended for development - no setup required)"
echo "2. PostgreSQL (Production-ready)"
echo "3. I'll set DATABASE_URL manually"

read -p "Enter your choice (1-3): " choice

case $choice in
    1)
        echo "🔧 Setting up SQLite database..."
        export DATABASE_URL='sqlite:///./legal_connect.db'
        echo "DATABASE_URL='sqlite:///./legal_connect.db'" >> .env
        echo "✅ SQLite configured!"
        ;;
    2)
        echo "🔧 Setting up PostgreSQL..."
        echo "Please provide your PostgreSQL details:"
        read -p "Database host (default: localhost): " db_host
        db_host=${db_host:-localhost}
        
        read -p "Database port (default: 5432): " db_port
        db_port=${db_port:-5432}
        
        read -p "Database name (default: legal_connect_db): " db_name
        db_name=${db_name:-legal_connect_db}
        
        read -p "Username: " db_user
        read -s -p "Password: " db_password
        echo
        
        DATABASE_URL="postgresql://$db_user:$db_password@$db_host:$db_port/$db_name"
        export DATABASE_URL
        echo "DATABASE_URL='$DATABASE_URL'" >> .env
        echo "✅ PostgreSQL configured!"
        
        # Test if database exists
        echo "🔍 Testing database connection..."
        python -c "
import os
os.environ['DATABASE_URL'] = '$DATABASE_URL'
try:
    import psycopg2
    conn = psycopg2.connect('$DATABASE_URL')
    conn.close()
    print('✅ Database connection successful!')
except Exception as e:
    print(f'❌ Connection failed: {e}')
    print('💡 You may need to create the database first:')
    print(f'   createdb $db_name')
"
        ;;
    3)
        echo "👍 Please set DATABASE_URL manually and run:"
        echo "   python init_db.py"
        exit 0
        ;;
    *)
        echo "❌ Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
echo "🚀 Initializing database..."
python init_db.py

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Setup complete!"
    echo "To use this database configuration in the future, run:"
    echo "   source .env  # (if using .env file)"
    echo "   export DATABASE_URL='$DATABASE_URL'"
else
    echo "❌ Database initialization failed. Check the error messages above."
fi
