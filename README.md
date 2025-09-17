# <div align="center"><img src="docs/logo.png" alt="Legal Connect" height="40" style="vertical-align: middle;"/> Legal Connect</div>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.3-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5.13-4FC08D?style=flat&logo=vue.js&logoColor=white)](https://vuejs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.109.2-009688?style=flat&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.5-336791?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat&logo=redis&logoColor=white)](https://redis.io/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.1.1-005571?style=flat&logo=elasticsearch&logoColor=white)](https://www.elastic.co/)
[![Qdrant](https://img.shields.io/badge/Qdrant-Vector_DB-FF6B6B?style=flat&logo=qdrant&logoColor=white)](https://qdrant.tech/)
[![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=JSON%20web%20tokens&logoColor=white)](https://jwt.io/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=swagger&logoColor=black)](https://swagger.io/)

<div align="center">
  <h2>Connecting Clients and Lawyers with Advanced AI Features</h2>
  
  <p align="center">
    <b>Legal Connect</b> is a comprehensive legal platform that bridges the gap between clients and lawyers, featuring AI-powered document analysis, real-time communication, case management, and integrated payment processing.
    <br />
    <a href="#getting-started"><strong>Quick Start »</strong></a>
    <br />
    <br />
    <a href="#key-features">Features</a>
    ·
    <a href="#architecture">Architecture</a>
    ·
    <a href="#api-documentation">API Docs</a>
  </p>
  
  <h3>🔗 Quick Links</h3>
  <p align="center">
    <a href="https://app.legalconnect.live/"><strong>Full Application</strong></a>
    ·
    <a href="https://core.legalconnect.live/v1/swagger-ui/index.html"><strong>Backend API Documentation</strong></a>
    ·
    <a href="https://rag.legalconnect.live/docs"><strong>AI Backend Documentation</strong></a>
  </p>
</div>

<div align="center">
  <h3>SonarCloud Analysis</h3>
</div>

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=alert_status)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=security_rating)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=bugs)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=code_smells)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Duplicated Lines](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=duplicated_lines_density)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=java-jedis_legal-connect&metric=coverage)](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)

<details>
<summary>
  <h2 style="margin: 0; display: inline;">📑 Table of Contents</h2>
</summary>

<div style="margin-top: 1em;">

- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technical Stack](#technical-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Quick Setup](#quick-setup)
  - [Individual Service Setup](#individual-service-setup)
- [Database Design](#database-design)
- [API Documentation](#api-documentation)
- [Testing and Quality](#testing-and-quality)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [Authors](#authors)

</div>

</details>

## Key Features

### For Clients

- **Lawyer Discovery**: Advanced search and filtering system to find qualified lawyers
- **Case Management**: Create, track, and manage legal cases with document organization
- **AI Legal Assistant**: RAG-powered document analysis and legal question answering
- **Real-time Communication**: Instant messaging and video conferencing with lawyers
- **Secure Payments**: Integrated Stripe payment processing with escrow functionality
- **Document Management**: Upload, organize, and share legal documents securely

### For Lawyers

- **Professional Profiles**: Comprehensive profile management with verification system
- **Case Tracking**: Manage client cases with document and note organization
- **AI Document Analysis**: Leverage AI for document review and legal research
- **Client Communication**: Real-time chat and video consultations
- **Blog Publishing**: Share legal insights and build thought leadership
- **Availability Management**: Set consultation slots and manage appointments

### For Administrators

- **Lawyer Verification**: Review and approve lawyer registrations and credentials
- **User Management**: Monitor and manage user accounts and activities
- **System Analytics**: Track platform usage and performance metrics
- **Content Moderation**: Manage blogs and user-generated content

## Architecture

Legal Connect follows a microservices architecture with three main components:

![System Architecture](docs/architecture.png)

### Service Overview

1. **Backend (Spring Boot)**: Core business logic, user management, case management
2. **Backend AI (FastAPI)**: RAG system for document analysis and AI chat
3. **Frontend (Vue.js)**: User interface and client-side application

### Integration Points

- **Authentication**: JWT-based authentication shared across services
- **Real-time Communication**: WebSocket integration for chat and notifications
- **Document Processing**: AI backend processes documents uploaded through main backend
- **Video Conferencing**: Jitsi Meet integration for lawyer-client consultations

## Technical Stack

### Backend Services

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Main Backend** | Spring Boot | 3.5.3 | Core business logic, REST APIs |
| **AI Backend** | FastAPI | 0.109.2 | RAG system, document analysis |
| **Frontend** | Vue.js | 3.5.13 | User interface, SPA |

### Databases & Storage

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Primary DB** | PostgreSQL | 17.5 | User data, cases, documents |
| **Vector DB** | Qdrant | Latest | Document embeddings, similarity search |
| **Cache** | Redis | 7 | Session management, caching |
| **Search** | Elasticsearch | 9.1.1 | Full-text search for blogs |
| **File Storage** | AWS S3 + Cloudinary | - | Document and media storage |

### External Integrations

- **Payment Processing**: Stripe
- **Video Conferencing**: Jitsi Meet (JAAS)
- **Email Services**: SMTP (Zoho)
- **AI Services**: Google Gemini, Google Embeddings
- **Calendar Integration**: Google Calendar API

## Getting Started

### Prerequisites

- **Docker & Docker Compose** (Recommended)
- **Java 21+** (for local backend development)
- **Python 3.12+** (for local AI backend development)
- **Node.js 20+** (for local frontend development)
- **PostgreSQL 17+**, **Redis 7+**, **Elasticsearch 9.1+** (for local development)

### Quick Setup

The fastest way to get Legal Connect running is using Docker Compose:

```bash
# Clone the repository
git clone https://github.com/java-jedis/legal-connect.git
cd legal-connect

# Create environment configuration file
cp .env.example .env

# Start all services with Docker Compose
docker-compose up -d
```

**Important Configuration Steps:**

Before starting the services, you must edit the `.env` file with your actual configuration values. Update the following required variables:

- **POSTGRES_PASSWORD** - Database password
- **JWT_SECRET** - Secret key for JWT token generation
- **AWS_ACCESS_KEY, AWS_SECRET_KEY, AWS_S3_BUCKET** - AWS S3 storage credentials
- **GOOGLE_API_KEY** - Google AI API key (for AI features)
- **QDRANT_URL, QDRANT_API_KEY** - Qdrant vector database credentials
- **MAIL_USERNAME, MAIL_PASSWORD** - Email service credentials
- **STRIPE_SECRET_KEY** - Stripe payment processing key
- **JAAS_APP_ID, JAAS_API_KEY** - Jitsi Meet video conferencing credentials

**Setting up Jitsi JAAS Credentials:**

1. Go to [https://jaas.8x8.vc/#/](https://jaas.8x8.vc/#/) and create an account
2. Create a new application and get your App ID and API Key
3. Download the private key file and place it in the backend resources directory:

   ```bash
   cp /path/to/your/downloaded/key.pk backend/legalconnect/src/main/resources/jaas-key.pk
   ```

4. Update your `.env` file with the JAAS credentials:

   ```env
   JAAS_APP_ID=vpaas-magic-cookie-your-app-id-here
   JAAS_API_KEY=vpaas-magic-cookie-your-app-id-here/your-kid-here
   ```

**Access the applications:**

- Frontend: <http://localhost:5173>
- Backend API: <http://localhost:8080/v1>
- AI Backend API: <http://localhost:8000/api/v1>
- Backend Swagger: <http://localhost:8080/v1/swagger-ui/index.html>
- AI Backend Docs: <http://localhost:8000/docs>

### Individual Service Setup

For detailed setup instructions for each service, refer to their respective README files:

#### 📋 Service Documentation

| Service | Description | Setup Guide |
|---------|-------------|-------------|
| **[Backend](backend/README.md)** | Spring Boot API server with PostgreSQL, Redis, Elasticsearch | [Backend Setup Guide](backend/README.md) |
| **[Backend AI](backend-ai/README.md)** | FastAPI RAG system with Qdrant vector database | [AI Backend Setup Guide](backend-ai/README.md) |
| **[Frontend](frontend/README.md)** | Vue.js SPA with real-time features | [Frontend Setup Guide](frontend/README.md) |

#### 🔧 Configuration Files

Each service includes example configuration files:

- **Backend**: `backend/.env.example`, `backend/legalconnect/src/main/resources/application.example.yml`
- **Backend AI**: `backend-ai/env.example`
- **Frontend**: `frontend/.env.example`

## Database Design

The system uses multiple databases optimized for different use cases:

![Database Schema](docs/database_schema.png)

### Database Architecture

- **PostgreSQL**: Primary relational database for user data, cases, documents, and business logic
- **Qdrant**: Vector database for storing document embeddings and similarity search
- **Redis**: In-memory cache for sessions, temporary data, and performance optimization
- **Elasticsearch**: Search engine for full-text search capabilities in blogs and documents

### Key Entities

- **Users & Lawyers**: User management with role-based access control
- **Cases & Documents**: Case lifecycle management with document organization
- **Chat & Messages**: Real-time messaging system
- **Blogs & Reviews**: Content management and rating system
- **Payments & Meetings**: Financial transactions and appointment scheduling

## API Documentation

### Interactive Documentation

Each service provides comprehensive API documentation:

| Service | Documentation | Endpoint |
|---------|---------------|----------|
| **Backend** | Swagger UI | <http://localhost:8080/v1/swagger-ui/index.html> |
| **AI Backend** | FastAPI Docs | <http://localhost:8000/docs> |
| **Frontend** | Component Docs | See [Frontend README](frontend/README.md) |

### API Sections

#### Main Backend APIs

- **Authentication**: User registration, login, email verification
- **User Management**: Profile management, lawyer verification
- **Case Management**: Case lifecycle, document handling
- **Chat System**: Real-time messaging, conversation management
- **Blog System**: Content creation, subscription management
- **Payment Processing**: Stripe integration, transaction management

#### AI Backend APIs

- **Chat Endpoints**: AI-powered legal assistance
- **Document Processing**: Upload and analysis of legal documents
- **Search Endpoints**: Semantic search across legal documents
- **Session Management**: Chat history and context management

## Testing and Quality

### Code Quality Monitoring

Legal Connect uses SonarCloud for continuous code quality monitoring:

- **Quality Gate**: Automated quality checks on every commit
- **Code Coverage**: Comprehensive test coverage tracking
- **Security Analysis**: Vulnerability detection and security hotspots
- **Maintainability**: Code smells and technical debt monitoring

View detailed analysis: [SonarCloud Dashboard](https://sonarcloud.io/summary/overall?id=java-jedis_legal-connect&branch=main)

### Testing Strategy

- **Backend**: JUnit 5, Mockito, Integration tests
- **AI Backend**: pytest, FastAPI TestClient
- **Frontend**: Vue Test Utils, Jest (planned)
- **E2E Testing**: Cypress (planned)

## Project Structure

```
legal-connect/
├── backend/                    # Spring Boot main backend
│   ├── legalconnect/          # Spring Boot application
│   ├── docker-compose.yml    # Backend services (PostgreSQL, Redis, Elasticsearch)
│   ├── Dockerfile            # Backend container configuration
│   └── README.md             # Backend setup guide
├── backend-ai/               # FastAPI AI backend
│   ├── app/                  # FastAPI application
│   ├── docker-compose.yml   # AI services (PostgreSQL, Redis)
│   ├── Dockerfile           # AI backend container configuration
│   └── README.md            # AI backend setup guide
├── frontend/                # Vue.js frontend
│   ├── src/                 # Vue.js application source
│   ├── Dockerfile          # Frontend container configuration
│   └── README.md           # Frontend setup guide
├── docs/                   # Documentation and assets
│   ├── logo.png           # Project logo
│   ├── architecture.png   # System architecture diagram
│   └── database_schema.png # Database schema diagram
├── docker-compose.yml     # Full system orchestration
└── README.md             # This file
```

## Contributing

We welcome contributions to Legal Connect! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Development Guidelines

- Follow the coding standards for each service (Java, Python, JavaScript)
- Write comprehensive tests for new features
- Update documentation for API changes
- Ensure all quality gates pass in SonarCloud

## Authors

**Java Jedis Team**

**Majedul Islam** - Full Stack Developer | System Architect

- GitHub: [@mr-majed7](https://github.com/mr-majed7)
- LinkedIn: [Majedul Islam](https://www.linkedin.com/in/majedul-islam-041637220/)

**Shakil Ahmed** - Full Stack Developer | AI Integration Specialist

- GitHub: [@ahmedmshakil](https://github.com/ahmedmshakil)
- LinkedIn: [Shakil Ahmed](https://www.linkedin.com/in/ahmedmshakil/)

**Contact**

- GitHub: [java-jedis](https://github.com/java-jedis)
- Project Link: [Legal Connect](https://github.com/java-jedis/legal-connect)
