<template>
  <div class="chat-layout">
    <!-- Main Chat Area -->
    <div class="chat-main">
      <!-- Loading State -->
      <div v-if="isInitialLoading" class="loading-container">
        <ChatLoadingSkeleton type="message-list" :count="8" :animated="true" />
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-container">
        <div class="error-content">
          <svg class="error-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <h2>{{ errorTitle }}</h2>
          <p>{{ errorMessage }}</p>
          <div class="error-actions">
            <button @click="retryLoad" class="retry-btn" :disabled="isRetrying">
              <template v-if="isRetrying">Retrying...</template>
              <template v-else>Try Again</template>
            </button>
          </div>
        </div>
      </div>

      <!-- Messages Area -->
      <div v-else class="chat-messages" ref="conversationContainerRef">
        <ChatConversationView
          :conversation-id="conversationId"
          :conversation="conversation"
          class="conversation-view"
        />
      </div>

      <!-- Message Input -->
      <div class="chat-input-container">
        <ChatMessageInput
          ref="messageInputRef"
          :placeholder="`Message ${participantName}...`"
          :is-sending="isSendingMessage"
          :error="sendError"
          @send="handleSendMessage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import ChatConversationView from "@/components/chat/ChatConversationView.vue";
import ChatLoadingSkeleton from "@/components/chat/ChatLoadingSkeleton.vue";
import ChatMessageInput from "@/components/chat/ChatMessageInput.vue";
import { useAuthStore } from "@/stores/auth";
import { useChatStore } from "@/stores/chat";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

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
  return (
    chatStore.conversations.find((c) => c.id === conversationId.value) || null
  );
});

const participantName = computed(() => {
  if (conversation.value) {
    return getParticipantName(conversation.value);
  }
  return "Unknown User";
});

const participantInitial = computed(() => {
  return participantName.value.charAt(0).toUpperCase();
});

const error = computed(() => {
  if (!conversationId.value) return "invalid_id";
  if (!isInitialLoading.value && !conversation.value && chatStore.conversations.length > 0) return "not_found";
  if (chatStore.error && !isInitialLoading.value) return "load_failed";
  return null;
});

const errorTitle = computed(() => {
  switch (error.value) {
    case "invalid_id": return "Invalid Conversation";
    case "not_found": return "Conversation Not Found";
    case "load_failed": return "Failed to Load Conversation";
    default: return "Error";
  }
});

const errorMessage = computed(() => {
  switch (error.value) {
    case "invalid_id": return "The conversation ID is invalid or missing.";
    case "not_found": return "This conversation does not exist or you do not have access to it.";
    case "load_failed": return chatStore.error || "Unable to load the conversation. Please check your connection and try again.";
    default: return "An unexpected error occurred.";
  }
});

const isSendingMessage = computed(() => chatStore.isSendingMessage);

// Methods
const getParticipantName = (conv) => {
  return conv.participantTwoName || conv.otherParticipantName || conv.participantName || "Unknown User";
};

const getReceiverId = (conv) => {
  return conv.participantTwoId || null;
};


const loadConversationData = async () => {
  if (!conversationId.value) {
    isInitialLoading.value = false;
    return;
  }
  try {
    chatStore.clearError();
    sendError.value = null;
    if (chatStore.conversations.length === 0) {
      await chatStore.fetchConversations();
    }
    const conv = chatStore.conversations.find((c) => c.id === conversationId.value);
    if (!conv) {
      isInitialLoading.value = false;
      return;
    }
    await chatStore.fetchConversationMessages(conversationId.value);
    await chatStore.markConversationAsRead(conversationId.value);
    chatStore.setCurrentConversation(conv);
  } catch (err) {
    console.error("Error loading conversation data:", err);
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
  if (!conversation.value || !content.trim()) return;
  sendError.value = null;
  try {
    const receiverId = getReceiverId(conversation.value);
    const result = await chatStore.sendMessage(receiverId, content.trim());
    if (!result.success) {
      sendError.value = result.message || "Failed to send message";
    }
  } catch (err) {
    console.error("Error sending message:", err);
    sendError.value = err.message || "Failed to send message";
  }
};

// Lifecycle
onMounted(async () => {
  if (!authStore.isLoggedIn) {
    router.push("/login");
    return;
  }
  await loadConversationData();
  if (messageInputRef.value && !error.value) {
    setTimeout(() => messageInputRef.value?.focusInput(), 100);
  }
});

onUnmounted(() => {
  chatStore.clearCurrentConversation();
  sendError.value = null;
});

// Watchers
watch(() => conversationId.value, async (newId, oldId) => {
  if (newId !== oldId) {
    isInitialLoading.value = true;
    sendError.value = null;
    chatStore.clearCurrentConversation();
    await loadConversationData();
  }
});

watch(() => chatStore.error, (newError) => {
  if (!newError) sendError.value = null;
});

watch(() => participantName.value, (name) => {
  document.title = name && name !== "Unknown User" ? `Chat with ${name} - LegalConnect` : "Chat - LegalConnect";
}, { immediate: true });
</script>

<style scoped>
.chat-layout {
  height: 95vh;
  max-width: 95vw;
  margin: 2.5vh auto;
  display: flex;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* Header */
.chat-header {
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  padding: 0.75rem 1rem;
  flex-shrink: 0;
}

.unified-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  max-width: 1200px;
  margin: 0 auto;
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

.participant-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.1rem;
}

.participant-details h1 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-heading);
}

/* Loading & Error States */
.loading-container, .error-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
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

.retry-btn {
  padding: 0.75rem 1.5rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
}

/* Messages Area */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.conversation-view {
  height: 100%;
}

/* Input Area */
.chat-input-container {
  flex-shrink: 0;
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
}

/* Responsive */
@media (max-width: 768px) {
  .chat-header {
    padding: 0.5rem;
  }
  .participant-avatar {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  .participant-details h1 {
    font-size: 1rem;
  }
}
</style>
