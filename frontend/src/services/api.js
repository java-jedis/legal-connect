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

// User API methods
export const userAPI = {
  // Get current user info
  getUserInfo: async () => {
    const response = await api.get("/user/user-info");
    return response.data;
  },

  // Upload profile picture
  uploadProfilePicture: async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post("/user/profile-picture", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
      timeout: 2000 * 60,
    });
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

  // Update hourly charge
  updateHourlyCharge: async (hourlyCharge) => {
    const response = await api.put("/lawyer/hourly-charge", { hourlyCharge });
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

// Notification API methods
export const notificationAPI = {
  /**
   * Get notifications for the authenticated user with pagination support
   * @param {number} page - Page number (default: 0)
   * @param {number} size - Page size (default: 10)
   * @param {boolean} unreadOnly - Filter for unread notifications only (default: false)
   * @returns {Promise<Object>} Response containing notifications list
   */
  getNotifications: async (page = 0, size = 10, unreadOnly = false) => {
    try {
      const response = await api.get(
        API_CONFIG.ENDPOINTS.NOTIFICATIONS.GET_ALL,
        {
          params: { page, size, unreadOnly },
        }
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching notifications:", error);
      throw error;
    }
  },

  /**
   * Get the count of unread notifications for the authenticated user
   * @returns {Promise<Object>} Response containing unread count
   */
  getUnreadCount: async () => {
    try {
      const response = await api.get(
        API_CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching unread count:", error);
      throw error;
    }
  },

  /**
   * Mark a specific notification as read
   * @param {string} notificationId - UUID of the notification to mark as read
   * @returns {Promise<Object>} Response containing updated notification
   */
  markAsRead: async (notificationId) => {
    try {
      const endpoint = API_CONFIG.ENDPOINTS.NOTIFICATIONS.MARK_AS_READ.replace(
        "{id}",
        notificationId
      );
      const response = await api.put(endpoint);
      return response.data;
    } catch (error) {
      console.error("Error marking notification as read:", error);
      throw error;
    }
  },

  /**
   * Mark all notifications as read for the authenticated user
   * @returns {Promise<Object>} Response containing updated unread count
   */
  markAllAsRead: async () => {
    try {
      const response = await api.put(
        API_CONFIG.ENDPOINTS.NOTIFICATIONS.MARK_ALL_READ
      );
      return response.data;
    } catch (error) {
      console.error("Error marking all notifications as read:", error);
      throw error;
    }
  },

  /**
   * Get all notification preferences for the authenticated user
   * @returns {Promise<Object>} Response containing notification preferences
   */
  getPreferences: async () => {
    try {
      const response = await api.get(
        API_CONFIG.ENDPOINTS.NOTIFICATIONS.PREFERENCES
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching notification preferences:", error);
      throw error;
    }
  },

  /**
   * Update a specific notification preference for the authenticated user
   * @param {string} type - Notification type (e.g., 'CASE_CREATE', 'EVENT_ADD')
   * @param {Object} preferences - Preference settings
   * @param {boolean} preferences.emailEnabled - Enable/disable email notifications
   * @param {boolean} preferences.webPushEnabled - Enable/disable web push notifications
   * @returns {Promise<Object>} Response containing updated preference
   */
  updatePreference: async (type, preferences) => {
    try {
      const endpoint =
        API_CONFIG.ENDPOINTS.NOTIFICATIONS.UPDATE_PREFERENCE.replace(
          "{type}",
          type
        );
      const response = await api.put(endpoint, {
        emailEnabled: preferences.emailEnabled,
        webPushEnabled: preferences.webPushEnabled,
      });
      return response.data;
    } catch (error) {
      console.error("Error updating notification preference:", error);
      throw error;
    }
  },

  /**
   * Send a notification to a specific user (internal service calls)
   * @param {Object} notificationData - Notification data
   * @param {string} notificationData.receiverId - UUID of the receiver
   * @param {string} notificationData.content - Notification content
   * @param {string} notificationData.type - Notification type
   * @returns {Promise<Object>} Response containing sent notification
   */
  sendNotification: async (notificationData) => {
    try {
      const response = await api.post(
        API_CONFIG.ENDPOINTS.NOTIFICATIONS.SEND,
        notificationData
      );
      return response.data;
    } catch (error) {
      console.error("Error sending notification:", error);
      throw error;
    }
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

// Chat API methods
export const chatAPI = {
  /**
   * Send a message to another user
   * Creates a new conversation if one doesn't exist between the participants
   * @param {Object} messageData - Message data
   * @param {string} messageData.receiverId - UUID of the receiver
   * @param {string} messageData.content - Message content (1-1000 characters)
   * @returns {Promise<Object>} Response containing sent message
   */
  sendMessage: async (messageData) => {
    const response = await api.post(
      API_CONFIG.ENDPOINTS.CHAT.SEND_MESSAGE,
      messageData
    );
    return response.data;
  },

  /**
   * Get all conversations for the authenticated user
   * Returns conversations ordered by most recent activity with unread counts and latest messages
   * @returns {Promise<Object>} Response containing conversations list
   */
  getConversations: async () => {
    const response = await api.get(API_CONFIG.ENDPOINTS.CHAT.GET_CONVERSATIONS);
    return response.data;
  },

  /**
   * Get messages for a specific conversation with pagination support
   * Only participants of the conversation can access its messages
   * @param {string} conversationId - UUID of the conversation
   * @param {number} page - Page number (default: 0)
   * @param {number} size - Page size (default: 20)
   * @returns {Promise<Object>} Response containing messages list
   */
  getConversationMessages: async (conversationId, page = 0, size = 20) => {
    const endpoint =
      API_CONFIG.ENDPOINTS.CHAT.GET_CONVERSATION_MESSAGES.replace(
        "{id}",
        conversationId
      );
    const response = await api.get(endpoint, {
      params: { page, size },
    });
    return response.data;
  },

  /**
   * Mark all messages in a conversation as read for the authenticated user
   * Only messages sent by other participants will be marked as read
   * @param {string} conversationId - UUID of the conversation
   * @returns {Promise<Object>} Response containing success status
   */
  markConversationAsRead: async (conversationId) => {
    const endpoint = API_CONFIG.ENDPOINTS.CHAT.MARK_CONVERSATION_READ.replace(
      "{id}",
      conversationId
    );
    const response = await api.put(endpoint);
    return response.data;
  },

  /**
   * Get the total count of unread messages for the authenticated user across all conversations
   * @returns {Promise<Object>} Response containing total unread count
   */
  getUnreadCount: async () => {
    const response = await api.get(API_CONFIG.ENDPOINTS.CHAT.UNREAD_COUNT);
    return response.data;
  },
};

// Meeting/Video Call API methods
export const meetingAPI = {
  // Get all meetings for authenticated user
  getMeetings: async () => {
    const response = await api.get("/videocalls/");
    return response.data;
  },

  // Get single meeting by ID
  getMeeting: async (meetingId) => {
    const response = await api.get(`/videocalls/${meetingId}`);
    return response.data;
  },

  // Schedule new meeting (lawyer only)
  scheduleMeeting: async (meetingData) => {
    const response = await api.post("/videocalls/", meetingData);
    return response.data;
  },

  // Update meeting (lawyer only)
  updateMeeting: async (meetingData) => {
    const response = await api.put("/videocalls/", meetingData);
    return response.data;
  },

  // Delete meeting (lawyer only)
  deleteMeeting: async (meetingId) => {
    const response = await api.delete(`/videocalls/${meetingId}`);
    return response.data;
  },

  // Generate meeting token for joining video call
  generateMeetingToken: async (meetingId) => {
    const response = await api.get(`/videocalls/${meetingId}/token`);
    return response.data;
  },
};

// Payment API methods
export const paymentAPI = {
  // Create a new payment (internal testing only)
  createPayment: async (paymentData) => {
    const response = await api.post("/payments/", paymentData);
    return response.data;
  },

  // Create Stripe checkout session for a payment
  createStripeSession: async (paymentId) => {
    const response = await api.post(`/payments/${paymentId}/stripe-session`);
    return response.data;
  },

  // Complete payment using Stripe session ID
  completePayment: async (sessionId) => {
    const response = await api.put(`/payments/complete/${sessionId}`);
    return response.data;
  },

  // Get payment by ID
  getPayment: async (paymentId) => {
    const response = await api.get(`/payments/${paymentId}`);
    return response.data;
  },

  // Get all payments for authenticated user
  getAllPayments: async (page = 0, size = 10, sortDirection = "DESC") => {
    const params = { page, size, sortDirection };
    const response = await api.get("/payments/", { params });
    return response.data;
  },

  // Release payment to payee
  releasePayment: async (paymentId) => {
    const response = await api.put(`/payments/${paymentId}/release`);
    return response.data;
  },

  // Cancel payment
  cancelPayment: async (paymentId) => {
    const response = await api.put(`/payments/${paymentId}/cancel`);
    return response.data;
  },
};

// Blog API methods
export const blogAPI = {
  // Create a new blog (lawyer only)
  createBlog: async (blogData) => {
    const response = await api.post("/blogs", blogData);
    return response.data;
  },

  // Update an existing blog (lawyer only)
  updateBlog: async (blogId, blogData) => {
    const response = await api.put(`/blogs/${blogId}`, blogData);
    return response.data;
  },

  // Delete a blog (lawyer only)
  deleteBlog: async (blogId) => {
    const response = await api.delete(`/blogs/${blogId}`);
    return response.data;
  },

  // Change blog status (DRAFT | PUBLISHED | ARCHIVED)
  changeBlogStatus: async (blogId, status) => {
    const response = await api.put(`/blogs/${blogId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  // Get a single blog by ID
  getBlog: async (blogId) => {
    const response = await api.get(`/blogs/${blogId}`);
    return response.data;
  },

  // Get all blogs by an author (if current user is author -> all, else -> published only)
  getAuthorBlogs: async (
    authorId,
    page = 0,
    size = 10,
    sortDirection = "DESC"
  ) => {
    const response = await api.get(`/blogs/authors/${authorId}`, {
      params: { page, size, sortDirection },
    });
    return response.data;
  },

  // Get subscribers for the authenticated author (lawyer only)
  getSubscribers: async (page = 0, size = 10) => {
    const response = await api.get(`/blogs/authors/subscribers`, {
      params: { page, size },
    });
    return response.data;
  },

  // Subscribe to an author
  subscribe: async (authorId) => {
    const response = await api.post(`/blogs/authors/${authorId}/subscribe`);
    return response.data;
  },

  // Unsubscribe from an author
  unsubscribe: async (authorId) => {
    const response = await api.delete(`/blogs/authors/${authorId}/subscribe`);
    return response.data;
  },

  // Get blogs from authors the authenticated user is subscribed to
  getSubscribedBlogs: async (page = 0, size = 10, sortDirection = "DESC") => {
    const response = await api.get(`/blogs/subscribed`, {
      params: { page, size, sortDirection },
    });
    return response.data;
  },

  // Search published blogs (title/content)
  searchPublished: async (q, page = 0, size = 10) => {
    const response = await api.get(`/blogs/search`, {
      params: { q, page, size },
    });
    return response.data;
  },
};

export default api;
