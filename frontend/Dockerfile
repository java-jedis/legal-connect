# ---- Build stage ----
FROM node:20-alpine AS builder
WORKDIR /app

# Install deps first (better layer caching)
COPY package*.json ./
RUN npm ci

# Copy source
COPY . .

# Accept build-time envs for Vite
ARG VITE_API_BASE_URL
ARG VITE_AI_CHAT_BASE_URL
ARG VITE_JAAS_URL
ARG VITE_JITSI_APP_ID
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_AI_CHAT_BASE_URL=${VITE_AI_CHAT_BASE_URL}
ENV VITE_JAAS_URL=${VITE_JAAS_URL}
ENV VITE_JITSI_APP_ID=${VITE_JITSI_APP_ID}

# Build
RUN npm run build

# ---- Runtime stage (serve) ----
FROM node:20-alpine
WORKDIR /app

ENV NODE_ENV=production
RUN npm i -g serve@14
COPY --from=builder /app/dist /app

# Use port 5173 to match development server
ENV PORT=5173
EXPOSE 5173

# Bind to 0.0.0.0 and use port 5173
CMD ["sh","-c","serve -s /app -l tcp://0.0.0.0:${PORT}"]