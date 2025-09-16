/**
 * API Configuration
 */

export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/v1",
  AI_CHAT_BASE_URL:
    import.meta.env.VITE_AI_CHAT_BASE_URL || "http://localhost:8000/api/v1",

  // API endpoints
  ENDPOINTS: {
    AUTH: {
      REGISTER: "/auth/register",
      LOGIN: "/auth/login",
      SEND_VERIFICATION_CODE: "/auth/send-verification-code",
      VERIFY_EMAIL: "/auth/verify-email",
    },
    LAWYER_DIRECTORY_FIND_LAWYERS: "/lawyer-directory/find-lawyers",
    NOTIFICATIONS: {
      GET_ALL: "/notifications/",
      UNREAD_COUNT: "/notifications/unread-count",
      MARK_AS_READ: "/notifications/{id}/read",
      MARK_ALL_READ: "/notifications/mark-all-read",
      PREFERENCES: "/notifications/preferences",
      UPDATE_PREFERENCE: "/notifications/preferences/{type}",
      SEND: "/notifications/send",
    },
    CHAT: {
      SEND_MESSAGE: "/chat/send",
      GET_CONVERSATIONS: "/chat/conversations",
      GET_CONVERSATION_MESSAGES: "/chat/conversations/{id}/messages",
      MARK_CONVERSATION_READ: "/chat/conversations/{id}/read",
      UNREAD_COUNT: "/chat/unread-count",
    },
  },

  // Request timeout in milliseconds (1 minute)
  TIMEOUT: 60000,

  // Retry configuration
  RETRY: {
    ATTEMPTS: 3, // Number of retry attempts
    DELAY: 1000, // Initial delay between retries in milliseconds
    DELAY_MULTIPLIER: 2, // Exponential backoff multiplier
    MAX_DELAY: 10000, // Maximum delay between retries
    RETRY_ON_STATUS_CODES: [408, 429, 500, 502, 503, 504], // HTTP status codes to retry on
    RETRY_ON_NETWORK_ERROR: true, // Retry on network errors
  },

  // Default headers
  DEFAULT_HEADERS: {
    "Content-Type": "application/json",
  },
};

export default API_CONFIG;
