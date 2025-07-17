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
      const isAuthRequest = error.config?.url?.includes("/auth/");
      const wasAuthenticated = localStorage.getItem("auth_token");

      console.log("401 Error detected:", {
        url: error.config?.url,
        isLoginRequest,
        isAuthRequest,
        wasAuthenticated,
      });

      if (!isLoginRequest && !isAuthRequest && wasAuthenticated) {
        // Token expired or invalid for authenticated requests, clear auth data and redirect
        console.log("Logging out user due to 401 error");
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

  // Initiate Google Calendar OAuth2 flow
  authorizeGoogleCalendar: async () => {
    const response = await api.get("/schedule/oauth/authorize");
    return response.data;
  },

  // Check Google Calendar integration status
  checkGoogleCalendarStatus: async () => {
    const response = await api.get("/schedule/oauth/status");
    return response.data;
  },
};

// Lawyer API methods
export const lawyerAPI = {
  // Get lawyer info
  getLawyerInfo: async (email = null) => {
    const params = email ? { email } : {};
    console.log("API call: getLawyerInfo with params:", params);
    const response = await api.get("/lawyer/profile", { params });
    console.log("API response:", response);
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
  viewCredentials: async (email = null) => {
    const params = email ? { email } : {};
    const response = await api.get("/lawyer/view-credentials", {
      params,
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

  // Find lawyers based on criteria
  findLawyers: async (payload, page = 0, size = 10) => {
    const response = await api.post(
      API_CONFIG.ENDPOINTS.LAWYER_DIRECTORY_FIND_LAWYERS ||
        "/lawyer-directory/find-lawyers",
      payload,
      {
        params: {
          page,
          size,
          sortDirection: "DESC", // Default sort direction, as per requirement to remove sort direction field
        },
      }
    );
    return response.data;
  },

  // Get reviews for a lawyer
  getLawyerReviews: async (
    lawyerId,
    page = 0,
    size = 10,
    sortDirection = "DESC"
  ) => {
    const response = await api.get(
      `/lawyer-directory/lawyers/${lawyerId}/reviews`,
      {
        params: { page, size, sortDirection },
      }
    );
    return response.data;
  },
};

// Admin API methods
export const adminAPI = {
  // Get pending lawyers with pagination
  getPendingLawyers: async (page = 0, size = 10) => {
    const response = await api.get("/admin/lawyers/pending", {
      params: { page, size },
    });
    return response.data;
  },

  // Get lawyers by verification status with pagination
  getLawyersByStatus: async (status = null, page = 0, size = 10) => {
    const params = { page, size };
    if (status) params.status = status;
    const response = await api.get("/admin/lawyers", { params });
    return response.data;
  },

  // Approve lawyer verification
  approveLawyer: async (lawyerId) => {
    const response = await api.put(`/admin/lawyers/${lawyerId}/approve`);
    return response.data;
  },

  // Reject lawyer verification
  rejectLawyer: async (lawyerId) => {
    const response = await api.put(`/admin/lawyers/${lawyerId}/reject`);
    return response.data;
  },
};

// Case Management API methods
export const caseAPI = {
  // Get all cases for authenticated user with pagination and filtering
  getAllUserCases: async (
    page = 0,
    size = 10,
    status = null,
    sortDirection = "DESC"
  ) => {
    const params = { page, size, sortDirection };
    if (status) params.status = status;
    const response = await api.get("/case/", { params });
    return response.data;
  },

  // Get single case by ID
  getCaseById: async (caseId) => {
    const response = await api.get(`/case/${caseId}`);
    return response.data;
  },

  // Create new case (lawyer only)
  createCase: async (caseData) => {
    const response = await api.post("/case/", caseData);
    return response.data;
  },

  // Update case title and description (lawyer only)
  updateCase: async (caseId, updateData) => {
    const response = await api.put(`/case/${caseId}`, updateData);
    return response.data;
  },

  // Update case status (lawyer only)
  updateCaseStatus: async (caseId, statusData) => {
    const response = await api.put(`/case/${caseId}/status`, statusData);
    return response.data;
  },
};

// Scheduling API methods
export const scheduleAPI = {
  // Get all schedules for a specific case
  getAllSchedulesForCase: async (
    caseId,
    page = 0,
    size = 10,
    sortDirection = "DESC"
  ) => {
    const params = { page, size, sortDirection };
    const response = await api.get(`/schedule/case/${caseId}`, { params });
    return response.data;
  },

  // Get all user schedules
  getAllUserSchedules: async (page = 0, size = 10, sortDirection = "DESC") => {
    const params = { page, size, sortDirection };
    const response = await api.get("/schedule/", { params });
    return response.data;
  },

  // Get single schedule by ID
  getScheduleById: async (scheduleId) => {
    const response = await api.get(`/schedule/${scheduleId}`);
    return response.data;
  },

  // Create new schedule
  createSchedule: async (scheduleData) => {
    const response = await api.post("/schedule/", scheduleData);
    return response.data;
  },

  // Update schedule
  updateSchedule: async (scheduleId, updateData) => {
    const response = await api.put(`/schedule/${scheduleId}`, updateData);
    return response.data;
  },

  // Delete schedule
  deleteSchedule: async (scheduleId) => {
    const response = await api.delete(`/schedule/${scheduleId}`);
    return response.data;
  },
};

// Case Assets API methods
export const caseAssetsAPI = {
  // Get all notes for a case
  getAllNotesForCase: async (
    caseId,
    page = 0,
    size = 10,
    sortDirection = "DESC"
  ) => {
    const params = { page, size, sortDirection };
    const response = await api.get(`/case-assets/cases/${caseId}/notes`, {
      params,
    });
    return response.data;
  },

  // Get all documents for a case
  getAllDocumentsForCase: async (
    caseId,
    page = 0,
    size = 10,
    sortDirection = "DESC"
  ) => {
    const params = { page, size, sortDirection };
    const response = await api.get(`/case-assets/cases/${caseId}/documents`, {
      params,
    });
    return response.data;
  },

  // Get single note by ID
  getNoteById: async (noteId) => {
    const response = await api.get(`/case-assets/notes/${noteId}`);
    return response.data;
  },

  // Get single document by ID
  getDocumentById: async (documentId) => {
    const response = await api.get(`/case-assets/documents/${documentId}`);
    return response.data;
  },

  // Create new note
  createNote: async (noteData) => {
    const response = await api.post("/case-assets/notes", noteData);
    return response.data;
  },

  // Update note
  updateNote: async (noteId, updateData) => {
    const response = await api.put(`/case-assets/notes/${noteId}`, updateData);
    return response.data;
  },

  // Delete note
  deleteNote: async (noteId) => {
    const response = await api.delete(`/case-assets/notes/${noteId}`);
    return response.data;
  },

  // Upload document
  uploadDocument: async (documentData, file) => {
    const formData = new FormData();
    formData.append("caseId", documentData.caseId);
    formData.append("title", documentData.title);
    formData.append("description", documentData.description);
    formData.append("privacy", documentData.privacy);
    formData.append("file", file);

    const response = await api.post("/case-assets/documents", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  // Update document
  updateDocument: async (documentId, updateData) => {
    const response = await api.put(
      `/case-assets/documents/${documentId}`,
      updateData
    );
    return response.data;
  },

  // Delete document
  deleteDocument: async (documentId) => {
    const response = await api.delete(`/case-assets/documents/${documentId}`);
    return response.data;
  },

  // View/Download document
  viewDocument: async (documentId) => {
    const response = await api.get(
      `/case-assets/documents/${documentId}/view`,
      {
        responseType: "blob",
      }
    );
    return response.data;
  },
};

export default api;
