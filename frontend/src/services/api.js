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
    if (
      token &&
      token.trim() !== "" &&
      token !== "null" &&
      token !== "undefined"
    ) {
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

  // Change password for authenticated user
  changePassword: async (data) => {
    const response = await api.put("/user/change-password", data);
    return response.data;
  },

  // Logout user (blacklist JWT)
  logout: async () => {
    const response = await api.post("/user/logout");
    return response.data;
  },
};

// Lawyer API methods
export const lawyerAPI = {
  // Get lawyer info
  getLawyerInfo: async () => {
    const response = await api.get("/lawyer/profile");
    return response.data;
  },

  // Create lawyer profile
  createLawyerProfile: async (profileData) => {
    const response = await api.post("/lawyer/profile", profileData);
    return response.data;
  },

  // Update lawyer profile
  updateLawyerProfile: async (profileData) => {
    const response = await api.put("/lawyer/profile", profileData);
    return response.data;
  },

  // Upload bar certificate
  uploadCredentials: async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post("/lawyer/upload-credentials", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  // View bar certificate
  viewCredentials: async () => {
    const response = await api.get("/lawyer/view-credentials", {
      responseType: "blob",
    });
    return response.data;
  },

  // Get availability slots
  getAvailabilitySlots: async (email = null) => {
    const params = email ? { email } : {};
    const response = await api.get("/lawyer/availability-slots", { params });
    return response.data;
  },

  // Create availability slot
  createAvailabilitySlot: async (slotData) => {
    const response = await api.post("/lawyer/availability-slots", slotData);
    return response.data;
  },

  // Update availability slot
  updateAvailabilitySlot: async (slotId, slotData) => {
    const response = await api.put(
      `/lawyer/availability-slots/${slotId}`,
      slotData
    );
    return response.data;
  },

  // Delete availability slot
  deleteAvailabilitySlot: async (slotId) => {
    const response = await api.delete(`/lawyer/availability-slots/${slotId}`);
    return response.data;
  },
};

export default api;
