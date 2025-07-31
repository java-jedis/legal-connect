import websocketManager from "./websocketManager";

/**
 * Chat WebSocket Manager
 * Extends the existing WebSocket manager with chat-specific functionality
 * Handles real-time messaging, read status updates, and unread count synchronization
 */
class ChatWebSocketManager {
  constructor() {
    this.isInitialized = false;
    this.chatSubscriptions = new Map();
    this.messageHandlers = new Map();
    this.isConnected = false;
    this.userId = null;

    // Event callbacks
    this.onMessageReceived = null;
    this.onReadStatusUpdate = null;
    this.onUnreadCountUpdate = null;
    this.onConnectionChange = null;
    this.onError = null;
  }

  /**
   * Initialize chat WebSocket functionality
   * @param {Object} callbacks - Event callback functions
   * @param {Function} callbacks.onMessageReceived - Called when a new message is received
   * @param {Function} callbacks.onReadStatusUpdate - Called when message read status changes
   * @param {Function} callbacks.onUnreadCountUpdate - Called when unread count changes
   * @param {Function} callbacks.onConnectionChange - Called when connection status changes
   * @param {Function} callbacks.onError - Called when an error occurs
   * @returns {Promise<boolean>} - Success status
   */
  async initialize(callbacks = {}) {
    if (this.isInitialized) {
      console.warn("Chat WebSocket manager is already initialized");
      return true;
    }

    try {
      // Set up event callbacks
      this.onMessageReceived = callbacks.onMessageReceived || null;
      this.onReadStatusUpdate = callbacks.onReadStatusUpdate || null;
      this.onUnreadCountUpdate = callbacks.onUnreadCountUpdate || null;
      this.onConnectionChange = callbacks.onConnectionChange || null;
      this.onError = callbacks.onError || null;

      // Set up WebSocket manager callbacks for chat
      websocketManager.setEventCallbacks({
        onConnect: () => {
          this.isConnected = true;
          this._setupChatSubscriptions();
          if (this.onConnectionChange) {
            this.onConnectionChange(true);
          }
        },
        onDisconnect: () => {
          this.isConnected = false;
          this._clearChatSubscriptions();
          if (this.onConnectionChange) {
            this.onConnectionChange(false);
          }
        },
        onError: (error) => {
          console.error("Chat WebSocket error:", error);
          if (this.onError) {
            this.onError(error);
          }
        },
        onReconnect: () => {
          this.isConnected = true;
          this._setupChatSubscriptions();
          if (this.onConnectionChange) {
            this.onConnectionChange(true);
          }
        },
      });

      this.isInitialized = true;
      console.log("Chat WebSocket manager initialized successfully");
      return true;
    } catch (error) {
      console.error("Failed to initialize chat WebSocket manager:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Connect to WebSocket with chat functionality
   * @param {string} token - JWT authentication token
   * @param {string} userId - User ID for authentication
   * @param {string} baseUrl - Base URL for WebSocket connection (optional)
   * @returns {Promise<boolean>} - Success status
   */
  async connect(token, userId, baseUrl = null) {
    if (!this.isInitialized) {
      throw new Error(
        "Chat WebSocket manager must be initialized before connecting"
      );
    }

    // Validate authentication parameters
    if (!token || !userId) {
      throw new Error(
        "Valid authentication token and user ID are required for chat WebSocket connection"
      );
    }

    // Check if token is expired
    try {
      const tokenPayload = JSON.parse(atob(token.split(".")[1]));
      const currentTime = Math.floor(Date.now() / 1000);

      if (tokenPayload.exp && tokenPayload.exp < currentTime) {
        throw new Error("Authentication token has expired");
      }
    } catch (tokenError) {
      console.error(
        "Invalid authentication token for chat WebSocket:",
        tokenError
      );
      throw new Error("Invalid authentication token");
    }

    try {
      this.userId = userId;

      // Connect using the existing WebSocket manager
      await websocketManager.connect(token, userId, baseUrl);

      // Subscribe to chat after connection is established
      if (websocketManager.getConnectionStatus().isConnected) {
        this._setupChatSubscriptions();
      }

      return true;
    } catch (error) {
      console.error("Failed to connect chat WebSocket:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Disconnect from WebSocket
   * @returns {Promise<boolean>} - Success status
   */
  async disconnect() {
    try {
      this._clearChatSubscriptions();
      await websocketManager.disconnect();
      this.isConnected = false;
      this.userId = null;
      return true;
    } catch (error) {
      console.error("Failed to disconnect chat WebSocket:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Send a message via WebSocket
   * @param {Object} messageData - Message data to send
   * @param {string} messageData.receiverId - ID of the message receiver
   * @param {string} messageData.content - Message content
   * @returns {boolean} - Success status
   */
  sendMessage(messageData) {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    if (!this.userId) {
      throw new Error("User not authenticated for WebSocket operations");
    }

    if (!messageData || !messageData.receiverId || !messageData.content) {
      throw new Error(
        "Invalid message data: receiverId and content are required"
      );
    }

    // Prevent sending messages to self
    if (messageData.receiverId === this.userId) {
      throw new Error("Cannot send message to yourself");
    }

    try {
      websocketManager.send("/app/chat/send", messageData);
      console.log("Message sent via WebSocket:", messageData);
      return true;
    } catch (error) {
      console.error("Failed to send message via WebSocket:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Mark a message as read via WebSocket
   * @param {string} messageId - ID of the message to mark as read
   * @returns {boolean} - Success status
   */
  markMessageAsRead(messageId) {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    if (!this.userId) {
      throw new Error("User not authenticated for WebSocket operations");
    }

    if (!messageId) {
      throw new Error("Message ID is required");
    }

    try {
      // Send just the message ID string as the backend expects
      websocketManager.client.publish({
        destination: "/app/chat/mark-read",
        body: messageId.toString(),
      });
      console.log("Mark-as-read sent via WebSocket for message:", messageId);
      return true;
    } catch (error) {
      console.error("Failed to send mark-as-read via WebSocket:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Mark a conversation as read via WebSocket
   * @param {string} conversationId - ID of the conversation to mark as read
   * @returns {boolean} - Success status
   */
  markConversationAsRead(conversationId) {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    if (!this.userId) {
      throw new Error("User not authenticated for WebSocket operations");
    }

    if (!conversationId) {
      throw new Error("Conversation ID is required");
    }

    try {
      websocketManager.send("/app/chat/mark-conversation-read", conversationId);
      console.log(
        "Mark-conversation-as-read sent via WebSocket for conversation:",
        conversationId
      );
      return true;
    } catch (error) {
      console.error(
        "Failed to send mark-conversation-as-read via WebSocket:",
        error
      );
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Subscribe to chat service
   * @returns {boolean} - Success status
   */
  subscribeToChat() {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    if (!this.userId) {
      throw new Error("User not authenticated for WebSocket operations");
    }

    try {
      websocketManager.send("/app/chat/subscribe");
      console.log("Chat subscription message sent");
      return true;
    } catch (error) {
      console.error("Failed to send chat subscription:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Send a ping to check chat connection
   * @returns {boolean} - Success status
   */
  pingChat() {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    if (!this.userId) {
      throw new Error("User not authenticated for WebSocket operations");
    }

    try {
      websocketManager.send("/app/chat/ping");
      console.log("Chat ping sent");
      return true;
    } catch (error) {
      console.error("Failed to send chat ping:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Get connection status
   * @returns {Object} - Connection status information
   */
  getConnectionStatus() {
    const baseStatus = websocketManager.getConnectionStatus();
    return {
      ...baseStatus,
      chatInitialized: this.isInitialized,
      chatConnected: this.isConnected,
      userId: this.userId,
      activeSubscriptions: this.chatSubscriptions.size,
    };
  }

  /**
   * Manual reconnect with chat functionality
   * @returns {Promise<boolean>} - Success status
   */
  async manualReconnect() {
    try {
      const success = await websocketManager.manualReconnect();
      if (success && websocketManager.getConnectionStatus().isConnected) {
        this._setupChatSubscriptions();
      }
      return success;
    } catch (error) {
      console.error("Chat WebSocket manual reconnection failed:", error);
      if (this.onError) {
        this.onError(error);
      }
      return false;
    }
  }

  /**
   * Set up chat-specific WebSocket subscriptions
   * @private
   */
  _setupChatSubscriptions() {
    if (!this.userId || !websocketManager.getConnectionStatus().isConnected) {
      console.warn(
        "Cannot setup chat subscriptions: missing userId or not connected"
      );
      return;
    }

    try {
      // Clear existing subscriptions first
      this._clearChatSubscriptions();

      // Subscribe to incoming messages for this user
      const messageDestination = `/topic/chat-${this.userId}`;
      const messageSubId = websocketManager.subscribe(
        messageDestination,
        (message) => {
          this._handleIncomingMessage(message);
        }
      );
      this.chatSubscriptions.set("messages", messageSubId);

      // Subscribe to unread count updates
      const unreadDestination = `/topic/chat-unread-${this.userId}`;
      const unreadSubId = websocketManager.subscribe(
        unreadDestination,
        (unreadData) => {
          this._handleUnreadCountUpdate(unreadData);
        }
      );
      this.chatSubscriptions.set("unread", unreadSubId);

      // Subscribe to read status updates
      const readStatusDestination = `/topic/chat-read-status-${this.userId}`;
      const readStatusSubId = websocketManager.subscribe(
        readStatusDestination,
        (readStatusData) => {
          this._handleReadStatusUpdate(readStatusData);
        }
      );
      this.chatSubscriptions.set("readStatus", readStatusSubId);

      // Subscribe to chat-specific status messages
      const chatStatusDestination = `/user/queue/chat-status`;
      const chatStatusSubId = websocketManager.subscribe(
        chatStatusDestination,
        (statusMessage) => {
          this._handleChatStatusMessage(statusMessage);
        }
      );
      this.chatSubscriptions.set("chatStatus", chatStatusSubId);

      // Subscribe to chat-specific error messages
      const chatErrorDestination = `/user/queue/chat-errors`;
      const chatErrorSubId = websocketManager.subscribe(
        chatErrorDestination,
        (errorMessage) => {
          this._handleChatError(errorMessage);
        }
      );
      this.chatSubscriptions.set("chatErrors", chatErrorSubId);

      // Subscribe to chat service
      this.subscribeToChat();

      console.log(
        "Chat WebSocket subscriptions established for user:",
        this.userId
      );
    } catch (error) {
      console.error("Failed to setup chat subscriptions:", error);
      if (this.onError) {
        this.onError(error);
      }
    }
  }

  /**
   * Clear all chat-specific subscriptions
   * @private
   */
  _clearChatSubscriptions() {
    this.chatSubscriptions.forEach((subscriptionId, key) => {
      try {
        websocketManager.unsubscribe(subscriptionId);
        console.log(`Unsubscribed from chat ${key} subscription`);
      } catch (error) {
        console.error(`Failed to unsubscribe from chat ${key}:`, error);
      }
    });
    this.chatSubscriptions.clear();
  }

  /**
   * Handle incoming chat messages
   * @param {Object} message - Incoming message data
   * @private
   */
  _handleIncomingMessage(message) {
    try {
      console.log("Received chat message via WebSocket:", message);

      // Validate message structure
      if (!message || !message.id || !message.conversationId) {
        console.warn("Invalid message structure received:", message);
        return;
      }

      // Ensure message has a valid timestamp
      if (!message.createdAt || new Date(message.createdAt).getTime() === 0) {
        console.warn(
          `WebSocket message ${message.id} has invalid timestamp, using current time`
        );
        message.createdAt = new Date().toISOString();
      }

      // Emit custom event for global handling
      if (typeof window !== "undefined") {
        const event = new CustomEvent("websocket-message", {
          detail: message,
        });
        window.dispatchEvent(event);
      }

      // Call callback if provided
      if (this.onMessageReceived) {
        this.onMessageReceived(message);
      }
    } catch (error) {
      console.error("Error handling incoming chat message:", error);
      if (this.onError) {
        this.onError(error);
      }
    }
  }

  /**
   * Handle unread count updates
   * @param {Object} unreadData - Unread count data
   * @private
   */
  _handleUnreadCountUpdate(unreadData) {
    try {
      console.log("Received unread count update via WebSocket:", unreadData);

      // Validate unread data structure
      if (!unreadData || typeof unreadData.totalUnreadCount !== "number") {
        console.warn("Invalid unread count data received:", unreadData);
        return;
      }

      // Emit custom event for global handling
      if (typeof window !== "undefined") {
        const event = new CustomEvent("websocket-unread-count-update", {
          detail: unreadData,
        });
        window.dispatchEvent(event);
      }

      // Call callback if provided
      if (this.onUnreadCountUpdate) {
        this.onUnreadCountUpdate(unreadData);
      }
    } catch (error) {
      console.error("Error handling unread count update:", error);
      if (this.onError) {
        this.onError(error);
      }
    }
  }

  /**
   * Handle read status updates
   * @param {Object} readStatusData - Read status update data
   * @private
   */
  _handleReadStatusUpdate(readStatusData) {
    try {
      console.log("Received read status update via WebSocket:", readStatusData);

      // Validate read status data structure
      if (!readStatusData || !readStatusData.messageId) {
        console.warn("Invalid read status data received:", readStatusData);
        return;
      }

      // Emit custom event for global handling
      if (typeof window !== "undefined") {
        const event = new CustomEvent("websocket-read-status-update", {
          detail: {
            messageId: readStatusData.messageId,
            isRead: readStatusData.isRead || readStatusData.read || true,
            conversationId: readStatusData.conversationId,
            timestamp: Date.now(),
          },
        });
        window.dispatchEvent(event);
      }

      // Call callback if provided
      if (this.onReadStatusUpdate) {
        this.onReadStatusUpdate({
          messageId: readStatusData.messageId,
          isRead: readStatusData.isRead || readStatusData.read || true,
          conversationId: readStatusData.conversationId,
        });
      }
    } catch (error) {
      console.error("Error handling read status update:", error);
      if (this.onError) {
        this.onError(error);
      }
    }
  }

  /**
   * Handle chat status messages
   * @param {string} statusMessage - Status message from server
   * @private
   */
  _handleChatStatusMessage(statusMessage) {
    try {
      console.log("Received chat status message:", statusMessage);

      // Handle mark-as-read acknowledgments
      if (
        typeof statusMessage === "string" &&
        statusMessage.includes("mark-read request received")
      ) {
        const messageId = statusMessage.split(
          "mark-read request received for: "
        )[1];
        if (messageId) {
          // Emit event for message read confirmation
          if (typeof window !== "undefined") {
            const event = new CustomEvent("websocket-mark-read-success", {
              detail: { messageId: messageId.trim() },
            });
            window.dispatchEvent(event);
          }
        }
      }

      // Handle conversation mark-as-read acknowledgments
      if (
        typeof statusMessage === "string" &&
        statusMessage.includes("mark-conversation-read request received")
      ) {
        const conversationId = statusMessage.split(
          "mark-conversation-read request received for: "
        )[1];
        if (conversationId) {
          // Emit event for conversation read confirmation
          if (typeof window !== "undefined") {
            const event = new CustomEvent("websocket-conversation-read", {
              detail: { conversationId: conversationId.trim() },
            });
            window.dispatchEvent(event);
          }
        }
      }
    } catch (error) {
      console.error("Error handling chat status message:", error);
      if (this.onError) {
        this.onError(error);
      }
    }
  }

  /**
   * Handle chat-specific errors
   * @param {string} errorMessage - Error message from server
   * @private
   */
  _handleChatError(errorMessage) {
    try {
      console.error("Received chat error via WebSocket:", errorMessage);

      const error = new Error(errorMessage || "Chat WebSocket error occurred");

      // Emit custom event for global error handling
      if (typeof window !== "undefined") {
        const event = new CustomEvent("websocket-chat-error", {
          detail: { error: error.message },
        });
        window.dispatchEvent(event);
      }

      // Call callback if provided
      if (this.onError) {
        this.onError(error);
      }
    } catch (error) {
      console.error("Error handling chat error message:", error);
    }
  }

  /**
   * Cleanup resources
   */
  cleanup() {
    this._clearChatSubscriptions();
    this.isInitialized = false;
    this.isConnected = false;
    this.userId = null;
    this.onMessageReceived = null;
    this.onReadStatusUpdate = null;
    this.onUnreadCountUpdate = null;
    this.onConnectionChange = null;
    this.onError = null;
  }
}

// Create singleton instance
const chatWebSocketManager = new ChatWebSocketManager();

export default chatWebSocketManager;
