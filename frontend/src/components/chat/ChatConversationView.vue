<template>
  <div class="chat-conversation-view" ref="conversationRef">
    <!-- Conversation Header -->
    <div class="conversation-header">
      <div class="participant-info">
        <router-link to="/chat" class="back-button" title="Back to Messages">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </router-link>
        <div class="participant-avatar">
          <svg
            class="avatar-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
        </div>
        <div class="participant-details">
          <h2 class="participant-name">{{ participantName }}</h2>
          <div class="connection-status">
            <span
              class="status-indicator"
              :class="{
                'status-indicator--connected': isConnected,
                'status-indicator--disconnected': !isConnected,
              }"
            ></span>
            <span class="status-text">
              {{ isConnected ? 'Online' : 'Offline' }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- Connection Status Banner -->
      <div v-if="!isConnected" class="connection-banner">
        <svg
          class="warning-icon"
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
        <span>Messages may not be delivered in real-time</span>
        <button
          @click="reconnectWebSocket"
          class="reconnect-btn"
          :disabled="isReconnecting"
        >
          <template v-if="isReconnecting">
            <svg class="loading-spinner-small" viewBox="0 0 24 24">
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
            Reconnecting...
          </template>
          <template v-else>Reconnect</template>
        </button>
      </div>
    </div>

    <!-- Messages Container -->
    <div class="messages-container" ref="messagesContainer">
      <!-- Load More Messages Button -->
      <div
        v-if="hasMoreMessages && !isLoadingMoreMessages"
        class="load-more-container"
      >
        <button @click="loadMoreMessages" class="load-more-btn">
          <svg
            class="load-more-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5 15l7-7 7 7"
            />
          </svg>
          Load earlier messages
        </button>
      </div>

      <!-- Loading More Messages -->
      <div v-if="isLoadingMoreMessages" class="loading-more">
        <ChatLoadingSkeleton 
          type="message-list" 
          :count="3" 
          :animated="true"
          size="small"
        />
      </div>

      <!-- Initial Loading State -->
      <div
        v-if="isLoadingMessages && messages.length === 0"
        class="messages-loading"
      >
        <ChatLoadingSkeleton 
          type="message-list" 
          :count="6" 
          :animated="true"
        />
      </div>

      <!-- Empty State -->
      <div v-else-if="messages.length === 0" class="messages-empty">
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
        <p>No messages yet</p>
        <span>Start the conversation by sending a message below</span>
      </div>

      <!-- Messages List -->
      <div v-else class="messages-list" ref="messagesList">
        <div
          v-for="(message, index) in messages"
          :key="message.id"
          class="message-wrapper"
          :class="{
            'message-wrapper--own': isOwnMessage(message),
            'message-wrapper--other': !isOwnMessage(message),
            'message-wrapper--first-in-group': isFirstInGroup(message, index),
            'message-wrapper--last-in-group': isLastInGroup(message, index),
          }"
        >
          <!-- Date Separator -->
          <div
            v-if="shouldShowDateSeparator(message, index)"
            class="date-separator"
          >
            <span class="date-text">{{ formatDate(message.createdAt) }}</span>
          </div>

          <!-- Message Bubble -->
          <div class="message-bubble">
            <!-- Sender Name (for other user's messages) -->
            <div
              v-if="!isOwnMessage(message) && isFirstInGroup(message, index)"
              class="sender-name"
            >
              {{ message.senderName }}
            </div>

            <!-- Message Content -->
            <div class="message-content">
              <p class="message-text">{{ message.content }}</p>
            </div>

            <!-- Message Footer -->
            <div class="message-footer">
              <time
                class="message-time"
                :datetime="message.createdAt"
                :title="formatFullDateTime(message.createdAt)"
              >
                {{ formatTime(message.createdAt) }}
              </time>

              <!-- Message Status (for own messages) -->
              <div
                v-if="isOwnMessage(message) && getMessageStatus(message)"
                class="message-status"
                :title="getStatusTitle(message)"
              >
                <!-- Sent Status -->
                <svg
                  v-if="getMessageStatus(message) === 'sent'"
                  class="status-icon status-icon--sent"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 13l4 4L19 7"
                  />
                </svg>

                <!-- Delivered Status -->
                <svg
                  v-else-if="getMessageStatus(message) === 'delivered'"
                  class="status-icon status-icon--delivered"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>

                <!-- Read Status -->
                <div
                  v-else-if="getMessageStatus(message) === 'read'"
                  class="status-icon status-icon--read"
                >
                  <svg
                    class="read-check read-check--first"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <svg
                    class="read-check read-check--second"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>

                <!-- Sending Status -->
                <svg
                  v-else
                  class="status-icon status-icon--sending"
                  viewBox="0 0 24 24"
                >
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
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-if="error" class="messages-error">
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
        <button @click="retryLoadMessages" class="retry-btn" :disabled="isRetrying">
          <template v-if="isRetrying">
            <svg class="loading-spinner-small" viewBox="0 0 24 24">
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
            Retrying...
          </template>
          <template v-else>Try again</template>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from "@/stores/auth";
import { useChatStore } from "@/stores/chat";
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import ChatLoadingSkeleton from "./ChatLoadingSkeleton.vue";

// Props
const props = defineProps({
  conversationId: {
    type: String,
    required: true,
  },
  conversation: {
    type: Object,
    default: null,
  },
});

// Stores
const chatStore = useChatStore();
const authStore = useAuthStore();

// Refs
const conversationRef = ref(null);
const messagesContainer = ref(null);
const messagesList = ref(null);
const isReconnecting = ref(false);
const isRetrying = ref(false);
const shouldAutoScroll = ref(true);
const lastScrollTop = ref(0);
const markAsReadTimeout = ref(null);

// Computed properties
const messages = computed(() => 
  chatStore.getConversationMessages(props.conversationId) || []
);

const isLoadingMessages = computed(() => chatStore.isLoadingMessages);
const isLoadingMoreMessages = computed(() => chatStore.isLoadingMoreMessages);
const error = computed(() => chatStore.error);
const isConnected = computed(() => chatStore.isConnected);

const messagePagination = computed(() => 
  chatStore.messagePagination[props.conversationId] || {}
);

const hasMoreMessages = computed(() => 
  messagePagination.value.hasMore || false
);

const participantName = computed(() => {
  if (props.conversation) {
    return getParticipantName(props.conversation);
  }
  
  // Fallback: try to get from conversation in store
  const conversation = chatStore.conversations.find(c => c.id === props.conversationId);
  return conversation ? getParticipantName(conversation) : 'Unknown User';
});

const errorMessage = computed(() => {
  if (!error.value) return "Failed to load messages";

  if (error.value.includes("after multiple attempts")) {
    return "Failed to load messages after multiple attempts";
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
const getParticipantName = (conversation) => {
  // Try multiple possible field names from the backend
  return conversation.participantTwoName || 
         conversation.otherParticipantName || 
         conversation.participantName ||
         'Unknown User';
};

const isOwnMessage = (message) => {
  return message.senderId === authStore.userInfo?.id;
};

const isFirstInGroup = (message, index) => {
  if (index === 0) return true;
  
  const previousMessage = messages.value[index - 1];
  return previousMessage.senderId !== message.senderId ||
         shouldShowDateSeparator(message, index);
};

const isLastInGroup = (message, index) => {
  if (index === messages.value.length - 1) return true;
  
  const nextMessage = messages.value[index + 1];
  return nextMessage.senderId !== message.senderId ||
         shouldShowDateSeparator(nextMessage, index + 1);
};

const shouldShowDateSeparator = (message, index) => {
  if (index === 0) return true;
  
  const previousMessage = messages.value[index - 1];
  const messageDate = new Date(message.createdAt);
  const previousDate = new Date(previousMessage.createdAt);
  
  return messageDate.toDateString() !== previousDate.toDateString();
};

const getMessageStatus = (message) => {
  // Enhanced status logic based on message properties and current user
  const currentUserId = authStore.userInfo?.id;
  
  // Only show status for own messages
  if (message.senderId !== currentUserId) {
    return null;
  }
  
  // Check if message has been read
  if (message.isRead) {
    return 'read';
  }
  
  // Check if message is being sent (temporary state)
  if (message.tempId && chatStore.isSendingMessage) {
    return 'sending';
  }
  
  // Default to delivered for sent messages
  return 'delivered';
};

const getStatusTitle = (message) => {
  const status = getMessageStatus(message);
  switch (status) {
    case 'sending':
      return 'Sending...';
    case 'sent':
      return 'Sent';
    case 'delivered':
      return 'Delivered';
    case 'read':
      return 'Read';
    default:
      return '';
  }
};

const formatTime = (timestamp) => {
  if (!timestamp || new Date(timestamp).getTime() === 0) {
    return 'Invalid time';
  }
  const date = new Date(timestamp);
  return date.toLocaleTimeString([], { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false 
  });
};

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  
  if (date.toDateString() === today.toDateString()) {
    return 'Today';
  } else if (date.toDateString() === yesterday.toDateString()) {
    return 'Yesterday';
  } else {
    return date.toLocaleDateString([], {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
};

const formatFullDateTime = (timestamp) => {
  const date = new Date(timestamp);
  return date.toLocaleString();
};

const loadMoreMessages = async () => {
  if (isLoadingMoreMessages.value || !hasMoreMessages.value) return;
  
  // Store current scroll position to maintain it after loading
  const container = messagesContainer.value;
  if (container) {
    lastScrollTop.value = container.scrollTop;
  }
  
  await chatStore.fetchMoreMessages(props.conversationId);
  
  // Restore scroll position after loading more messages
  await nextTick();
  if (container) {
    container.scrollTop = lastScrollTop.value;
  }
};

const reconnectWebSocket = async () => {
  if (isReconnecting.value) return;
  
  isReconnecting.value = true;
  try {
    await chatStore.manualReconnect();
  } catch (error) {
    console.error("Failed to reconnect:", error);
  } finally {
    isReconnecting.value = false;
  }
};

const retryLoadMessages = async () => {
  if (isRetrying.value) return;
  
  isRetrying.value = true;
  chatStore.clearError();
  
  try {
    await chatStore.fetchConversationMessages(props.conversationId);
  } catch (error) {
    console.error("Retry load messages failed:", error);
  } finally {
    isRetrying.value = false;
  }
};

const scrollToBottom = (smooth = true) => {
  const container = messagesContainer.value;
  if (container) {
    // Use a more reliable approach to ensure DOM is updated
    const scrollToBottomNow = () => {
      const scrollOptions = {
        top: container.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      };
      console.log('Scrolling to bottom:', {
        scrollHeight: container.scrollHeight,
        clientHeight: container.clientHeight,
        currentScrollTop: container.scrollTop
      });
      container.scrollTo(scrollOptions);
    };
    
    // Try immediate scroll first
    scrollToBottomNow();
    
    // Also try after a small delay to ensure DOM is fully updated
    setTimeout(scrollToBottomNow, 10);
  }
};

const handleScroll = () => {
  const container = messagesContainer.value;
  if (!container) return;
  
  const { scrollTop, scrollHeight, clientHeight } = container;
  const isNearBottom = scrollHeight - scrollTop - clientHeight < 100;
  
  // Update auto-scroll behavior based on user's scroll position
  shouldAutoScroll.value = isNearBottom;
  
  // Mark visible messages as read when scrolling
  markVisibleMessagesAsRead();
  
  // Prevent scroll events from causing layout issues
  requestAnimationFrame(() => {
    // Ensure container maintains its size
    if (container.scrollHeight > container.clientHeight) {
      container.style.overflowY = 'auto';
    }
  });
};

// Mark new messages as read if they're from other users
const markNewMessagesAsRead = async (newMessages) => {
  if (!newMessages || newMessages.length === 0) return;
  
  const currentUserId = authStore.userInfo?.id;
  const unreadMessages = newMessages.filter(message => 
    !message.isRead && message.senderId !== currentUserId
  );
  
  if (unreadMessages.length > 0) {
    console.log(`Auto-marking ${unreadMessages.length} new messages as read`);
    
    // Mark messages as read in the store immediately for UI responsiveness
    unreadMessages.forEach(message => {
      chatStore.updateMessageReadStatus(props.conversationId, message.id, true);
    });
    
    // Send read status updates via WebSocket for real-time sync
    if (chatStore.isConnected) {
      unreadMessages.forEach(message => {
        try {
          chatStore.markMessageAsRead(message.id);
        } catch (error) {
          console.warn('Failed to send read status via WebSocket:', error);
        }
      });
    }
    
    // Update conversation as read to sync unread counts
    await chatStore.markConversationAsRead(props.conversationId);
  }
};

// Enhanced method to mark messages as read when they become visible
const markMessagesAsReadWhenVisible = async () => {
  if (!props.conversationId || !messagesContainer.value) return;
  
  const currentUserId = authStore.userInfo?.id;
  const container = messagesContainer.value;
  const messageElements = container.querySelectorAll('.message-wrapper');
  
  const unreadMessagesToMark = [];
  
  messageElements.forEach((element, index) => {
    const message = messages.value[index];
    if (!message || message.isRead || message.senderId === currentUserId) return;
    
    // Check if message is visible in viewport with improved detection
    const rect = element.getBoundingClientRect();
    const containerRect = container.getBoundingClientRect();
    
    const isVisible = (
      rect.top >= containerRect.top - 50 && // Add some buffer
      rect.bottom <= containerRect.bottom + 50 &&
      rect.left >= containerRect.left &&
      rect.right <= containerRect.right
    );
    
    if (isVisible) {
      unreadMessagesToMark.push(message);
    }
  });
  
  if (unreadMessagesToMark.length > 0) {
    console.log(`Auto-marking ${unreadMessagesToMark.length} visible messages as read`);
    
    // Mark messages as read in the store immediately
    unreadMessagesToMark.forEach(message => {
      chatStore.updateMessageReadStatus(props.conversationId, message.id, true);
    });
    
    // Send read status updates via WebSocket with debouncing
    if (chatStore.isConnected) {
      // Debounce multiple read status updates
      clearTimeout(markAsReadTimeout.value);
      markAsReadTimeout.value = setTimeout(() => {
        unreadMessagesToMark.forEach(message => {
          try {
            chatStore.markMessageAsRead(message.id);
          } catch (error) {
            console.warn('Failed to send read status via WebSocket:', error);
          }
        });
      }, 500);
    }
    
    // Update conversation as read to sync unread counts
    await chatStore.markConversationAsRead(props.conversationId);
  }
};

// Use the enhanced method for marking messages as read
const markVisibleMessagesAsRead = markMessagesAsReadWhenVisible;

// Watch for new messages and auto-scroll if appropriate
watch(
  () => messages.value.length,
  async (newLength, oldLength) => {
    if (newLength > oldLength && shouldAutoScroll.value) {
      // Auto-mark new messages as read if they're from other users
      const newMessages = messages.value.slice(oldLength);
      await markNewMessagesAsRead(newMessages);
      
      // Scroll to bottom after marking messages as read
      nextTick(() => {
        scrollToBottom();
      });
    }
  }
);

// Watch for messages becoming visible and mark them as read
watch(
  () => messages.value,
  async (newMessages) => {
    if (newMessages && newMessages.length > 0) {
      // Mark unread messages as read when viewing conversation
      await markVisibleMessagesAsRead();
    }
  },
  { deep: true }
);

// Watch for conversation changes
watch(
  () => props.conversationId,
  async (newConversationId) => {
    if (newConversationId) {
      // Reset auto-scroll when switching conversations
      shouldAutoScroll.value = true;
      
      // Load messages for the new conversation
      await chatStore.fetchConversationMessages(newConversationId);
      
      // Mark conversation as read
      await chatStore.markConversationAsRead(newConversationId);
      
      // Scroll to bottom after loading with multiple attempts to ensure it works
      await nextTick();
      scrollToBottom(false);
      
      // Also try scrolling after a short delay to ensure DOM is fully rendered
      setTimeout(() => {
        scrollToBottom(false);
      }, 100);
    }
  },
  { immediate: true }
);

// Real-time event handlers
const handleRealTimeMessage = (event) => {
  const message = event.detail;
  console.log("ChatConversationView: Real-time message received:", message);
  
  // Only handle messages for the current conversation
  if (message.conversationId === props.conversationId) {
    // Message will be added to store automatically by the store's event handler
    // We just need to scroll to bottom if appropriate
    if (shouldAutoScroll.value) {
      nextTick(() => {
        scrollToBottom();
      });
    }
  }
};

const handleRealTimeReadStatus = (event) => {
  const readData = event.detail;
  console.log("ChatConversationView: Real-time read status update:", readData);
  
  // Handle different types of read status updates
  if (readData.conversationId === props.conversationId || 
      (readData.messageId && messages.value.some(m => m.id === readData.messageId))) {
    
    // Update message read status in UI immediately for better responsiveness
    if (readData.messageId) {
      const message = messages.value.find(m => m.id === readData.messageId);
      if (message && !message.isRead) {
        console.log(`Updating read status for message ${readData.messageId} in UI`);
        // The store will handle the actual update, but we can trigger a reactive update
        chatStore.updateMessageReadStatus(props.conversationId, readData.messageId, true);
      }
    }
    
    // Handle conversation-level read status updates
    if (readData.conversationId && !readData.messageId) {
      console.log(`All messages in conversation ${readData.conversationId} marked as read`);
      // Mark all unread messages in this conversation as read
      const currentUserId = authStore.userInfo?.id;
      messages.value.forEach(message => {
        if (!message.isRead && message.senderId === currentUserId) {
          chatStore.updateMessageReadStatus(props.conversationId, message.id, true);
        }
      });
    }
  }
};

const handleWebSocketReconnection = (event) => {
  const status = event.detail;
  console.log("ChatConversationView: WebSocket reconnection status:", status);
  
  if (status.success && !status.reconnecting) {
    // Successful reconnection - refresh messages for current conversation
    console.log("WebSocket reconnected successfully, refreshing conversation messages...");
    if (props.conversationId) {
      chatStore.fetchConversationMessages(props.conversationId);
    }
  }
};

// Lifecycle
onMounted(async () => {
  // Set up scroll listener
  if (messagesContainer.value) {
    messagesContainer.value.addEventListener('scroll', handleScroll);
  }
  
  // Add real-time event listeners
  window.addEventListener("websocket-message", handleRealTimeMessage);
  window.addEventListener("websocket-message-read", handleRealTimeReadStatus);
  window.addEventListener("websocket-conversation-read", handleRealTimeReadStatus);
  window.addEventListener("websocket-read-status-update", handleRealTimeReadStatus);
  window.addEventListener("cross-tab-message-read", handleRealTimeReadStatus);
  window.addEventListener("websocket-reconnection-status", handleWebSocketReconnection);
  
  // Load initial messages if not already loaded
  if (props.conversationId && messages.value.length === 0) {
    await chatStore.fetchConversationMessages(props.conversationId);
    await chatStore.markConversationAsRead(props.conversationId);
    
    await nextTick();
    scrollToBottom(false);
    
    // Also try scrolling after a delay to ensure DOM is fully rendered
    setTimeout(() => {
      scrollToBottom(false);
    }, 100);
  }
});

onUnmounted(() => {
  // Clean up scroll listener
  if (messagesContainer.value) {
    messagesContainer.value.removeEventListener('scroll', handleScroll);
  }
  
  // Remove real-time event listeners
  window.removeEventListener("websocket-message", handleRealTimeMessage);
  window.removeEventListener("websocket-message-read", handleRealTimeReadStatus);
  window.removeEventListener("websocket-conversation-read", handleRealTimeReadStatus);
  window.removeEventListener("websocket-read-status-update", handleRealTimeReadStatus);
  window.removeEventListener("cross-tab-message-read", handleRealTimeReadStatus);
  window.removeEventListener("websocket-reconnection-status", handleWebSocketReconnection);
  
  // Clear any pending timeouts
  if (markAsReadTimeout.value) {
    clearTimeout(markAsReadTimeout.value);
  }
});
</script>

<style scoped>
.chat-conversation-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--color-background);
  overflow: hidden;
}

/* Conversation Header */
.conversation-header {
  flex-shrink: 0;
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  padding: 1rem 1.5rem;
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: var(--color-text);
  text-decoration: none;
  border-radius: 50%;
  transition: background-color var(--transition-fast);
}

.back-button:hover {
  background-color: var(--color-background-mute);
}

.back-button svg {
  width: 22px;
  height: 22px;
}

.participant-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.participant-avatar {
  width: 48px;
  height: 48px;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  flex-shrink: 0;
}

.avatar-icon {
  width: 24px;
  height: 24px;
}

.participant-details {
  flex: 1;
  min-width: 0;
}

.participant-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.25rem 0;
  line-height: 1.2;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator--connected {
  background-color: #4caf50;
}

.status-indicator--disconnected {
  background-color: #f44336;
}

.status-text {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

/* Connection Banner */
.connection-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
  padding: 0.75rem 1rem;
  background-color: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.2);
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  color: var(--color-text);
}

.connection-banner .warning-icon {
  width: 16px;
  height: 16px;
  color: #f44336;
  flex-shrink: 0;
}

.reconnect-btn {
  margin-left: auto;
  padding: 0.25rem 0.75rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex-shrink: 0;
}

.reconnect-btn:hover:not(:disabled) {
  background: var(--color-secondary);
}

.reconnect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Messages Container */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 0;
  scroll-behavior: smooth;
  min-height: 0;
}

/* Load More Messages */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 0 1.5rem 1rem;
}

.load-more-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.load-more-btn:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.load-more-icon {
  width: 16px;
  height: 16px;
}

/* Loading States */
.loading-more,
.messages-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1.5rem;
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

/* Empty State */
.messages-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1.5rem;
  text-align: center;
  color: var(--color-text-muted);
}

.messages-empty .empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1.5rem;
  opacity: 0.5;
}

.messages-empty p {
  font-size: 1.125rem;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.messages-empty span {
  font-size: 0.875rem;
}

/* Error State */
.messages-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
  text-align: center;
  color: var(--color-text-muted);
}

.messages-error .error-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 1rem;
  color: var(--color-error);
}

.messages-error p {
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 1rem;
}

.retry-btn {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.retry-btn:hover:not(:disabled) {
  background: var(--color-secondary);
}

.retry-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Messages List */
.messages-list {
  padding: 0 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

/* Date Separator */
.date-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 1.5rem 0;
  position: relative;
}

.date-separator::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--color-border);
  z-index: 1;
}

.date-text {
  background: var(--color-background);
  padding: 0 1rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 2;
}

/* Message Wrapper */
.message-wrapper {
  margin-bottom: 0.5rem;
}

.message-wrapper--first-in-group {
  margin-top: 1rem;
}

.message-wrapper--last-in-group {
  margin-bottom: 1rem;
}

.message-wrapper--own {
  display: flex;
  justify-content: flex-end;
}

.message-wrapper--other {
  display: flex;
  justify-content: flex-start;
}

/* Message Bubble */
.message-bubble {
  max-width: 70%;
  min-width: 120px;
  position: relative;
}

.message-wrapper--own .message-bubble {
  background: var(--color-primary);
  color: var(--color-background);
  border-radius: 1.125rem 1.125rem 0.25rem 1.125rem;
}

.message-wrapper--other .message-bubble {
  background: var(--color-background-soft);
  color: var(--color-text);
  border-radius: 1.125rem 1.125rem 1.125rem 0.25rem;
  border: 1px solid var(--color-border);
}

.message-wrapper--first-in-group.message-wrapper--own .message-bubble {
  border-radius: 1.125rem 1.125rem 0.25rem 1.125rem;
}

.message-wrapper--first-in-group.message-wrapper--other .message-bubble {
  border-radius: 1.125rem 1.125rem 1.125rem 0.25rem;
}

.message-wrapper--last-in-group.message-wrapper--own .message-bubble {
  border-radius: 1.125rem 0.25rem 1.125rem 1.125rem;
}

.message-wrapper--last-in-group.message-wrapper--other .message-bubble {
  border-radius: 0.25rem 1.125rem 1.125rem 1.125rem;
}

/* Sender Name */
.sender-name {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
  padding: 0 1rem;
}

/* Message Content */
.message-content {
  padding: 0.75rem 1rem 0.5rem;
}

.message-text {
  margin: 0;
  line-height: 1.4;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-wrapper--own .message-text {
  color: var(--color-background);
}

.message-wrapper--other .message-text {
  color: var(--color-text);
}

/* Message Footer */
.message-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0 1rem 0.5rem;
  font-size: 0.75rem;
}

.message-time {
  opacity: 0.7;
  font-weight: 500;
}

.message-wrapper--own .message-time {
  color: var(--color-background);
}

.message-wrapper--other .message-time {
  color: var(--color-text-muted);
}

/* Message Status */
.message-status {
  display: flex;
  align-items: center;
  margin-left: 0.5rem;
  flex-shrink: 0;
}

.status-icon {
  width: 14px;
  height: 14px;
  opacity: 0.7;
  transition: opacity var(--transition-fast);
}

.status-icon--sending {
  color: var(--color-text-muted);
  animation: spin 1s linear infinite;
}

.status-icon--sent {
  color: var(--color-text-muted);
}

.status-icon--delivered {
  color: var(--color-text-muted);
}

.status-icon--read {
  position: relative;
  display: flex;
  align-items: center;
}

.read-check {
  width: 14px;
  height: 14px;
  color: var(--color-primary);
}

.read-check--first {
  position: relative;
  z-index: 1;
}

.read-check--second {
  position: absolute;
  left: 3px;
  z-index: 2;
}

/* Enhanced read status styling for own messages */
.message-wrapper--own .status-icon--sent,
.message-wrapper--own .status-icon--delivered,
.message-wrapper--own .status-icon--sending {
  color: var(--color-background);
}

.message-wrapper--own .read-check {
  color: #4caf50;
}

/* Loading Spinners */
.loading-spinner {
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}

.loading-spinner-small {
  width: 14px;
  height: 14px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .conversation-header {
    padding: 1rem;
  }
  
  .participant-avatar {
    width: 40px;
    height: 40px;
  }
  
  .avatar-icon {
    width: 20px;
    height: 20px;
  }
  
  .participant-name {
    font-size: 1rem;
  }
  
  .messages-list {
    padding: 0 1rem;
  }
  
  .message-bubble {
    max-width: 85%;
  }
  
  .connection-banner {
    margin-top: 0.5rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.8125rem;
  }
  
  .reconnect-btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.6875rem;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .messages-container {
    scroll-behavior: auto;
  }
  
  .loading-spinner,
  .loading-spinner-small,
  .status-icon--sending {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .message-wrapper--other .message-bubble {
    border-width: 2px;
  }
  
  .date-separator::before {
    height: 2px;
  }
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .message-wrapper--own .message-bubble {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
  
  .message-wrapper--other .message-bubble {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}

/* Enhanced focus states for better accessibility */
.load-more-btn:focus-visible,
.retry-btn:focus-visible,
.reconnect-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

/* Enhanced hover states with consistent transitions */
.load-more-btn:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.retry-btn:hover {
  background: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.reconnect-btn:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* Consistent spacing and typography */
.participant-name {
  font-size: 1.125rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.status-text {
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.message-text {
  font-size: 0.9375rem;
  line-height: 1.4;
  letter-spacing: -0.005em;
}

.message-time {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.sender-name {
  font-size: 0.8125rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.date-text {
  font-size: 0.8125rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* Enhanced visual hierarchy */
.conversation-header {
  background: linear-gradient(135deg, var(--color-background-soft), var(--color-background-mute));
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.participant-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
}

.connection-banner {
  background: linear-gradient(135deg, rgba(244, 67, 54, 0.1), rgba(244, 67, 54, 0.05));
  border: 1px solid rgba(244, 67, 54, 0.2);
  box-shadow: var(--shadow-sm);
}

.warning-icon {
  color: var(--color-error);
  filter: drop-shadow(0 1px 2px rgba(244, 67, 54, 0.2));
}

/* Improved message bubble styling */
.message-wrapper--own .message-bubble {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  border-color: transparent;
  box-shadow: var(--shadow-md);
}

.message-wrapper--own .message-text {
  color: var(--color-background);
}

.message-wrapper--own .message-time {
  color: rgba(255, 255, 255, 0.8);
}

/* Enhanced message status indicators */
.status-icon--sent {
  color: var(--color-text-muted);
}

.status-icon--delivered {
  color: var(--color-primary);
}

.status-icon--read {
  color: var(--color-success);
}

.status-icon--sending {
  color: var(--color-text-muted);
  opacity: 0.6;
}

.read-check--first,
.read-check--second {
  color: var(--color-success);
}

.read-check--second {
  margin-left: -4px;
}

/* Better date separator styling */
.date-separator {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  margin: 1.5rem 0;
}

.date-text {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
}

/* Enhanced loading and error states */
.messages-loading,
.messages-empty,
.messages-error {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  margin: 2rem;
  box-shadow: var(--shadow-sm);
}

.empty-icon,
.error-icon {
  color: var(--color-text-muted);
  opacity: 0.6;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

/* Improved button styling */
.load-more-btn {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.retry-btn {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
}

.reconnect-btn {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
}

/* Enhanced connection status indicators */
.status-indicator--connected {
  background-color: var(--color-success);
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.status-indicator--disconnected {
  background-color: var(--color-error);
  box-shadow: 0 0 0 2px rgba(244, 67, 54, 0.2);
}

/* Better loading states */
.loading-more {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  margin: 1rem;
  padding: 1rem;
}

.loading-spinner {
  color: var(--color-primary);
  opacity: 0.8;
  filter: drop-shadow(0 2px 4px rgba(6, 147, 227, 0.2));
}

.loading-spinner-small {
  color: var(--color-primary);
  opacity: 0.8;
}

/* Improved scrollbar styling */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: var(--color-background-soft);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 3px;
  transition: background var(--transition-fast);
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted);
}
</style>