<template>
  <div class="chat-inbox-dropdown" ref="dropdownRef">
    <!-- Chat Icon Button -->
    <button
      @click="toggleDropdown"
      class="chat-toggle"
      :aria-label="
        hasUnreadMessages
          ? `${totalUnreadCount} unread messages`
          : 'Chat messages'
      "
      :class="{ 'chat-toggle--active': isOpen }"
      aria-haspopup="true"
      :aria-expanded="isOpen"
      aria-controls="chat-menu"
    >
      <svg
        class="chat-icon"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
        />
      </svg>

      

      <!-- Unread Count Badge -->
      <span
        v-if="hasUnreadMessages"
        class="chat-badge"
        :aria-label="`${totalUnreadCount} unread`"
        role="status"
      >
        {{ totalUnreadCount > 99 ? "99+" : totalUnreadCount }}
      </span>
    </button>

    <!-- Dropdown Menu -->
    <div
      id="chat-menu"
      class="chat-menu"
      :class="{ 'chat-menu--open': isOpen }"
      role="menu"
      aria-labelledby="chat-toggle"
    >
      <!-- Header -->
      <div class="chat-header">
        <h3 class="chat-title">Messages</h3>
        <button
          class="view-all-btn"
          @click="handleViewAllClick"
        >
          View all
        </button>
      </div>

      

      <!-- Conversations List -->
      <div class="chat-list" ref="chatListRef">
        <!-- Loading State -->
        <div
          v-if="isLoading && recentConversations.length === 0"
          class="chat-loading"
        >
          <svg class="loading-spinner" viewBox="0 0 24 24">
            <circle
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="2"
              fill="none"
              opacity="0.3"
            />
            <path
              d="M12 2a10 10 0 0 1 10 10"
              stroke="currentColor"
              stroke-width="2"
              fill="none"
            />
          </svg>
          <span>Loading conversations...</span>
        </div>

        <!-- Empty State -->
        <div v-else-if="recentConversations.length === 0" class="chat-empty">
          <svg
            class="empty-icon"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <p>No conversations yet</p>
          <span>Start a conversation with a lawyer to see messages here</span>
        </div>

        <!-- Conversation Items -->
        <div v-else class="chat-items">
          <div
            v-for="(conversation, index) in recentConversations"
            :key="conversation.id"
            class="chat-item"
            :class="{ 'chat-item--unread': getConversationUnreadCount(conversation.id) > 0 }"
            @click="handleConversationClick(conversation)"
            role="menuitem"
            tabindex="0"
            @keydown.enter="handleConversationClick(conversation)"
            @keydown.space.prevent="handleConversationClick(conversation)"
            @keydown.down="focusNextConversation(index)"
            @keydown.up="focusPreviousConversation(index)"
            @keydown.home="focusFirstConversation"
            @keydown.end="focusLastConversation"
            :aria-label="
              getConversationUnreadCount(conversation.id) > 0 ? 'Unread conversation' : 'Read conversation'
            "
          >
            <div class="chat-content">
              <div class="chat-participant">
                {{ getParticipantName(conversation) }}
              </div>
              <p class="chat-preview">
                {{ conversation.latestMessage?.content || 'No messages yet' }}
              </p>
              <time
                class="chat-time"
                :datetime="conversation.latestMessage?.createdAt || conversation.updatedAt"
              >
                {{ formatTime(conversation.latestMessage?.createdAt || conversation.updatedAt) }}
              </time>
            </div>
            <div
              v-if="getConversationUnreadCount(conversation.id) > 0"
              class="chat-unread-badge"
              :aria-label="`${getConversationUnreadCount(conversation.id)} unread messages`"
            >
              {{ getConversationUnreadCount(conversation.id) > 9 ? "9+" : getConversationUnreadCount(conversation.id) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-if="error" class="chat-error">
        <ChatErrorBoundary
          error-title="Failed to Load Conversations"
          :error-message="errorMessage"
          :show-details="false"
          :retryable="true"
          :show-fallback="true"
          fallback-text="Use Offline Mode"
          @retry="handleErrorRetry"
          @fallback="handleOfflineMode"
          @error="handleErrorBoundaryError"
        >
          <div class="error-fallback-content">
            <svg
              class="error-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <p>{{ errorMessage }}</p>
          </div>
        </ChatErrorBoundary>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useChatStore } from "../stores/chat";
import ChatErrorBoundary from "./ChatErrorBoundary.vue";

// Router
const router = useRouter();

// Stores
const chatStore = useChatStore();
const authStore = useAuthStore();

// Refs
const dropdownRef = ref(null);
const chatListRef = ref(null);
const isOpen = ref(false);
const isRetrying = ref(false);
const reconnectionStatus = ref({
  reconnecting: false,
  attempt: 0,
  maxAttempts: 5,
  maxAttemptsReached: false,
  error: null,
});

// Computed properties
const conversations = computed(() => chatStore.sortedConversations);
const recentConversations = computed(() => conversations.value.slice(0, 10)); // Show last 10 conversations
const totalUnreadCount = computed(() => chatStore.totalUnreadCount);
const hasUnreadMessages = computed(() => chatStore.hasUnreadMessages);
const isLoading = computed(() => chatStore.isLoading);
const error = computed(() => chatStore.error);
const isConnected = computed(() => chatStore.isConnected);
const getConversationUnreadCount = computed(() => chatStore.getConversationUnreadCount);

const errorMessage = computed(() => {
  if (!error.value) return "Failed to load conversations";

  // Format error message for better readability
  if (error.value.includes("after multiple attempts")) {
    return "Failed to load conversations after multiple attempts";
  } else if (
    error.value.includes("network") ||
    error.value.includes("timeout")
  ) {
    return "Network error. Please check your connection and try again.";
  } else {
    return error.value;
  }
});

// Methods
const toggleDropdown = async () => {
  isOpen.value = !isOpen.value;

  if (isOpen.value) {
    // Fetch conversations when opening dropdown if not already loaded
    if (conversations.value.length === 0) {
      await chatStore.fetchConversations();
    }

    // Focus management for accessibility
    setTimeout(() => {
      const firstItem = dropdownRef.value?.querySelector(".chat-item");
      if (firstItem) {
        firstItem.focus();
      } else {
        // If no conversations, focus on the header or view all button
        const viewAllBtn = dropdownRef.value?.querySelector(".view-all-btn");
        const header = dropdownRef.value?.querySelector(".chat-header");
        if (viewAllBtn) {
          viewAllBtn.focus();
        } else if (header) {
          header.setAttribute("tabindex", "-1");
          header.focus();
        }
      }
    }, 100);
  } else {
    // Return focus to the chat toggle button when closing
    const toggleButton = dropdownRef.value?.querySelector(".chat-toggle");
    if (toggleButton) {
      toggleButton.focus();
    }
  }
};

const closeDropdown = () => {
  isOpen.value = false;
};

const handleConversationClick = async (conversation) => {
  // Mark conversation as read if it has unread messages
  if (getConversationUnreadCount.value(conversation.id) > 0) {
    await chatStore.markConversationAsRead(conversation.id);
  }
  
  // Navigate to conversation detail view
  // Check if the route exists before navigating
  try {
    await router.push(`/chat/${conversation.id}`);
  } catch (error) {
    console.warn('Chat route not available yet:', error);
    // For now, just close the dropdown
  }
  closeDropdown();
};

const handleViewAllClick = () => {
  // Navigate to chat inbox view
  // Check if the route exists before navigating
  try {
    router.push('/chat');
  } catch (error) {
    console.warn('Chat inbox route not available yet:', error);
    // For now, just close the dropdown
  }
  closeDropdown();
};

const getParticipantName = (conversation) => {
  // Try multiple possible field names from the backend
  return conversation.participantTwoName || 
         conversation.otherParticipantName || 
         conversation.participantName ||
         'Unknown User';
};



const reconnectWebSocket = async () => {
  try {
    // Set local reconnecting state
    reconnectionStatus.value.reconnecting = true;

    // Use the manual reconnect method from the chat store
    const success = await chatStore.manualReconnect();

    if (success) {
      // Refresh data after successful reconnection
      await chatStore.fetchUnreadCount();

      // If dropdown is open, also refresh conversations
      if (isOpen.value) {
        await chatStore.fetchConversations();
      }
    }
  } catch (error) {
    console.error("Failed to reconnect WebSocket:", error);

    // Update local reconnection status with error
    reconnectionStatus.value.error = error.message;
  }
};

const formatTime = (timestamp) => {
  if (!timestamp || new Date(timestamp).getTime() === 0) return 'Invalid time';
  
  const date = new Date(timestamp);
  const now = new Date();
  const diffInMinutes = Math.floor((now - date) / (1000 * 60));

  if (diffInMinutes < 1) {
    return "Just now";
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}m ago`;
  } else if (diffInMinutes < 1440) {
    const hours = Math.floor(diffInMinutes / 60);
    return `${hours}h ago`;
  } else {
    const days = Math.floor(diffInMinutes / 1440);
    return `${days}d ago`;
  }
};

// Click outside handler
const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    closeDropdown();
  }
};

// Keyboard handler
const handleKeydown = (event) => {
  if (event.key === "Escape" && isOpen.value) {
    closeDropdown();
  }
};

// Handle WebSocket reconnection status
const handleReconnectionStatus = (event) => {
  const status = event.detail;
  reconnectionStatus.value = {
    ...reconnectionStatus.value,
    ...status,
  };
};

// Lifecycle
onMounted(() => {
  document.addEventListener("click", handleClickOutside);
  document.addEventListener("keydown", handleKeydown);
  window.addEventListener(
    "websocket-reconnection-status",
    handleReconnectionStatus
  );
  
  // Add real-time event listeners
  window.addEventListener("websocket-message", handleRealTimeMessage);
  window.addEventListener("websocket-unread-count-update", handleRealTimeUnreadUpdate);
  window.addEventListener("websocket-message-read", handleRealTimeReadStatus);
  window.addEventListener("websocket-conversation-read", handleRealTimeReadStatus);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  document.removeEventListener("keydown", handleKeydown);
  window.removeEventListener(
    "websocket-reconnection-status",
    handleReconnectionStatus
  );
  
  // Remove real-time event listeners
  window.removeEventListener("websocket-message", handleRealTimeMessage);
  window.removeEventListener("websocket-unread-count-update", handleRealTimeUnreadUpdate);
  window.removeEventListener("websocket-message-read", handleRealTimeReadStatus);
  window.removeEventListener("websocket-conversation-read", handleRealTimeReadStatus);
});

// Keyboard navigation for conversations
const focusNextConversation = (currentIndex) => {
  const items = dropdownRef.value?.querySelectorAll(".chat-item");
  if (!items || items.length === 0) return;

  const nextIndex = currentIndex + 1;
  if (nextIndex < items.length) {
    items[nextIndex].focus();
  }
};

const focusPreviousConversation = (currentIndex) => {
  const items = dropdownRef.value?.querySelectorAll(".chat-item");
  if (!items || items.length === 0) return;

  const prevIndex = currentIndex - 1;
  if (prevIndex >= 0) {
    items[prevIndex].focus();
  } else {
    // Focus back to the chat toggle button if at the first item
    const toggleButton = dropdownRef.value?.querySelector(".chat-toggle");
    if (toggleButton) {
      toggleButton.focus();
    }
  }
};

const focusFirstConversation = () => {
  const items = dropdownRef.value?.querySelectorAll(".chat-item");
  if (!items || items.length === 0) return;

  items[0].focus();
};

const focusLastConversation = () => {
  const items = dropdownRef.value?.querySelectorAll(".chat-item");
  if (!items || items.length === 0) return;

  items[items.length - 1].focus();
};

// Real-time event handlers
const handleRealTimeMessage = (event) => {
  const message = event.detail;
  console.log("ChatInboxDropdown: Real-time message received:", message);
  
  // If dropdown is open, refresh conversations to show updated last message
  if (isOpen.value) {
    chatStore.fetchConversations();
  }
};

const handleRealTimeUnreadUpdate = (event) => {
  const unreadData = event.detail;
  console.log("ChatInboxDropdown: Real-time unread count update:", unreadData);
  
  // The store will handle the unread count update automatically
  // We just need to refresh conversations if dropdown is open
  if (isOpen.value) {
    chatStore.fetchConversations();
  }
};

const handleRealTimeReadStatus = (event) => {
  const readData = event.detail;
  console.log("ChatInboxDropdown: Real-time read status update:", readData);
  
  // Refresh conversations to update unread indicators
  if (isOpen.value) {
    chatStore.fetchConversations();
  }
};

// Enhanced error handling methods
const handleErrorRetry = async () => {
  console.log('Retrying conversation fetch from error boundary...');
  
  chatStore.clearError();
  isRetrying.value = true;
  
  try {
    await chatStore.fetchConversations();
  } catch (err) {
    console.error('Error boundary retry failed:', err);
  } finally {
    isRetrying.value = false;
  }
};

const handleOfflineMode = () => {
  console.log('Switching to offline mode...');
  
  // Clear error and show cached conversations only
  chatStore.clearError();
  
  // Set up offline polling fallback
  const offlineInterval = chatStore.handleWebSocketUnavailable();
  
  // Store interval for cleanup
  reconnectionStatus.value.offlineInterval = offlineInterval;
};

const handleErrorBoundaryError = (errorInfo) => {
  console.error('ChatInboxDropdown error boundary caught:', errorInfo);
  
  // Set a fallback error state
  chatStore.setError('Chat dropdown encountered an unexpected error');
};

// Watch for store initialization
watch(
  () => chatStore.isConnected,
  (isConnected) => {
    if (isConnected) {
      // Clear offline mode if it was active
      if (reconnectionStatus.value.offlineInterval) {
        clearInterval(reconnectionStatus.value.offlineInterval);
        reconnectionStatus.value.offlineInterval = null;
      }
      
      // Refresh unread count when WebSocket connects
      chatStore.fetchUnreadCount();
      
      // Refresh conversations if dropdown is open
      if (isOpen.value) {
        chatStore.fetchConversations();
      }
    }
  }
);
</script>

<style scoped>
.chat-inbox-dropdown {
  position: relative;
}

.chat-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: var(--color-background-soft);
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}

.chat-toggle:hover,
.chat-toggle--active {
  background: var(--color-primary);
  color: var(--color-background);
  transform: scale(1.05);
}

.chat-toggle:active {
  transform: scale(0.95);
}

.chat-icon {
  width: 20px;
  height: 20px;
  transition: transform var(--transition-fast);
}

.chat-toggle:hover .chat-icon {
  transform: rotate(15deg);
}

/* Connection status indicator */
.connection-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--color-background);
}

.connection-status--connected {
  background-color: #4caf50; /* Green */
}

.connection-status--disconnected {
  background-color: #f44336; /* Red */
}

/* Connection status banner */
.connection-status-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background-color: rgba(244, 67, 54, 0.1);
  border-bottom: 1px solid var(--color-border);
  font-size: 0.875rem;
}

.connection-status-banner .warning-icon {
  width: 16px;
  height: 16px;
  color: #f44336;
}

.reconnect-btn {
  margin-left: auto;
  padding: 0.25rem 0.5rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.75rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.reconnect-btn:hover {
  background: var(--color-secondary);
}

.chat-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 18px;
  height: 18px;
  background: var(--color-error);
  color: var(--color-background);
  border-radius: 9px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid var(--color-background);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(207, 46, 46, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(207, 46, 46, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(207, 46, 46, 0);
  }
}

.chat-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 380px;
  max-height: 500px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all var(--transition-normal);
  z-index: 1000;
  overflow: hidden;
}

.chat-menu--open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-background-soft);
}

.chat-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.view-all-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.view-all-btn:hover {
  background: var(--color-background-mute);
  color: var(--color-primary);
}

.chat-list {
  max-height: 400px;
  overflow-y: auto;
}

.chat-loading,
.chat-empty,
.chat-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem;
  text-align: center;
  color: var(--color-text-muted);
}

.chat-empty .empty-icon,
.chat-error .error-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.chat-empty p,
.chat-error p {
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.chat-empty span {
  font-size: 0.875rem;
}

.retry-btn {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.retry-btn:hover {
  background: var(--color-secondary);
  transform: translateY(-1px);
}

.chat-items {
  padding: 0.5rem 0;
}

.chat-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  border-left: 3px solid transparent;
  position: relative;
  opacity: 0.7;
}

.chat-item:hover {
  background: var(--color-background-soft);
}

.chat-item:focus {
  outline: none;
  background: var(--color-background-mute);
  border-left-color: var(--color-primary);
}

.chat-item--unread {
  background: var(--color-background-mute);
  border-left-color: var(--color-primary);
  opacity: 1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chat-item--unread:hover {
  background: var(--color-background-soft);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.chat-content {
  flex: 1;
  min-width: 0;
}

.chat-participant {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 0.25rem;
  line-height: 1.2;
}

.chat-item--unread .chat-participant {
  font-weight: 700;
}

.chat-preview {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0 0 0.25rem 0;
  line-height: 1.4;
  word-wrap: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.chat-item--unread .chat-preview {
  color: var(--color-text);
  font-weight: 500;
}

.chat-time {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.chat-unread-badge {
  min-width: 20px;
  height: 20px;
  background: var(--color-primary);
  color: var(--color-background);
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

.loading-spinner-small {
  width: 12px;
  height: 12px;
  animation: spin 1s linear infinite;
  margin-right: 4px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .chat-menu {
    width: calc(100vw - 40px);
    max-width: 320px;
    right: -20px;
  }

  .chat-header {
    padding: 0.875rem 1rem;
  }

  .chat-item {
    padding: 0.75rem 1rem;
  }

  .view-all-btn {
    font-size: 0.8125rem;
  }

  .chat-participant {
    font-size: 0.8125rem;
  }

  .chat-preview {
    font-size: 0.8125rem;
  }

  .chat-time {
    font-size: 0.6875rem;
  }

  /* Improve touch target sizes */
  .chat-toggle {
    width: 44px;
    height: 44px;
  }

  .chat-item {
    min-height: 60px; /* Ensure touch targets are at least 44px high */
  }
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .chat-badge {
    border-color: var(--color-background);
  }
}

html.dark .chat-badge {
  border-color: var(--color-background) !important;
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .chat-toggle,
  .chat-menu,
  .chat-item,
  .chat-badge {
    transition: none;
  }

  .chat-badge {
    animation: none;
  }

  .loading-spinner {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .chat-menu {
    border-width: 2px;
  }

  .chat-item--unread {
    border-left-width: 4px;
  }

  .chat-unread-badge {
    border: 1px solid var(--color-text);
  }
}

/* Enhanced focus states for better accessibility */
.chat-toggle:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

.chat-item:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: -2px;
  box-shadow: inset 0 0 0 2px var(--color-primary);
}

.view-all-btn:focus-visible,
.reconnect-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* Enhanced hover states with consistent transitions */
.chat-toggle:hover .chat-icon {
  transform: rotate(15deg) scale(1.1);
}

.chat-item:hover {
  background: var(--color-background-soft);
  transform: translateX(2px);
  box-shadow: var(--shadow-sm);
}

.view-all-btn:hover {
  background: var(--color-background-mute);
  color: var(--color-primary);
  transform: translateY(-1px);
}

.reconnect-btn:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* Consistent spacing and typography */
.chat-participant {
  font-size: 0.875rem;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.01em;
}

.chat-preview {
  font-size: 0.875rem;
  line-height: 1.4;
  letter-spacing: -0.005em;
}

.chat-time {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.01em;
}

/* Enhanced visual hierarchy */
.chat-title {
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.view-all-btn {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* Improved loading states */
.chat-loading {
  padding: 3rem 1.25rem;
}

.loading-spinner {
  color: var(--color-primary);
  opacity: 0.8;
}

/* Better error state styling */
.chat-error {
  padding: 3rem 1.25rem;
}

.error-icon {
  color: var(--color-error);
  opacity: 0.7;
}

/* Enhanced empty state */
.chat-empty {
  padding: 3rem 1.25rem;
}

.empty-icon {
  color: var(--color-text-muted);
  opacity: 0.6;
}
</style>