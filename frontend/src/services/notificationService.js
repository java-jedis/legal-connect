import { notificationAPI } from "./api";

/**
 * Notification Service
 * Handles all notification-related API calls
 */
export const notificationService = {
  /**
   * Get notifications for the authenticated user with pagination support
   * @param {number} page - Page number (default: 0)
   * @param {number} size - Page size (default: 10)
   * @param {boolean} unreadOnly - Filter for unread notifications only (default: false)
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing notifications list
   */
  getNotifications: async (
    page = 0,
    size = 10,
    unreadOnly = false,
    retryCount = 0,
    maxRetries = 2
  ) => {
    try {
      const response = await notificationAPI.getNotifications(
        page,
        size,
        unreadOnly
      );
      return response;
    } catch (error) {
      console.error(
        `Error fetching notifications (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.getNotifications(
          page,
          size,
          unreadOnly,
          retryCount + 1,
          maxRetries
        );
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to fetch notifications after multiple attempts"
      );
    }
  },

  /**
   * Get the count of unread notifications for the authenticated user
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing unread count
   */
  getUnreadCount: async (retryCount = 0, maxRetries = 2) => {
    try {
      const response = await notificationAPI.getUnreadCount();
      return response;
    } catch (error) {
      console.error(
        `Error fetching unread count (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);

        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.getUnreadCount(retryCount + 1, maxRetries);
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to fetch unread count after multiple attempts"
      );
    }
  },

  /**
   * Mark a specific notification as read
   * @param {string} notificationId - UUID of the notification to mark as read
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing updated notification
   */
  markAsRead: async (notificationId, retryCount = 0, maxRetries = 2) => {
    try {
      const response = await notificationAPI.markAsRead(notificationId);
      return response;
    } catch (error) {
      console.error(
        `Error marking notification as read (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);

        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.markAsRead(
          notificationId,
          retryCount + 1,
          maxRetries
        );
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to mark notification as read after multiple attempts"
      );
    }
  },

  /**
   * Mark all notifications as read for the authenticated user
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing updated unread count
   */
  markAllAsRead: async (retryCount = 0, maxRetries = 2) => {
    try {
      const response = await notificationAPI.markAllAsRead();
      return response;
    } catch (error) {
      console.error(
        `Error marking all notifications as read (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.markAllAsRead(retryCount + 1, maxRetries);
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to mark all notifications as read after multiple attempts"
      );
    }
  },

  /**
   * Get all notification preferences for the authenticated user
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing notification preferences
   */
  getPreferences: async (retryCount = 0, maxRetries = 2) => {
    try {
      const response = await notificationAPI.getPreferences();
      return response;
    } catch (error) {
      console.error(
        `Error fetching notification preferences (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);

        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.getPreferences(retryCount + 1, maxRetries);
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to fetch notification preferences after multiple attempts"
      );
    }
  },

  /**
   * Update a specific notification preference for the authenticated user
   * @param {string} type - Notification type (e.g., 'CASE_CREATE', 'EVENT_ADD')
   * @param {Object} preferences - Preference settings
   * @param {boolean} preferences.emailEnabled - Enable/disable email notifications
   * @param {boolean} preferences.webPushEnabled - Enable/disable web push notifications
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing updated preference
   */
  updatePreference: async (
    type,
    preferences,
    retryCount = 0,
    maxRetries = 2
  ) => {
    try {
      const response = await notificationAPI.updatePreference(
        type,
        preferences
      );
      return response;
    } catch (error) {
      console.error(
        `Error updating notification preference (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);

        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.updatePreference(
          type,
          preferences,
          retryCount + 1,
          maxRetries
        );
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to update notification preference after multiple attempts"
      );
    }
  },

  /**
   * Send a notification to a specific user (internal service calls)
   * @param {Object} notificationData - Notification data
   * @param {string} notificationData.receiverId - UUID of the receiver
   * @param {string} notificationData.content - Notification content
   * @param {string} notificationData.type - Notification type
   * @param {number} retryCount - Number of retry attempts (default: 0)
   * @param {number} maxRetries - Maximum number of retry attempts (default: 2)
   * @returns {Promise<Object>} Response containing sent notification
   */
  sendNotification: async (
    notificationData,
    retryCount = 0,
    maxRetries = 2
  ) => {
    try {
      const response = await notificationAPI.sendNotification(notificationData);
      return response;
    } catch (error) {
      console.error(
        `Error sending notification (attempt ${retryCount + 1}/${maxRetries + 1}):`,
        error
      );

      // Check if we should retry
      if (retryCount < maxRetries) {
        // Exponential backoff delay
        const delay = Math.min(1000 * Math.pow(2, retryCount), 5000);

        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Retry with incremented retry count
        return notificationService.sendNotification(
          notificationData,
          retryCount + 1,
          maxRetries
        );
      }

      // Max retries reached, throw error
      throw new Error(
        error.response?.data?.message ||
          "Failed to send notification after multiple attempts"
      );
    }
  },
};

export default notificationService;
