<template>
  <div class="chat-conversation-list">
    <!-- Loading State -->
    <div
      v-if="loading && conversations.length === 0"
      class="conversation-loading"
    >
      <ChatErrorBoundary
        error-title="Loading Error"
        error-message="Failed to load conversation list"
        :retryable="false"
        @error="handleLoadingError"
      >
        <ChatLoadingSkeleton 
          type="conversation-list" 
          :count="6" 
          :animated="true"
        />
      </ChatErrorBoundary>
    </div>

    <!-- Empty State -->
    <div v-else-if="conversations.length === 0" class="conversation-empty">
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
      <h3>No conversations yet</h3>
      <p>Start a conversation with a lawyer to see messages here</p>
      <div class="empty-actions">
        <button @click="$router.push('/find-lawyer')" class="btn btn-primary">
          Find a Lawyer
        </button>
      </div>
    </div>

    <!-- Conversations List -->
    <div v-else class="conversation-items">
      <div
        v-for="(conversation, index) in conversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{
          'conversation-item--unread':
            getConversationUnreadCount(conversation.id) > 0,
          'conversation-item--focused': focusedIndex === index,
        }"
        @click="handleConversationClick(conversation)"
        role="button"
        tabindex="0"
        @keydown.enter="handleConversationClick(conversation)"
        @keydown.space.prevent="handleConversationClick(conversation)"
        @keydown.down="focusNextConversation(index)"
        @keydown.up="focusPreviousConversation(index)"
        @keydown.home="focusFirstConversation"
        @keydown.end="focusLastConversation"
        :aria-label="getConversationAriaLabel(conversation)"
        :ref="(el) => setConversationRef(el, index)"
      >
        <!-- Participant Avatar -->
        <div class="conversation-avatar">
          <div class="avatar-circle">
            {{ getParticipantInitials(conversation) }}
          </div>
          <div
            v-if="getConversationUnreadCount(conversation.id) > 0"
            class="avatar-unread-indicator"
            :aria-label="`${getConversationUnreadCount(conversation.id)} unread messages`"
          ></div>
        </div>

        <!-- Conversation Content -->
        <div class="conversation-content">
          <div class="conversation-header">
            <h4 class="participant-name">
              {{ getParticipantName(conversation) }}
            </h4>
            <time
              class="conversation-time"
              :datetime="conversation.latestMessage?.createdAt || conversation.updatedAt"
              :title="
                formatFullDateTime(
                  conversation.latestMessage?.createdAt || conversation.updatedAt
                )
              "
            >
              {{
                formatTime(
                  conversation.latestMessage?.createdAt || conversation.updatedAt
                )
              }}
            </time>
          </div>

          <div class="conversation-preview">
            <p class="last-message">
              {{ conversation.latestMessage?.content || "No messages yet" }}
            </p>
            <div
              v-if="getConversationUnreadCount(conversation.id) > 0"
              class="unread-badge"
              :aria-label="`${getConversationUnreadCount(conversation.id)} unread messages`"
            >
              {{
                getConversationUnreadCount(conversation.id) > 9
                  ? "9+"
                  : getConversationUnreadCount(conversation.id)
              }}
            </div>
          </div>
        </div>

        <!-- Conversation Actions -->
        <div class="conversation-actions">
          <button
            @click.stop="handleMarkAsRead(conversation)"
            v-if="getConversationUnreadCount(conversation.id) > 0"
            class="action-btn mark-read-btn"
            :aria-label="`Mark conversation with ${getParticipantName(conversation)} as read`"
            title="Mark as read"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M20 6L9 17l-5-5"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>

          <button
            @click.stop="handleConversationClick(conversation)"
            class="action-btn view-btn"
            :aria-label="`View conversation with ${getParticipantName(conversation)}`"
            title="View conversation"
          >
            <svg
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
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from "vue";
import { useChatStore } from "../stores/chat";
import ChatErrorBoundary from "./ChatErrorBoundary.vue";
import ChatLoadingSkeleton from "./ChatLoadingSkeleton.vue";

// Props
const props = defineProps({
  conversations: {
    type: Array,
    required: true,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

// Emits
const emit = defineEmits(["conversation-click", "loading-error"]);

// Stores
const chatStore = useChatStore();

// Reactive state
const focusedIndex = ref(-1);
const conversationRefs = ref([]);

// Computed properties
const getConversationUnreadCount = computed(
  () => chatStore.getConversationUnreadCount
);

// Methods
const getParticipantName = (conversation) => {
  // Try multiple possible field names from the backend
  return conversation.participantTwoName || 
         conversation.otherParticipantName || 
         conversation.participantName ||
         'Unknown User';
};

const getParticipantInitials = (conversation) => {
  const name = getParticipantName(conversation);
  const nameParts = name.split(" ");

  if (nameParts.length >= 2) {
    return `${nameParts[0][0]}${nameParts[1][0]}`.toUpperCase();
  } else if (nameParts.length === 1) {
    return nameParts[0].substring(0, 2).toUpperCase();
  } else {
    return "UN";
  }
};

const getConversationAriaLabel = (conversation) => {
  const participantName = getParticipantName(conversation);
  const unreadCount = getConversationUnreadCount.value(conversation.id);
  const lastMessage = conversation.latestMessage?.content || "No messages yet";
  const time = formatTime(
    conversation.latestMessage?.createdAt || conversation.updatedAt
  );

  let label = `Conversation with ${participantName}. Last message: ${lastMessage}. ${time}.`;

  if (unreadCount > 0) {
    label += ` ${unreadCount} unread message${unreadCount > 1 ? "s" : ""}.`;
  }

  return label;
};

const formatTime = (timestamp) => {
  if (!timestamp || new Date(timestamp).getTime() === 0) return "Invalid time";

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
  } else if (diffInMinutes < 10080) {
    // 7 days
    const days = Math.floor(diffInMinutes / 1440);
    return `${days}d ago`;
  } else {
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    });
  }
};

const formatFullDateTime = (timestamp) => {
  if (!timestamp) return "";

  const date = new Date(timestamp);
  return date.toLocaleString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const handleConversationClick = async (conversation) => {
  // Mark conversation as read if it has unread messages
  if (getConversationUnreadCount.value(conversation.id) > 0) {
    await handleMarkAsRead(conversation);
  }

  // Emit the click event for parent components to handle
  emit("conversation-click", conversation);
};

const handleMarkAsRead = async (conversation) => {
  try {
    await chatStore.markConversationAsRead(conversation.id);
  } catch (error) {
    console.error("Failed to mark conversation as read:", error);
    // Could show a toast notification here
  }
};

// Keyboard navigation methods
const setConversationRef = (el, index) => {
  if (el) {
    conversationRefs.value[index] = el;
  }
};

const focusConversation = async (index) => {
  if (index >= 0 && index < props.conversations.length) {
    focusedIndex.value = index;
    await nextTick();
    const element = conversationRefs.value[index];
    if (element) {
      element.focus();
    }
  }
};

const focusNextConversation = async (currentIndex) => {
  const nextIndex = currentIndex + 1;
  if (nextIndex < props.conversations.length) {
    await focusConversation(nextIndex);
  }
};

const focusPreviousConversation = async (currentIndex) => {
  const prevIndex = currentIndex - 1;
  if (prevIndex >= 0) {
    await focusConversation(prevIndex);
  }
};

const focusFirstConversation = async () => {
  if (props.conversations.length > 0) {
    await focusConversation(0);
  }
};

const focusLastConversation = async () => {
  if (props.conversations.length > 0) {
    await focusConversation(props.conversations.length - 1);
  }
};

// Error handling methods
const handleLoadingError = (errorInfo) => {
  console.error('ChatConversationList loading error:', errorInfo);
  
  // Emit error to parent component for handling
  emit('loading-error', errorInfo);
};
</script>

<style scoped>
.chat-conversation-list {
  background: var(--color-background);
}

/* Loading State */
.conversation-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
  color: var(--color-text-muted);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  margin-bottom: 1rem;
  color: var(--color-primary);
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

/* Empty State */
.conversation-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  color: var(--color-text-muted);
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1.5rem;
  opacity: 0.6;
}

.conversation-empty h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.75rem 0;
}

.conversation-empty p {
  font-size: 1rem;
  margin: 0 0 2rem 0;
  max-width: 400px;
  line-height: 1.5;
}

.empty-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

/* Conversation Items */
.conversation-items {
  display: flex;
  flex-direction: column;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  border-bottom: 1px solid var(--color-border);
  position: relative;
  background: var(--color-background);
}

.conversation-item:hover {
  background: var(--color-background-soft);
}

.conversation-item:focus {
  outline: none;
  background: var(--color-background-mute);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.conversation-item--focused {
  background: var(--color-background-mute);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.conversation-item--unread {
  background: var(--color-background-mute);
  border-left: 3px solid var(--color-primary);
  font-weight: 500;
}

.conversation-item--unread:hover {
  background: var(--color-background-soft);
}

.conversation-item:last-child {
  border-bottom: none;
}

/* Avatar */
.conversation-avatar {
  position: relative;
  flex-shrink: 0;
}

.avatar-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.conversation-item--unread .avatar-circle {
  background: var(--color-secondary);
  box-shadow: 0 0 0 2px var(--color-primary);
}

.avatar-unread-indicator {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  background: var(--color-error);
  border: 2px solid var(--color-background);
  border-radius: 50%;
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

/* Conversation Content */
.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 0.25rem;
}

.participant-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.conversation-item--unread .participant-name {
  font-weight: 700;
  color: var(--color-heading);
}

.conversation-time {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  flex-shrink: 0;
  margin-left: 1rem;
}

.conversation-item--unread .conversation-time {
  color: var(--color-primary);
  font-weight: 600;
}

.conversation-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.last-message {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.conversation-item--unread .last-message {
  color: var(--color-text);
  font-weight: 500;
}

.unread-badge {
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
}

/* Conversation Actions */
.conversation-actions {
  display: flex;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity var(--transition-fast);
  flex-shrink: 0;
}

.conversation-item:hover .conversation-actions,
.conversation-item:focus .conversation-actions,
.conversation-item--focused .conversation-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: var(--color-background-soft);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: var(--color-primary);
  color: var(--color-background);
  transform: scale(1.1);
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

.mark-read-btn:hover {
  background: #4caf50;
}

.view-btn:hover {
  background: var(--color-primary);
}

/* Button Styles */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid transparent;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-primary {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-secondary);
  border-color: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn svg {
  width: 16px;
  height: 16px;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .conversation-item {
    padding: 1rem;
    gap: 0.75rem;
  }

  .avatar-circle {
    width: 40px;
    height: 40px;
    font-size: 0.75rem;
  }

  .participant-name {
    font-size: 0.9375rem;
  }

  .last-message {
    font-size: 0.8125rem;
  }

  .conversation-time {
    font-size: 0.6875rem;
  }

  .conversation-actions {
    opacity: 1; /* Always show on mobile */
  }

  .action-btn {
    width: 36px;
    height: 36px;
  }

  .action-btn svg {
    width: 18px;
    height: 18px;
  }
}

@media (max-width: 480px) {
  .conversation-item {
    padding: 0.875rem;
  }

  .conversation-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .conversation-time {
    margin-left: 0;
  }

  .conversation-preview {
    margin-top: 0.5rem;
  }

  .empty-actions {
    flex-direction: column;
    align-items: center;
  }
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .conversation-item,
  .action-btn,
  .avatar-unread-indicator {
    transition: none;
  }

  .avatar-unread-indicator {
    animation: none;
  }

  .loading-spinner {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .conversation-item--unread {
    border-left-width: 4px;
  }

  .unread-badge {
    border: 1px solid var(--color-text);
  }

  .avatar-unread-indicator {
    border-width: 3px;
  }
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .avatar-unread-indicator {
    border-color: var(--color-background);
  }
}

html.dark .avatar-unread-indicator {
  border-color: var(--color-background) !important;
}

/* Enhanced focus states for better accessibility */
.conversation-item:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: -2px;
  box-shadow: inset 0 0 0 2px var(--color-primary);
}

.action-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

.btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

/* Enhanced hover states with consistent transitions */
.conversation-item:hover {
  background: var(--color-background-soft);
  transform: translateX(2px);
  box-shadow: var(--shadow-sm);
}

.conversation-item--unread:hover {
  background: var(--color-background-soft);
  box-shadow: var(--shadow-md);
  transform: translateX(2px);
}

.action-btn:hover {
  background: var(--color-primary);
  color: var(--color-background);
  transform: scale(1.1);
  box-shadow: var(--shadow-sm);
}

.mark-read-btn:hover {
  background: var(--color-success);
  transform: scale(1.1);
}

/* Consistent spacing and typography */
.participant-name {
  font-size: 1rem;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.01em;
}

.conversation-item--unread .participant-name {
  font-weight: 700;
  letter-spacing: -0.015em;
}

.last-message {
  font-size: 0.875rem;
  line-height: 1.4;
  letter-spacing: -0.005em;
}

.conversation-time {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.conversation-item--unread .conversation-time {
  font-weight: 600;
}

/* Enhanced visual hierarchy */
.conversation-empty h3 {
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.conversation-empty p {
  font-size: 1rem;
  line-height: 1.5;
  letter-spacing: -0.005em;
}

/* Improved loading states */
.conversation-loading {
  padding: 4rem 2rem;
}

.loading-spinner {
  color: var(--color-primary);
  opacity: 0.8;
}

/* Better avatar styling */
.avatar-circle {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
  font-weight: 700;
  letter-spacing: 0.02em;
}

.conversation-item--unread .avatar-circle {
  background: linear-gradient(135deg, var(--color-secondary), var(--color-accent));
  box-shadow: 0 0 0 3px var(--color-primary), var(--shadow-md);
}

/* Enhanced unread badge */
.unread-badge {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
  font-weight: 700;
  letter-spacing: 0.01em;
}

/* Improved action buttons */
.conversation-actions {
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.conversation-item:hover .conversation-actions,
.conversation-item:focus .conversation-actions,
.conversation-item--focused .conversation-actions {
  opacity: 1;
}

.action-btn {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.action-btn:hover {
  border-color: transparent;
  box-shadow: var(--shadow-md);
}
</style>
