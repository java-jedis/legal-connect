import { chatAPI } from "./api";

/**
 * Chat Service
 * Handles all chat-related API calls with error handling and retry logic
 */

// Helper function to handle authentication errors
const handleAuthError = (error) => {
  if (error.response?.status === 401) {
    // Token expired or invalid - redirect to login
    console.warn("Chat API authentication failed - redirecting to login");
    
    // Clear auth data
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_isLoggedIn");
    localStorage.removeItem("auth_userType");
    localStorage.removeItem("auth_userInfo");
    
    // Redirect to login page
    if (typeof window !== "undefined" && window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
    
    throw new Error("Authentication expired. Please log in again.");
  }
  
  if (error.response?.status === 403) {
    throw new Error("Access denied. You do not have permission for this action.");
  }
  
  // Re-throw other errors
  throw error;
};

export const chatService = {
  /**
   * Send a message to another user
   * Creates a new conversation if one doesn't exist between the participants
   * @param {Object} messageData - Message data
   * @param {string} messageData.receiverId - UUID of the receiver
   * @param {string} messageData.content - Message content (1-1000 characters)
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing sent message
   */
  sendMessage: async (messageData, retryCount = 0, maxRetries = 2) => {
    try {
      const response = await chatAPI.sendMessage(messageData);
      return response;
    } catch (error) {
      console.error(
        `Error sending message (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Handle authentication errors first
      try {
        handleAuthError(error);
      } catch (authError) {
        throw authError;
      }

      // Check if we should retry
      if (retryCount < maxRetries && error.response?.status >= 500) {
        // Only retry on server errors (5xx)
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return chatService.sendMessage(messageData, retryCount + 1, maxRetries);
      }

      // Don't retry on client errors (4xx) or max retries reached
      throw new Error(
        error.response?.data?.message ||
          "Failed to send message"
      );
    }
  },

  /**
   * Get all conversations for the authenticated user
   * Returns conversations ordered by most recent activity with unread counts and latest messages
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing conversations list
   */
  getConversations: async (retryCount = 0, maxRetries = 2) => {
    try {
      const response = await chatAPI.getConversations();
      return response;
    } catch (error) {
      console.error(
        `Error fetching conversations (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Handle authentication errors first
      try {
        handleAuthError(error);
      } catch (authError) {
        throw authError;
      }

      // Check if we should retry
      if (retryCount < maxRetries && error.response?.status >= 500) {
        // Only retry on server errors (5xx)
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return chatService.getConversations(retryCount + 1, maxRetries);
      }

      // Don't retry on client errors (4xx) or max retries reached
      throw new Error(
        error.response?.data?.message ||
          "Failed to fetch conversations"
      );
    }
  },

  /**
   * Get messages for a specific conversation with pagination support
   * Only participants of the conversation can access its messages
   * @param {string} conversationId - UUID of the conversation
   * @param {number} page - Page number (default: 0)
   * @param {number} size - Page size (default: 20)
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing messages list
   */
  getConversationMessages: async (
    conversationId,
    page = 0,
    size = 20,
    retryCount = 0,
    maxRetries = 2
  ) => {
    try {
      const response = await chatAPI.getConversationMessages(conversationId, page, size);
      return response;
    } catch (error) {
      console.error(
        `Error fetching conversation messages (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Handle authentication errors first
      try {
        handleAuthError(error);
      } catch (authError) {
        throw authError;
      }

      // Check if we should retry
      if (retryCount < maxRetries && error.response?.status >= 500) {
        // Only retry on server errors (5xx)
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return chatService.getConversationMessages(
          conversationId,
          page,
          size,
          retryCount + 1,
          maxRetries
        );
      }

      // Don't retry on client errors (4xx) or max retries reached
      throw new Error(
        error.response?.data?.message ||
          "Failed to fetch conversation messages"
      );
    }
  },

  /**
   * Mark all messages in a conversation as read for the authenticated user
   * Only messages sent by other participants will be marked as read
   * @param {string} conversationId - UUID of the conversation
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing success status
   */
  markConversationAsRead: async (
    conversationId,
    retryCount = 0,
    maxRetries = 2
  ) => {
    try {
      const response = await chatAPI.markConversationAsRead(conversationId);
      return response;
    } catch (error) {
      console.error(
        `Error marking conversation as read (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Handle authentication errors first
      try {
        handleAuthError(error);
      } catch (authError) {
        throw authError;
      }

      // Check if we should retry
      if (retryCount < maxRetries && error.response?.status >= 500) {
        // Only retry on server errors (5xx)
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return chatService.markConversationAsRead(
          conversationId,
          retryCount + 1,
          maxRetries
        );
      }

      // Don't retry on client errors (4xx) or max retries reached
      throw new Error(
        error.response?.data?.message ||
          "Failed to mark conversation as read"
      );
    }
  },

  /**
   * Get the total count of unread messages for the authenticated user across all conversations
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing total unread count
   */
  getUnreadCount: async (retryCount = 0, maxRetries = 2) => {
    try {
      const response = await chatAPI.getUnreadCount();
      return response;
    } catch (error) {
      console.error(
        `Error fetching unread count (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Handle authentication errors first
      try {
        handleAuthError(error);
      } catch (authError) {
        throw authError;
      }

      // Check if we should retry
      if (retryCount < maxRetries && error.response?.status >= 500) {
        // Only retry on server errors (5xx)
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return chatService.getUnreadCount(retryCount + 1, maxRetries);
      }

      // Don't retry on client errors (4xx) or max retries reached
      throw new Error(
        error.response?.data?.message ||
          "Failed to fetch unread count"
      );
    }
  },

  /**
   * Create or find an existing conversation with another user
   * This is a helper method that sends an initial message to establish a conversation
   * @param {string} receiverId - UUID of the user to start conversation with
   * @param {string} initialMessage - Initial message content (optional, defaults to greeting)
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing the conversation message
   */
  startConversation: async (
    receiverId,
    initialMessage = "Hello! I'd like to discuss my legal needs with you.",
    retryCount = 0,
    maxRetries = 2
  ) => {
    try {
      const messageData = {
        receiverId,
        content: initialMessage,
      };
      
      const response = await chatService.sendMessage(messageData, retryCount, maxRetries);
      return response;
    } catch (error) {
      console.error(
        `Error starting conversation (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Error handling is already done in sendMessage, just re-throw
      throw new Error(
        error.message || "Failed to start conversation"
      );
    }
  },
};

export default chatService;