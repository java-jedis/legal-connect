import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

/**
 * WebSocket Manager for handling real-time notifications
 * Manages STOMP connections with SockJS fallback, authentication, and automatic reconnection
 */
class WebSocketManager {
  constructor() {
    this.client = null;
    this.isConnected = false;
    this.isConnecting = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.baseReconnectDelay = 1000; // 1 second
    this.maxReconnectDelay = 30000; // 30 seconds
    this.reconnectTimer = null;
    this.subscriptions = new Map();
    this.messageHandlers = new Map();
    this.connectionPromise = null;
    this.token = null;
    this.userId = null;

    // Event callbacks
    this.onConnectCallback = null;
    this.onDisconnectCallback = null;
    this.onErrorCallback = null;
    this.onReconnectCallback = null;
    this.onConnectionChange = null;
  }

  /**
   * Initialize and connect to WebSocket
   * @param {string} token - JWT authentication token
   * @param {string} userId - User ID for authentication
   * @param {string} baseUrl - Base URL for WebSocket connection (optional)
   * @returns {Promise<void>}
   */
  async connect(token, userId, baseUrl = null) {
    if (this.isConnected || this.isConnecting) {
      console.warn("WebSocket is already connected or connecting");
      return this.connectionPromise;
    }

    if (!token || !userId) {
      throw new Error("Token and userId are required for WebSocket connection");
    }

    this.token = token;
    this.userId = userId;
    this.isConnecting = true;

    // Determine WebSocket URL
    const wsUrl = this._getWebSocketUrl(baseUrl);

    this.connectionPromise = new Promise((resolve, reject) => {
      try {
        // Create STOMP client with SockJS
        this.client = new Client({
          webSocketFactory: () => new SockJS(wsUrl),
          connectHeaders: {
            Authorization: `Bearer ${token}`,
            token: token,
          },
          debug: (str) => {
            console.debug("STOMP Debug:", str);
          },
          reconnectDelay: 0, // We handle reconnection manually
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: (frame) => {
            console.log("WebSocket connected successfully:", frame);
            this.isConnected = true;
            this.isConnecting = false;
            this.reconnectAttempts = 0;

            // Subscribe to user-specific notification queue
            this._subscribeToNotifications();

            // Subscribe to error queue
            this._subscribeToErrors();

            // Subscribe to status queue
            this._subscribeToStatus();

            // Send subscription message to backend
            this._sendSubscriptionMessage();

            if (this.onConnectCallback) {
              this.onConnectCallback();
            }
            if (this.onConnectionChange) {
              this.onConnectionChange(true);
            }

            resolve();
          },
          onDisconnect: (frame) => {
            console.log("WebSocket disconnected:", frame);
            this.isConnected = false;
            this.isConnecting = false;

            if (this.onDisconnectCallback) {
              this.onDisconnectCallback();
            }
            if (this.onConnectionChange) {
              this.onConnectionChange(false);
            }

            // Attempt reconnection if not manually disconnected
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
              this._scheduleReconnect();
            }
          },
          onStompError: (frame) => {
            console.error("STOMP error:", frame);
            this.isConnected = false;
            this.isConnecting = false;

            if (this.onErrorCallback) {
              this.onErrorCallback(
                new Error(`STOMP error: ${frame.headers.message}`)
              );
            }

            // Attempt reconnection on error
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
              this._scheduleReconnect();
            } else {
              reject(
                new Error(
                  `Failed to connect after ${this.maxReconnectAttempts} attempts`
                )
              );
            }
          },
          onWebSocketError: (error) => {
            console.error("WebSocket error:", error);
            this.isConnected = false;
            this.isConnecting = false;

            if (this.onErrorCallback) {
              this.onErrorCallback(error);
            }

            reject(error);
          },
        });

        // Activate the client
        this.client.activate();
      } catch (error) {
        console.error("Failed to create WebSocket connection:", error);
        this.isConnecting = false;
        reject(error);
      }
    });

    return this.connectionPromise;
  }

  /**
   * Disconnect from WebSocket
   */
  async disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    this.reconnectAttempts = this.maxReconnectAttempts; // Prevent reconnection

    if (this.client && this.isConnected) {
      console.log("Disconnecting WebSocket...");

      // Clear all subscriptions
      this.subscriptions.clear();
      this.messageHandlers.clear();

      // Deactivate client
      await this.client.deactivate();
      this.client = null;
    }

    this.isConnected = false;
    this.isConnecting = false;
    this.connectionPromise = null;
  }

  /**
   * Subscribe to a destination with a message handler
   * @param {string} destination - STOMP destination to subscribe to
   * @param {Function} handler - Message handler function
   * @returns {string} - Subscription ID
   */
  subscribe(destination, handler) {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    const subscriptionId = `sub_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

    try {
      const subscription = this.client.subscribe(destination, (message) => {
        try {
          const parsedMessage = JSON.parse(message.body);
          handler(parsedMessage);
        } catch (error) {
          console.error("Error parsing WebSocket message:", error);
          handler({ error: "Failed to parse message", raw: message.body });
        }
      });

      this.subscriptions.set(subscriptionId, subscription);
      this.messageHandlers.set(subscriptionId, { destination, handler });

      console.log(`Subscribed to ${destination} with ID: ${subscriptionId}`);
      return subscriptionId;
    } catch (error) {
      console.error(`Failed to subscribe to ${destination}:`, error);
      throw error;
    }
  }

  /**
   * Unsubscribe from a destination
   * @param {string} subscriptionId - Subscription ID returned by subscribe()
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions.get(subscriptionId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(subscriptionId);
      this.messageHandlers.delete(subscriptionId);
      console.log(`Unsubscribed from subscription: ${subscriptionId}`);
    }
  }

  /**
   * Send a message to a destination
   * @param {string} destination - STOMP destination
   * @param {Object} message - Message payload
   */
  send(destination, message = {}) {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    try {
      this.client.publish({
        destination,
        body: JSON.stringify(message),
      });
      console.log(`Message sent to ${destination}:`, message);
    } catch (error) {
      console.error(`Failed to send message to ${destination}:`, error);
      throw error;
    }
  }

  /**
   * Send ping to check connection status
   */
  ping() {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    this.send("/app/notifications/ping");
  }

  /**
   * Mark notification as read via WebSocket
   * @param {string} notificationId - Notification ID to mark as read
   */
  markNotificationAsRead(notificationId) {
    if (!this.isConnected) {
      throw new Error("WebSocket is not connected");
    }

    // Send just the UUID string, not as JSON
    try {
      this.client.publish({
        destination: "/app/notifications/mark-read",
        body: notificationId.toString(),
      });
      console.log(
        `Mark-as-read message sent for notification: ${notificationId}`
      );
    } catch (error) {
      console.error(`Failed to send mark-as-read message:`, error);
      throw error;
    }
  }

  /**
   * Get connection status
   * @returns {boolean}
   */
  getConnectionStatus() {
    return {
      isConnected: this.isConnected,
      isConnecting: this.isConnecting,
      reconnectAttempts: this.reconnectAttempts,
      hasClient: !!this.client,
    };
  }

  /**
   * Set event callbacks
   * @param {Object} callbacks - Event callback functions
   */
  setEventCallbacks(callbacks = {}) {
    this.onConnectCallback = callbacks.onConnect || null;
    this.onDisconnectCallback = callbacks.onDisconnect || null;
    this.onErrorCallback = callbacks.onError || null;
    this.onReconnectCallback = callbacks.onReconnect || null;
    this.onConnectionChange = callbacks.onConnectionChange || null;
  }

  /**
   * Get WebSocket URL based on current environment
   * @param {string} baseUrl - Optional base URL override
   * @returns {string}
   */
  _getWebSocketUrl(baseUrl = null) {
    if (baseUrl) {
      return `${baseUrl}/ws`;
    }

    // Use environment variable or default to localhost with v1 context path for WebSocket
    const apiBaseUrl =
      import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

    // Add /v1/ws to the base URL (WebSocket needs the full context path)
    return `${apiBaseUrl}/v1/ws`;
  }

  /**
   * Subscribe to user-specific notification topic
   * @private
   */
  _subscribeToNotifications() {
    if (!this.client || !this.isConnected) {
      return;
    }

    // Use the same destination pattern as the backend: /topic/user-{userId}
    const destination = `/topic/user-${this.userId}`;

    try {
      const subscription = this.client.subscribe(destination, (message) => {
        try {
          const notification = JSON.parse(message.body);
          console.log("Received notification:", notification);

          // Emit notification event for external handlers
          this._emitNotificationEvent(notification);
        } catch (error) {
          console.error("Error processing notification:", error);
        }
      });

      this.subscriptions.set("notifications", subscription);
      console.log(`Subscribed to notifications topic: ${destination}`);
    } catch (error) {
      console.error("Failed to subscribe to notifications:", error);
    }
  }

  /**
   * Subscribe to error queue
   * @private
   */
  _subscribeToErrors() {
    if (!this.client || !this.isConnected) {
      return;
    }

    const destination = "/user/queue/errors";

    try {
      const subscription = this.client.subscribe(destination, (message) => {
        console.error("WebSocket error message:", message.body);

        if (this.onErrorCallback) {
          this.onErrorCallback(new Error(message.body));
        }
      });

      this.subscriptions.set("errors", subscription);
      console.log("Subscribed to error queue");
    } catch (error) {
      console.error("Failed to subscribe to errors:", error);
    }
  }

  /**
   * Subscribe to status queue
   * @private
   */
  _subscribeToStatus() {
    if (!this.client || !this.isConnected) {
      return;
    }

    const destination = "/user/queue/status";

    try {
      const subscription = this.client.subscribe(destination, (message) => {
        console.log("WebSocket status message:", message.body);

        // Handle mark-as-read acknowledgments
        if (
          message.body &&
          message.body.includes("Notification mark-read request received for:")
        ) {
          const notificationId = message.body.split(
            "Notification mark-read request received for: "
          )[1];
          if (notificationId) {
            // Emit event for notification store to update local state
            const event = new CustomEvent("websocket-mark-read-success", {
              detail: { notificationId: notificationId.trim() },
            });
            if (typeof window !== "undefined") {
              window.dispatchEvent(event);
            }
            console.log(
              `Mark-as-read success event emitted for notification: ${notificationId}`
            );
          }
        }
      });

      this.subscriptions.set("status", subscription);
      console.log("Subscribed to status queue");
    } catch (error) {
      console.error("Failed to subscribe to status:", error);
    }
  }

  /**
   * Send subscription message to backend
   * @private
   */
  _sendSubscriptionMessage() {
    try {
      this.send("/app/notifications/subscribe");
      console.log("Sent subscription message to backend");
    } catch (error) {
      console.error("Failed to send subscription message:", error);
    }
  }

  /**
   * Schedule reconnection with exponential backoff
   * @private
   */
  _scheduleReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    this.reconnectAttempts++;

    // Calculate delay with exponential backoff
    const delay = Math.min(
      this.baseReconnectDelay * Math.pow(2, this.reconnectAttempts - 1),
      this.maxReconnectDelay
    );

    console.log(
      `Scheduling reconnection attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${delay}ms`
    );

    // Emit reconnection status event for UI feedback
    this._emitReconnectionStatusEvent({
      reconnecting: true,
      attempt: this.reconnectAttempts,
      maxAttempts: this.maxReconnectAttempts,
      nextAttemptIn: delay,
    });

    this.reconnectTimer = setTimeout(async () => {
      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        console.log(
          `Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`
        );

        try {
          await this.connect(this.token, this.userId);

          if (this.onReconnectCallback) {
            this.onReconnectCallback();
          }

          // Emit successful reconnection event
          this._emitReconnectionStatusEvent({
            reconnecting: false,
            success: true,
            attempt: this.reconnectAttempts,
            maxAttempts: this.maxReconnectAttempts,
          });
        } catch (error) {
          console.error("Reconnection failed:", error);

          // Emit failed reconnection attempt event
          this._emitReconnectionStatusEvent({
            reconnecting: false,
            success: false,
            attempt: this.reconnectAttempts,
            maxAttempts: this.maxReconnectAttempts,
            error: error.message,
          });

          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this._scheduleReconnect();
          } else {
            console.error("Max reconnection attempts reached");
            if (this.onErrorCallback) {
              this.onErrorCallback(
                new Error("Max reconnection attempts reached")
              );
            }

            // Emit max attempts reached event
            this._emitReconnectionStatusEvent({
              reconnecting: false,
              success: false,
              attempt: this.reconnectAttempts,
              maxAttempts: this.maxReconnectAttempts,
              maxAttemptsReached: true,
              error: "Max reconnection attempts reached",
            });
          }
        }
      }
    }, delay);
  }

  /**
   * Emit notification event for external handlers
   * @param {Object} notification - Notification data
   * @private
   */
  _emitNotificationEvent(notification) {
    // Create custom event for notification
    const event = new CustomEvent("websocket-notification", {
      detail: notification,
    });

    // Dispatch to window for global handling
    if (typeof window !== "undefined") {
      window.dispatchEvent(event);
    }
  }

  /**
   * Emit reconnection status event for external handlers
   * @param {Object} status - Reconnection status data
   * @private
   */
  _emitReconnectionStatusEvent(status) {
    // Create custom event for reconnection status
    const event = new CustomEvent("websocket-reconnection-status", {
      detail: status,
    });

    // Dispatch to window for global handling
    if (typeof window !== "undefined") {
      window.dispatchEvent(event);
    }
  }

  /**
   * Manual reconnect method that can be called from UI
   * Resets reconnection attempts and tries to connect again
   * @returns {Promise<void>}
   */
  async manualReconnect() {
    // Clear any existing reconnection timer
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    // Reset reconnection attempts
    this.reconnectAttempts = 0;

    // Emit reconnection status event
    this._emitReconnectionStatusEvent({
      reconnecting: true,
      attempt: 1,
      maxAttempts: this.maxReconnectAttempts,
      manual: true,
    });

    try {
      // Try to connect
      await this.connect(this.token, this.userId);

      // Emit successful reconnection event
      this._emitReconnectionStatusEvent({
        reconnecting: false,
        success: true,
        manual: true,
      });

      if (this.onReconnectCallback) {
        this.onReconnectCallback();
      }

      return true;
    } catch (error) {
      console.error("Manual reconnection failed:", error);

      // Emit failed reconnection event
      this._emitReconnectionStatusEvent({
        reconnecting: false,
        success: false,
        manual: true,
        error: error.message,
      });

      // Start automatic reconnection process
      this._scheduleReconnect();

      return false;
    }
  }
}

// Create singleton instance
const websocketManager = new WebSocketManager();

export default websocketManager;
