/**
 * API Configuration
 */

export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/v1",

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
  },

  // Request timeout in milliseconds
  TIMEOUT: 10000,

  // Default headers
  DEFAULT_HEADERS: {
    "Content-Type": "application/json",
  },
};

export default API_CONFIG;
