<template>
  <div class="chat-conversation-layout">
    <!-- Chat History Sidebar -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>Conversations</h3>
      </div>
      <div class="sidebar-content">
        <ChatConversationList
          :conversations="conversationsSorted"
          :loading="isLoadingConversations"
          @conversation-click="handleConversationClickFromList"
        />
      </div>
    </div>

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
      <div v-else class="chat-messages" ref="conversationContainerRef" role="region" aria-label="Conversation messages">
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
          :max-rows="3"
          @send="handleSendMessage"
        />
      </div>
    </div>

    <!-- Right Actions Sidebar -->
    <div class="right-actions-sidebar">
      <div class="actions-header">
        <h4>Quick Actions</h4>
      </div>
      <div class="action-buttons">
        <!-- User/Client Actions -->
        <template v-if="isUser">
          <button @click="goToMyMeetings" class="action-btn">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M3 9h18" stroke="currentColor" stroke-width="2"/>
                <circle cx="8" cy="14" r="1.5" fill="currentColor"/>
                <circle cx="12" cy="14" r="1.5" fill="currentColor"/>
                <circle cx="16" cy="14" r="1.5" fill="currentColor"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">My Meetings</span>
              <span class="action-subtitle">View and manage your meetings</span>
            </div>
          </button>
          <button @click="goToMyCalendar" class="action-btn">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 2v4M17 2v4M3 9h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">My Calendar</span>
              <span class="action-subtitle">See your schedule</span>
            </div>
          </button>
        </template>

        <!-- Lawyer Actions -->
        <template v-else-if="isLawyer">
          <button @click="goToScheduleMeeting" class="action-btn">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">Schedule Meeting</span>
              <span class="action-subtitle">Create a new consultation</span>
            </div>
          </button>
          <button @click="goToLawyerCalendar" class="action-btn">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="4" width="18" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 2v4M17 2v4M3 9h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="action-text">
              <span class="action-title">My Calendar</span>
              <span class="action-subtitle">See your schedule</span>
            </div>
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import ChatConversationList from "@/components/chat/ChatConversationList.vue";
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

// Conversations sidebar data
const conversationsSorted = computed(() => chatStore.sortedConversations);
const isLoadingConversations = computed(() => chatStore.isLoading);

// Role
const isLawyer = computed(() => authStore.userInfo?.role === "LAWYER" || authStore.userType === "lawyer");
const isUser = computed(() => authStore.userInfo?.role === "USER" || authStore.userType === "user");

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

// Sidebar actions
const handleConversationClickFromList = (conv) => {
  chatStore.setCurrentConversation(conv);
  router.push(`/chat/${conv.id}`);
};

const goToMyMeetings = () => router.push("/my-meetings");
const goToMyCalendar = () => router.push({ name: 'user-dashboard', query: { focus: 'calendar' } });
const goToScheduleMeeting = () => router.push("/meetings");
const goToLawyerCalendar = () => router.push({ name: 'lawyer-dashboard', query: { focus: 'calendar' } });

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
.chat-conversation-layout {
  display: flex;
  height: calc(100vh - 70px); /* account for sticky header */
  background: var(--color-background);
  position: relative;
  overflow: hidden;
}

/* Sidebar (left) */
.chat-sidebar {
  width: 360px;
  background: linear-gradient(135deg, var(--color-background-soft), var(--color-background));
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.0625rem;
  font-weight: 700;
  color: var(--color-heading);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

/* Main Chat Area (center) */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.loading-container,
.error-container {
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

.retry-btn {
  padding: 0.75rem 1.5rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
}

.chat-messages {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 72px; /* leave space for input */
  overflow-y: auto;
  padding: 1rem 1rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
}

.conversation-view { height: 100%; }

/* Input Area */
.chat-input-container {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  padding: 0.75rem 1rem;
}

.chat-input-container :deep(.input-wrapper) {
  max-width: none;
  width: 100%;
  margin: 0;
}

/* Right Actions Sidebar */
.right-actions-sidebar {
  width: 300px;
  background: var(--color-background-soft);
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.actions-header {
  padding: 1.25rem 1rem 0.75rem;
  border-bottom: 1px solid var(--color-border);
}

.actions-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.action-buttons {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: left;
}

.action-btn:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.action-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon svg { width: 20px; height: 20px; }

.action-text { display: flex; flex-direction: column; gap: 0.2rem; }
.action-title { font-size: 0.95rem; font-weight: 600; color: var(--color-heading); }
.action-subtitle { font-size: 0.8125rem; color: var(--color-text-muted); }

/* Responsive */
@media (max-width: 1024px) {
  .right-actions-sidebar { display: none; }
}

@media (max-width: 768px) {
  .chat-sidebar { position: fixed; left: 0; top: 0; bottom: 0; width: 80vw; z-index: 20; transform: translateX(0); }
  .chat-conversation-layout { flex-direction: column; }
  .chat-main { height: calc(100vh - 0px); }
  .chat-messages { bottom: 64px; }
}
</style>
