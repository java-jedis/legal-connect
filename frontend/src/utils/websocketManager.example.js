/**
 * Example usage of WebSocketManager
 * This file demonstrates how to use the WebSocket manager in your application
 */

import websocketManager from './websocketManager.js';

// Example: Basic connection setup
async function connectToWebSocket(token, userId) {
  try {
    // Set up event callbacks
    websocketManager.setEventCallbacks({
      onConnect: () => {
        console.log('Connected to WebSocket!');
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
      },
      onError: (error) => {
        console.error('WebSocket error:', error);
      },
      onReconnect: () => {
        console.log('Reconnected to WebSocket!');
      }
    });

    // Connect to WebSocket
    await websocketManager.connect(token, userId);
    
    // Listen for notifications via custom events
    window.addEventListener('websocket-notification', (event) => {
      const notification = event.detail;
      console.log('New notification received:', notification);
      
      // Handle the notification (e.g., show banner, update store)
      handleNewNotification(notification);
    });

  } catch (error) {
    console.error('Failed to connect to WebSocket:', error);
  }
}

// Example: Custom subscription
function subscribeToCustomDestination() {
  try {
    const subscriptionId = websocketManager.subscribe('/user/queue/custom', (message) => {
      console.log('Custom message received:', message);
    });
    
    // Store subscription ID for later cleanup
    return subscriptionId;
  } catch (error) {
    console.error('Failed to subscribe:', error);
  }
}

// Example: Send a message
function sendPing() {
  try {
    websocketManager.ping();
  } catch (error) {
    console.error('Failed to send ping:', error);
  }
}

// Example: Mark notification as read
function markNotificationAsRead(notificationId) {
  try {
    websocketManager.markNotificationAsRead(notificationId);
  } catch (error) {
    console.error('Failed to mark notification as read:', error);
  }
}

// Example: Check connection status
function checkConnectionStatus() {
  const status = websocketManager.getConnectionStatus();
  console.log('Connection status:', status);
  return status;
}

// Example: Disconnect
async function disconnectWebSocket() {
  try {
    await websocketManager.disconnect();
    console.log('WebSocket disconnected successfully');
  } catch (error) {
    console.error('Error disconnecting WebSocket:', error);
  }
}

// Example notification handler
function handleNewNotification(notification) {
  // This would typically update your notification store
  // and trigger UI updates like showing a banner
  console.log('Handling notification:', notification);
  
  // Example: Show notification banner
  // notificationStore.addNotification(notification);
  
  // Example: Update unread count
  // notificationStore.incrementUnreadCount();
}

export {
  connectToWebSocket,
  subscribeToCustomDestination,
  sendPing,
  markNotificationAsRead,
  checkConnectionStatus,
  disconnectWebSocket
};