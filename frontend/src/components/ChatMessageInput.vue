<template>
  <div class="chat-message-input">
    <!-- Error Banner -->
    <div v-if="error" class="error-banner">
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
      <span>{{ errorMessage }}</span>
      <button
        v-if="showRetry"
        @click="retryLastMessage"
        class="retry-btn"
        :disabled="isRetrying"
      >
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
        <template v-else>Retry</template>
      </button>
      <button @click="dismissError" class="dismiss-btn" aria-label="Dismiss error">
        <svg
          class="dismiss-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M6 18L18 6M6 6l12 12"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>

    <!-- Message Input Form -->
    <form @submit.prevent="handleSubmit" class="message-form">
      <div class="input-container">
        <!-- Message Textarea -->
        <div class="textarea-wrapper">
          <textarea
            ref="messageTextarea"
            v-model="messageContent"
            :placeholder="placeholder"
            :disabled="isSending || disabled"
            :maxlength="maxLength"
            class="message-textarea"
            :class="{
              'message-textarea--error': hasValidationError,
              'message-textarea--disabled': isSending || disabled,
            }"
            rows="1"
            @keydown="handleKeyDown"
            @input="handleInput"
            @paste="handlePaste"
            aria-label="Type your message"
          ></textarea>
          
          
        </div>

        <!-- Send Button -->
        <button
          type="submit"
          class="send-button"
          :disabled="!canSend"
          :class="{
            'send-button--disabled': !canSend,
            'send-button--sending': isSending,
          }"
          :aria-label="isSending ? 'Sending message...' : 'Send message'"
        >
          <template v-if="isSending">
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
          </template>
          <template v-else>
            <svg
              class="send-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </template>
        </button>
      </div>

      <!-- Validation Error -->
      <div v-if="validationError" class="validation-error">
        {{ validationError }}
      </div>
    </form>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from "vue";

// Props
const props = defineProps({
  placeholder: {
    type: String,
    default: "Type a message...",
  },
  maxLength: {
    type: Number,
    default: 2000,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  isSending: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
  
  autoResize: {
    type: Boolean,
    default: true,
  },
  maxRows: {
    type: Number,
    default: 6,
  },
});

// Emits
const emit = defineEmits(["send", "input", "focus", "blur"]);

// Refs
const messageTextarea = ref(null);
const messageContent = ref("");
const validationError = ref("");
const lastFailedMessage = ref("");
const isRetrying = ref(false);

// Computed properties
const canSend = computed(() => {
  return (
    !props.isSending &&
    !props.disabled &&
    messageContent.value.trim().length > 0 &&
    messageContent.value.length <= props.maxLength &&
    !hasValidationError.value
  );
});

const hasValidationError = computed(() => {
  return !!validationError.value;
});

const isNearLimit = computed(() => {
  const remaining = props.maxLength - messageContent.value.length;
  return remaining <= 100 && remaining > 0;
});

const isOverLimit = computed(() => {
  return messageContent.value.length > props.maxLength;
});

const errorMessage = computed(() => {
  if (!props.error) return "";

  if (props.error.includes("network") || props.error.includes("timeout")) {
    return "Network error. Please check your connection and try again.";
  } else if (props.error.includes("rate limit")) {
    return "You're sending messages too quickly. Please wait a moment.";
  } else if (props.error.includes("unauthorized")) {
    return "You're not authorized to send messages in this conversation.";
  } else {
    return props.error;
  }
});

const showRetry = computed(() => {
  return (
    props.error &&
    lastFailedMessage.value &&
    (props.error.includes("network") ||
      props.error.includes("timeout") ||
      props.error.includes("Failed to send"))
  );
});

// Methods
const validateMessage = (content) => {
  const trimmed = content.trim();

  if (trimmed.length === 0) {
    return "Message cannot be empty";
  }

  if (trimmed.length > props.maxLength) {
    return `Message cannot exceed ${props.maxLength} characters`;
  }

  // Check for potentially harmful content
  if (/<script|javascript:|data:/i.test(content)) {
    return "Message contains invalid content";
  }

  return "";
};

const handleInput = (event) => {
  const value = event.target.value;
  
  // Clear validation error when user starts typing
  if (validationError.value) {
    validationError.value = "";
  }

  // Auto-resize textarea if enabled
  if (props.autoResize) {
    autoResizeTextarea();
  }

  // Emit input event
  emit("input", value);
};

const handleKeyDown = (event) => {
  // Handle Enter key submission
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    handleSubmit();
  }
  
  // Handle Escape key to clear input
  else if (event.key === "Escape") {
    event.preventDefault();
    clearInput();
  }
};

const handlePaste = () => {
  // Allow paste but validate after
  nextTick(() => {
    if (props.autoResize) {
      autoResizeTextarea();
    }
  });
};

const handleSubmit = async () => {
  if (!canSend.value) return;

  const content = messageContent.value.trim();
  const error = validateMessage(content);

  if (error) {
    validationError.value = error;
    return;
  }

  // Store message in case we need to retry
  lastFailedMessage.value = content;

  // Clear input immediately for better UX
  const originalContent = messageContent.value;
  messageContent.value = "";
  validationError.value = "";
  
  // Reset textarea height
  if (props.autoResize) {
    resetTextareaHeight();
  }

  try {
    // Emit send event
    emit("send", content);
  } catch (err) {
    // Restore content if send fails immediately
    messageContent.value = originalContent;
    console.error("Error sending message:", err);
  }
};

const retryLastMessage = async () => {
  if (!lastFailedMessage.value || isRetrying.value) return;

  isRetrying.value = true;
  
  try {
    emit("send", lastFailedMessage.value);
    lastFailedMessage.value = "";
  } catch (err) {
    console.error("Error retrying message:", err);
  } finally {
    isRetrying.value = false;
  }
};

const dismissError = () => {
  lastFailedMessage.value = "";
  // Note: We don't clear props.error as it's controlled by parent
};

const clearInput = () => {
  messageContent.value = "";
  validationError.value = "";
  lastFailedMessage.value = "";
  
  if (props.autoResize) {
    resetTextareaHeight();
  }
};

const focusInput = () => {
  if (messageTextarea.value) {
    messageTextarea.value.focus();
  }
};

const autoResizeTextarea = () => {
  if (!messageTextarea.value || !props.autoResize) return;

  const textarea = messageTextarea.value;
  
  // Reset height to calculate new height
  textarea.style.height = "auto";
  
  // Calculate new height based on content
  const scrollHeight = textarea.scrollHeight;
  const lineHeight = parseInt(getComputedStyle(textarea).lineHeight);
  const maxHeight = lineHeight * props.maxRows;
  
  // Set new height (limited by maxRows)
  const newHeight = Math.min(scrollHeight, maxHeight);
  textarea.style.height = `${newHeight}px`;
  
  // Show scrollbar if content exceeds max height
  textarea.style.overflowY = scrollHeight > maxHeight ? "auto" : "hidden";
};

const resetTextareaHeight = () => {
  if (!messageTextarea.value || !props.autoResize) return;
  
  messageTextarea.value.style.height = "auto";
  messageTextarea.value.style.overflowY = "hidden";
};

// Watch for external changes to reset validation
watch(
  () => props.error,
  (newError) => {
    if (!newError) {
      lastFailedMessage.value = "";
      isRetrying.value = false;
    }
  }
);

// Watch for sending state changes
watch(
  () => props.isSending,
  (isSending) => {
    if (!isSending && !props.error) {
      // Message sent successfully, clear retry state
      lastFailedMessage.value = "";
    }
  }
);

// Expose methods for parent component
defineExpose({
  focusInput,
  clearInput,
});
</script>

<style scoped>
.chat-message-input {
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  padding: 1rem 1.5rem;
  flex-shrink: 0;
}

/* Error Banner */
.error-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background-color: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  color: var(--color-text);
}

.error-banner .error-icon {
  width: 16px;
  height: 16px;
  color: #ef4444;
  flex-shrink: 0;
}

.retry-btn {
  margin-left: auto;
  padding: 0.25rem 0.75rem;
  background: #ef4444;
  color: white;
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

.retry-btn:hover:not(:disabled) {
  background: #dc2626;
}

.retry-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.dismiss-btn {
  padding: 0.25rem;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color var(--transition-fast);
  flex-shrink: 0;
  border-radius: var(--border-radius);
}

.dismiss-btn:hover {
  color: var(--color-text);
  background: rgba(0, 0, 0, 0.05);
}

.dismiss-icon {
  width: 14px;
  height: 14px;
}

/* Message Form */
.message-form {
  width: 100%;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 0.75rem;
  transition: all var(--transition-fast);
}

.input-container:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(6, 147, 227, 0.1);
}

.textarea-wrapper {
  flex: 1;
  position: relative;
}

/* Message Textarea */
.message-textarea {
  width: 100%;
  min-height: 20px;
  max-height: 120px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--color-text);
  font-size: 0.875rem;
  line-height: 1.5;
  resize: none;
  outline: none;
  font-family: inherit;
  overflow-y: hidden;
}

.message-textarea::placeholder {
  color: var(--color-text-muted);
}

.message-textarea--disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message-textarea--error {
  color: #ef4444;
}

/* Character Count */
.character-count {
  position: absolute;
  bottom: -1.5rem;
  right: 0;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-weight: 500;
}

.character-count--warning {
  color: #f59e0b;
}

.character-count--error {
  color: #ef4444;
}

/* Send Button */
.send-button {
  width: 40px;
  height: 40px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: scale(1.05);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button--disabled {
  background: var(--color-background-mute);
  color: var(--color-text-muted);
  cursor: not-allowed;
  transform: none;
}

.send-button--sending {
  background: var(--color-primary);
  cursor: not-allowed;
}

.send-icon {
  width: 18px;
  height: 18px;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  animation: spin 1s linear infinite;
}

.loading-spinner-small {
  width: 14px;
  height: 14px;
  animation: spin 1s linear infinite;
}

/* Validation Error */
.validation-error {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #ef4444;
  font-weight: 500;
}

/* Animations */
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
  .chat-message-input {
    padding: 0.75rem 1rem;
  }
  
  .input-container {
    padding: 0.5rem;
    gap: 0.5rem;
  }
  
  .send-button {
    width: 36px;
    height: 36px;
  }
  
  .send-icon {
    width: 16px;
    height: 16px;
  }
  
  .loading-spinner {
    width: 16px;
    height: 16px;
  }
  
  .error-banner {
    margin-bottom: 0.75rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.8125rem;
  }
  
  .retry-btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.6875rem;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner,
  .loading-spinner-small {
    animation: none;
  }
  
  .send-button:hover:not(:disabled) {
    transform: none;
  }
  
  .send-button:active:not(:disabled) {
    transform: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .input-container {
    border-width: 2px;
  }
  
  .error-banner {
    border-width: 2px;
  }
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .input-container {
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }
  
  .send-button {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
  
  .dismiss-btn:hover {
    background: rgba(255, 255, 255, 0.1);
  }
}

/* Enhanced focus states for better accessibility */
.message-textarea:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(6, 147, 227, 0.1);
}

.send-button:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

.retry-btn:focus-visible,
.dismiss-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* Enhanced hover states with consistent transitions */
.input-container:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(6, 147, 227, 0.1), var(--shadow-sm);
  transform: translateY(-1px);
}

.send-button:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.retry-btn:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.dismiss-btn:hover {
  color: var(--color-text);
  background: var(--color-background-mute);
  transform: scale(1.1);
}

/* Consistent spacing and typography */
.message-textarea {
  font-size: 0.875rem;
  line-height: 1.5;
  letter-spacing: -0.005em;
}

.message-textarea::placeholder {
  color: var(--color-text-muted);
  font-weight: 400;
}

.character-count {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.validation-error {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.005em;
}

/* Enhanced visual hierarchy */
.error-banner {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05));
  border: 1px solid rgba(239, 68, 68, 0.2);
  box-shadow: var(--shadow-sm);
}

.error-banner .error-icon {
  color: var(--color-error);
}

/* Improved button styling */
.send-button {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
  border: none;
}

.send-button--disabled {
  background: var(--color-background-mute);
  color: var(--color-text-muted);
  box-shadow: none;
}

.retry-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: var(--shadow-sm);
}

/* Better input container styling */
.input-container {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

/* Enhanced character count styling */
.character-count--warning {
  color: var(--color-warning);
  font-weight: 600;
}

.character-count--error {
  color: var(--color-error);
  font-weight: 700;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* Improved validation error styling */
.validation-error {
  color: var(--color-error);
  background: rgba(239, 68, 68, 0.1);
  padding: 0.5rem 0.75rem;
  border-radius: var(--border-radius);
  border-left: 3px solid var(--color-error);
}
</style>