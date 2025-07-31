<template>
  <div class="chat-loading-skeleton" :class="skeletonClass">
    <!-- Conversation List Skeleton -->
    <div v-if="type === 'conversation-list'" class="conversation-list-skeleton">
      <div 
        v-for="n in count" 
        :key="n" 
        class="conversation-item-skeleton"
      >
        <div class="skeleton-avatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-header">
            <div class="skeleton-name"></div>
            <div class="skeleton-time"></div>
          </div>
          <div class="skeleton-message"></div>
        </div>
      </div>
    </div>

    <!-- Message List Skeleton -->
    <div v-else-if="type === 'message-list'" class="message-list-skeleton">
      <div 
        v-for="n in count" 
        :key="n" 
        class="message-skeleton"
        :class="{ 
          'message-skeleton--own': n % 3 === 0,
          'message-skeleton--other': n % 3 !== 0 
        }"
      >
        <div class="message-bubble-skeleton">
          <div class="skeleton-text" :style="getMessageWidth(n)"></div>
          <div v-if="n % 2 === 0" class="skeleton-text skeleton-text--short"></div>
          <div class="skeleton-footer">
            <div class="skeleton-time"></div>
            <div v-if="n % 3 === 0" class="skeleton-status"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Inbox Header Skeleton -->
    <div v-else-if="type === 'inbox-header'" class="inbox-header-skeleton">
      <div class="skeleton-title"></div>
      <div class="skeleton-subtitle"></div>
      <div class="skeleton-actions">
        <div class="skeleton-button"></div>
        <div class="skeleton-button skeleton-button--primary"></div>
      </div>
    </div>

    <!-- Conversation Header Skeleton -->
    <div v-else-if="type === 'conversation-header'" class="conversation-header-skeleton">
      <div class="skeleton-avatar skeleton-avatar--large"></div>
      <div class="skeleton-participant-info">
        <div class="skeleton-name skeleton-name--large"></div>
        <div class="skeleton-status"></div>
      </div>
    </div>

    <!-- Search Bar Skeleton -->
    <div v-else-if="type === 'search-bar'" class="search-bar-skeleton">
      <div class="skeleton-search-input"></div>
      <div class="skeleton-filter-select"></div>
      <div class="skeleton-sort-button"></div>
    </div>

    <!-- Generic Content Skeleton -->
    <div v-else class="generic-skeleton">
      <div 
        v-for="n in count" 
        :key="n" 
        class="skeleton-line"
        :style="getLineWidth(n)"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

// Props
const props = defineProps({
  type: {
    type: String,
    default: 'generic',
    validator: (value) => [
      'conversation-list',
      'message-list', 
      'inbox-header',
      'conversation-header',
      'search-bar',
      'generic'
    ].includes(value)
  },
  count: {
    type: Number,
    default: 3
  },
  animated: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
});

// Computed
const skeletonClass = computed(() => ({
  'chat-loading-skeleton--animated': props.animated,
  [`chat-loading-skeleton--${props.size}`]: true
}));

// Methods
const getMessageWidth = (index) => {
  const widths = ['60%', '80%', '45%', '90%', '35%', '70%'];
  return { width: widths[index % widths.length] };
};

const getLineWidth = (index) => {
  const widths = ['100%', '85%', '92%', '78%', '95%'];
  return { width: widths[index % widths.length] };
};
</script>

<style scoped>
.chat-loading-skeleton {
  width: 100%;
}

/* Base skeleton styles */
.skeleton-base {
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--border-radius);
}

.chat-loading-skeleton--animated .skeleton-base {
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Apply skeleton base to all skeleton elements */
.skeleton-avatar,
.skeleton-name,
.skeleton-time,
.skeleton-message,
.skeleton-text,
.skeleton-title,
.skeleton-subtitle,
.skeleton-button,
.skeleton-status,
.skeleton-search-input,
.skeleton-filter-select,
.skeleton-sort-button,
.skeleton-line {
  @extend .skeleton-base;
}

/* Conversation List Skeleton */
.conversation-list-skeleton {
  display: flex;
  flex-direction: column;
}

.conversation-item-skeleton {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.conversation-item-skeleton:last-child {
  border-bottom: none;
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-avatar--large {
  width: 56px;
  height: 56px;
}

.skeleton-content {
  flex: 1;
  min-width: 0;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.skeleton-name {
  height: 16px;
  width: 120px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-name--large {
  height: 20px;
  width: 160px;
}

.skeleton-time {
  height: 12px;
  width: 60px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-message {
  height: 14px;
  width: 80%;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

/* Message List Skeleton */
.message-list-skeleton {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-skeleton {
  display: flex;
  max-width: 70%;
}

.message-skeleton--own {
  align-self: flex-end;
  justify-content: flex-end;
}

.message-skeleton--other {
  align-self: flex-start;
  justify-content: flex-start;
}

.message-bubble-skeleton {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  padding: 0.75rem 1rem;
  min-width: 100px;
  max-width: 100%;
}

.message-skeleton--own .message-bubble-skeleton {
  background: var(--color-primary-light);
}

.skeleton-text {
  height: 14px;
  margin-bottom: 0.5rem;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-text--short {
  width: 60%;
}

.skeleton-text:last-of-type {
  margin-bottom: 0.75rem;
}

.skeleton-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.skeleton-footer .skeleton-time {
  width: 40px;
  height: 10px;
}

.skeleton-status {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

/* Inbox Header Skeleton */
.inbox-header-skeleton {
  padding: 2rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--color-border);
}

.skeleton-title {
  height: 32px;
  width: 200px;
  margin-bottom: 0.5rem;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-subtitle {
  height: 16px;
  width: 150px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-actions {
  display: flex;
  gap: 1rem;
}

.skeleton-button {
  height: 36px;
  width: 100px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-button--primary {
  background: linear-gradient(
    90deg,
    var(--color-primary-light) 25%,
    var(--color-primary) 50%,
    var(--color-primary-light) 75%
  );
  background-size: 200% 100%;
}

/* Conversation Header Skeleton */
.conversation-header-skeleton {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.skeleton-participant-info {
  flex: 1;
}

.skeleton-participant-info .skeleton-status {
  width: 80px;
  height: 12px;
  margin-top: 0.5rem;
  border-radius: var(--border-radius);
}

/* Search Bar Skeleton */
.search-bar-skeleton {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 1rem;
  flex-wrap: wrap;
}

.skeleton-search-input {
  flex: 1;
  height: 40px;
  min-width: 200px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-filter-select {
  height: 40px;
  width: 120px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

.skeleton-sort-button {
  height: 40px;
  width: 100px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

/* Generic Skeleton */
.generic-skeleton {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.skeleton-line {
  height: 16px;
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
}

/* Size variations */
.chat-loading-skeleton--small .skeleton-avatar {
  width: 32px;
  height: 32px;
}

.chat-loading-skeleton--small .skeleton-name {
  height: 12px;
  width: 80px;
}

.chat-loading-skeleton--small .skeleton-message {
  height: 10px;
}

.chat-loading-skeleton--large .skeleton-avatar {
  width: 64px;
  height: 64px;
}

.chat-loading-skeleton--large .skeleton-name {
  height: 20px;
  width: 180px;
}

.chat-loading-skeleton--large .skeleton-message {
  height: 18px;
}

/* Apply animation to all skeleton elements when animated */
.chat-loading-skeleton--animated .skeleton-avatar,
.chat-loading-skeleton--animated .skeleton-name,
.chat-loading-skeleton--animated .skeleton-time,
.chat-loading-skeleton--animated .skeleton-message,
.chat-loading-skeleton--animated .skeleton-text,
.chat-loading-skeleton--animated .skeleton-title,
.chat-loading-skeleton--animated .skeleton-subtitle,
.chat-loading-skeleton--animated .skeleton-button,
.chat-loading-skeleton--animated .skeleton-status,
.chat-loading-skeleton--animated .skeleton-search-input,
.chat-loading-skeleton--animated .skeleton-filter-select,
.chat-loading-skeleton--animated .skeleton-sort-button,
.chat-loading-skeleton--animated .skeleton-line {
  animation: shimmer 1.5s infinite;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .conversation-item-skeleton {
    padding: 1rem;
    gap: 0.75rem;
  }
  
  .skeleton-avatar {
    width: 40px;
    height: 40px;
  }
  
  .skeleton-name {
    width: 100px;
  }
  
  .inbox-header-skeleton {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .skeleton-actions {
    justify-content: center;
  }
  
  .search-bar-skeleton {
    flex-direction: column;
    align-items: stretch;
  }
  
  .skeleton-search-input {
    min-width: auto;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .chat-loading-skeleton--animated .skeleton-avatar,
  .chat-loading-skeleton--animated .skeleton-name,
  .chat-loading-skeleton--animated .skeleton-time,
  .chat-loading-skeleton--animated .skeleton-message,
  .chat-loading-skeleton--animated .skeleton-text,
  .chat-loading-skeleton--animated .skeleton-title,
  .chat-loading-skeleton--animated .skeleton-subtitle,
  .chat-loading-skeleton--animated .skeleton-button,
  .chat-loading-skeleton--animated .skeleton-status,
  .chat-loading-skeleton--animated .skeleton-search-input,
  .chat-loading-skeleton--animated .skeleton-filter-select,
  .chat-loading-skeleton--animated .skeleton-sort-button,
  .chat-loading-skeleton--animated .skeleton-line {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .skeleton-avatar,
  .skeleton-name,
  .skeleton-time,
  .skeleton-message,
  .skeleton-text,
  .skeleton-title,
  .skeleton-subtitle,
  .skeleton-button,
  .skeleton-status,
  .skeleton-search-input,
  .skeleton-filter-select,
  .skeleton-sort-button,
  .skeleton-line {
    background: var(--color-border);
  }
}

/* Enhanced skeleton styling with consistent design system */
.skeleton-avatar {
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
  border-radius: 50%;
  box-shadow: var(--shadow-sm);
}

.skeleton-name,
.skeleton-time,
.skeleton-message,
.skeleton-text,
.skeleton-title,
.skeleton-subtitle,
.skeleton-button,
.skeleton-status,
.skeleton-search-input,
.skeleton-filter-select,
.skeleton-sort-button,
.skeleton-line {
  background: linear-gradient(
    90deg,
    var(--color-background-soft) 25%,
    var(--color-background-mute) 50%,
    var(--color-background-soft) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--border-radius);
}

/* Enhanced message bubble skeleton */
.message-bubble-skeleton {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.message-skeleton--own .message-bubble-skeleton {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  opacity: 0.1;
}

/* Enhanced conversation item skeleton */
.conversation-item-skeleton {
  background: var(--color-background);
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

.conversation-item-skeleton:hover {
  background: var(--color-background-soft);
  transform: translateX(2px);
  box-shadow: var(--shadow-sm);
}

/* Better visual hierarchy for skeleton elements */
.skeleton-title {
  height: 24px;
  width: 200px;
  border-radius: var(--border-radius);
}

.skeleton-subtitle {
  height: 16px;
  width: 150px;
  border-radius: var(--border-radius);
}

.skeleton-button {
  height: 36px;
  width: 100px;
  border-radius: var(--border-radius);
}

.skeleton-button--primary {
  background: linear-gradient(
    90deg,
    rgba(6, 147, 227, 0.2) 25%,
    rgba(6, 147, 227, 0.3) 50%,
    rgba(6, 147, 227, 0.2) 75%
  );
  background-size: 200% 100%;
}

/* Enhanced search bar skeleton */
.skeleton-search-input {
  height: 40px;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
}

.skeleton-filter-select,
.skeleton-sort-button {
  height: 40px;
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
}

/* Improved spacing and layout */
.conversation-list-skeleton {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.message-list-skeleton {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
}

.inbox-header-skeleton {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
}

.conversation-header-skeleton {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
}

/* Enhanced animation timing */
.chat-loading-skeleton--animated .skeleton-avatar,
.chat-loading-skeleton--animated .skeleton-name,
.chat-loading-skeleton--animated .skeleton-time,
.chat-loading-skeleton--animated .skeleton-message,
.chat-loading-skeleton--animated .skeleton-text,
.chat-loading-skeleton--animated .skeleton-title,
.chat-loading-skeleton--animated .skeleton-subtitle,
.chat-loading-skeleton--animated .skeleton-button,
.chat-loading-skeleton--animated .skeleton-status,
.chat-loading-skeleton--animated .skeleton-search-input,
.chat-loading-skeleton--animated .skeleton-filter-select,
.chat-loading-skeleton--animated .skeleton-sort-button,
.chat-loading-skeleton--animated .skeleton-line {
  animation: shimmer 1.8s ease-in-out infinite;
}

/* Staggered animation for better visual effect */
.conversation-item-skeleton:nth-child(1) .skeleton-avatar,
.conversation-item-skeleton:nth-child(1) .skeleton-name,
.conversation-item-skeleton:nth-child(1) .skeleton-message {
  animation-delay: 0s;
}

.conversation-item-skeleton:nth-child(2) .skeleton-avatar,
.conversation-item-skeleton:nth-child(2) .skeleton-name,
.conversation-item-skeleton:nth-child(2) .skeleton-message {
  animation-delay: 0.1s;
}

.conversation-item-skeleton:nth-child(3) .skeleton-avatar,
.conversation-item-skeleton:nth-child(3) .skeleton-name,
.conversation-item-skeleton:nth-child(3) .skeleton-message {
  animation-delay: 0.2s;
}

.conversation-item-skeleton:nth-child(4) .skeleton-avatar,
.conversation-item-skeleton:nth-child(4) .skeleton-name,
.conversation-item-skeleton:nth-child(4) .skeleton-message {
  animation-delay: 0.3s;
}

.conversation-item-skeleton:nth-child(5) .skeleton-avatar,
.conversation-item-skeleton:nth-child(5) .skeleton-name,
.conversation-item-skeleton:nth-child(5) .skeleton-message {
  animation-delay: 0.4s;
}
</style>