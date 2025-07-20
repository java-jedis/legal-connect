<template>
  <div class="notification-banner-container" aria-live="polite">
    <transition-group 
      name="notification-banner" 
      tag="div" 
      class="notification-banner-list"
    >
      <div 
        v-for="notification in visibleNotifications" 
        :key="notification.id" 
        class="notification-banner"
        :class="{ 'notification-banner--closing': notification.isClosing }"
        :data-type="notification.type"
        :data-error="notification.error"
        @click="dismissNotification(notification)"
        role="alert"
        tabindex="0"
        @keydown.enter="dismissNotification(notification)"
        @keydown.space.prevent="dismissNotification(notification)"
        @touchstart="handleTouchStart($event, notification)"
        @touchmove="handleTouchMove($event)"
        @touchend="handleTouchEnd($event, notification)"
      >
        <div class="notification-banner-content">
          <div class="notification-banner-icon" aria-hidden="true">
            <svg 
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <path 
                stroke-linecap="round" 
                stroke-linejoin="round" 
                stroke-width="2" 
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
          </div>
          <div class="notification-banner-text">
            <p>{{ notification.content }}</p>
            <time class="notification-banner-time" :datetime="notification.createdAt">
              {{ formatTime(notification.createdAt) }}
            </time>
          </div>
        </div>
        <button 
          class="notification-banner-close" 
          @click.stop="dismissNotification(notification)"
          aria-label="Dismiss notification"
        >
          <svg 
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
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useNotificationStore } from '../stores/notification';

// Store
const notificationStore = useNotificationStore();

// Constants
const AUTO_DISMISS_DELAY = 5000; // 5 seconds
const MAX_VISIBLE_NOTIFICATIONS = 3;

// State
const visibleNotifications = ref([]);
const notificationQueue = ref([]);

// Touch gesture handling
const touchStartX = ref(0);
const swipeThreshold = 80; // Minimum distance for swipe

const handleTouchStart = (event) => {
  touchStartX.value = event.changedTouches[0].screenX;
};

const handleTouchMove = (event) => {
  const currentX = event.changedTouches[0].screenX;
  const swipeDistance = currentX - touchStartX.value;
  
  // If swiping left (negative distance), apply a transform to the element
  if (swipeDistance < 0) {
    const element = event.currentTarget;
    const transformValue = Math.max(swipeDistance, -100); // Limit the swipe
    
    // Apply transform to show swipe effect
    element.style.transform = `translateX(${transformValue}px)`;
    element.style.opacity = 1 + transformValue / 200; // Fade out slightly while swiping
  }
};

const handleTouchEnd = async (event, notification) => {
  const currentX = event.changedTouches[0].screenX;
  const swipeDistance = currentX - touchStartX.value;
  const element = event.currentTarget;
  
  // Reset the element style
  element.style.transform = '';
  element.style.opacity = '';
  
  // If swipe distance exceeds threshold, dismiss the notification
  if (swipeDistance < -swipeThreshold) {
    dismissNotification(notification);
  }
};

// Methods
const showNotification = (notification) => {
  // Prevent duplicate banners from being shown
  const isAlreadyVisible =
    visibleNotifications.value.some((n) => n.id === notification.id) ||
    notificationQueue.value.some((n) => n.id === notification.id);

  if (isAlreadyVisible) {
    console.warn(
      `NotificationBanner: Notification with ID ${notification.id} is already visible or queued.`
    );
    return;
  }

  // Add isClosing flag for animation
  const notificationWithState = {
    ...notification,
    isClosing: false,
    timerId: null
  };
  
  // If we have room to show more notifications
  if (visibleNotifications.value.length < MAX_VISIBLE_NOTIFICATIONS) {
    // Add to visible notifications
    visibleNotifications.value.push(notificationWithState);
    
    // Set auto-dismiss timer
    notificationWithState.timerId = setTimeout(() => {
      dismissNotification(notificationWithState);
    }, AUTO_DISMISS_DELAY);
  } else {
    // Add to queue if we're at max visible
    notificationQueue.value.push(notificationWithState);
  }
};

const dismissNotification = async (notification) => {
  // If already closing, do nothing
  if (notification.isClosing) return;
  
  // Mark as closing for animation
  notification.isClosing = true;
  
  // Clear auto-dismiss timer if exists
  if (notification.timerId) {
    clearTimeout(notification.timerId);
    notification.timerId = null;
  }
  
  // Mark as read in store if not already read and it's a real backend notification
  // Don't try to mark connection status notifications as read since they're not from backend
  if (!notification.isRead && notification.type !== 'CONNECTION_STATUS') {
    await notificationStore.markAsRead(notification.id);
  }
  
  // Remove after animation completes
  setTimeout(() => {
    const index = visibleNotifications.value.findIndex(n => n.id === notification.id);
    if (index !== -1) {
      visibleNotifications.value.splice(index, 1);
      
      // Show next notification from queue if available
      if (notificationQueue.value.length > 0) {
        const nextNotification = notificationQueue.value.shift();
        showNotification(nextNotification);
      }
    }
  }, 300); // Match animation duration
};

const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffInMinutes = Math.floor((now - date) / (1000 * 60));
  
  if (diffInMinutes < 1) {
    return 'Just now';
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

// Event handlers
const handleNewNotification = (event) => {
  const notification = event.detail;
  
  // Filter out connection confirmation messages
  if (notification.content === "Successfully connected to notification service") {
    return;
  }
  showNotification(notification);
};

// Connection status notifications are completely disabled to prevent
// "Connected to notification service" banners from appearing

// Note: Connection status notifications are disabled to avoid showing 
// "Connected to notification service" banners when WebSocket connects

// Lifecycle
onMounted(() => {
  // Listen for new notifications (emitted by notification store)
  window.addEventListener('new-notification', handleNewNotification);
});

onUnmounted(() => {
  // Clean up event listeners
  window.removeEventListener('new-notification', handleNewNotification);
  
  // Clear all timers
  visibleNotifications.value.forEach(notification => {
    if (notification.timerId) {
      clearTimeout(notification.timerId);
    }
  });
});
</script>

<style scoped>
.notification-banner-container {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 350px;
  max-width: calc(100vw - 40px);
  z-index: 9999;
  pointer-events: none;
}

.notification-banner-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-banner {
  background: var(--color-background);
  border-left: 4px solid var(--color-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  padding: 12px 16px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  pointer-events: auto;
  cursor: pointer;
  transition: all 0.3s ease;
  transform-origin: top right;
  max-width: 100%;
}

/* Error notification styling */
.notification-banner[data-type="error"],
.notification-banner[data-type="CONNECTION_STATUS"][data-error="true"] {
  border-left-color: var(--color-error, #ef4444);
  background-color: rgba(var(--color-error-rgb, 239, 68, 68), 0.05);
}

.notification-banner:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.notification-banner--closing {
  opacity: 0;
  transform: translateX(100%);
}

.notification-banner-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.notification-banner-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.notification-banner-text {
  flex: 1;
  min-width: 0;
}

.notification-banner-text p {
  margin: 0 0 4px 0;
  font-size: 0.875rem;
  line-height: 1.4;
  color: var(--color-text);
  word-wrap: break-word;
}

.notification-banner-time {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.notification-banner-close {
  background: none;
  border: none;
  padding: 4px;
  width: 24px;
  height: 24px;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-left: 8px;
}

.notification-banner-close:hover {
  background: var(--color-background-mute);
  color: var(--color-text);
}

/* Animation classes */
.notification-banner-enter-active,
.notification-banner-leave-active {
  transition: all 0.3s ease;
}

.notification-banner-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-banner-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .notification-banner-container {
    top: 10px;
    right: 10px;
    width: calc(100% - 20px);
  }
  
  .notification-banner {
    padding: 10px 12px;
    min-height: 60px; /* Ensure touch targets are at least 44px high */
  }
  
  .notification-banner-icon {
    width: 20px;
    height: 20px;
  }
  
  .notification-banner-text p {
    font-size: 0.8125rem;
  }
  
  .notification-banner-time {
    font-size: 0.6875rem;
  }
  
  .notification-banner-close {
    width: 32px; /* Larger touch target */
    height: 32px; /* Larger touch target */
    padding: 6px;
  }
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .notification-banner,
  .notification-banner-enter-active,
  .notification-banner-leave-active {
    transition: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .notification-banner {
    border-left-width: 6px;
    box-shadow: 0 0 0 1px var(--color-border);
  }
}
</style>