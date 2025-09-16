# Legal Connect Backend

A comprehensive Spring Boot application providing the core backend services for the Legal Connect platform. This system connects clients with lawyers and includes advanced features like AI-powered document analysis, real-time chat, video conferencing, payment processing, and case management.

## 🏗️ Architecture

The backend implements a microservices-oriented architecture with the following components:

- **Spring Boot Framework**: Core application framework with Spring Security
- **PostgreSQL Database**: Primary data storage with JPA/Hibernate ORM
- **Redis Cache**: Session management and caching layer
- **Elasticsearch**: Full-text search for blogs and legal documents
- **WebSocket Support**: Real-time chat and notifications
- **Job Scheduling**: Quartz-based background job processing
- **File Storage**: AWS S3 integration with Cloudinary for media
- **Payment Processing**: Stripe integration for secure transactions
- **Video Conferencing**: Jitsi Meet integration
- **Email Services**: SMTP-based email notifications

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.3
- **Java Version**: Java 21 (Eclipse Temurin)
- **Database**: PostgreSQL 17.5
- **Cache**: Redis 7
- **Search Engine**: Elasticsearch 9.1.1
- **Build Tool**: Maven 3.9
- **Security**: Spring Security with JWT authentication
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito
- **Code Quality**: SonarCloud integration

## 📋 Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 17+
- Redis 7+
- Elasticsearch 9.1+
- Docker & Docker Compose (for containerized setup)

## 🚀 Quick Start

### Option 1: Docker Setup (Recommended)

1. **Clone and navigate to the backend**:
   ```bash
   cd backend
   ```

2. **Create and configure environment file**:
   ```bash
   # Copy the example environment file
   cp .env.example .env
   
   # Edit .env with your actual credentials
   nano .env  # or use your preferred editor
   ```

3. **Set up Jitsi JAAS credentials**:
   ```bash
   # 1. Go to https://jaas.8x8.vc/#/ and create an account
   # 2. Create a new application and get your App ID and API Key
   # 3. Download the private key file
   # 4. Place the private key file in the resources directory
   cp /path/to/your/downloaded/key.pk legalconnect/src/main/resources/jaas-key.pk
   
   # 5. Update your .env file with the JAAS credentials:
   # JAAS_APP_ID=vpaas-magic-cookie-your-app-id-here
   # JAAS_API_KEY=vpaas-magic-cookie-your-app-id-here/your-kid-here
   ```

4. **Start all services**:
   ```bash
   docker-compose up -d
   ```

4. **Check service health**:
   ```bash
   # Check all services are running
   docker-compose ps
   
   # View backend logs
   docker-compose logs -f backend
   ```

5. **Access the application**:
   - API: http://localhost:8080/v1
   - Swagger UI: http://localhost:8080/
   - Health Check: http://localhost:8080/v1/actuator/health

### Option 2: Local Development Setup

1. **Install dependencies**:
   ```bash
   # Install Java 21, Maven, PostgreSQL, Redis, Elasticsearch
   ```

2. **Set up PostgreSQL**:
   ```bash
   createdb legalconnect
   # Import Quartz tables
   psql -d legalconnect -f legalconnect/src/main/resources/quartz_tables.sql
   ```

3. **Start Redis**:
   ```bash
   redis-server
   ```

4. **Start Elasticsearch**:
   ```bash
   # Follow Elasticsearch installation guide for your OS
   elasticsearch
   ```

5. **Configure application**:
   ```bash
   cd legalconnect
   
   # Copy the example configuration file
   cp src/main/resources/application.example.yml src/main/resources/application.yml
   
   # Edit application.yml with your actual credentials
   nano src/main/resources/application.yml
   ```

6. **Set up Jitsi JAAS credentials**:
   ```bash
   # 1. Go to https://jaas.8x8.vc/#/ and create an account
   # 2. Create a new application and get your App ID and API Key
   # 3. Download the private key file
   # 4. Place the private key file in the resources directory
   cp /path/to/your/downloaded/key.pk src/main/resources/jaas-key.pk
   
   # 5. Update your application.yml file with the JAAS credentials
   ```

7. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

## ⚙️ Configuration

### Environment Variables

The application uses the following key environment variables (see `.env` file):

#### Database Configuration
```env
POSTGRES_DB=legalconnect
POSTGRES_USER=root
POSTGRES_PASSWORD=your_password
```

#### Security Configuration
```env
JWT_SECRET=your_jwt_secret_key
SONAR_TOKEN=your_sonar_token
```

#### External Services
```env
# AWS S3
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_S3_BUCKET=your_s3_bucket

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Stripe
STRIPE_SECRET_KEY=your_stripe_secret_key

# Google OAuth
GOOGLE_OAUTH_CLIENT_ID=your_google_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_google_client_secret

# Email
MAIL_USERNAME=your_email_username
MAIL_PASSWORD=your_email_password
```

### Application Profiles

- **Development**: `spring.profiles.active=dev`
- **Production**: `spring.profiles.active=prod`

## 📚 API Documentation

### Core Endpoints

#### Authentication (`/v1/auth`)
- `POST /login` - User login
- `POST /register` - User registration
- `POST /verify-email` - Email verification
- `POST /reset-password` - Password reset
- `POST /send-verification-code` - Send OTP

#### User Management (`/v1/users`)
- `GET /profile` - Get user profile
- `PUT /profile` - Update user profile
- `POST /profile-picture` - Upload profile picture

#### Lawyer Management (`/v1/lawyers`)
- `POST /profile` - Create lawyer profile
- `GET /profile` - Get lawyer information
- `POST /credentials` - Upload bar certificate
- `GET /availability-slots` - Get availability slots
- `POST /availability-slots` - Create availability slot

#### Case Management (`/v1/cases`)
- `GET /` - List user cases
- `POST /` - Create new case
- `GET /{caseId}` - Get case details
- `PUT /{caseId}` - Update case
- `PUT /{caseId}/status` - Update case status

#### Document Management (`/v1/documents`)
- `GET /cases/{caseId}/documents` - List case documents
- `POST /upload` - Upload document
- `GET /{documentId}` - View document
- `PUT /{documentId}` - Update document
- `DELETE /{documentId}` - Delete document

#### Chat System (`/v1/chat`)
- `GET /conversations` - List conversations
- `GET /conversations/{id}/messages` - Get messages
- `POST /messages` - Send message
- `PUT /conversations/{id}/read` - Mark as read

#### Blog System (`/v1/blogs`)
- `GET /` - List published blogs
- `POST /` - Create blog
- `GET /{blogId}` - Get blog details
- `PUT /{blogId}` - Update blog
- `GET /search` - Search blogs

### WebSocket Endpoints

- `/ws` - WebSocket connection endpoint
- `/topic/chat/{userId}` - User-specific chat messages
- `/topic/notifications/{userId}` - User notifications

## 🔧 Development

### Running Tests
```bash
cd legalconnect
./mvnw test
```

### Code Quality
```bash
# Run SonarCloud analysis
./mvnw sonar:sonar

# Generate test coverage report
./mvnw jacoco:report
```

### Database Management
```bash
# Create migration (if using Flyway)
./mvnw flyway:migrate

# Reset database (development only)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=create-drop"
```

### Building for Production
```bash
# Build JAR file
./mvnw clean package -DskipTests

# Build Docker image
docker build -t legal-connect-backend .
```

## 🐳 Docker Configuration

### Services Overview

- **backend**: Spring Boot application (port 8080)
- **db**: PostgreSQL database (port 5432)
- **redis**: Redis cache (port 6379)
- **elasticsearch**: Search engine (port 9200)

### Health Checks

All services include comprehensive health checks:
- PostgreSQL: `pg_isready`
- Redis: `redis-cli ping`
- Elasticsearch: HTTP health endpoint
- Backend: Spring Boot Actuator health endpoint

### Volumes

- `pg_data`: PostgreSQL data persistence
- `redis_data`: Redis data persistence
- `es_data`: Elasticsearch data persistence

## 📁 Project Structure

```
backend/
├── legalconnect/
│   ├── src/main/java/com/javajedis/legalconnect/
│   │   ├── admin/                    # Admin management
│   │   ├── auth/                     # Authentication & authorization
│   │   ├── blogs/                    # Blog management system
│   │   ├── caseassets/              # Case documents & notes
│   │   ├── casemanagement/          # Case lifecycle management
│   │   ├── chat/                    # Real-time messaging
│   │   ├── common/                  # Shared utilities & services
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── exception/           # Global exception handling
│   │   │   ├── security/            # Security configurations
│   │   │   ├── service/             # Common services (AWS, Email, etc.)
│   │   │   ├── utility/             # Utility classes
│   │   │   └── validation/          # Custom validators
│   │   ├── config/                  # Application configurations
│   │   ├── jobscheduler/           # Background job processing
│   │   ├── lawyer/                  # Lawyer profile management
│   │   ├── lawyerdirectory/        # Lawyer search & reviews
│   │   ├── meeting/                # Video meeting integration
│   │   ├── notification/           # Push notifications
│   │   ├── payment/                # Payment processing
│   │   ├── schedule/               # Appointment scheduling
│   │   └── user/                   # User management
│   ├── src/main/resources/
│   │   ├── application.yml         # Main configuration
│   │   ├── quartz_tables.sql      # Quartz scheduler tables
│   │   └── templates/             # Email templates
│   └── src/test/                  # Test files
├── docker-compose.yml             # Multi-service container setup
├── Dockerfile                     # Container build instructions
├── .env                          # Environment variables
└── README.md                     # This file
```

## 🔐 Security

- **JWT Authentication**: Stateless authentication with configurable expiration
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Input Validation**: Comprehensive request validation with custom validators
- **SQL Injection Protection**: JPA/Hibernate ORM with parameterized queries
- **File Upload Security**: Type validation and size limits
- **Password Security**: BCrypt hashing with strength configuration
- **Email Verification**: Required email verification for new accounts

## 📊 Monitoring & Observability

### Health Checks
- `/v1/actuator/health` - Application health status
- `/v1/actuator/info` - Application information
- `/v1/actuator/metrics` - Application metrics

### Logging
- Structured logging with configurable levels
- File-based logging with rotation
- Console and file output patterns
- Request/response logging for debugging

### Metrics
- JVM metrics monitoring
- Database connection pool metrics
- Custom business metrics
- Performance monitoring

## 🤝 Integration

This backend integrates with:
- **Frontend Application**: RESTful API consumption
- **AI Backend**: Document analysis and chat features
- **External Services**: AWS S3, Stripe, Google Calendar, Jitsi Meet
- **Email Services**: SMTP-based notifications
- **Cloud Storage**: Cloudinary for media management

## 📝 API Versioning

The API uses URL-based versioning with `/v1` prefix. All endpoints are documented using OpenAPI 3.0 specification available at the root path (`/`).

## 🆘 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check PostgreSQL status
   docker-compose logs db
   
   # Verify database exists
   docker exec -it lc_postgres psql -U root -l
   ```

2. **Redis Connection Issues**
   ```bash
   # Check Redis status
   docker-compose logs redis
   
   # Test Redis connection
   docker exec -it lc_redis redis-cli ping
   ```

3. **Elasticsearch Issues**
   ```bash
   # Check Elasticsearch status
   curl http://localhost:9200/_cluster/health
   
   # View Elasticsearch logs
   docker-compose logs elasticsearch
   ```

4. **Application Startup Issues**
   ```bash
   # Check application logs
   docker-compose logs backend
   
   # Verify all dependencies are healthy
   docker-compose ps
   ```

5. **Port Conflicts**
   ```bash
   # Check if ports are in use
   netstat -tulpn | grep :8080
   netstat -tulpn | grep :5432
   ```

### Getting Help

1. Check application logs for detailed error messages
2. Verify all environment variables are properly set
3. Ensure all external services (database, Redis, Elasticsearch) are running
4. Check network connectivity between services
5. Verify file permissions for mounted volumes

For additional support, refer to the main Legal Connect project documentation.