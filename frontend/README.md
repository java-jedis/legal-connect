# Legal Connect Frontend

A modern Vue.js 3 application providing the user interface for the Legal Connect platform. This frontend connects clients with lawyers and includes features like AI-powered chat, real-time messaging, case management, document handling, video conferencing, and blog management.

## 🏗️ Architecture

The frontend implements a modern single-page application (SPA) architecture with the following components:

- **Vue.js 3**: Progressive JavaScript framework with Composition API
- **Pinia**: State management for Vue.js applications
- **Vue Router**: Client-side routing for navigation
- **Vite**: Fast build tool and development server
- **WebSocket Integration**: Real-time chat and notifications
- **AI Chat Integration**: RAG-powered legal document analysis
- **Video Conferencing**: Jitsi Meet integration
- **Document Management**: File upload and viewing capabilities
- **Calendar Integration**: FullCalendar for scheduling

## 🛠️ Tech Stack

- **Framework**: Vue.js 3.5.13
- **Build Tool**: Vite 6.2.4
- **State Management**: Pinia 3.0.1
- **Routing**: Vue Router 4.5.0
- **HTTP Client**: Axios 1.10.0
- **WebSocket**: STOMP.js 7.1.1 with SockJS fallback
- **Calendar**: FullCalendar 6.1.18
- **Chat Components**: Vue Advanced Chat 2.1.0
- **PDF Generation**: jsPDF 3.0.1
- **UUID Generation**: UUID 11.1.0
- **Development**: ESLint, Prettier, Vue DevTools

## 📋 Prerequisites

- Node.js 20+
- npm or yarn
- Docker (for containerized deployment)

## 🚀 Quick Start

### Option 1: Local Development

1. **Clone and navigate to the frontend**:
   ```bash
   cd frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Create environment file**:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Start development server**:
   ```bash
   npm run dev
   ```

5. **Access the application**:
   - Development: http://localhost:5173
   - Network access: Available on all network interfaces

### Option 2: Docker Setup

1. **Build the Docker image**:
   ```bash
   docker build --no-cache -t legalconnect-frontend:dev \
     --build-arg VITE_API_BASE_URL="$VITE_API_BASE_URL" \
     --build-arg VITE_AI_CHAT_BASE_URL="$VITE_AI_CHAT_BASE_URL" \
     --build-arg VITE_JAAS_URL="$VITE_JAAS_URL" \
     frontend
   ```

2. **Run the container**:
   ```bash
   docker run -p 5173:5173 legalconnect-frontend:dev
   ```

3. **Access the application**:
   - Docker: http://localhost:5173

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the frontend directory with the following variables:

```env
# Backend API Configuration
VITE_API_BASE_URL=http://localhost:8080/v1
VITE_AI_CHAT_BASE_URL=http://localhost:8000/api/v1

# Jitsi Meet Configuration
VITE_JAAS_URL=https://8x8.vc
VITE_JITSI_APP_ID=vpaas-magic-cookie-your-app-id-here

# WebSocket Configuration
VITE_WS_BASE_URL=ws://localhost:8080

# Application Configuration
VITE_APP_NAME=Legal Connect
VITE_APP_VERSION=1.0.0

# Feature Flags
VITE_ENABLE_AI_CHAT=true
VITE_ENABLE_VIDEO_CALLS=true
VITE_ENABLE_NOTIFICATIONS=true
```

### Build Arguments

For Docker builds, the following build arguments are supported:

- `VITE_API_BASE_URL`: Backend API base URL
- `VITE_AI_CHAT_BASE_URL`: AI chat service base URL
- `VITE_JAAS_URL`: Jitsi as a Service URL
- `VITE_JITSI_APP_ID`: Jitsi application ID

## 📚 Application Structure

### Core Features

#### Authentication & User Management
- User registration and login
- Email verification
- Password reset functionality
- Profile management with picture upload
- JWT-based authentication

#### Lawyer Management
- Lawyer profile creation and verification
- Availability slot management
- Lawyer directory with search and filters
- Review and rating system

#### Case Management
- Case creation and lifecycle management
- Document upload and management
- Note-taking functionality
- Case status tracking

#### Real-time Chat System
- Direct messaging between users
- WebSocket-based real-time communication
- Message read status tracking
- Unread message counters

#### AI-Powered Legal Assistant
- RAG-based document analysis
- Legal document search
- Chat history management
- Document upload to chat sessions

#### Video Conferencing
- Jitsi Meet integration
- Scheduled meeting management
- Calendar integration

#### Blog System
- Legal insights and articles
- Author subscription system
- Blog search functionality
- Content management for lawyers

#### Admin Panel
- Lawyer verification management
- User management
- System monitoring

### Component Architecture

```
src/
├── components/
│   ├── admin/                    # Admin panel components
│   ├── ai/                      # AI chat components
│   ├── blogs/                   # Blog management components
│   ├── case/                    # Case management components
│   ├── caseassets/             # Document and note components
│   ├── chat/                   # Real-time chat components
│   ├── icons/                  # Icon components
│   ├── lawyer/                 # Lawyer profile components
│   ├── notification/           # Notification components
│   ├── schedule/               # Calendar and scheduling components
│   └── user/                   # User profile components
├── stores/                     # Pinia state management
├── services/                   # API and external services
├── utils/                      # Utility functions and helpers
├── views/                      # Page components
├── router/                     # Vue Router configuration
└── config/                     # Application configuration
```

## 🔧 Development

### Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint

# Format code
npm run format
```

### Code Quality

The project uses ESLint and Prettier for code quality and formatting:

```bash
# Run linting with auto-fix
npm run lint

# Format code
npm run format
```

### State Management

The application uses Pinia for state management with the following stores:

- **auth**: Authentication and user session
- **blog**: Blog management and content
- **case**: Case management and lifecycle
- **chat**: Real-time messaging
- **lawyer**: Lawyer profiles and directory
- **notification**: Push notifications
- **theme**: UI theme management
- **admin**: Admin panel functionality
- **counter**: Example counter store
- **review**: Lawyer reviews and ratings

### API Integration

The frontend communicates with multiple backend services:

- **Main Backend**: Spring Boot API for core functionality
- **AI Backend**: FastAPI RAG system for document analysis
- **WebSocket**: Real-time communication
- **File Storage**: Document and media upload

## 🎨 UI/UX Features

### Responsive Design
- Mobile-first approach
- Responsive layouts for all screen sizes
- Touch-friendly interface

### Theme Support
- Light and dark theme toggle
- Consistent design system
- Accessible color schemes

### Real-time Features
- Live chat messaging
- Push notifications
- WebSocket connection management
- Auto-reconnection handling

### Advanced Components
- Calendar integration with FullCalendar
- Advanced chat interface
- Document viewer and editor
- Video call modal integration
- File upload with progress tracking

## 🔐 Security

- **JWT Authentication**: Secure token-based authentication
- **CORS Handling**: Proper cross-origin request handling
- **Input Validation**: Client-side validation with server-side verification
- **File Upload Security**: Type and size validation
- **XSS Protection**: Sanitized user inputs
- **Secure WebSocket**: Authenticated WebSocket connections

## 🐳 Docker Configuration

### Multi-stage Build

The Dockerfile uses a multi-stage build process:

1. **Build Stage**: Node.js 20 Alpine for building the application
2. **Runtime Stage**: Node.js 20 Alpine with serve for production

### Build Arguments

The Docker build accepts the following arguments:

```dockerfile
ARG VITE_API_BASE_URL
ARG VITE_AI_CHAT_BASE_URL
ARG VITE_JAAS_URL
ARG VITE_JITSI_APP_ID
```

### Production Deployment

For production deployment, the application is served using the `serve` package:

```bash
# Build with production environment variables
docker build --no-cache -t legalconnect-frontend:prod \
  --build-arg VITE_API_BASE_URL="https://api.legalconnect.live/v1" \
  --build-arg VITE_AI_CHAT_BASE_URL="https://ai.legalconnect.live/api/v1" \
  --build-arg VITE_JAAS_URL="https://8x8.vc" \
  frontend

# Run in production mode
docker run -p 5173:5173 legalconnect-frontend:prod
```

## 📱 Features Overview

### For Clients
- **Find Lawyers**: Search and filter lawyers by specialization, location, and experience
- **Case Management**: Create and track legal cases
- **Document Management**: Upload and organize case documents
- **AI Legal Assistant**: Get AI-powered legal document analysis
- **Real-time Chat**: Communicate with lawyers instantly
- **Video Meetings**: Schedule and join video consultations
- **Payment Integration**: Secure payment processing

### For Lawyers
- **Profile Management**: Create and manage professional profiles
- **Availability Management**: Set availability slots for consultations
- **Case Tracking**: Manage client cases and documents
- **Blog Publishing**: Share legal insights and articles
- **Client Communication**: Chat and video calls with clients
- **Document Analysis**: AI-powered document review tools

### For Admins
- **Lawyer Verification**: Approve and manage lawyer registrations
- **User Management**: Monitor and manage user accounts
- **System Monitoring**: Track application performance and usage
- **Content Moderation**: Manage blogs and user-generated content

## 🔌 API Integration

### Backend Services

The frontend integrates with multiple backend services:

```javascript
// Main Spring Boot Backend
VITE_API_BASE_URL=http://localhost:8080/v1

// AI RAG Backend
VITE_AI_CHAT_BASE_URL=http://localhost:8000/api/v1

// WebSocket Connection
VITE_WS_BASE_URL=ws://localhost:8080
```

### Service Classes

- **api.js**: Main API service with Axios configuration
- **aiChatService.js**: AI chat and document analysis
- **chatService.js**: Real-time messaging
- **notificationService.js**: Push notification handling

## 🚀 Performance Optimization

### Build Optimization
- **Vite**: Fast build tool with hot module replacement
- **Code Splitting**: Automatic route-based code splitting
- **Tree Shaking**: Unused code elimination
- **Asset Optimization**: Image and asset compression

### Runtime Optimization
- **Lazy Loading**: Components and routes loaded on demand
- **Caching**: API response caching with Axios
- **WebSocket Management**: Efficient connection handling
- **State Management**: Optimized Pinia stores

## 🆘 Troubleshooting

### Common Issues

1. **Development Server Won't Start**
   ```bash
   # Clear node_modules and reinstall
   rm -rf node_modules package-lock.json
   npm install
   npm run dev
   ```

2. **Build Failures**
   ```bash
   # Check for TypeScript errors
   npm run lint
   
   # Clear Vite cache
   rm -rf node_modules/.vite
   npm run build
   ```

3. **WebSocket Connection Issues**
   ```bash
   # Check backend WebSocket endpoint
   # Verify CORS configuration
   # Check network connectivity
   ```

4. **Docker Build Issues**
   ```bash
   # Build with verbose output
   docker build --no-cache --progress=plain -t legalconnect-frontend:dev frontend
   
   # Check build arguments
   echo $VITE_API_BASE_URL
   ```

5. **Environment Variable Issues**
   ```bash
   # Verify .env file exists and has correct format
   cat .env
   
   # Check if variables are loaded
   console.log(import.meta.env.VITE_API_BASE_URL)
   ```

### Getting Help

1. Check browser console for JavaScript errors
2. Verify all environment variables are properly set
3. Ensure backend services are running and accessible
4. Check network connectivity and CORS configuration
5. Review build logs for detailed error messages

For additional support, refer to the main Legal Connect project documentation.

## 📄 License

This project is part of the Legal Connect system. See the main project LICENSE file for details.