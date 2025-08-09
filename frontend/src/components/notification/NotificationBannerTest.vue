<template>
  <div class="notification-test">
    <h2>Notification Banner Test</h2>
    <div class="test-controls">
      <button @click="sendTestNotification" class="test-button">
        Send Test Notification
      </button>
      <button @click="sendMultipleNotifications" class="test-button">
        Send Multiple Notifications
      </button>
    </div>
    
    <!-- Include the notification banner component -->
    <NotificationBanner />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import NotificationBanner from './NotificationBanner.vue';

// Test data
const notificationCount = ref(1);

// Methods
const sendTestNotification = () => {
  const notification = {
    id: `test-${Date.now()}`,
    content: `Test notification #${notificationCount.value}`,
    createdAt: new Date().toISOString(),
    isRead: false
  };
  
  // Increment counter for next notification
  notificationCount.value++;
  
  // Dispatch event to trigger notification banner
  const event = new CustomEvent('new-notification', {
    detail: notification
  });
  window.dispatchEvent(event);
};

const sendMultipleNotifications = () => {
  // Send 5 notifications with a slight delay between them
  for (let i = 0; i < 5; i++) {
    setTimeout(() => {
      const notification = {
        id: `test-${Date.now()}-${i}`,
        content: `Multiple test notification #${notificationCount.value + i}`,
        createdAt: new Date().toISOString(),
        isRead: false
      };
      
      // Dispatch event to trigger notification banner
      const event = new CustomEvent('new-notification', {
        detail: notification
      });
      window.dispatchEvent(event);
    }, i * 500); // 500ms delay between notifications
  }
  
  // Update counter
  notificationCount.value += 5;
};
</script>

<style scoped>
.notification-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.test-button {
  padding: 8px 16px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.test-button:hover {
  background: var(--color-secondary);
}
</style>