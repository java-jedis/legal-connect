import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { chatService } from "../services/chatService";
import { useAuthStore } from "./auth";

// Dynamic import helper for chatWebSocketManager
let chatWebSocketManagerInstance = null;
const getChatWebSocketManager = async () => {
  if (!chatWebSocketManagerInstance) {
    try {
      const module = await import("../utils/chatWebSocketManager");
      chatWebSocketManagerInstance = module.default;
    } catch (error) {
      console.error("Failed to import chatWebSocketManager:", error);
      // Create a fallback object to prevent runtime errors
      chatWebSocketManagerInstance = {
        initialize: () => Promise.resolve(false),
        connect: () => Promise.resolve(false),
        disconnect: () => Promise.resolve(false),
        manualReconnect: () => Promise.resolve(false),
        sendMessage: () => false,
        markMessageAsRead: () => false,
        markConversationAsRead: () => false,
        cleanup: () => {},
      };
    }
  }
  return chatWebSocketManagerInstance;
};

export const useChatStore = defineStore("chat", () => {
  // State
  const conversations = ref([]);
  const currentConversation = ref(null);
  const messages = ref({}); // Keyed by conversationId

  // UI State
  const isLoading = ref(false);
  const isLoadingMessages = ref(false);
  const isSendingMessage = ref(false);
  const isLoadingMoreMessages = ref(false);

  // WebSocket
  const isConnected = ref(false);

  // Pagination
  const conversationPagination = ref({
    currentPage: 0,
    totalPages: 0,
    hasMore: true,
  });

  const messagePagination = ref({}); // Keyed by conversationId

  // Unread counts
  const totalUnreadCount = ref(0);
  const unreadCounts = ref({}); // Per conversation

  // Error handling
  const error = ref(null);
  const lastError = ref(null);
  const errorRetryCount = ref(0);
  const maxRetryAttempts = ref(3);
  const isRetrying = ref(false);
  const networkStatus = ref("online"); // 'online', 'offline', 'slow'

  // Computed properties
  const hasUnreadMessages = computed(() => totalUnreadCount.value > 0);

  const sortedConversations = computed(() => {
    return [...conversations.value].sort((a, b) => {
      const timeA = new Date(a.latestMessage?.createdAt || a.updatedAt);
      const timeB = new Date(b.latestMessage?.createdAt || b.updatedAt);
      return timeB - timeA; // Most recent first
    });
  });

  const unreadConversations = computed(() => {
    return conversations.value.filter(
      (conv) => unreadCounts.value[conv.id] && unreadCounts.value[conv.id] > 0
    );
  });

  const getConversationMessages = computed(() => {
    return (conversationId) => {
      const conversationMessages = messages.value[conversationId] || [];
      return conversationMessages.sort(
        (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
      );
    };
  });

  const getConversationUnreadCount = computed(() => {
    return (conversationId) => unreadCounts.value[conversationId] || 0;
  });

  // Cache management
  const _cacheTotalUnreadCount = () => {
    localStorage.setItem(
      "chat_totalUnreadCount",
      totalUnreadCount.value.toString()
    );
  };

  const _cacheConversations = () => {
    try {
      // Only cache a limited number of conversations to avoid storage limits
      const conversationsToCache = conversations.value
        .slice(0, 50)
        .map((conv) => ({
          id: conv.id,
          participantOneId: conv.participantOneId,
          participantTwoId: conv.participantTwoId,
          participantTwoName: conv.participantTwoName,
          latestMessage: conv.latestMessage,
          unreadCount: conv.unreadCount,
          updatedAt: conv.updatedAt,
        }));
      localStorage.setItem(
        "chat_conversations",
        JSON.stringify(conversationsToCache)
      );
    } catch (err) {
      console.error("Error caching conversations:", err);
    }
  };

  const _cacheUnreadCounts = () => {
    try {
      localStorage.setItem(
        "chat_unreadCounts",
        JSON.stringify(unreadCounts.value)
      );
    } catch (err) {
      console.error("Error caching unread counts:", err);
    }
  };

  const _cacheConnectionStatus = () => {
    localStorage.setItem(
      "chat_connectionStatus",
      JSON.stringify({
        isConnected: isConnected.value,
        timestamp: Date.now(),
      })
    );
  };

  const _loadCachedTotalUnreadCount = () => {
    const cached = localStorage.getItem("chat_totalUnreadCount");
    if (cached && !isNaN(parseInt(cached))) {
      totalUnreadCount.value = parseInt(cached);
    }
  };

  const _loadCachedConversations = () => {
    try {
      const cached = localStorage.getItem("chat_conversations");
      if (cached) {
        const parsedConversations = JSON.parse(cached);
        if (
          Array.isArray(parsedConversations) &&
          parsedConversations.length > 0
        ) {
          conversations.value = parsedConversations;
        }
      }
    } catch (err) {
      console.error("Error loading cached conversations:", err);
    }
  };

  const _loadCachedUnreadCounts = () => {
    try {
      const cached = localStorage.getItem("chat_unreadCounts");
      if (cached) {
        const parsedCounts = JSON.parse(cached);
        if (parsedCounts && typeof parsedCounts === "object") {
          unreadCounts.value = parsedCounts;
        }
      }
    } catch (err) {
      console.error("Error loading cached unread counts:", err);
    }
  };

  const _loadCachedConnectionStatus = () => {
    try {
      const cached = localStorage.getItem("chat_connectionStatus");
      if (cached) {
        const status = JSON.parse(cached);
        // Only use cached connection status if it's recent (within last 5 minutes)
        const isRecent =
          status.timestamp && Date.now() - status.timestamp < 5 * 60 * 1000;
        if (isRecent) {
          isConnected.value = status.isConnected;
        }
      }
    } catch (err) {
      console.error("Error loading cached connection status:", err);
    }
  };

  const _clearCache = () => {
    localStorage.removeItem("chat_totalUnreadCount");
    localStorage.removeItem("chat_conversations");
    localStorage.removeItem("chat_unreadCounts");
    localStorage.removeItem("chat_connectionStatus");
  };

  // Authentication integration
  const checkAuthentication = () => {
    const authStore = useAuthStore();

    if (!authStore.isLoggedIn || !authStore.token || !authStore.userInfo?.id) {
      console.warn("Chat operation requires authentication");
      setError(
        "Authentication required. Please log in to access chat features."
      );
      return false;
    }

    // Check if token is expired or invalid
    try {
      const tokenPayload = JSON.parse(atob(authStore.token.split(".")[1]));
      const currentTime = Math.floor(Date.now() / 1000);

      if (tokenPayload.exp && tokenPayload.exp < currentTime) {
        console.warn("Authentication token has expired");
        setError("Your session has expired. Please log in again.");
        return false;
      }
    } catch {
      console.warn("Invalid authentication token format");
      setError("Invalid authentication. Please log in again.");
      return false;
    }

    return true;
  };

  // Validate message sending permissions
  const checkMessageSendPermission = (receiverId) => {
    if (!checkAuthentication()) {
      return false;
    }

    const authStore = useAuthStore();
    const currentUserId = authStore.userInfo?.id;

    if (!receiverId || receiverId === currentUserId) {
      console.warn("Invalid receiver ID or attempting to send message to self");
      setError("Invalid message recipient.");
      return false;
    }

    return true;
  };

  // WebSocket Management
  const connectWebSocket = async () => {
    if (!checkAuthentication()) {
      return false;
    }

    const authStore = useAuthStore();

    try {
      // Get chat WebSocket manager instance
      const wsManager = await getChatWebSocketManager();

      // Initialize chat WebSocket manager with callbacks
      await wsManager.initialize({
        onMessageReceived: (message) => {
          // Add message to the appropriate conversation
          addMessage(message.conversationId, message);

          // Update conversation's last message info
          updateConversationLastMessage(message.conversationId, message);

          // Update unread count if message is from another user
          if (message.senderId !== authStore.userInfo?.id) {
            incrementUnreadCount(message.conversationId);
          }

          // Broadcast to other tabs
          _broadcastNewMessage(message);
        },
        onReadStatusUpdate: (readStatusData) => {
          // Update message read status
          if (readStatusData.messageId) {
            // Find conversation containing this message
            for (const [conversationId, conversationMessages] of Object.entries(
              messages.value
            )) {
              const message = conversationMessages.find(
                (m) => m.id === readStatusData.messageId
              );
              if (message) {
                updateMessageReadStatus(
                  conversationId,
                  readStatusData.messageId,
                  readStatusData.isRead
                );
                _broadcastMessageRead(conversationId, readStatusData.messageId);
                break;
              }
            }
          }
        },
        onUnreadCountUpdate: (unreadData) => {
          // Update total unread count
          if (typeof unreadData.totalUnreadCount === "number") {
            totalUnreadCount.value = unreadData.totalUnreadCount;
            _cacheTotalUnreadCount();
          }
        },
        onConnectionChange: (connected) => {
          isConnected.value = connected;
          _cacheConnectionStatus();
          _broadcastConnectionStatus(connected);

          if (connected) {
            error.value = null;
            // Refresh chat data after reconnection
            fetchUnreadCount();
          }
        },
        onError: (err) => {
          console.error("Chat WebSocket error:", err);
          error.value = err.message;
          isConnected.value = false;
        },
      });

      // Connect to WebSocket
      const success = await wsManager.connect(
        authStore.token,
        authStore.userInfo.id
      );

      if (success) {
        // Set up additional event listeners
        _setupChatListener();
        _setupStorageListener();
      }

      return success;
    } catch (err) {
      console.error("Failed to connect chat WebSocket:", err);
      error.value = err.message;
      isConnected.value = false;
      return false;
    }
  };

  const disconnectWebSocket = async () => {
    try {
      const wsManager = await getChatWebSocketManager();
      await wsManager.disconnect();
      isConnected.value = false;
      return true;
    } catch (err) {
      console.error("Error disconnecting chat WebSocket:", err);
      return false;
    }
  };

  const manualReconnect = async () => {
    try {
      error.value = null;
      const wsManager = await getChatWebSocketManager();
      return await wsManager.manualReconnect();
    } catch (err) {
      console.error("Manual reconnection failed:", err);
      error.value = err.message || "Failed to reconnect";
      return false;
    }
  };

  const _setupChatListener = () => {
    if (typeof window !== "undefined") {
      // Listen for new messages via WebSocket
      window.addEventListener("websocket-message", (event) => {
        const message = event.detail;
        console.log("Real-time message received:", message);

        // Add message to the appropriate conversation
        addMessage(message.conversationId, message);

        // Update conversation's last message info
        updateConversationLastMessage(message.conversationId, message);

        // Update unread count if message is from another user
        const authStore = useAuthStore();
        if (message.senderId !== authStore.userInfo?.id) {
          incrementUnreadCount(message.conversationId);

          // Update total unread count in real-time
          totalUnreadCount.value = totalUnreadCount.value + 1;
          _cacheTotalUnreadCount();
        }

        // Broadcast to other tabs
        _broadcastNewMessage(message);
      });

      // Listen for message read status updates
      window.addEventListener("websocket-message-read", (event) => {
        const { conversationId, messageId } = event.detail;
        console.log("Real-time message read status update:", {
          conversationId,
          messageId,
        });

        // Update message read status
        updateMessageReadStatus(conversationId, messageId, true);

        // Broadcast to other tabs
        _broadcastMessageRead(conversationId, messageId);
      });

      // Listen for read status updates (new event from enhanced WebSocket manager)
      window.addEventListener("websocket-read-status-update", (event) => {
        const { messageId, isRead, conversationId } = event.detail;
        console.log("Real-time read status update:", {
          messageId,
          isRead,
          conversationId,
        });

        // Update message read status in the appropriate conversation
        if (conversationId) {
          updateMessageReadStatus(conversationId, messageId, isRead);
        } else {
          // If conversationId is not provided, search for the message across all conversations
          for (const [convId, conversationMessages] of Object.entries(
            messages.value
          )) {
            const message = conversationMessages.find(
              (m) => m.id === messageId
            );
            if (message) {
              updateMessageReadStatus(convId, messageId, isRead);
              break;
            }
          }
        }

        // Broadcast to other tabs
        _broadcastMessageRead(conversationId || "unknown", messageId);
      });

      // Listen for conversation read status updates
      window.addEventListener("websocket-conversation-read", (event) => {
        const { conversationId } = event.detail;
        console.log("Real-time conversation read status update:", {
          conversationId,
        });

        // Mark all messages in conversation as read
        markConversationMessagesAsRead(conversationId);

        // Reset unread count for conversation
        setConversationUnreadCount(conversationId, 0);

        // Broadcast to other tabs
        _broadcastConversationRead(conversationId);
      });

      // Listen for unread count updates via WebSocket
      window.addEventListener("websocket-unread-count-update", (event) => {
        const unreadData = event.detail;
        console.log("Real-time unread count update:", unreadData);

        if (typeof unreadData.totalUnreadCount === "number") {
          totalUnreadCount.value = unreadData.totalUnreadCount;
          _cacheTotalUnreadCount();
        }

        // Update per-conversation unread counts if provided
        if (
          unreadData.conversationUnreadCounts &&
          typeof unreadData.conversationUnreadCounts === "object"
        ) {
          Object.assign(
            unreadCounts.value,
            unreadData.conversationUnreadCounts
          );
          _cacheUnreadCounts();
        }
      });

      // Listen for WebSocket reconnection events
      window.addEventListener("websocket-reconnection-status", (event) => {
        const status = event.detail;
        console.log("WebSocket reconnection status:", status);

        if (status.success && !status.reconnecting) {
          // Successful reconnection - sync data
          console.log(
            "WebSocket reconnected successfully, syncing chat data..."
          );
          _syncAfterReconnection();
        }
      });

      // Listen for mark-as-read acknowledgments
      window.addEventListener("websocket-mark-read-success", (event) => {
        const { messageId } = event.detail;
        console.log(
          "Mark-as-read acknowledgment received for message:",
          messageId
        );

        // The message should already be marked as read locally, but we can confirm it here
        for (const [conversationId, conversationMessages] of Object.entries(
          messages.value
        )) {
          const message = conversationMessages.find((m) => m.id === messageId);
          if (message && !message.isRead) {
            console.log(
              `Confirming read status for message ${messageId} in conversation ${conversationId}`
            );
            updateMessageReadStatus(conversationId, messageId, true);
            break;
          }
        }
      });
    }
  };

  // Cross-tab communication
  const _setupStorageListener = () => {
    if (typeof window !== "undefined") {
      window.addEventListener("storage", (event) => {
        // Handle total unread count sync
        if (event.key === "chat_totalUnreadCount") {
          const newCount = parseInt(event.newValue);
          if (!isNaN(newCount) && newCount !== totalUnreadCount.value) {
            totalUnreadCount.value = newCount;
          }
        }

        // Handle new message from other tab
        else if (event.key === "chat_newMessage") {
          try {
            const message = JSON.parse(event.newValue);
            if (message && message.id) {
              addMessage(message.conversationId, message);
              updateConversationLastMessage(message.conversationId, message);
            }
          } catch (err) {
            console.error("Error processing cross-tab new message:", err);
          }
        }

        // Handle message read from other tab
        else if (event.key === "chat_messageRead") {
          try {
            const data = JSON.parse(event.newValue);
            if (data && data.conversationId && data.messageId) {
              updateMessageReadStatus(
                data.conversationId,
                data.messageId,
                true
              );

              // Emit event for components to react to cross-tab read status updates
              if (typeof window !== "undefined") {
                const crossTabEvent = new CustomEvent(
                  "cross-tab-message-read",
                  {
                    detail: {
                      conversationId: data.conversationId,
                      messageId: data.messageId,
                      timestamp: Date.now(),
                    },
                  }
                );
                window.dispatchEvent(crossTabEvent);
              }
            }
          } catch (err) {
            console.error("Error processing cross-tab message read:", err);
          }
        }

        // Handle conversation read from other tab
        else if (event.key === "chat_conversationRead") {
          try {
            const data = JSON.parse(event.newValue);
            if (data && data.conversationId) {
              markConversationMessagesAsRead(data.conversationId);
              setConversationUnreadCount(data.conversationId, 0);
            }
          } catch (err) {
            console.error("Error processing cross-tab conversation read:", err);
          }
        }

        // Handle connection status from other tab
        else if (event.key === "chat_connectionStatus") {
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
  const _broadcastNewMessage = (message) => {
    if (typeof window !== "undefined" && message) {
      try {
        localStorage.setItem("chat_newMessage", JSON.stringify(message));
        setTimeout(() => {
          localStorage.removeItem("chat_newMessage");
        }, 100);
      } catch (err) {
        console.error("Error broadcasting new message to other tabs:", err);
      }
    }
  };

  const _broadcastMessageRead = (conversationId, messageId) => {
    if (typeof window !== "undefined" && conversationId && messageId) {
      try {
        localStorage.setItem(
          "chat_messageRead",
          JSON.stringify({ conversationId, messageId })
        );
        setTimeout(() => {
          localStorage.removeItem("chat_messageRead");
        }, 100);
      } catch (err) {
        console.error("Error broadcasting message read to other tabs:", err);
      }
    }
  };

  const _broadcastConversationRead = (conversationId) => {
    if (typeof window !== "undefined" && conversationId) {
      try {
        localStorage.setItem(
          "chat_conversationRead",
          JSON.stringify({ conversationId })
        );
        setTimeout(() => {
          localStorage.removeItem("chat_conversationRead");
        }, 100);
      } catch (err) {
        console.error(
          "Error broadcasting conversation read to other tabs:",
          err
        );
      }
    }
  };

  const _broadcastConnectionStatus = (connected) => {
    if (typeof window !== "undefined") {
      try {
        localStorage.setItem(
          "chat_connectionStatus",
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

  // Sync data after WebSocket reconnection
  const _syncAfterReconnection = async () => {
    try {
      console.log("Syncing chat data after WebSocket reconnection...");

      // Fetch latest unread count
      await fetchUnreadCount();

      // Refresh conversations to get latest data
      await fetchConversations();

      // If we have a current conversation, refresh its messages
      if (currentConversation.value) {
        await fetchConversationMessages(currentConversation.value.id);
      }

      console.log("Chat data sync completed successfully");
    } catch (error) {
      console.error("Error syncing chat data after reconnection:", error);
      error.value = error.message || "Failed to sync chat data";
    }
  };

  // API Actions - Enhanced with retry logic and better error handling
  const fetchConversations = async () => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }
    const result = await fetchConversationsWithRetry();
    return result;
  };

  const fetchConversationMessages = async (
    conversationId,
    page = 0,
    size = 20,
    append = false
  ) => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }

    const result = await fetchConversationMessagesWithRetry(
      conversationId,
      page,
      size,
      append
    );
    return result;
  };

  const fetchMoreMessages = async (conversationId) => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }

    const pagination = messagePagination.value[conversationId];
    if (!pagination || !pagination.hasMore || isLoadingMoreMessages.value) {
      return { success: false, message: "No more messages to load" };
    }

    const nextPage = pagination.currentPage + 1;
    return await fetchConversationMessagesWithRetry(
      conversationId,
      nextPage,
      20,
      true
    );
  };

  const sendMessage = async (receiverId, content) => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }

    if (!checkMessageSendPermission(receiverId)) {
      return { success: false, message: "Invalid message recipient" };
    }

    const result = await sendMessageWithRetry(receiverId, content);
    return result;
  };

  const markConversationAsRead = async (conversationId) => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }

    try {
      const response = await chatService.markConversationAsRead(conversationId);

      if (response) {
        // Update local state
        markConversationMessagesAsRead(conversationId);
        setConversationUnreadCount(conversationId, 0);

        // Send via WebSocket for real-time sync
        if (isConnected.value) {
          try {
            const wsManager = await getChatWebSocketManager();
            wsManager.markConversationAsRead(conversationId);
          } catch (wsError) {
            console.warn("Failed to send mark-as-read via WebSocket:", wsError);
          }
        }

        return { success: true, data: response.data };
      } else {
        throw new Error(
          response?.error?.message || "Failed to mark conversation as read"
        );
      }
    } catch (err) {
      console.error("Error marking conversation as read:", err);

      // Handle authentication errors specifically
      if (err.response?.status === 401) {
        setError("Your session has expired. Please log in again.");
        return {
          success: false,
          message: "Authentication expired",
          requiresLogin: true,
        };
      }

      error.value = err.message;
      return { success: false, message: err.message };
    }
  };

  const markMessageAsRead = async (messageId) => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }

    try {
      // Send via WebSocket for real-time sync
      if (isConnected.value) {
        const wsManager = await getChatWebSocketManager();
        wsManager.markMessageAsRead(messageId);

        // Also update local state immediately for better UX
        // Find the message and mark it as read locally
        for (const [conversationId, conversationMessages] of Object.entries(
          messages.value
        )) {
          const message = conversationMessages.find((m) => m.id === messageId);
          if (message && !message.isRead) {
            message.isRead = true;
            console.log(
              `Locally marked message ${messageId} as read in conversation ${conversationId}`
            );
            break;
          }
        }

        return { success: true };
      } else {
        throw new Error("WebSocket not connected");
      }
    } catch (err) {
      console.error("Error marking message as read:", err);

      // Handle authentication errors specifically
      if (err.response?.status === 401) {
        setError("Your session has expired. Please log in again.");
        return {
          success: false,
          message: "Authentication expired",
          requiresLogin: true,
        };
      }

      return { success: false, message: err.message };
    }
  };

  const fetchUnreadCount = async () => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }

    try {
      const response = await chatService.getUnreadCount();

      if (response && response.data !== undefined) {
        const newCount = response.data.unreadCount || 0;
        totalUnreadCount.value = newCount;
        _cacheTotalUnreadCount();
        return { success: true, data: newCount };
      } else {
        throw new Error(
          response?.error?.message || "Failed to fetch unread count"
        );
      }
    } catch (err) {
      console.error("Error fetching unread count:", err);

      // Handle authentication errors specifically
      if (err.response?.status === 401) {
        setError("Your session has expired. Please log in again.");
        return {
          success: false,
          message: "Authentication expired",
          requiresLogin: true,
        };
      }

      error.value = err.message;
      return { success: false, message: err.message };
    }
  };

  const startConversation = async (receiverId, initialMessage) => {
    if (!checkAuthentication()) {
      return { success: false, message: "Authentication required" };
    }
    return await sendMessage(receiverId, initialMessage);
  };

  // Local state management
  const addMessage = (conversationId, message) => {
    if (!messages.value[conversationId]) {
      messages.value[conversationId] = [];
    }

    // Check if message already exists to prevent duplicates
    const exists = messages.value[conversationId].some(
      (m) => m.id === message.id
    );
    if (exists) {
      console.warn(
        `Chat message with ID ${message.id} already exists. Ignoring.`
      );
      return;
    }

    // Ensure message has a valid timestamp
    if (!message.createdAt || new Date(message.createdAt).getTime() === 0) {
      console.warn(
        `Message ${message.id} has invalid timestamp, using current time`
      );
      message.createdAt = new Date().toISOString();
    }

    // Add message in chronological order
    messages.value[conversationId].push(message);

    // Sort messages by creation time to maintain order
    messages.value[conversationId].sort(
      (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
    );

    // Limit cached messages per conversation to prevent memory issues
    if (messages.value[conversationId].length > 200) {
      messages.value[conversationId] =
        messages.value[conversationId].slice(-200);
    }
  };

  const updateConversationLastMessage = (conversationId, message) => {
    const conversation = conversations.value.find(
      (c) => c.id === conversationId
    );
    if (conversation) {
      conversation.latestMessage = message;

      // Cache updated conversations
      _cacheConversations();
    }
  };

  const updateMessageReadStatus = (conversationId, messageId, isRead) => {
    const conversationMessages = messages.value[conversationId];
    if (conversationMessages) {
      const message = conversationMessages.find((m) => m.id === messageId);
      if (message) {
        message.isRead = isRead;
      }
    }
  };

  const markConversationMessagesAsRead = (conversationId) => {
    const conversationMessages = messages.value[conversationId];
    if (conversationMessages) {
      conversationMessages.forEach((message) => {
        message.isRead = true;
      });
    }
  };

  const setConversationUnreadCount = (conversationId, count) => {
    const oldCount = unreadCounts.value[conversationId] || 0;
    unreadCounts.value[conversationId] = count;

    // Update total unread count
    totalUnreadCount.value = totalUnreadCount.value - oldCount + count;

    // Cache updated counts
    _cacheUnreadCounts();
    _cacheTotalUnreadCount();
  };

  const incrementUnreadCount = (conversationId) => {
    const currentCount = unreadCounts.value[conversationId] || 0;
    setConversationUnreadCount(conversationId, currentCount + 1);
  };

  const setCurrentConversation = (conversation) => {
    currentConversation.value = conversation;
  };

  const clearCurrentConversation = () => {
    currentConversation.value = null;
  };

  // Enhanced error handling methods
  const clearError = () => {
    error.value = null;
    lastError.value = null;
    errorRetryCount.value = 0;
    isRetrying.value = false;
  };

  const setError = (errorMessage, context = null) => {
    const errorInfo = {
      message: errorMessage,
      timestamp: Date.now(),
      context,
      retryCount: errorRetryCount.value,
    };

    error.value = errorMessage;
    lastError.value = errorInfo;

    // Log error for debugging
    console.error("Chat Store Error:", errorInfo);
  };

  // Chat system cleanup for logout scenarios
  const cleanup = async () => {
    try {
      console.log("Cleaning up chat system...");

      // Disconnect WebSocket
      await disconnectWebSocket();

      // Clear all state
      conversations.value = [];
      currentConversation.value = null;
      messages.value = {};
      totalUnreadCount.value = 0;
      unreadCounts.value = {};

      // Reset UI state
      isLoading.value = false;
      isLoadingMessages.value = false;
      isSendingMessage.value = false;
      isLoadingMoreMessages.value = false;
      isConnected.value = false;

      // Reset pagination
      conversationPagination.value = {
        currentPage: 0,
        totalPages: 0,
        hasMore: true,
      };
      messagePagination.value = {};

      // Clear errors
      clearError();
      networkStatus.value = "online";

      // Clear cache
      _clearCache();

      // Cleanup WebSocket manager
      try {
        const wsManager = await getChatWebSocketManager();
        if (wsManager && typeof wsManager.cleanup === "function") {
          wsManager.cleanup();
        }
      } catch (wsError) {
        console.warn("Error cleaning up WebSocket manager:", wsError);
      }

      console.log("Chat system cleanup completed");
      return true;
    } catch (error) {
      console.error("Error during chat system cleanup:", error);
      return false;
    }
  };

  // Initialize chat system (called after login)
  const initialize = async () => {
    if (!checkAuthentication()) {
      return false;
    }

    try {
      console.log("Initializing chat system...");

      // Load cached data first for better UX
      _loadCachedTotalUnreadCount();
      _loadCachedConversations();
      _loadCachedUnreadCounts();
      _loadCachedConnectionStatus();

      // Connect WebSocket
      const wsConnected = await connectWebSocket();

      // Fetch fresh data
      await fetchUnreadCount();

      console.log("Chat system initialized successfully");
      return wsConnected;
    } catch (error) {
      console.error("Error initializing chat system:", error);
      setError("Failed to initialize chat system");
      return false;
    }
  };

  const handleApiError = (err, operation = "unknown") => {
    let errorMessage = "An unexpected error occurred";

    if (err.response) {
      // HTTP error response
      const status = err.response.status;
      const data = err.response.data;

      switch (status) {
        case 400:
          errorMessage =
            data?.message || "Invalid request. Please check your input.";
          break;
        case 401:
          errorMessage = "You are not authorized. Please log in again.";
          break;
        case 403:
          errorMessage =
            "Access denied. You do not have permission for this action.";
          break;
        case 404:
          errorMessage = "The requested resource was not found.";
          break;
        case 429:
          errorMessage =
            "Too many requests. Please wait a moment before trying again.";
          break;
        case 500:
        case 502:
        case 503:
        case 504:
          errorMessage = "Server error. Please try again later.";
          break;
        default:
          errorMessage =
            data?.message || `Request failed with status ${status}`;
      }
    } else if (err.request) {
      // Network error
      if (!navigator.onLine) {
        networkStatus.value = "offline";
        errorMessage =
          "No internet connection. Please check your network and try again.";
      } else {
        networkStatus.value = "slow";
        errorMessage =
          "Network error. Please check your connection and try again.";
      }
    } else {
      // Other error
      errorMessage = err.message || "An unexpected error occurred";
    }

    setError(errorMessage, { operation, originalError: err });
    return errorMessage;
  };

  const retryWithBackoff = async (
    operation,
    maxRetries = maxRetryAttempts.value
  ) => {
    if (isRetrying.value) {
      return { success: false, message: "Already retrying" };
    }

    isRetrying.value = true;
    let lastError = null;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        errorRetryCount.value = attempt - 1;

        // Clear error before retry
        if (attempt > 1) {
          error.value = null;
        }

        const result = await operation();

        // Success - reset retry state
        errorRetryCount.value = 0;
        isRetrying.value = false;
        networkStatus.value = "online";

        return { success: true, data: result };
      } catch (err) {
        lastError = err;
        errorRetryCount.value = attempt;

        // Don't retry on client errors (4xx) except 429 (rate limit)
        if (
          err.response &&
          err.response.status >= 400 &&
          err.response.status < 500 &&
          err.response.status !== 429
        ) {
          break;
        }

        // Wait before retrying (exponential backoff)
        if (attempt < maxRetries) {
          const delay = Math.min(1000 * Math.pow(2, attempt - 1), 10000);
          await new Promise((resolve) => setTimeout(resolve, delay));
        }
      }
    }

    // All retries failed
    isRetrying.value = false;
    const errorMessage = handleApiError(lastError, "retry");
    return { success: false, message: errorMessage };
  };

  const checkNetworkStatus = () => {
    if (typeof navigator !== "undefined") {
      if (!navigator.onLine) {
        networkStatus.value = "offline";
        setError("No internet connection detected");
        return false;
      } else {
        networkStatus.value = "online";
        return true;
      }
    }
    return true;
  };

  // Enhanced API methods with better error handling
  const fetchConversationsWithRetry = async () => {
    if (!checkNetworkStatus()) {
      return { success: false, message: "No internet connection" };
    }

    return await retryWithBackoff(async () => {
      isLoading.value = true;

      try {
        const response = await chatService.getConversations();

        if (response && response.data) {
          const conversationList = response.data.conversations || [];
          const authStore = useAuthStore();
          const currentUserId = authStore.userInfo?.id;

          conversations.value = conversationList.map((conv) => {
            const authStore = useAuthStore();
            const currentUserId = authStore.userInfo?.id;
            const currentUserFullName =
              authStore.userInfo?.fullName || authStore.userInfo?.name || "You";
            let otherParticipant = null;

            // Debug logging to see what data we're receiving
            console.log("Raw conversation data:", conv);

            if (conv.participants && Array.isArray(conv.participants)) {
              otherParticipant = conv.participants.find(
                (p) => p.id !== currentUserId
              );
            } else if (conv.otherParticipantId || conv.otherParticipantName) {
              otherParticipant = {
                id: conv.otherParticipantId,
                name: conv.otherParticipantName,
              };
            }

            const mappedConversation = {
              ...conv,
              participantOneId: currentUserId,
              participantOneName: currentUserFullName,
              participantTwoId: otherParticipant?.id || null,
              participantTwoName: otherParticipant?.name || "Unknown User",
              latestMessage: conv.latestMessage || null,
            };

            // Debug logging to see the mapped conversation
            console.log("Mapped conversation:", mappedConversation);

            return mappedConversation;
          });

          // Update unread counts from conversation data
          const newUnreadCounts = {};
          let totalUnread = 0;
          conversationList.forEach((conv) => {
            if (conv.unreadCount !== undefined) {
              newUnreadCounts[conv.id] = conv.unreadCount;
              totalUnread += conv.unreadCount;
            }
          });
          unreadCounts.value = newUnreadCounts;
          totalUnreadCount.value = totalUnread;

          // Cache conversations and unread counts
          _cacheConversations();
          _cacheUnreadCounts();
          _cacheTotalUnreadCount();

          return response.data;
        } else {
          throw new Error(
            response?.error?.message || "Failed to fetch conversations"
          );
        }
      } finally {
        isLoading.value = false;
      }
    });
  };

  const fetchConversationMessagesWithRetry = async (
    conversationId,
    page = 0,
    size = 20,
    append = false
  ) => {
    if (!checkNetworkStatus()) {
      return { success: false, message: "No internet connection" };
    }

    return await retryWithBackoff(async () => {
      if (!append) {
        isLoadingMessages.value = true;
      } else {
        isLoadingMoreMessages.value = true;
      }

      try {
        const response = await chatService.getConversationMessages(
          conversationId,
          page,
          size
        );

        console.log("API response for messages:", response);

        if (response && response.data) {
          const newMessages = response.data.messages || [];
          console.log("New messages from API:", newMessages);
          // Synthesize senderName for each message
          const authStore = useAuthStore();
          const currentUserId = authStore.userInfo?.id;
          const conversation = conversations.value.find(
            (c) => c.id === conversationId
          );
          const otherName = conversation
            ? conversation.participantTwoName
            : "Other";
          const myName = authStore.userInfo?.name || "You";
          const mappedMessages = newMessages.map((msg) => ({
            ...msg,
            senderName: msg.senderId === currentUserId ? myName : otherName,
            isRead: msg.read !== undefined ? msg.read : msg.isRead, // normalize field
          }));
          if (!messages.value[conversationId]) {
            messages.value[conversationId] = [];
          }
          if (append) {
            messages.value[conversationId] = [
              ...mappedMessages,
              ...messages.value[conversationId],
            ];
          } else {
            messages.value[conversationId] = mappedMessages;
          }

          // Update pagination info
          if (!messagePagination.value[conversationId]) {
            messagePagination.value[conversationId] = {};
          }

          messagePagination.value[conversationId] = {
            currentPage: response.data.page || page,
            totalPages: response.data.totalPages || 0,
            hasMore:
              (response.data.page || page) <
              (response.data.totalPages || 0) - 1,
          };

          return response.data;
        } else {
          throw new Error(
            response?.error?.message || "Failed to fetch conversation messages"
          );
        }
      } finally {
        isLoadingMessages.value = false;
        isLoadingMoreMessages.value = false;
      }
    });
  };

  const sendMessageWithRetry = async (receiverId, content) => {
    if (!checkNetworkStatus()) {
      return { success: false, message: "No internet connection" };
    }

    return await retryWithBackoff(async () => {
      isSendingMessage.value = true;

      try {
        const messageData = { receiverId, content };

        // Try WebSocket first if connected
        if (isConnected.value) {
          try {
            const wsManager = await getChatWebSocketManager();
            wsManager.sendMessage(messageData);
          } catch (wsError) {
            console.warn(
              "Failed to send message via WebSocket, falling back to API:",
              wsError
            );
          }
        }

        // Always send via API for persistence
        const response = await chatService.sendMessage(messageData);

        if (response && response.data) {
          const sentMessage = response.data;

          // Add message to local state
          addMessage(sentMessage.conversationId, sentMessage);

          // Update conversation's last message
          updateConversationLastMessage(
            sentMessage.conversationId,
            sentMessage
          );

          // If this is a new conversation, refresh conversations list
          const existingConv = conversations.value.find(
            (c) => c.id === sentMessage.conversationId
          );
          if (!existingConv) {
            await fetchConversationsWithRetry();
          }

          return response.data;
        } else {
          throw new Error(response?.error?.message || "Failed to send message");
        }
      } finally {
        isSendingMessage.value = false;
      }
    });
  };

  // Graceful degradation when WebSocket is unavailable
  const handleWebSocketUnavailable = () => {
    console.warn("WebSocket unavailable, falling back to polling mode");

    // Set up periodic polling as fallback
    const pollInterval = setInterval(async () => {
      if (isConnected.value) {
        clearInterval(pollInterval);
        return;
      }

      try {
        // Poll for new messages every 30 seconds
        await fetchUnreadCount();

        // If we have a current conversation, refresh its messages
        if (currentConversation.value) {
          await fetchConversationMessagesWithRetry(
            currentConversation.value.id
          );
        }
      } catch (err) {
        console.warn("Polling failed:", err);
      }
    }, 30000);

    return pollInterval;
  };

  // Network status monitoring
  const setupNetworkMonitoring = () => {
    if (typeof window !== "undefined") {
      window.addEventListener("online", () => {
        networkStatus.value = "online";
        clearError();

        // Attempt to reconnect WebSocket
        if (!isConnected.value) {
          manualReconnect();
        }

        // Refresh data after coming back online
        fetchUnreadCount();
        if (conversations.value.length === 0) {
          fetchConversationsWithRetry();
        }
      });

      window.addEventListener("offline", () => {
        networkStatus.value = "offline";
        setError("No internet connection");
      });
    }
  };

  // Load cached data on store creation
  _loadCachedTotalUnreadCount();
  _loadCachedConversations();
  _loadCachedUnreadCounts();

  return {
    // State
    conversations,
    currentConversation,
    messages,
    isLoading,
    isLoadingMessages,
    isSendingMessage,
    isLoadingMoreMessages,
    isConnected,
    conversationPagination,
    messagePagination,
    totalUnreadCount,
    unreadCounts,
    error,
    lastError,
    errorRetryCount,
    maxRetryAttempts,
    isRetrying,
    networkStatus,

    // Computed
    hasUnreadMessages,
    sortedConversations,
    unreadConversations,
    getConversationMessages,
    getConversationUnreadCount,

    // Methods
    initialize,
    cleanup,
    connectWebSocket,
    disconnectWebSocket,
    manualReconnect,
    fetchConversations,
    fetchConversationMessages,
    fetchMoreMessages,
    sendMessage,
    markConversationAsRead,
    markMessageAsRead,
    fetchUnreadCount,
    startConversation,
    addMessage,
    updateConversationLastMessage,
    updateMessageReadStatus,
    markConversationMessagesAsRead,
    setConversationUnreadCount,
    incrementUnreadCount,
    setCurrentConversation,
    clearCurrentConversation,
    clearError,
    setError,
    handleApiError,
    retryWithBackoff,
    checkNetworkStatus,
    handleWebSocketUnavailable,
    setupNetworkMonitoring,
  };
});
