<template>
  <header class="header">
    <div class="container">
      <nav class="nav">
        <div class="nav-brand">
          <router-link to="/" class="brand">
            <svg
              class="brand-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 2L2 7L12 12L22 7L12 2Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M2 17L12 22L22 17"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M2 12L12 17L22 12"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span class="brand-text">Legal Connect</span>
          </router-link>
        </div>

        <div class="nav-menu" :class="{ 'nav-menu--open': isMenuOpen }">
          <!-- Public navigation -->
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/" class="nav-link">Home</router-link>
            <router-link to="/services" class="nav-link">Services</router-link>
            <router-link to="/how-it-works" class="nav-link">How It Works</router-link>
            <router-link to="/about" class="nav-link">About</router-link>
            <router-link to="/contact" class="nav-link">Contact</router-link>
          </template>

          <!-- User navigation -->
          <template v-else-if="authStore.isUser()">
            <router-link to="/dashboard/user" class="nav-link">Dashboard</router-link>
            <router-link to="/cases" class="nav-link">My Cases</router-link>
            <router-link to="/find-lawyer" class="nav-link">Find Lawyers</router-link>
            <router-link to="/ai-chat" class="nav-link">Legal Chat</router-link>
            <router-link to="/profile" class="nav-link">Profile</router-link>
          </template>

          <!-- Lawyer navigation -->
          <template v-else-if="authStore.isLawyer()">
            <router-link to="/dashboard/lawyer" class="nav-link">Dashboard</router-link>
            <router-link to="/cases" class="nav-link">Client Cases</router-link>
            <router-link to="/ai-chat" class="nav-link">AIChatBox</router-link>
            <router-link to="/profile" class="nav-link">Profile</router-link>
          </template>

          <!-- Admin navigation -->
          <template v-else-if="authStore.isAdmin()">
            <router-link to="/dashboard/admin" class="nav-link">Admin Dashboard</router-link>
            <router-link to="/admin/lawyers" class="nav-link">Manage Lawyers</router-link>
            <router-link to="/admin/feedback" class="nav-link">Feedback</router-link>
            <router-link to="/admin/complaints" class="nav-link">Complaints</router-link>
          </template>
        </div>

        <div class="nav-actions">
          <ThemeToggle />
          
          <!-- Chat inbox dropdown (only for authenticated users) -->
          <ChatInboxDropdown v-if="authStore.isLoggedIn" />
          
          <!-- Notification dropdown (only for authenticated users) -->
          <NotificationDropdown v-if="authStore.isLoggedIn" />

          <!-- Public actions -->
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" class="btn btn-secondary nav-btn">Sign In</router-link>
            <router-link to="/register" class="btn nav-btn">Sign Up</router-link>
          </template>

          <!-- Logged in actions -->
          <template v-else>
            <div class="user-menu">
              <button class="user-menu-toggle" @click="toggleUserMenu">
                <div class="user-avatar">
                  <span>{{ userInitial }}</span>
                </div>
                <span class="user-name">{{ userFullName }}</span>
                <svg
                  class="dropdown-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M6 9L12 15L18 9"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>

              <div class="user-dropdown" :class="{ 'user-dropdown--open': isUserMenuOpen }">
                <div class="dropdown-header">
                  <span class="user-email">{{ authStore.userInfo?.email }}</span>
                  <span class="user-type">{{ authStore.userType }}</span>
                </div>
                <div class="dropdown-actions">
                  <button class="dropdown-item" @click="logout">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M9 21H5A2 2 0 0 1 3 19V5A2 2 0 0 1 5 3H9"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <polyline
                        points="16,17 21,12 16,7"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <line
                        x1="21"
                        y1="12"
                        x2="9"
                        y2="12"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                    Sign Out
                  </button>
                </div>
              </div>
            </div>
          </template>

          <button
            @click="toggleMenu"
            class="menu-toggle"
            :aria-label="isMenuOpen ? 'Close menu' : 'Open menu'"
          >
            <span class="menu-line" :class="{ 'menu-line--open': isMenuOpen }"></span>
            <span class="menu-line" :class="{ 'menu-line--open': isMenuOpen }"></span>
            <span class="menu-line" :class="{ 'menu-line--open': isMenuOpen }"></span>
          </button>
        </div>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useChatStore } from '../stores/chat'
import { useNotificationStore } from '../stores/notification'
import ChatInboxDropdown from './chat/ChatInboxDropdown.vue'
import NotificationDropdown from './notification/NotificationDropdown.vue'
import ThemeToggle from './ThemeToggle.vue'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const chatStore = useChatStore()
const isMenuOpen = ref(false)
const isUserMenuOpen = ref(false)

// Initialize notification and chat stores when component is mounted
onMounted(() => {
  if (authStore.isLoggedIn) {
    notificationStore.initialize()
    chatStore.initialize()
  }
})

// Computed properties for user display
const userFullName = computed(() => {
  if (authStore.userInfo?.firstName && authStore.userInfo?.lastName) {
    return `${authStore.userInfo.firstName} ${authStore.userInfo.lastName}`
  }
  return authStore.userInfo?.firstName || authStore.userInfo?.email || 'User'
})

const userInitial = computed(() => {
  if (authStore.userInfo?.firstName) {
    return authStore.userInfo.firstName.charAt(0).toUpperCase()
  }
  if (authStore.userInfo?.email) {
    return authStore.userInfo.email.charAt(0).toUpperCase()
  }
  return 'U'
})

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const toggleUserMenu = () => {
  isUserMenuOpen.value = !isUserMenuOpen.value
}

const logout = async () => {
  // Clean up notification and chat stores before logout
  await notificationStore.cleanup()
  await chatStore.cleanup()
  
  // Perform logout
  await authStore.logout()
  isUserMenuOpen.value = false
  router.push('/')
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  backdrop-filter: blur(10px);
  transition: all var(--transition-normal);
}

.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
  min-height: 70px;
}

.nav-brand {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--color-heading);
  font-weight: 700;
  font-size: 1.5rem;
  transition: color var(--transition-fast);
}

.brand:hover {
  color: var(--color-primary);
}

.brand-icon {
  width: 32px;
  height: 32px;
  margin-right: 0.5rem;
  color: var(--color-primary);
}

.brand-text {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  text-decoration: none;
  color: var(--color-text);
  font-weight: 500;
  transition: all var(--transition-fast);
  position: relative;
}

.nav-link:hover {
  color: var(--color-primary);
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--color-primary);
  transition: width var(--transition-fast);
}

.nav-link:hover::after {
  width: 100%;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  z-index: 101; /* Ensure dropdowns appear above other elements */
}

.nav-btn {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

/* User Menu Styles */
.user-menu {
  position: relative;
}

.user-menu-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: var(--color-text);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

.user-menu-toggle:hover {
  background: var(--color-background-soft);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 500;
}

.dropdown-icon {
  width: 16px;
  height: 16px;
  transition: transform var(--transition-fast);
}

.user-menu-toggle:hover .dropdown-icon {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 250px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all var(--transition-normal);
  z-index: 1000;
  margin-top: 0.5rem;
}

.user-dropdown--open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-header {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.user-email {
  display: block;
  font-size: 0.875rem;
  color: var(--color-text);
  margin-bottom: 0.25rem;
}

.user-type {
  display: block;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  text-transform: capitalize;
}

.dropdown-actions {
  padding: 0.5rem;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem;
  background: none;
  border: none;
  color: var(--color-text);
  cursor: pointer;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
  font-size: 0.875rem;
}

.dropdown-item:hover {
  background: var(--color-background-soft);
  color: var(--color-primary);
}

.dropdown-item svg {
  width: 16px;
  height: 16px;
}

.menu-toggle {
  display: none;
  flex-direction: column;
  justify-content: space-around;
  width: 30px;
  height: 30px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
}

.menu-line {
  width: 100%;
  height: 3px;
  background: var(--color-text);
  border-radius: 2px;
  transition: all var(--transition-fast);
}

.menu-line--open:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
}

.menu-line--open:nth-child(2) {
  opacity: 0;
}

.menu-line--open:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
}

@media (max-width: 768px) {
  .nav-menu {
    position: fixed;
    top: 70px;
    left: 0;
    right: 0;
    background: var(--color-background);
    border-bottom: 1px solid var(--color-border);
    flex-direction: column;
    padding: 2rem;
    gap: 1.5rem;
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all var(--transition-normal);
  }

  .nav-menu--open {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .nav-actions {
    gap: 0.5rem;
    margin-left: auto; /* Push actions to the right */
  }

  .nav-btn {
    display: none;
  }

  .menu-toggle {
    display: flex;
    margin-left: 0.5rem; /* Add spacing between notification dropdown and menu toggle */
  }
  
  /* Ensure user name is hidden on smaller screens but avatar remains */
  .user-name {
    display: none;
  }
  
  /* Adjust user menu position for better mobile experience */
  .user-dropdown {
    right: -10px;
  }
}

@media (max-width: 480px) {
  .nav {
    padding: 0.75rem 0;
    min-height: 60px;
  }

  .brand-text {
    font-size: 1.25rem;
  }

  .brand-icon {
    width: 28px;
    height: 28px;
  }
}
</style>
