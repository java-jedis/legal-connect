/**
 * API Configuration
 */

export const API_CONFIG = {
  // Base URL for the backend API
  BASE_URL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/v1",

  // API endpoints
  ENDPOINTS: {
    AUTH: {
      REGISTER: "/auth/register",
      LOGIN: "/auth/login",
      SEND_VERIFICATION_CODE: "/auth/send-verification-code",
      VERIFY_EMAIL: "/auth/verify-email",
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
