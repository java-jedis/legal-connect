<template>
  <div class="chat-conversation-detail-view">
    <!-- Navigation Breadcrumbs -->
    <div class="breadcrumb-container">
      <nav class="breadcrumb" aria-label="Breadcrumb navigation">
        <ol class="breadcrumb-list">
          <li class="breadcrumb-item">
            <router-link to="/chat" class="breadcrumb-link">
              <svg
                class="breadcrumb-icon"
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
              Messages
            </router-link>
          </li>
          <li class="breadcrumb-separator" aria-hidden="true">
            <svg
              class="separator-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M9 18l6-6-6-6"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </li>
          <li class="breadcrumb-item breadcrumb-item--current">
            <span class="breadcrumb-current" :title="participantName">
              {{ participantName }}
            </span>
          </li>
        </ol>
      </nav>
    </div>

    <!-- Loading State -->
    <div v-if="isInitialLoading" class="loading-container">
      <ChatErrorBoundary
        :retryable="false"
        @error="handleErrorBoundaryError"
      >
        <div class="loading-content">
          <ChatLoadingSkeleton 
            type="conversation-header" 
            :animated="true"
          />
          <div class="loading-messages">
            <ChatLoadingSkeleton 
              type="message-list" 
              :count="8" 
              :animated="true"
            />
          </div>
        </div>
      </ChatErrorBoundary>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <div class="error-content">
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
        <h2>{{ errorTitle }}</h2>
        <p>{{ errorMessage }}</p>
        <div class="error-actions">
          <button @click="retryLoad" class="retry-btn" :disabled="isRetrying">
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
            <template v-else>Try Again</template>
          </button>
          <router-link to="/chat" class="back-btn">
            Back to Messages
          </router-link>
        </div>
      </div>
    </div>

    <!-- Chat Interface -->
    <div v-else class="chat-interface">
      <ChatErrorBoundary
        error-title="Chat Interface Error"
        error-message="An error occurred while displaying the chat interface."
        :show-details="true"
        :retryable="true"
        @retry="handleChatInterfaceRetry"
        @error="handleChatInterfaceError"
      >
        <!-- Conversation View -->
        <div class="conversation-container" ref="conversationContainerRef">
          <ChatConversationView
            :conversation-id="conversationId"
            :conversation="conversation"
          />
        </div>

        <!-- Message Input -->
        <div class="message-input-container">
          <ChatMessageInput
            ref="messageInputRef"
            :placeholder="`Message ${participantName}...`"
            :is-sending="isSendingMessage"
            :error="sendError"
            @send="handleSendMessage"
          />
        </div>
      </ChatErrorBoundary>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import ChatConversationView from "../components/ChatConversationView.vue";
import ChatErrorBoundary from "../components/ChatErrorBoundary.vue";
import ChatLoadingSkeleton from "../components/ChatLoadingSkeleton.vue";
import ChatMessageInput from "../components/ChatMessageInput.vue";
import { useAuthStore } from "../stores/auth";
import { useChatStore } from "../stores/chat";

// Router and route
const route = useRoute();
const router = useRouter();

// Stores
const chatStore = useChatStore();
const authStore = useAuthStore();

// Refs
const messageInputRef = ref(null);
const conversationContainerRef = ref(null);
const isInitialLoading = ref(true);
const isRetrying = ref(false);
const sendError = ref(null);

// Props from route
const conversationId = computed(() => route.params.id);

// Computed properties
const conversation = computed(() => {
  return chatStore.conversations.find(c => c.id === conversationId.value) || null;
});

const participantName = computed(() => {
  if (conversation.value) {
    return getParticipantName(conversation.value);
  }
  return 'Unknown User';
});

const error = computed(() => {
  // Check for various error conditions
  if (!conversationId.value) {
    return 'invalid_id';
  }
  
  if (!isInitialLoading.value && !conversation.value && chatStore.conversations.length > 0) {
    return 'not_found';
  }
  
  if (chatStore.error && !isInitialLoading.value) {
    return 'load_failed';
  }
  
  return null;
});

const errorTitle = computed(() => {
  switch (error.value) {
    case 'invalid_id':
      return 'Invalid Conversation';
    case 'not_found':
      return 'Conversation Not Found';
    case 'load_failed':
      return 'Failed to Load Conversation';
    default:
      return 'Error';
  }
});

const errorMessage = computed(() => {
  switch (error.value) {
    case 'invalid_id':
      return 'The conversation ID is invalid or missing.';
    case 'not_found':
      return 'This conversation does not exist or you do not have access to it.';
    case 'load_failed':
      return chatStore.error || 'Unable to load the conversation. Please check your connection and try again.';
    default:
      return 'An unexpected error occurred.';
  }
});

const isSendingMessage = computed(() => chatStore.isSendingMessage);

// Methods
const scrollToBottom = () => {
  nextTick(() => {
    const container = conversationContainerRef.value;
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  });
};

const getParticipantName = (conversation) => {
  // Try multiple possible field names from the backend
  return conversation.participantTwoName || 
         conversation.otherParticipantName || 
         conversation.participantName ||
         'Unknown User';
};

const getReceiverId = (conversation) => {
  return conversation.participantTwoId || null;
};

const loadConversationData = async () => {
  if (!conversationId.value) {
    isInitialLoading.value = false;
    return;
  }

  try {
    // Clear any previous errors
    chatStore.clearError();
    sendError.value = null;

    // If conversations are not loaded yet, load them first
    if (chatStore.conversations.length === 0) {
      await chatStore.fetchConversations();
    }

    // Check if conversation exists after loading conversations
    const conv = chatStore.conversations.find(c => c.id === conversationId.value);
    if (!conv) {
      isInitialLoading.value = false;
      return;
    }

    // Load messages for this conversation
    await chatStore.fetchConversationMessages(conversationId.value);

    // Mark conversation as read
    await chatStore.markConversationAsRead(conversationId.value);

    // Set as current conversation
    chatStore.setCurrentConversation(conv);

  } catch (err) {
    console.error('Error loading conversation data:', err);
  } finally {
    isInitialLoading.value = false;
  }
};

const retryLoad = async () => {
  if (isRetrying.value) return;
  
  isRetrying.value = true;
  isInitialLoading.value = true;
  
  try {
    await loadConversationData();
  } finally {
    isRetrying.value = false;
  }
};

const handleSendMessage = async (content) => {
  if (!conversation.value || !content.trim()) {
    return;
  }

  // Clear any previous send errors
  sendError.value = null;

  try {
    const receiverId = getReceiverId(conversation.value);
    const result = await chatStore.sendMessage(receiverId, content.trim());

    if (!result.success) {
      sendError.value = result.message || 'Failed to send message';
    }
  } catch (err) {
    console.error('Error sending message:', err);
    sendError.value = err.message || 'Failed to send message';
  }
};

const handleErrorBoundaryError = (errorInfo) => {
  console.error('ChatConversationDetailView error boundary caught:', errorInfo);
  
  // Set a generic error state
  chatStore.setError('An unexpected error occurred while loading the conversation');
  isInitialLoading.value = false;
};

const handleChatInterfaceRetry = async () => {
  console.log('Retrying chat interface...');
  
  // Clear errors and reload conversation data
  chatStore.clearError();
  sendError.value = null;
  
  try {
    await loadConversationData();
  } catch (err) {
    console.error('Chat interface retry failed:', err);
    chatStore.setError('Failed to reload chat interface');
  }
};

const handleChatInterfaceError = (errorInfo) => {
  console.error('Chat interface error boundary caught:', errorInfo);
  
  // Set a specific error for chat interface
  chatStore.setError('Chat interface encountered an error');
};

const handleRouteChange = async () => {
  // Reset state when route changes
  isInitialLoading.value = true;
  sendError.value = null;
  
  // Clear current conversation
  chatStore.clearCurrentConversation();
  
  // Load new conversation data
  await loadConversationData();
};

// Watchers
watch(
  () => conversationId.value,
  async (newId, oldId) => {
    if (newId !== oldId) {
      await handleRouteChange();
    }
  }
);

// Watch for new messages and scroll to bottom
watch(
  () => conversation.value?.messages?.length,
  () => {
    scrollToBottom();
  }
);

// Clear send error when chat store error changes
watch(
  () => chatStore.error,
  () => {
    if (!chatStore.error) {
      sendError.value = null;
    }
  }
);

// Lifecycle
onMounted(async () => {
  // Validate authentication
  if (!authStore.isLoggedIn) {
    router.push('/login');
    return;
  }

  // Load conversation data
  await loadConversationData();

  // Focus message input after loading
  if (messageInputRef.value && !error.value) {
    setTimeout(() => {
      messageInputRef.value?.focusInput();
    }, 100);
  }
});

onUnmounted(() => {
  // Clear current conversation when leaving
  chatStore.clearCurrentConversation();
  
  // Clear any send errors
  sendError.value = null;
});

// Set page title
watch(
  () => participantName.value,
  (name) => {
    if (name && name !== 'Unknown User') {
      document.title = `Chat with ${name} - LegalConnect`;
    } else {
      document.title = 'Chat - LegalConnect';
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.chat-conversation-detail-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--color-background);
}

/* Breadcrumb Navigation */
.breadcrumb-container {
  flex-shrink: 0;
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  padding: 1rem 1.5rem;
}

.breadcrumb {
  max-width: 1200px;
  margin: 0 auto;
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
}

.breadcrumb-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

.breadcrumb-link:hover {
  background: var(--color-background-mute);
  color: var(--color-secondary);
}

.breadcrumb-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.breadcrumb-separator {
  display: flex;
  align-items: center;
  color: var(--color-text-muted);
}

.separator-icon {
  width: 14px;
  height: 14px;
}

.breadcrumb-item--current {
  color: var(--color-text);
}

.breadcrumb-current {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-heading);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Loading State */
.loading-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 1.5rem;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  text-align: center;
  color: var(--color-text-muted);
}

.loading-content p {
  font-size: 1rem;
  font-weight: 500;
  margin: 0;
}

/* Error State */
.error-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 1.5rem;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  text-align: center;
  max-width: 400px;
}

.error-icon {
  width: 64px;
  height: 64px;
  color: var(--color-error);
}

.error-content h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.error-content p {
  font-size: 1rem;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0;
}

.error-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.retry-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.retry-btn:hover:not(:disabled) {
  background: var(--color-secondary);
}

.retry-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  padding: 0.75rem 1.5rem;
  background: var(--color-background-soft);
  color: var(--color-text);
  text-decoration: none;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.back-btn:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* Chat Interface */
.chat-interface {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* Allow flex children to shrink */
}

.conversation-container {
  flex: 1;
  min-height: 0; /* Allow flex children to shrink */
  overflow-y: auto; /* Allow vertical scrolling */
}

.message-input-container {
  flex-shrink: 0;
  position: relative; /* Ensure it stays in place */
  z-index: 10; /* Keep it above other content */
}

/* Loading Spinners */
.loading-spinner {
  width: 32px;
  height: 32px;
  animation: spin 1s linear infinite;
  color: var(--color-primary);
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
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
  .breadcrumb-container {
    padding: 0.75rem 1rem;
  }
  
  .breadcrumb-current {
    max-width: 150px;
  }
  
  .loading-container,
  .error-container {
    padding: 2rem 1rem;
  }
  
  .error-content {
    gap: 1rem;
  }
  
  .error-icon {
    width: 48px;
    height: 48px;
  }
  
  .error-content h2 {
    font-size: 1.25rem;
  }
  
  .error-content p {
    font-size: 0.875rem;
  }
  
  .error-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .retry-btn,
  .back-btn {
    width: 100%;
    justify-content: center;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner,
  .loading-spinner-small {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .breadcrumb-container {
    border-bottom-width: 2px;
  }
  
  .back-btn {
    border-width: 2px;
  }
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .breadcrumb-container {
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }
  
  .retry-btn {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
  
  .back-btn {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}

/* Focus management for accessibility */
.breadcrumb-link:focus,
.retry-btn:focus,
.back-btn:focus {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* Print styles */
@media print {
  .breadcrumb-container,
  .message-input-container {
    display: none;
  }
  
  .chat-interface {
    height: auto;
  }
}

/* Enhanced focus states for better accessibility */
.breadcrumb-link:focus-visible,
.retry-btn:focus-visible,
.back-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

/* Enhanced hover states with consistent transitions */
.breadcrumb-link:hover {
  background: var(--color-background-mute);
  color: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.retry-btn:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.back-btn:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* Consistent spacing and typography */
.breadcrumb-link {
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.breadcrumb-current {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.error-content h2 {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.error-content p {
  font-size: 1rem;
  line-height: 1.5;
  letter-spacing: -0.005em;
}

.retry-btn,
.back-btn {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* Enhanced visual hierarchy */
.breadcrumb-container {
  background: linear-gradient(135deg, var(--color-background-soft), var(--color-background-mute));
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.breadcrumb-link {
  background: transparent;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

.breadcrumb-current {
  background: rgba(255, 255, 255, 0.5);
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius);
}

/* Improved loading and error states */
.loading-container,
.error-container {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  margin: 2rem;
  box-shadow: var(--shadow-lg);
}

.loading-content {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  padding: 2rem;
}

.error-content {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 3rem 2rem;
  box-shadow: var(--shadow-lg);
}

.error-icon {
  color: var(--color-error);
  filter: drop-shadow(0 2px 4px rgba(239, 68, 68, 0.2));
  background: rgba(239, 68, 68, 0.1);
  border-radius: 50%;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

/* Better button styling */
.retry-btn {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
}

.back-btn {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

/* Enhanced loading spinner */
.loading-spinner {
  color: var(--color-primary);
  opacity: 0.8;
  filter: drop-shadow(0 2px 4px rgba(6, 147, 227, 0.2));
}

.loading-spinner-small {
  color: var(--color-primary);
  opacity: 0.8;
}

/* Improved chat interface styling */
.chat-interface {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  margin: 1rem 2rem;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.conversation-container {
  background: var(--color-background);
}

.message-input-container {
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
}

/* Better responsive design */
@media (max-width: 768px) {
  .chat-interface {
    margin: 0.5rem 1rem;
  }
  
  .loading-container,
  .error-container {
    margin: 1rem;
  }
  
  .breadcrumb-container {
    padding: 0.75rem 1rem;
  }
  
  .breadcrumb-current {
    max-width: 120px;
  }
}

@media (max-width: 480px) {
  .chat-interface {
    margin: 0.25rem 0.5rem;
    border-radius: var(--border-radius);
  }
  
  .loading-container,
  .error-container {
    margin: 0.5rem;
  }
  
  .error-content {
    padding: 2rem 1rem;
  }
  
  .breadcrumb-current {
    max-width: 100px;
  }
}
</style>