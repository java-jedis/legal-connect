<script setup>
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
import NotificationBanner from '@/components/NotificationBanner.vue'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { useChatStore } from '@/stores/chat'
import { ref, onMounted, watch } from 'vue'
import { initializeApp } from './services/init';

const themeStore = useThemeStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const chatStore = useChatStore()

// Connection warning state
const showConnectionWarning = ref(false)
const connectionWarningDelay = ref(null)

// Methods for connection warning
const reconnectWebSocket = async () => {
  try {
    await notificationStore.connectWebSocket()
  } catch (error) {
    console.error('Failed to reconnect WebSocket:', error)
  }
}

const dismissConnectionWarning = () => {
  showConnectionWarning.value = false
}

onMounted(async () => {
  themeStore.initTheme();
  console.log('Theme initialized:', themeStore.isDark ? 'dark' : 'light');
  await initializeApp();
});

// Watch for auth state changes to initialize/cleanup notification and chat stores
watch(() => authStore.isLoggedIn, async (isLoggedIn) => {
  if (!isLoggedIn) {
    await notificationStore.cleanup();
    await chatStore.cleanup();
    console.log('Notification and chat systems cleaned up after logout.');
  }
});

// Watch for connection status changes to show persistent warning after delay
watch(() => notificationStore.isConnected, (isConnected, oldValue) => {
  // Clear any existing timer
  if (connectionWarningDelay.value) {
    clearTimeout(connectionWarningDelay.value)
    connectionWarningDelay.value = null
  }
  
  // Only show warning if we were previously connected and now disconnected
  if (oldValue === true && isConnected === false) {
    // Show warning after 30 seconds of disconnection
    connectionWarningDelay.value = setTimeout(() => {
      showConnectionWarning.value = true
    }, 30000) // 30 seconds
  } else if (isConnected) {
    // Hide warning when reconnected
    showConnectionWarning.value = false
  }
})

// Transition hooks for smooth animations
const beforeEnter = (el) => {
  el.style.opacity = '0'
  el.style.transform = 'translateY(20px)'
}

const enter = (el, done) => {
  // Use requestAnimationFrame to ensure the initial state is applied
  requestAnimationFrame(() => {
    el.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
    el.style.opacity = '1'
    el.style.transform = 'translateY(0)'
    
    // Clean up transition after it completes
    setTimeout(() => {
      el.style.transition = ''
      done()
    }, 400)
  })
}

const leave = (el, done) => {
  el.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
  el.style.opacity = '0'
  el.style.transform = 'translateY(-20px)'
  
  setTimeout(done, 300)
}
</script>

<template>
  <div id="app">
    <!-- Add NotificationBanner component for real-time notifications -->
    <NotificationBanner v-if="authStore.isLoggedIn" />
    
    <!-- Connection status indicator for persistent disconnections -->
    <div v-if="authStore.isLoggedIn && !notificationStore.isConnected && showConnectionWarning" class="connection-warning">
      <div class="connection-warning-content">
        <svg class="warning-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span>Disconnected from notification service. Some features may be unavailable.</span>
        <button @click="reconnectWebSocket" class="reconnect-btn">Reconnect</button>
        <button @click="dismissConnectionWarning" class="dismiss-btn" aria-label="Dismiss warning">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
    
    <AppHeader />
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition 
          name="page" 
          mode="out-in"
          @before-enter="beforeEnter"
          @enter="enter"
          @leave="leave"
        >
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
  </div>
</template>

<style>
/* Global app styles */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Global page transition styles */
.page-enter-active,
.page-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Ensure smooth scrolling for the entire app */
html {
  scroll-behavior: smooth;
}

/* Prevent layout shift during transitions */
.main-content {
  min-height: calc(100vh - 140px); /* Adjust based on header/footer height */
}

/* Smooth transitions for all interactive elements */
* {
  transition: color 0.2s ease, background-color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

/* Enhanced button transitions */
.btn {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.btn:hover {
  transform: translateY(-2px);
}

/* Smooth form input transitions */
input, textarea, select {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Card hover effects */
.card, .dashboard-card, .quick-action-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Loading states */
.loading {
  transition: opacity 0.3s ease;
}

/* Fade in animation for content */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Stagger animation for lists */
.stagger-item {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  animation-fill-mode: both;
}

.stagger-item:nth-child(1) { animation-delay: 0.1s; }
.stagger-item:nth-child(2) { animation-delay: 0.2s; }
.stagger-item:nth-child(3) { animation-delay: 0.3s; }
.stagger-item:nth-child(4) { animation-delay: 0.4s; }
.stagger-item:nth-child(5) { animation-delay: 0.5s; }
.stagger-item:nth-child(6) { animation-delay: 0.6s; }

/* Connection warning styles */
.connection-warning {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba(244, 67, 54, 0.1);
  border-bottom: 1px solid var(--color-border);
  z-index: 1000;
  padding: 0.5rem 0;
}

.connection-warning-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  font-size: 0.875rem;
}

.connection-warning .warning-icon {
  width: 18px;
  height: 18px;
  color: #F44336;
  flex-shrink: 0;
}

.reconnect-btn {
  padding: 0.25rem 0.75rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.reconnect-btn:hover {
  background: var(--color-secondary);
}

.dismiss-btn {
  width: 24px;
  height: 24px;
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: 50%;
  padding: 4px;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dismiss-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--color-text);
}

.dismiss-btn svg {
  width: 16px;
  height: 16px;
}

@media (max-width: 768px) {
  .connection-warning-content {
    flex-wrap: wrap;
    padding: 0.5rem 1rem;
  }
  
  .connection-warning-content span {
    flex: 1 0 100%;
    margin-bottom: 0.5rem;
    text-align: center;
  }
  
  .reconnect-btn {
    margin-left: auto;
  }
}
</style>
