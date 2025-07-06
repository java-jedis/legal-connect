import axios from "axios";
import { API_CONFIG } from "../config/api";

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: API_CONFIG.DEFAULT_HEADERS,
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("auth_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle common errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Only redirect if this is not a login request and user was previously authenticated
      const isLoginRequest = error.config?.url?.includes("/auth/login");
      const wasAuthenticated = localStorage.getItem("auth_token");

      if (!isLoginRequest && wasAuthenticated) {
        // Token expired or invalid for authenticated requests, clear auth data and redirect
        localStorage.removeItem("auth_token");
        localStorage.removeItem("auth_isLoggedIn");
        localStorage.removeItem("auth_userType");
        localStorage.removeItem("auth_userInfo");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

// Auth API methods
export const authAPI = {
  // Register a new user
  register: async (userData) => {
    const response = await api.post("/auth/register", userData);
    return response.data;
  },

  // Login user
  login: async (credentials) => {
    const response = await api.post("/auth/login", credentials);
    return response.data;
  },

  // Send verification code
  sendVerificationCode: async (email) => {
    const response = await api.post("/auth/send-verification-code", { email });
    return response.data;
  },

  // Verify email with OTP
  verifyEmail: async (verificationData) => {
    const response = await api.post("/auth/verify-email", verificationData);
    return response.data;
  },

  // Send OTP for password reset
  sendPasswordResetOTP: async (email) => {
    const response = await api.post("/auth/send-verification-code", { email });
    return response.data;
  },

  // Reset password with OTP
  resetPassword: async (resetData) => {
    const response = await api.post("/auth/reset-password", resetData);
    return response.data;
  },
};

export default api;
