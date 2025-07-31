<template>
  <div class="chat-error-boundary">
    <!-- Error State -->
    <div v-if="hasError" class="error-boundary-content">
      <div class="error-icon-container">
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
      </div>
      
      <div class="error-content">
        <h3 class="error-title">{{ errorTitle }}</h3>
        <p class="error-message">{{ errorMessage }}</p>
        
        <div class="error-details" v-if="showDetails && errorDetails">
          <button 
            @click="toggleDetails" 
            class="details-toggle"
            :aria-expanded="detailsExpanded"
          >
            <svg 
              class="toggle-icon" 
              :class="{ 'toggle-icon--expanded': detailsExpanded }"
              viewBox="0 0 24 24" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <path 
                d="M6 9l6 6 6-6" 
                stroke="currentColor" 
                stroke-width="2" 
                stroke-linecap="round" 
                stroke-linejoin="round"
              />
            </svg>
            {{ detailsExpanded ? 'Hide' : 'Show' }} Details
          </button>
          
          <div v-if="detailsExpanded" class="error-details-content">
            <pre class="error-stack">{{ errorDetails }}</pre>
          </div>
        </div>
        
        <div class="error-actions">
          <button 
            @click="handleRetry" 
            class="retry-button"
            :disabled="isRetrying"
          >
            <template v-if="isRetrying">
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
              Retrying...
            </template>
            <template v-else>
              <svg 
                class="retry-icon" 
                viewBox="0 0 24 24" 
                fill="none" 
                xmlns="http://www.w3.org/2000/svg"
              >
                <path 
                  d="M1 4v6h6M23 20v-6h-6" 
                  stroke="currentColor" 
                  stroke-width="2" 
                  stroke-linecap="round" 
                  stroke-linejoin="round"
                />
                <path 
                  d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" 
                  stroke="currentColor" 
                  stroke-width="2" 
                  stroke-linecap="round" 
                  stroke-linejoin="round"
                />
              </svg>
              Try Again
            </template>
          </button>
          
          <button 
            v-if="showReload"
            @click="handleReload" 
            class="reload-button"
          >
            <svg 
              class="reload-icon" 
              viewBox="0 0 24 24" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <path 
                d="M1 4v6h6M23 20v-6h-6" 
                stroke="currentColor" 
                stroke-width="2" 
                stroke-linecap="round" 
                stroke-linejoin="round"
              />
              <path 
                d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" 
                stroke="currentColor" 
                stroke-width="2" 
                stroke-linecap="round" 
                stroke-linejoin="round"
              />
            </svg>
            Reload Page
          </button>
          
          <button 
            v-if="showFallback"
            @click="handleFallback" 
            class="fallback-button"
          >
            {{ fallbackText }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- Normal Content -->
    <div v-else class="error-boundary-slot">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed, onErrorCaptured, ref } from 'vue';

// Props
const props = defineProps({
  errorTitle: {
    type: String,
    default: 'Something went wrong'
  },
  errorMessage: {
    type: String,
    default: 'An unexpected error occurred. Please try again.'
  },
  showDetails: {
    type: Boolean,
    default: false
  },
  showReload: {
    type: Boolean,
    default: false
  },
  showFallback: {
    type: Boolean,
    default: false
  },
  fallbackText: {
    type: String,
    default: 'Use Fallback'
  },
  retryable: {
    type: Boolean,
    default: true
  }
});

// Emits
const emit = defineEmits(['retry', 'reload', 'fallback', 'error']);

// State
const hasError = ref(false);
const errorDetails = ref('');
const detailsExpanded = ref(false);
const isRetrying = ref(false);

// Methods
const toggleDetails = () => {
  detailsExpanded.value = !detailsExpanded.value;
};

const handleRetry = async () => {
  if (!props.retryable || isRetrying.value) return;
  
  isRetrying.value = true;
  
  try {
    emit('retry');
    
    // Reset error state after a short delay to allow retry to complete
    setTimeout(() => {
      hasError.value = false;
      errorDetails.value = '';
      detailsExpanded.value = false;
    }, 100);
  } catch (err) {
    console.error('Retry failed:', err);
  } finally {
    setTimeout(() => {
      isRetrying.value = false;
    }, 1000);
  }
};

const handleReload = () => {
  emit('reload');
  window.location.reload();
};

const handleFallback = () => {
  emit('fallback');
  hasError.value = false;
};

// Error capture
onErrorCaptured((error, instance, info) => {
  console.error('ChatErrorBoundary caught error:', error, info);
  
  hasError.value = true;
  errorDetails.value = `${error.message}\n\nComponent: ${info}\n\nStack: ${error.stack}`;
  
  // Emit error event for parent components
  emit('error', {
    error,
    instance,
    info,
    timestamp: Date.now()
  });
  
  // Prevent error from propagating further
  return false;
});

// Expose methods for parent components
defineExpose({
  reset: () => {
    hasError.value = false;
    errorDetails.value = '';
    detailsExpanded.value = false;
    isRetrying.value = false;
  },
  setError: (error, details = '') => {
    hasError.value = true;
    errorDetails.value = details || error.message || error.toString();
  }
});
</script>

<style scoped>
.chat-error-boundary {
  width: 100%;
  height: 100%;
}

.error-boundary-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
  min-height: 300px;
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
}

.error-icon-container {
  margin-bottom: 1.5rem;
}

.error-icon {
  width: 64px;
  height: 64px;
  color: var(--color-error);
}

.error-content {
  max-width: 500px;
  width: 100%;
}

.error-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 1rem 0;
}

.error-message {
  font-size: 1rem;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0 0 2rem 0;
}

.error-details {
  margin-bottom: 2rem;
  text-align: left;
}

.details-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  color: var(--color-text);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-bottom: 1rem;
}

.details-toggle:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
}

.toggle-icon {
  width: 16px;
  height: 16px;
  transition: transform var(--transition-fast);
}

.toggle-icon--expanded {
  transform: rotate(180deg);
}

.error-details-content {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: 1rem;
  max-height: 200px;
  overflow-y: auto;
}

.error-stack {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}

.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.retry-button,
.reload-button,
.fallback-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 120px;
  justify-content: center;
}

.retry-button {
  background: var(--color-primary);
  color: var(--color-background);
}

.retry-button:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: translateY(-1px);
}

.retry-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.reload-button {
  background: var(--color-background-soft);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.reload-button:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.fallback-button {
  background: var(--color-warning);
  color: var(--color-background);
}

.fallback-button:hover {
  background: var(--color-warning-dark);
  transform: translateY(-1px);
}

.retry-icon,
.reload-icon {
  width: 16px;
  height: 16px;
}

.loading-spinner {
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

.error-boundary-slot {
  width: 100%;
  height: 100%;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .error-boundary-content {
    padding: 2rem 1rem;
    min-height: 250px;
  }
  
  .error-icon {
    width: 48px;
    height: 48px;
  }
  
  .error-title {
    font-size: 1.25rem;
  }
  
  .error-message {
    font-size: 0.875rem;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .retry-button,
  .reload-button,
  .fallback-button {
    width: 100%;
    max-width: 200px;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner {
    animation: none;
  }
  
  .toggle-icon {
    transition: none;
  }
  
  .retry-button:hover:not(:disabled),
  .fallback-button:hover {
    transform: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .error-boundary-content {
    border-width: 2px;
  }
  
  .details-toggle,
  .reload-button {
    border-width: 2px;
  }
}

/* Enhanced focus states for better accessibility */
.retry-button:focus-visible,
.reload-button:focus-visible,
.fallback-button:focus-visible,
.details-toggle:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

/* Enhanced hover states with consistent transitions */
.retry-button:hover:not(:disabled) {
  background: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.reload-button:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.fallback-button:hover {
  background: var(--color-warning);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.details-toggle:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  transform: translateY(-1px);
}

/* Consistent spacing and typography */
.error-title {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.error-message {
  font-size: 1rem;
  line-height: 1.5;
  letter-spacing: -0.005em;
}

.details-toggle {
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.retry-button,
.reload-button,
.fallback-button {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* Enhanced visual hierarchy */
.error-boundary-content {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.error-icon-container {
  background: rgba(239, 68, 68, 0.1);
  border-radius: 50%;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.error-icon {
  color: var(--color-error);
  filter: drop-shadow(0 2px 4px rgba(239, 68, 68, 0.2));
}

/* Improved button styling */
.retry-button {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
}

.reload-button {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.fallback-button {
  background: linear-gradient(135deg, var(--color-warning), #e6a700);
  box-shadow: var(--shadow-sm);
}

.details-toggle {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

/* Better error details styling */
.error-details-content {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.error-stack {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Source Code Pro', monospace;
  font-size: 0.75rem;
  line-height: 1.4;
  color: var(--color-text-muted);
}

/* Enhanced loading spinner */
.loading-spinner {
  color: var(--color-primary);
  opacity: 0.8;
}

/* Improved spacing and layout */
.error-content {
  max-width: 500px;
  width: 100%;
}

.error-actions {
  gap: 1rem;
  margin-top: 2rem;
}

.error-details {
  margin: 1.5rem 0 2rem 0;
  text-align: left;
}

/* Enhanced animation for toggle icon */
.toggle-icon {
  transition: transform var(--transition-normal);
}

.toggle-icon--expanded {
  transform: rotate(180deg);
}

/* Better responsive design */
@media (max-width: 768px) {
  .error-boundary-content {
    padding: 2rem 1rem;
    margin: 1rem;
  }
  
  .error-icon {
    width: 48px;
    height: 48px;
  }
  
  .error-title {
    font-size: 1.25rem;
  }
  
  .error-message {
    font-size: 0.875rem;
  }
}
</style>