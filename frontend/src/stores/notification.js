import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { notificationService } from "../services/notificationService";
import websocketManager from "../utils/websocketManager";
import { useAuthStore } from "./auth";

export const useNotificationStore = defineStore("notification", () => {
  // State
  const notifications = ref([]);
  const unreadCount = ref(0);
  const preferences = ref([]);
  const isConnected = ref(false);
  const isLoading = ref(false);
  const error = ref(null);
  const currentPage = ref(0);
  const totalPages = ref(0);
  const hasMore = ref(true);
  const isLoadingMore = ref(false);

  // Computed
  const unreadNotifications = computed(() =>
    notifications.value.filter((n) => !n.read)
  );

  const readNotifications = computed(() =>
    notifications.value.filter((n) => n.read)
  );

  const hasUnreadNotifications = computed(() => unreadCount.value > 0);

  // Cache management
  const _cacheUnreadCount = () => {
    localStorage.setItem(
      "notification_unreadCount",
      unreadCount.value.toString()
    );
  };

  const _cacheNotifications = () => {
    try {
      // Only cache a limited number of notifications to avoid storage limits
      const notificationsToCache = notifications.value
        .slice(0, 20)
        .map((n) => ({
          id: n.id,
          content: n.content,
          read: n.read,
          createdAt: n.createdAt,
          type: n.type,
        }));
      localStorage.setItem(
        "notification_items",
        JSON.stringify(notificationsToCache)
      );
    } catch (err) {
      console.error("Error caching notifications:", err);
    }
  };

  const _cacheConnectionStatus = () => {
    localStorage.setItem(
      "notification_connectionStatus",
      JSON.stringify({
        isConnected: isConnected.value,
        timestamp: Date.now(),
      })
    );
  };

  const _loadCachedUnreadCount = () => {
    const cached = localStorage.getItem("notification_unreadCount");
    if (cached && !isNaN(parseInt(cached))) {
      unreadCount.value = parseInt(cached);
    }
  };

  const _loadCachedNotifications = () => {
    try {
      const cached = localStorage.getItem("notification_items");
      if (cached) {
        const parsedNotifications = JSON.parse(cached);
        if (
          Array.isArray(parsedNotifications) &&
          parsedNotifications.length > 0
        ) {
          notifications.value = parsedNotifications;
        }
      }
    } catch (err) {
      console.error("Error loading cached notifications:", err);
    }
  };

  const _loadCachedConnectionStatus = () => {
    try {
      const cached = localStorage.getItem("notification_connectionStatus");
      if (cached) {
        const status = JSON.parse(cached);
        // Only use cached connection status if it's recent (within last 5 minutes)
        const isRecent =
          status.timestamp && Date.now() - status.timestamp < 5 * 60 * 1000;
        console.log(
          "NotificationStore: Loading cached connection status:",
          status,
          "isRecent:",
          isRecent
        );
        if (isRecent) {
          isConnected.value = status.isConnected;
          console.log(
            "NotificationStore: Set isConnected from cache to:",
            status.isConnected
          );
        }
      }
    } catch (err) {
      console.error("Error loading cached connection status:", err);
    }
  };

  const _clearCache = () => {
    localStorage.removeItem("notification_unreadCount");
    localStorage.removeItem("notification_items");
    localStorage.removeItem("notification_connectionStatus");
    localStorage.removeItem("notification_preferences");
  };

  // WebSocket Management
  const connectWebSocket = async () => {
    const authStore = useAuthStore();

    if (!authStore.isLoggedIn || !authStore.token || !authStore.userInfo?.id) {
      console.warn("Cannot connect WebSocket: User not authenticated");
      return false;
    }

    try {
      websocketManager.setEventCallbacks({
        onConnectionChange: (connected) => {
          console.log(
            "NotificationStore: Connection status changed to:",
            connected
          );
          isConnected.value = connected;
          if (connected) {
            error.value = null;
            fetchUnreadCount();
          }
          _cacheConnectionStatus();
          _broadcastConnectionStatus(connected);
        },
        onError: (err) => {
          console.error("WebSocket error:", err);
          error.value = err.message;
          isConnected.value = false;
        },
      });

      // Connect to WebSocket
      await websocketManager.connect(authStore.token, authStore.userInfo.id);

      // Listen for notification events
      _setupNotificationListener();

      // Listen for storage events (cross-tab communication)
      _setupStorageListener();

      // Listen for WebSocket mark-as-read success events
      _setupWebSocketMarkReadListener();

      return true;
    } catch (err) {
      console.error("Failed to connect WebSocket:", err);
      error.value = err.message;
      isConnected.value = false;
      return false;
    }
  };

  const disconnectWebSocket = async () => {
    try {
      await websocketManager.disconnect();
      isConnected.value = false;
      return true;
    } catch (err) {
      console.error("Error disconnecting WebSocket:", err);
      return false;
    }
  };

  /**
   * Manual reconnect method that can be triggered by the user
   * Uses the WebSocketManager's manualReconnect method
   * @returns {Promise<boolean>} Success status
   */
  const manualReconnect = async () => {
    try {
      // Clear any existing error
      error.value = null;

      // Use the WebSocketManager's manualReconnect method
      if (websocketManager.manualReconnect) {
        return await websocketManager.manualReconnect();
      } else {
        // Fallback if manualReconnect is not available
        await disconnectWebSocket();
        return await connectWebSocket();
      }
    } catch (err) {
      console.error("Manual reconnection failed:", err);
      error.value = err.message || "Failed to reconnect";
      return false;
    }
  };

  const _setupNotificationListener = () => {
    // Listen for WebSocket notification events
    if (typeof window !== "undefined") {
      window.addEventListener("websocket-notification", (event) => {
        const notification = event.detail;

        // Filter out connection confirmation messages
        if (
          notification.content ===
          "Successfully connected to notification service"
        ) {
          return;
        }

        // Normalize createdAt to a valid ISO timestamp
        try {
          let rawTs = notification.createdAt;
          if (rawTs === null || rawTs === undefined || rawTs === "") {
            notification.createdAt = new Date().toISOString();
          } else if (typeof rawTs === "number") {
            // If seconds precision, convert to ms
            const ms = rawTs < 1e12 ? rawTs * 1000 : rawTs;
            const d = new Date(ms);
            notification.createdAt = isNaN(d.getTime())
              ? new Date().toISOString()
              : d.toISOString();
          } else if (typeof rawTs === "string") {
            const d = new Date(rawTs);
            notification.createdAt = isNaN(d.getTime())
              ? new Date().toISOString()
              : d.toISOString();
          }
        } catch {
          notification.createdAt = new Date().toISOString();
        }

        // Add notification to the beginning of the list
        addNotification(notification);

        // Increment unread count
        unreadCount.value++;
        _cacheUnreadCount();

        // Broadcast notification to other tabs
        _broadcastNotification(notification);

        // Emit custom event for notification banner
        const bannerEvent = new CustomEvent("new-notification", {
          detail: notification,
        });
        window.dispatchEvent(bannerEvent);
      });
    }
  };

  // Listen for WebSocket mark-as-read success events
  const _setupWebSocketMarkReadListener = () => {
    if (typeof window !== "undefined") {
      window.addEventListener("websocket-mark-read-success", (event) => {
        const { notificationId } = event.detail;

        // Update local notification state immediately
        const notification = notifications.value.find(
          (n) => n.id === notificationId
        );
        if (notification && !notification.read) {
          notification.read = true;
          unreadCount.value = Math.max(0, unreadCount.value - 1);
          _cacheUnreadCount();
          _cacheNotifications();

          // Broadcast to other tabs
          _broadcastMarkAsRead(notificationId);
        }
      });
    }
  };

  // Cross-tab communication
  const _setupStorageListener = () => {
    if (typeof window !== "undefined") {
      window.addEventListener("storage", (event) => {
        // Handle unread count sync
        if (event.key === "notification_unreadCount") {
          const newCount = parseInt(event.newValue);
          if (!isNaN(newCount) && newCount !== unreadCount.value) {
            unreadCount.value = newCount;
          }
        }

        // Handle new notification from other tab
        else if (event.key === "notification_new") {
          try {
            const notification = JSON.parse(event.newValue);
            if (notification && notification.id) {
              // Check if we already have this notification
              const exists = notifications.value.some(
                (n) => n.id === notification.id
              );
              if (!exists) {
                addNotification(notification);
                // Note: Don't emit banner event here - this is for cross-tab sync only
                // The original tab that received the WebSocket notification will show the banner
              }
            }
          } catch (err) {
            console.error("Error processing cross-tab notification:", err);
          }
        }

        // Handle mark as read from other tab
        else if (event.key === "notification_markAsRead") {
          try {
            const data = JSON.parse(event.newValue);
            if (data && data.id) {
              const notification = notifications.value.find(
                (n) => n.id === data.id
              );
              if (notification && !notification.read) {
                notification.read = true;
                // Update unread count if needed
                if (unreadCount.value > 0) {
                  unreadCount.value--;
                  _cacheUnreadCount();
                }
              }
            }
          } catch (err) {
            console.error("Error processing cross-tab mark as read:", err);
          }
        }

        // Handle mark all as read from other tab
        else if (event.key === "notification_markAllAsRead") {
          notifications.value.forEach((notification) => {
            notification.read = true;
          });
          unreadCount.value = 0;
          _cacheUnreadCount();
        }

        // Handle connection status from other tab
        else if (event.key === "notification_connectionStatus") {
          try {
            const status = JSON.parse(event.newValue);
            if (status && typeof status.connected === "boolean") {
              isConnected.value = status.connected;
            }
          } catch (err) {
            console.error("Error processing cross-tab connection status:", err);
          }
        }
      });
    }
  };

  // Broadcast functions for cross-tab communication
  const _broadcastNotification = (notification) => {
    if (typeof window !== "undefined" && notification) {
      try {
        localStorage.setItem("notification_new", JSON.stringify(notification));
        // Remove it after a short delay to ensure it triggers storage events
        setTimeout(() => {
          localStorage.removeItem("notification_new");
        }, 100);
      } catch (err) {
        console.error("Error broadcasting notification to other tabs:", err);
      }
    }
  };

  const _broadcastMarkAsRead = (notificationId) => {
    if (typeof window !== "undefined" && notificationId) {
      try {
        localStorage.setItem(
          "notification_markAsRead",
          JSON.stringify({ id: notificationId })
        );
        // Remove it after a short delay
        setTimeout(() => {
          localStorage.removeItem("notification_markAsRead");
        }, 100);
      } catch (err) {
        console.error("Error broadcasting mark as read to other tabs:", err);
      }
    }
  };

  const _broadcastMarkAllAsRead = () => {
    if (typeof window !== "undefined") {
      try {
        localStorage.setItem(
          "notification_markAllAsRead",
          Date.now().toString()
        );
        // Remove it after a short delay
        setTimeout(() => {
          localStorage.removeItem("notification_markAllAsRead");
        }, 100);
      } catch (err) {
        console.error(
          "Error broadcasting mark all as read to other tabs:",
          err
        );
      }
    }
  };

  const _broadcastConnectionStatus = (connected) => {
    if (typeof window !== "undefined") {
      try {
        localStorage.setItem(
          "notification_connectionStatus",
          JSON.stringify({ connected, timestamp: Date.now() })
        );
      } catch (err) {
        console.error(
          "Error broadcasting connection status to other tabs:",
          err
        );
      }
    }
  };

  // API Actions
  const fetchNotifications = async (
    page = 0,
    size = 10,
    unreadOnly = false,
    append = false
  ) => {
    if (!append) {
      isLoading.value = true;
    } else {
      isLoadingMore.value = true;
    }

    error.value = null;

    try {
      const response = await notificationService.getNotifications(
        page,
        size,
        unreadOnly
      );

      // Handle ApiResponse structure: response.data contains the actual data
      if (response && response.data) {
        const newNotifications = response.data.notifications || [];

        if (append) {
          // Append to existing notifications for pagination
          notifications.value = [...notifications.value, ...newNotifications];
        } else {
          // Replace notifications for fresh load
          notifications.value = newNotifications;
        }

        currentPage.value = response.data.currentPage || page;
        totalPages.value = response.data.totalPages || 0;
        hasMore.value = currentPage.value < totalPages.value - 1;

        return { success: true, data: response.data };
      } else {
        throw new Error(
          response?.error?.message || "Failed to fetch notifications"
        );
      }
    } catch (err) {
      console.error("Error fetching notifications:", err);
      error.value = err.message;
      return { success: false, message: err.message };
    } finally {
      isLoading.value = false;
      isLoadingMore.value = false;
    }
  };

  const fetchMoreNotifications = async (unreadOnly = false) => {
    if (!hasMore.value || isLoadingMore.value) {
      return;
    }

    const nextPage = currentPage.value + 1;
    return await fetchNotifications(nextPage, 10, unreadOnly, true);
  };

  const fetchUnreadCount = async () => {
    try {
      const response = await notificationService.getUnreadCount();

      // Handle ApiResponse structure: response.data contains the actual data
      if (response && response.data !== undefined) {
        const newCount = response.data.unreadCount || 0;
        unreadCount.value = newCount;
        _cacheUnreadCount();
        return { success: true, data: newCount };
      } else {
        throw new Error(
          response?.error?.message || "Failed to fetch unread count"
        );
      }
    } catch (err) {
      console.error("Error fetching unread count:", err);
      error.value = err.message;
      return { success: false, message: err.message };
    }
  };

  const markAsRead = async (notificationId) => {
    try {
      const response = await notificationService.markAsRead(notificationId);

      // Handle ApiResponse structure: response contains the data directly
      if (response) {
        // Update local notification state
        const notification = notifications.value.find(
          (n) => n.id === notificationId
        );
        if (notification && !notification.read) {
          notification.read = true;
          unreadCount.value = Math.max(0, unreadCount.value - 1);
          _cacheUnreadCount();
          _cacheNotifications(); // Cache updated notification state

          // Broadcast to other tabs
          _broadcastMarkAsRead(notificationId);
        }

        // Also send via WebSocket for real-time sync
        if (isConnected.value) {
          try {
            websocketManager.markNotificationAsRead(notificationId);
          } catch (wsError) {
            console.warn("Failed to send mark-as-read via WebSocket:", wsError);
          }
        }

        return { success: true, data: response.data };
      } else {
        throw new Error(
          response?.error?.message || "Failed to mark notification as read"
        );
      }
    } catch (err) {
      console.error("Error marking notification as read:", err);
      error.value = err.message;
      return { success: false, message: err.message };
    }
  };

  const markAllAsRead = async () => {
    try {
      const response = await notificationService.markAllAsRead();

      // Handle ApiResponse structure: response contains the data directly
      if (response) {
        // Update local state
        notifications.value.forEach((notification) => {
          notification.read = true;
        });
        unreadCount.value = 0;
        _cacheUnreadCount();
        _cacheNotifications(); // Cache updated notification state

        // Broadcast to other tabs
        _broadcastMarkAllAsRead();

        return { success: true, data: response.data };
      } else {
        throw new Error(
          response?.error?.message || "Failed to mark all notifications as read"
        );
      }
    } catch (err) {
      console.error("Error marking all notifications as read:", err);
      error.value = err.message;
      return { success: false, message: err.message };
    }
  };

  const fetchPreferences = async () => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await notificationService.getPreferences();

      // Handle ApiResponse structure: response.data contains the actual data
      if (response && response.data) {
        preferences.value = response.data.preferences || [];

        // Cache preferences
        try {
          localStorage.setItem(
            "notification_preferences",
            JSON.stringify(preferences.value)
          );
        } catch (cacheError) {
          console.error("Error caching notification preferences:", cacheError);
        }

        return { success: true, data: preferences.value };
      } else {
        throw new Error(
          response?.error?.message || "Failed to fetch notification preferences"
        );
      }
    } catch (err) {
      console.error("Error fetching notification preferences:", err);
      error.value = err.message;

      // Try to load cached preferences if API call fails
      try {
        const cachedPreferences = localStorage.getItem(
          "notification_preferences"
        );
        if (cachedPreferences) {
          const parsedPreferences = JSON.parse(cachedPreferences);
          if (
            Array.isArray(parsedPreferences) &&
            parsedPreferences.length > 0
          ) {
            preferences.value = parsedPreferences;
          }
        }
      } catch (cacheError) {
        console.error("Error loading cached preferences:", cacheError);
      }

      return { success: false, message: err.message };
    } finally {
      isLoading.value = false;
    }
  };

  const updatePreference = async (type, preferenceData) => {
    try {
      const response = await notificationService.updatePreference(
        type,
        preferenceData
      );

      // Handle ApiResponse structure: response.data contains the actual preference data
      if (response && response.data) {
        // Update local preferences with the response data
        const index = preferences.value.findIndex((p) => p.type === type);
        if (index !== -1) {
          // Update existing preference with data from backend response
          preferences.value[index] = {
            type: response.data.type,
            displayName: response.data.displayName,
            emailEnabled: response.data.emailEnabled,
            webPushEnabled: response.data.webPushEnabled,
          };
        } else {
          // Add new preference if it doesn't exist (shouldn't happen with existing backend)
          preferences.value.push({
            type: response.data.type,
            displayName: response.data.displayName,
            emailEnabled: response.data.emailEnabled,
            webPushEnabled: response.data.webPushEnabled,
          });
        }

        // Cache updated preferences
        try {
          localStorage.setItem(
            "notification_preferences",
            JSON.stringify(preferences.value)
          );
        } catch (cacheError) {
          console.error(
            "Error caching updated notification preferences:",
            cacheError
          );
        }

        return { success: true, data: response.data };
      } else {
        throw new Error(
          response?.error?.message || "Failed to update notification preference"
        );
      }
    } catch (err) {
      console.error("Error updating notification preference:", err);
      error.value = err.message;
      return { success: false, message: err.message };
    }
  };

  // Local state management
  const addNotification = (notification) => {
    // Check if the notification already exists to prevent duplicates
    const exists = notifications.value.some((n) => n.id === notification.id);
    if (exists) {
      console.warn(
        `NotificationStore: Notification with ID ${notification.id} already exists. Ignoring.`
      );
      return;
    }

    // Add to beginning of notifications array
    notifications.value.unshift(notification);

    // Limit the number of cached notifications to prevent memory issues
    if (notifications.value.length > 100) {
      notifications.value = notifications.value.slice(0, 100);
    }

    // Cache updated notifications
    _cacheNotifications();
  };

  const removeNotification = (notificationId) => {
    const index = notifications.value.findIndex((n) => n.id === notificationId);
    if (index !== -1) {
      const notification = notifications.value[index];
      notifications.value.splice(index, 1);

      // Update unread count if notification was unread
      if (!notification.read) {
        unreadCount.value = Math.max(0, unreadCount.value - 1);
        _cacheUnreadCount();
      }
    }
  };

  const clearNotifications = () => {
    notifications.value = [];
    unreadCount.value = 0;
    currentPage.value = 0;
    totalPages.value = 0;
    hasMore.value = true;
    _clearCache();
  };

  const clearError = () => {
    error.value = null;
  };

  // Initialize store
  const initialize = async () => {
    // Load cached data
    _loadCachedUnreadCount();
    _loadCachedNotifications();
    _loadCachedConnectionStatus();

    // Connect WebSocket if user is authenticated
    const authStore = useAuthStore();
    if (authStore.isLoggedIn) {
      try {
        await connectWebSocket();

        // Fetch initial data
        try {
          await Promise.all([
            fetchUnreadCount(),
            fetchNotifications(0, 10, false),
            fetchPreferences(),
          ]);
        } catch (dataError) {
          console.error("Error fetching initial notification data:", dataError);
          error.value = dataError.message || "Failed to load notification data";

          // Even if data fetch fails, we can still use WebSocket for real-time updates
          // So we don't throw the error further
        }
      } catch (wsError) {
        console.error("Failed to initialize WebSocket connection:", wsError);
        error.value =
          wsError.message || "Failed to connect to notification service";
        isConnected.value = false;

        // Schedule a retry after a delay
        setTimeout(async () => {
          if (!isConnected.value) {
            try {
              await connectWebSocket();
            } catch (retryError) {
              console.error("WebSocket reconnection failed:", retryError);
            }
          }
        }, 5000);
      }
    }
  };

  // Cleanup on logout
  const cleanup = async () => {
    await disconnectWebSocket();
    clearNotifications();
    preferences.value = [];
    error.value = null;
    isLoading.value = false;
    isConnected.value = false;

    // Clear all cached notification data
    _clearCache();

    // Also clear any temporary storage items used for cross-tab communication
    localStorage.removeItem("notification_new");
    localStorage.removeItem("notification_markAsRead");
    localStorage.removeItem("notification_markAllAsRead");
  };

  // Load cached data on store creation
  _loadCachedUnreadCount();

  return {
    // State
    notifications,
    unreadCount,
    preferences,
    isConnected,
    isLoading,
    isLoadingMore,
    error,
    currentPage,
    totalPages,
    hasMore,

    // Computed
    unreadNotifications,
    readNotifications,
    hasUnreadNotifications,

    // WebSocket actions
    connectWebSocket,
    disconnectWebSocket,
    manualReconnect,

    // API actions
    fetchNotifications,
    fetchMoreNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    fetchPreferences,
    updatePreference,

    // Local state management
    addNotification,
    removeNotification,
    clearNotifications,
    clearError,

    // Lifecycle
    initialize,
    cleanup,
  };
});
