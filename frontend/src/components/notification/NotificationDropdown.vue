<template>
  <div class="notification-dropdown" ref="dropdownRef">
    <!-- Notification Icon Button -->
    <button
      @click="toggleDropdown"
      class="notification-toggle"
      :aria-label="
        hasUnreadNotifications
          ? `${unreadCount} unread notifications`
          : 'Notifications'
      "
      :class="{ 'notification-toggle--active': isOpen }"
      aria-haspopup="true"
      :aria-expanded="isOpen"
      aria-controls="notification-menu"
    >
      <svg
        class="notification-icon"
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
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        />
      </svg>

      

      <!-- Unread Count Badge -->
      <span
        v-if="hasUnreadNotifications"
        class="notification-badge"
        :aria-label="`${unreadCount} unread`"
        role="status"
      >
        {{ unreadCount > 99 ? "99+" : unreadCount }}
      </span>
    </button>

    <!-- Dropdown Menu -->
    <div
      id="notification-menu"
      class="notification-menu"
      :class="{ 'notification-menu--open': isOpen }"
      role="menu"
      aria-labelledby="notification-toggle"
    >
      <!-- Header -->
      <div class="notification-header">
        <h3 class="notification-title">Notifications</h3>
        <button
          v-if="hasUnreadNotifications"
          @click="handleMarkAllAsRead"
          class="mark-all-read-btn"
          :disabled="isMarkingAllAsRead"
        >
          <span v-if="!isMarkingAllAsRead">Mark all as read</span>
          <span v-else class="loading-text">
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
            Marking...
          </span>
        </button>
      </div>

      

      <!-- Notifications List -->
      <div class="notification-list" ref="notificationListRef">
        <!-- Loading State -->
        <div
          v-if="isLoading && notifications.length === 0"
          class="notification-loading"
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
          <span>Loading notifications...</span>
        </div>

        <!-- Empty State -->
        <div v-else-if="notifications.length === 0" class="notification-empty">
          <svg
            class="empty-icon"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <p>No notifications yet</p>
          <span>You'll see notifications here when you receive them</span>
        </div>

        <!-- Notification Items -->
        <div v-else class="notification-items">
          <div
            v-for="(notification, index) in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ 'notification-item--unread': !notification.read }"
            @click="handleNotificationClick(notification)"
            role="menuitem"
            tabindex="0"
            @keydown.enter="handleNotificationClick(notification)"
            @keydown.space.prevent="handleNotificationClick(notification)"
            @keydown.down="focusNextNotification(index)"
            @keydown.up="focusPreviousNotification(index)"
            @keydown.home="focusFirstNotification"
            @keydown.end="focusLastNotification"
            @touchstart="handleTouchStart($event, notification)"
            @touchmove="handleTouchMove($event, notification)"
            @touchend="handleTouchEnd($event, notification)"
            :aria-label="
              notification.read ? 'Read notification' : 'Unread notification'
            "
          >
            <div class="notification-content">
              <p class="notification-text">{{ formatNotificationContent(notification.content) }}</p>
              <time
                class="notification-time"
                :datetime="notification.createdAt"
              >
                {{ formatTime(notification.createdAt) }}
              </time>
            </div>
            <div
              v-if="!notification.read"
              class="notification-unread-dot"
              aria-hidden="true"
            ></div>
          </div>

          <!-- Load More Button -->
          <button
            v-if="hasMore && !isLoadingMore"
            @click="loadMoreNotifications"
            class="load-more-btn"
          >
            Load more notifications
          </button>

          <!-- Loading More State -->
          <div v-if="isLoadingMore" class="loading-more">
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
            <span>Loading more...</span>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-if="error" class="notification-error">
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
        <button @click="retryFetch" class="retry-btn" :disabled="isRetrying">
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
          <template v-else> Try again </template>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useNotificationStore } from "../../stores/notification";

// Store
const notificationStore = useNotificationStore();

// Refs
const dropdownRef = ref(null);
const notificationListRef = ref(null);
const isOpen = ref(false);
const isMarkingAllAsRead = ref(false);
const isRetrying = ref(false);
const reconnectionStatus = ref({
  reconnecting: false,
  attempt: 0,
  maxAttempts: 5,
  maxAttemptsReached: false,
  error: null,
});

// Computed properties
const notifications = computed(() => notificationStore.notifications);
const unreadCount = computed(() => notificationStore.unreadCount);
const hasUnreadNotifications = computed(
  () => notificationStore.hasUnreadNotifications
);
const isLoading = computed(() => notificationStore.isLoading);
const isLoadingMore = computed(() => notificationStore.isLoadingMore);
const hasMore = computed(() => notificationStore.hasMore);
const error = computed(() => notificationStore.error);
const isConnected = computed(() => notificationStore.isConnected);
const errorMessage = computed(() => {
  if (!error.value) return "Failed to load notifications";

  // Format error message for better readability
  if (error.value.includes("after multiple attempts")) {
    return "Failed to load notifications after multiple attempts";
  } else if (
    error.value.includes("network") ||
    error.value.includes("timeout")
  ) {
    return "Network error. Please check your connection and try again.";
  } else {
    return error.value;
  }
});

// Format ISO-like date/time strings in notification content into human-readable text
const formatNotificationContent = (content) => {
  if (!content || typeof content !== "string") return content;

  // Collect date-only occurrences to decide if we can show time-only for matching datetimes
  const dateOnlyRegex = /\b(\d{4})-(\d{2})-(\d{2})\b/g;
  const dateSet = new Set();
  let m;
  while ((m = dateOnlyRegex.exec(content)) !== null) {
    dateSet.add(`${m[1]}-${m[2]}-${m[3]}`);
  }

  const dateFormatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
  const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
  const timeFormatter = new Intl.DateTimeFormat(undefined, {
    hour: "numeric",
    minute: "2-digit",
  });

  // Replace ISO datetime with Z (UTC), e.g., 2025-08-09T03:00Z or with seconds
  const isoDateTimeRegex = /\b(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}(?::\d{2})?)Z\b/g;
  let result = content.replace(isoDateTimeRegex, (match, datePart) => {
    const d = new Date(match); // parse as UTC -> local
    if (isNaN(d)) return match;
    return dateSet.has(datePart) ? timeFormatter.format(d) : dateTimeFormatter.format(d);
  });

  // Replace date-only (treat as local date to avoid TZ off-by-one)
  result = result.replace(dateOnlyRegex, (full, y, mo, d) => {
    const year = parseInt(y, 10);
    const month = parseInt(mo, 10) - 1;
    const day = parseInt(d, 10);
    const localDate = new Date(year, month, day);
    if (isNaN(localDate)) return full;
    return dateFormatter.format(localDate);
  });

  return result;
};

// Methods
const toggleDropdown = async () => {
  isOpen.value = !isOpen.value;

  if (isOpen.value) {
    // Debug: Log connection status when opening dropdown
    console.log('NotificationDropdown: Opening dropdown, isConnected:', isConnected.value);
    
    // Fetch notifications when opening dropdown
    if (notifications.value.length === 0) {
      await notificationStore.fetchNotifications(0, 10, false);
    }

    // Focus management for accessibility
    setTimeout(() => {
      const firstItem = dropdownRef.value?.querySelector(".notification-item");
      if (firstItem) {
        firstItem.focus();
      } else {
        // If no notifications, focus on the header or mark all as read button
        const markAllBtn =
          dropdownRef.value?.querySelector(".mark-all-read-btn");
        const header = dropdownRef.value?.querySelector(".notification-header");
        if (markAllBtn) {
          markAllBtn.focus();
        } else if (header) {
          header.setAttribute("tabindex", "-1");
          header.focus();
        }
      }
    }, 100);
  } else {
    // Return focus to the notification toggle button when closing
    const toggleButton = dropdownRef.value?.querySelector(
      ".notification-toggle"
    );
    if (toggleButton) {
      toggleButton.focus();
    }
  }
};

const closeDropdown = () => {
  isOpen.value = false;
};

const handleNotificationClick = async (notification) => {
  if (!notification.read) {
    await notificationStore.markAsRead(notification.id);
  }
  // Could add navigation logic here if notifications have associated routes
};

const handleMarkAllAsRead = async () => {
  if (isMarkingAllAsRead.value) return;

  isMarkingAllAsRead.value = true;
  try {
    await notificationStore.markAllAsRead();
  } finally {
    isMarkingAllAsRead.value = false;
  }
};

const loadMoreNotifications = async () => {
  await notificationStore.fetchMoreNotifications(false);
};

const retryFetch = async () => {
  if (isRetrying.value) return;

  isRetrying.value = true;
  notificationStore.clearError();

  try {
    await notificationStore.fetchNotifications(0, 10, false);
  } catch (error) {
    console.error("Retry fetch failed:", error);
  } finally {
    isRetrying.value = false;
  }
};

const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  if (isNaN(date.getTime())) return "Just now";
  let diffMs = now - date;
  // Treat slight future timestamps (<= 1 minute ahead) as "Just now"
  if (diffMs < 0 && Math.abs(diffMs) <= 60 * 1000) return "Just now";
  const diffInMinutes = Math.floor(diffMs / (1000 * 60));

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
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  document.removeEventListener("keydown", handleKeydown);
  window.removeEventListener(
    "websocket-reconnection-status",
    handleReconnectionStatus
  );
});

// Keyboard navigation for notifications
const focusNextNotification = (currentIndex) => {
  const items = dropdownRef.value?.querySelectorAll(".notification-item");
  if (!items || items.length === 0) return;

  const nextIndex = currentIndex + 1;
  if (nextIndex < items.length) {
    items[nextIndex].focus();
  }
};

const focusPreviousNotification = (currentIndex) => {
  const items = dropdownRef.value?.querySelectorAll(".notification-item");
  if (!items || items.length === 0) return;

  const prevIndex = currentIndex - 1;
  if (prevIndex >= 0) {
    items[prevIndex].focus();
  } else {
    // Focus back to the notification toggle button if at the first item
    const toggleButton = dropdownRef.value?.querySelector(
      ".notification-toggle"
    );
    if (toggleButton) {
      toggleButton.focus();
    }
  }
};

const focusFirstNotification = () => {
  const items = dropdownRef.value?.querySelectorAll(".notification-item");
  if (!items || items.length === 0) return;

  items[0].focus();
};

const focusLastNotification = () => {
  const items = dropdownRef.value?.querySelectorAll(".notification-item");
  if (!items || items.length === 0) return;

  items[items.length - 1].focus();
};

// Touch gesture handling
const touchStartX = ref(0);
const touchEndX = ref(0);
const swipeThreshold = 80; // Minimum distance for swipe
const swipeNotification = ref(null);

const handleTouchStart = (event, notification) => {
  touchStartX.value = event.changedTouches[0].screenX;
  swipeNotification.value = notification;
};

const handleTouchMove = (event) => {
  touchEndX.value = event.changedTouches[0].screenX;

  // Calculate swipe distance
  const swipeDistance = touchEndX.value - touchStartX.value;

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
  const swipeDistance = touchEndX.value - touchStartX.value;
  const element = event.currentTarget;

  // Reset the element style
  element.style.transform = "";
  element.style.opacity = "";

  // If swipe distance exceeds threshold, mark as read
  if (swipeDistance < -swipeThreshold && notification && !notification.read) {
    await notificationStore.markAsRead(notification.id);
  }
};

// Watch for store initialization
watch(
  () => notificationStore.isConnected,
  (isConnected, oldValue) => {
    console.log('NotificationDropdown: Connection status changed from', oldValue, 'to', isConnected);
    if (isConnected) {
      // Refresh unread count when WebSocket connects
      notificationStore.fetchUnreadCount();
    }
  }
);
</script>

<style scoped>
.notification-dropdown {
  position: relative;
}

.notification-toggle {
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

.notification-toggle:hover,
.notification-toggle--active {
  background: var(--color-primary);
  color: var(--color-background);
  transform: scale(1.05);
}

.notification-toggle:active {
  transform: scale(0.95);
}

.notification-icon {
  width: 20px;
  height: 20px;
  transition: transform var(--transition-fast);
}

.notification-toggle:hover .notification-icon {
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

.notification-badge {
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

.notification-menu {
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

.notification-menu--open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-background-soft);
}

.notification-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.mark-all-read-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.mark-all-read-btn:hover:not(:disabled) {
  background: var(--color-background-mute);
  color: var(--color-primary);
}

.mark-all-read-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-loading,
.notification-empty,
.notification-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem;
  text-align: center;
  color: var(--color-text-muted);
}

.notification-empty .empty-icon,
.notification-error .error-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.notification-empty p,
.notification-error p {
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.notification-empty span {
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

.notification-items {
  padding: 0.5rem 0;
}

.notification-item {
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

.notification-item:hover {
  background: var(--color-background-soft);
}

.notification-item:focus {
  outline: none;
  background: var(--color-background-mute);
  border-left-color: var(--color-primary);
}

.notification-item--unread {
  background: var(--color-background-mute);
  border-left-color: var(--color-primary);
  opacity: 1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.notification-item--unread:hover {
  background: var(--color-background-soft);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 0.875rem;
  color: var(--color-text);
  margin: 0 0 0.25rem 0;
  line-height: 1.4;
  word-wrap: break-word;
}

.notification-item--unread .notification-text {
  font-weight: 500;
}

.notification-time {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.notification-unread-dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  margin-top: 0.25rem;
  flex-shrink: 0;
}

.load-more-btn {
  width: 100%;
  padding: 0.75rem 1.25rem;
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  border-top: 1px solid var(--color-border);
}

.load-more-btn:hover {
  background: var(--color-background-soft);
  color: var(--color-secondary);
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  border-top: 1px solid var(--color-border);
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

.loading-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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
  .notification-menu {
    width: calc(100vw - 40px);
    max-width: 320px;
    right: -20px;
  }

  .notification-header {
    padding: 0.875rem 1rem;
  }

  .notification-item {
    padding: 0.75rem 1rem;
  }

  .mark-all-read-btn {
    font-size: 0.8125rem;
  }

  .notification-text {
    font-size: 0.8125rem;
  }

  .notification-time {
    font-size: 0.6875rem;
  }

  /* Improve touch target sizes */
  .notification-toggle {
    width: 44px;
    height: 44px;
  }

  .notification-item {
    min-height: 60px; /* Ensure touch targets are at least 44px high */
  }

  .load-more-btn {
    padding: 1rem 1.25rem; /* Larger touch target */
  }
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .notification-badge {
    border-color: var(--color-background);
  }
}

html.dark .notification-badge {
  border-color: var(--color-background) !important;
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .notification-toggle,
  .notification-menu,
  .notification-item,
  .notification-badge {
    transition: none;
  }

  .notification-badge {
    animation: none;
  }

  .loading-spinner {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .notification-menu {
    border-width: 2px;
  }

  .notification-item--unread {
    border-left-width: 4px;
  }

  .notification-unread-dot {
    width: 10px;
    height: 10px;
  }
}
</style>
