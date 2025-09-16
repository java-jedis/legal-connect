# Legal Connect RAG Backend AI

A sophisticated Retrieval-Augmented Generation (RAG) system built with FastAPI for legal document search and analysis. This backend provides AI-powered chat capabilities, document processing, and semantic search functionality for Bangladesh legal documents.

## 🏗️ Architecture

The system implements an agentic RAG architecture with the following components:

- **FastAPI Backend**: High-performance async API server
- **Vector Database**: Qdrant for semantic search and embeddings storage
- **LLM Integration**: Google Gemini for response generation
- **Document Processing**: PDF, DOCX, and JSON document ingestion
- **Chat Management**: Session-based conversation handling
- **Authentication**: JWT-based user authentication (integrates with main Spring Boot backend)

## 🛠️ Tech Stack

- **Framework**: FastAPI 0.109.2
- **Database**: PostgreSQL with SQLAlchemy ORM
- **Vector DB**: Qdrant Cloud
- **LLM**: Google Gemini 2.5 Pro
- **Embeddings**: Google Embedding Model (embedding-001)
- **Caching**: Redis
- **Authentication**: JWT with PyJWT
- **Document Processing**: PyPDF2, python-docx
- **Deployment**: Docker, Google App Engine

## 📋 Prerequisites

- Python 3.12+
- PostgreSQL 17+ (or SQLite for development)
- Redis 7+
- Google AI API Key
- Qdrant Cloud instance (or local Qdrant)

## 🚀 Quick Start

### Option 1: Docker Setup (Recommended)

1. **Clone and navigate to the project**:
   ```bash
   cd backend-ai
   ```

2. **Configure environment**:
   ```bash
   cp env.example .env
   # Edit .env with your actual credentials
   ```

3. **Start services with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

4. **Initialize database**:
   ```bash
   docker exec -it lc_fastapi python init_db.py
   ```

5. **Access the API**:
   - API: http://localhost:8000
   - Documentation: http://localhost:8000/docs
   - Health Check: http://localhost:8000/health

### Option 2: Local Development Setup

1. **Create virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

2. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

3. **Configure environment**:
   ```bash
   cp env.example .env
   # Edit .env with your configuration (see Configuration section)
   ```

4. **Set up PostgreSQL** (or use SQLite for development):
   ```bash
   # PostgreSQL
   createdb legal_connect_db
   
   # Or SQLite (development only)
   export DATABASE_URL='sqlite:///./legal_connect.db'
   ```

5. **Initialize database**:
   ```bash
   python init_db.py
   ```

6. **Start Redis** (if not using Docker):
   ```bash
   redis-server
   ```

7. **Run the application**:
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```

## ⚙️ Configuration

### Environment Variables

Copy `env.example` to `.env` and configure the following:

#### Database Configuration
```env
DATABASE_URL=postgresql://username:password@localhost:5432/legal_connect_db
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
POSTGRES_DB=legal_connect_db
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
```

#### Vector Database (Qdrant)
```env
QDRANT_URL=https://your-qdrant-url.com
QDRANT_API_KEY=your_qdrant_api_key
QDRANT_COLLECTION_NAME=legal_documents
```

#### Google AI Configuration
```env
GOOGLE_API_KEY=your_google_api_key_here
GEMINI_MODEL=gemini-2.5-pro
EMBEDDING_MODEL=models/embedding-001
```

#### Redis Configuration
```env
REDIS_URL=redis://localhost:6379/0
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
```

#### JWT Configuration
```env
JWT_SECRET_KEY=your_super_secret_jwt_key_here
JWT_ALGORITHM=HS256
JWT_ACCESS_TOKEN_EXPIRE_MINUTES=60
```

#### RAG Configuration
```env
CHUNK_SIZE=1000
CHUNK_OVERLAP=200
TOP_K_RESULTS=5
SIMILARITY_THRESHOLD=0.7
MAX_CONTEXT_LENGTH=4000
```

## 📚 API Endpoints

### Chat Endpoints (`/api/v1/chat`)
- `POST /chat` - Send chat message and get AI response
- `POST /sessions` - Create new chat session
- `GET /sessions/{session_id}` - Get session details
- `DELETE /sessions/{session_id}` - Delete chat session
- `GET /sessions/{session_id}/history` - Get chat history
- `POST /sessions/{session_id}/documents` - Upload document to session
- `GET /sessions/{session_id}/documents` - List session documents
- `DELETE /sessions/{session_id}/documents/{document_id}` - Remove document
- `POST /sessions/{session_id}/search` - Search within session documents

### Document Endpoints (`/api/v1/documents`)
- `GET /documents` - List all documents
- `GET /documents/{document_id}` - Get document details
- `POST /documents/process` - Process documents from JSON files
- `POST /documents/{document_id}/reindex` - Reindex specific document
- `DELETE /documents/{document_id}` - Delete document
- `GET /documents/stats` - Get document statistics

### History Endpoints (`/api/v1/history`)
- `GET /sessions` - List user's chat sessions
- `POST /sessions` - Create new session
- `GET /sessions/{session_id}` - Get session details
- `PUT /sessions/{session_id}` - Update session
- `DELETE /sessions/{session_id}` - Delete session
- `GET /sessions/{session_id}/messages` - Get session messages
- `POST /sessions/{session_id}/messages` - Add message to session

## 🔧 Development

### Running Tests
```bash
# Run validation tests
python test-validation-files/test_setup.py

# Test specific components
python test-validation-files/test_database_connection.py
python test-validation-files/test_qdrant_check.py
python test-validation-files/test_gemini.py
```

### Code Quality
```bash
# Format code
black .

# Lint code
flake8 .
```

### Database Management
```bash
# Initialize/reset database
python init_db.py

# Sync Qdrant to PostgreSQL
python sync_qdrant_to_postgres.py
```

### Document Processing
```bash
# Process documents from bdcode_json folder
python preprocess_documents.py
```

## 🐳 Docker Configuration

### Dockerfile
The application uses a multi-stage Docker build:
- Base: Python 3.12 slim
- Dependencies cached for faster rebuilds
- Application code copied last
- Exposes port 8000

### Docker Compose Services
- **fastapi-app**: Main application container
- **db**: PostgreSQL 17.5 database
- **redis**: Redis 7 for caching

### Health Checks
All services include health checks:
- PostgreSQL: `pg_isready`
- Redis: `redis-cli ping`
- FastAPI: Built-in health endpoint

## 📁 Project Structure

```
backend-ai/
├── app/
│   ├── api/
│   │   └── endpoints/          # API route handlers
│   │       ├── chat.py         # Chat and RAG endpoints
│   │       ├── documents.py    # Document management
│   │       └── history.py      # Chat history management
│   ├── core/
│   │   ├── auth.py            # JWT authentication
│   │   ├── config.py          # Application settings
│   │   └── rag_config.py      # RAG-specific configuration
│   ├── db/
│   │   ├── database.py        # Database connection and setup
│   │   └── models.py          # SQLAlchemy models
│   ├── services/
│   │   ├── chat_document_processor.py  # Session document handling
│   │   ├── chat_service.py             # Chat session management
│   │   ├── document_processor.py       # Document ingestion
│   │   ├── embeddings.py              # Google embeddings service
│   │   ├── llm.py                     # Gemini LLM service
│   │   ├── rag.py                     # RAG orchestration
│   │   └── vectordb.py                # Qdrant vector database
│   └── utils/
│       └── text_processing.py         # Text chunking and processing
├── bdcode_json/               # Document storage directory
├── test-validation-files/     # Test and validation scripts
├── sh-scripts/               # Shell scripts for setup
├── docker-compose.yml        # Docker services configuration
├── Dockerfile               # Container build instructions
├── requirements.txt         # Python dependencies
├── env.example             # Environment template
├── main.py                 # FastAPI application entry point
└── init_db.py             # Database initialization script
```

## 🔐 Security

- JWT-based authentication with configurable expiration
- CORS middleware with configurable origins
- Environment-based configuration (no hardcoded secrets)
- Input validation with Pydantic models
- SQL injection protection via SQLAlchemy ORM

## 📊 Monitoring

### Health Checks
- `/health` - Service health status
- `/` - Basic API information
- Component-specific health in service responses

### Logging
Configured logging levels and formats:
- Console and file logging
- Structured log format
- Configurable log levels per component

## 🤝 Integration

This backend integrates with:
- **Main Spring Boot Backend**: User authentication via JWT
- **Frontend Application**: RESTful API consumption
- **Qdrant Cloud**: Vector storage and similarity search
- **Google AI**: Embeddings and text generation
- **PostgreSQL**: Relational data storage
- **Redis**: Caching and session storage

## 📝 License

This project is part of the Legal Connect system. See the main project LICENSE file for details.

## 🆘 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check PostgreSQL status
   sudo systemctl status postgresql
   
   # Verify database exists
   psql -l | grep legal_connect
   ```

2. **Qdrant Connection Issues**
   - Verify QDRANT_URL and QDRANT_API_KEY
   - Check network connectivity to Qdrant Cloud

3. **Google AI API Errors**
   - Verify GOOGLE_API_KEY is valid
   - Check API quotas and billing

4. **Redis Connection Failed**
   ```bash
   # Start Redis
   redis-server
   
   # Test connection
   redis-cli ping
   ```

5. **Docker Issues**
   ```bash
   # Check container logs
   docker-compose logs fastapi-app
   
   # Restart services
   docker-compose restart
   ```

### Getting Help

1. Check the `/health` endpoint for service status
2. Review application logs for detailed error messages
3. Run validation tests in `test-validation-files/`
4. Verify environment configuration against `env.example`

For additional support, refer to the main Legal Connect project documentation.