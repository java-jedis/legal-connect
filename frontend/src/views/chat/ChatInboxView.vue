<template>
  <div class="chat-inbox-view">
    <div class="container">
      <!-- Header Section -->
      <div class="inbox-header">
        <div class="header-content">
          <div class="header-title">
            <h1>Messages</h1>
            <p class="header-subtitle">
              {{ totalConversations }} conversation{{ totalConversations !== 1 ? 's' : '' }}
              <span v-if="hasUnreadMessages" class="unread-indicator">
                • {{ totalUnreadCount }} unread
              </span>
            </p>
          </div>
          <div class="header-actions">
            <button 
              v-if="!isConnected" 
              @click="reconnectWebSocket" 
              class="btn btn-outline"
              :disabled="isReconnecting"
            >
              <svg v-if="!isReconnecting" class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 4v6h6M23 20v-6h-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else class="icon loading-spinner" viewBox="0 0 24 24">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" opacity="0.3"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" fill="none"/>
              </svg>
              {{ isReconnecting ? 'Reconnecting...' : 'Reconnect' }}
            </button>
            <button @click="refreshConversations" class="btn btn-primary" :disabled="isLoading">
              <svg v-if="!isLoading" class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 4v6h6M23 20v-6h-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else class="icon loading-spinner" viewBox="0 0 24 24">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" opacity="0.3"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" fill="none"/>
              </svg>
              {{ isLoading ? 'Refreshing...' : 'Refresh' }}
            </button>
          </div>
        </div>

        <!-- Connection Status Banner -->
        <div v-if="!isConnected" class="connection-status-banner">
          <svg class="warning-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Disconnected from chat service. Messages may not update in real-time.</span>
        </div>

        <!-- Search and Filters -->
        <div class="search-filters">
          <div class="search-bar">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search conversations..."
              class="search-input"
              @input="debouncedSearch"
            />
            <button 
              v-if="searchQuery" 
              @click="clearSearch" 
              class="clear-search-btn"
              aria-label="Clear search"
            >
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          
          <div class="filter-controls">
            <select v-model="selectedFilter" @change="applyFilter" class="filter-select">
              <option value="all">All conversations</option>
              <option value="unread">Unread only</option>
              <option value="recent">Recent activity</option>
            </select>
            
            <button 
              @click="toggleSortOrder" 
              class="sort-btn"
              :title="sortOrder === 'desc' ? 'Sort oldest first' : 'Sort newest first'"
            >
              <svg class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 6h18M7 12h10m-7 6h4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path v-if="sortOrder === 'desc'" d="M16 4l4 4-4 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path v-else d="M16 20l4-4-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ sortOrder === 'desc' ? 'Newest first' : 'Oldest first' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="inbox-content">
        <!-- Loading State -->
        <div v-if="isLoading && filteredConversations.length === 0" class="loading-state">
          <ChatLoadingSkeleton 
            type="conversation-list" 
            :count="5" 
            :animated="true"
          />
        </div>

        <!-- Error State -->
        <div v-else-if="error && filteredConversations.length === 0" class="error-state">
          <svg class="error-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h3>Failed to load conversations</h3>
          <p>{{ errorMessage }}</p>
          <button @click="retryFetch" class="btn btn-primary" :disabled="isRetrying">
            <svg v-if="isRetrying" class="icon loading-spinner" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" opacity="0.3"/>
              <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" fill="none"/>
            </svg>
            {{ isRetrying ? 'Retrying...' : 'Try again' }}
          </button>
        </div>

        <!-- Empty State -->
        <div v-else-if="filteredConversations.length === 0 && !isLoading" class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h3>{{ getEmptyStateTitle() }}</h3>
          <p>{{ getEmptyStateMessage() }}</p>
          <div class="empty-actions">
            <button @click="$router.push('/find-lawyer')" class="btn btn-primary">
              Find a Lawyer
            </button>
            <button v-if="searchQuery || selectedFilter !== 'all'" @click="clearFilters" class="btn btn-outline">
              Clear Filters
            </button>
          </div>
        </div>

        <!-- Conversations List -->
        <div v-else class="conversations-container">
          <ChatConversationList 
            :conversations="paginatedConversations"
            :loading="isLoading"
            @conversation-click="handleConversationClick"
          />

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="pagination">
            <button 
              @click="goToPage(currentPage - 1)"
              :disabled="currentPage <= 1"
              class="pagination-btn"
            >
              <svg class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <polyline points="15,18 9,12 15,6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Previous
            </button>

            <div class="pagination-info">
              <span class="page-numbers">
                Page {{ currentPage }} of {{ totalPages }}
              </span>
              <span class="total-count">
                {{ filteredConversations.length }} conversation{{ filteredConversations.length !== 1 ? 's' : '' }}
              </span>
            </div>

            <button 
              @click="goToPage(currentPage + 1)"
              :disabled="currentPage >= totalPages"
              class="pagination-btn"
            >
              Next
              <svg class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <polyline points="9,18 15,12 9,6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import ChatConversationList from '@/components/chat/ChatConversationList.vue'
import ChatLoadingSkeleton from '@/components/chat/ChatLoadingSkeleton.vue'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

// Stores
const chatStore = useChatStore()
const authStore = useAuthStore()
const router = useRouter()

// Reactive state
const searchQuery = ref('')
const selectedFilter = ref('all')
const sortOrder = ref('desc') // 'desc' for newest first, 'asc' for oldest first
const currentPage = ref(1)
const itemsPerPage = ref(20)
const isRetrying = ref(false)
const isReconnecting = ref(false)
const searchTimeout = ref(null)

// Computed properties
const conversations = computed(() => chatStore.sortedConversations)
const isLoading = computed(() => chatStore.isLoading)
const error = computed(() => chatStore.error)
const isConnected = computed(() => chatStore.isConnected)
const totalUnreadCount = computed(() => chatStore.totalUnreadCount)
const hasUnreadMessages = computed(() => chatStore.hasUnreadMessages)

const errorMessage = computed(() => {
  if (!error.value) return 'Failed to load conversations'
  
  if (error.value.includes('network') || error.value.includes('timeout')) {
    return 'Network error. Please check your connection and try again.'
  } else if (error.value.includes('unauthorized') || error.value.includes('403')) {
    return 'You are not authorized to access conversations.'
  } else {
    return error.value
  }
})

const filteredConversations = computed(() => {
  let filtered = [...conversations.value]

  // Apply search filter
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim()
    filtered = filtered.filter(conv => {
      const participantName = getParticipantName(conv).toLowerCase()
      const lastMessage = (conv.lastMessage || '').toLowerCase()
      return participantName.includes(query) || lastMessage.includes(query)
    })
  }

  // Apply status filter
  switch (selectedFilter.value) {
    case 'unread':
      filtered = filtered.filter(conv => 
        chatStore.getConversationUnreadCount(conv.id) > 0
      )
      break
    case 'recent': {
      // Show conversations with activity in the last 7 days
      const sevenDaysAgo = new Date()
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
      filtered = filtered.filter(conv => {
        const lastActivity = new Date(conv.lastMessageTime || conv.updatedAt)
        return lastActivity >= sevenDaysAgo
      })
      break
    }
    // 'all' case doesn't need filtering
  }

  // Apply sorting
  filtered.sort((a, b) => {
    const timeA = new Date(a.lastMessageTime || a.updatedAt)
    const timeB = new Date(b.lastMessageTime || b.updatedAt)
    
    if (sortOrder.value === 'desc') {
      return timeB - timeA // Newest first
    } else {
      return timeA - timeB // Oldest first
    }
  })

  return filtered
})

const totalConversations = computed(() => conversations.value.length)
const totalPages = computed(() => Math.ceil(filteredConversations.value.length / itemsPerPage.value))

const paginatedConversations = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredConversations.value.slice(start, end)
})

// Methods
const getParticipantName = (conversation) => {
  // Try multiple possible field names from the backend
  return conversation.participantTwoName || 
         conversation.otherParticipantName || 
         conversation.participantName ||
         'Unknown User';
};

const getEmptyStateTitle = () => {
  if (searchQuery.value.trim()) {
    return 'No conversations found'
  } else if (selectedFilter.value === 'unread') {
    return 'No unread conversations'
  } else if (selectedFilter.value === 'recent') {
    return 'No recent conversations'
  } else {
    return 'No conversations yet'
  }
}

const getEmptyStateMessage = () => {
  if (searchQuery.value.trim()) {
    return `No conversations match "${searchQuery.value}". Try adjusting your search terms.`
  } else if (selectedFilter.value === 'unread') {
    return 'All your conversations are up to date. Great job staying on top of your messages!'
  } else if (selectedFilter.value === 'recent') {
    return 'No conversations have had activity in the last 7 days.'
  } else {
    return 'Start a conversation by finding a lawyer and sending them a message.'
  }
}

const debouncedSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  
  searchTimeout.value = setTimeout(() => {
    // Reset to first page when searching
    currentPage.value = 1
  }, 300)
}

const clearSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
}

const applyFilter = () => {
  // Reset to first page when filtering
  currentPage.value = 1
}

const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  currentPage.value = 1
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedFilter.value = 'all'
  sortOrder.value = 'desc'
  currentPage.value = 1
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    // Scroll to top of conversations list
    const container = document.querySelector('.conversations-container')
    if (container) {
      container.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }
}

const handleConversationClick = (conversation) => {
  console.log('Conversation clicked:', conversation);
  
  // Set the current conversation in the chat store
  chatStore.setCurrentConversation(conversation);
  
  // Navigate to conversation detail view
  router.push(`/chat/${conversation.id}`);
}

const refreshConversations = async () => {
  chatStore.clearError()
  await chatStore.fetchConversations()
}

const retryFetch = async () => {
  if (isRetrying.value) return
  
  isRetrying.value = true
  chatStore.clearError()
  
  try {
    await chatStore.fetchConversations()
  } catch (error) {
    console.error('Retry fetch failed:', error)
  } finally {
    isRetrying.value = false
  }
}

const reconnectWebSocket = async () => {
  if (isReconnecting.value) return
  
  isReconnecting.value = true
  
  try {
    const success = await chatStore.manualReconnect()
    if (success) {
      // Refresh conversations after successful reconnection
      await refreshConversations()
    }
  } catch (error) {
    console.error('Failed to reconnect WebSocket:', error)
  } finally {
    isReconnecting.value = false
  }
}

// Real-time event handlers
const handleRealTimeMessage = (event) => {
  const message = event.detail;
  console.log("ChatInboxView: Real-time message received:", message);
  
  // Refresh conversations to show updated last message and unread counts
  refreshConversations();
};

const handleRealTimeUnreadUpdate = (event) => {
  const unreadData = event.detail;
  console.log("ChatInboxView: Real-time unread count update:", unreadData);
  
  // The store will handle the unread count update automatically
  // We just need to refresh conversations to show updated data
  refreshConversations();
};

const handleRealTimeReadStatus = (event) => {
  const readData = event.detail;
  console.log("ChatInboxView: Real-time read status update:", readData);
  
  // Refresh conversations to update unread indicators
  refreshConversations();
};

const handleWebSocketReconnection = (event) => {
  const status = event.detail;
  console.log("ChatInboxView: WebSocket reconnection status:", status);
  
  if (status.success && !status.reconnecting) {
    // Successful reconnection - refresh data
    console.log("WebSocket reconnected successfully, refreshing chat data...");
    refreshConversations();
  }
};

// Lifecycle hooks
onMounted(async () => {
  // Initialize chat store if not already done
  if (!chatStore.isConnected && authStore.isLoggedIn) {
    await chatStore.initialize()
  }
  
  // Fetch conversations if not already loaded
  if (conversations.value.length === 0) {
    await chatStore.fetchConversations()
  }
  
  // Add real-time event listeners
  window.addEventListener("websocket-message", handleRealTimeMessage);
  window.addEventListener("websocket-unread-count-update", handleRealTimeUnreadUpdate);
  window.addEventListener("websocket-message-read", handleRealTimeReadStatus);
  window.addEventListener("websocket-conversation-read", handleRealTimeReadStatus);
  window.addEventListener("websocket-reconnection-status", handleWebSocketReconnection);
})

onUnmounted(() => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  
  // Remove real-time event listeners
  window.removeEventListener("websocket-message", handleRealTimeMessage);
  window.removeEventListener("websocket-unread-count-update", handleRealTimeUnreadUpdate);
  window.removeEventListener("websocket-message-read", handleRealTimeReadStatus);
  window.removeEventListener("websocket-conversation-read", handleRealTimeReadStatus);
  window.removeEventListener("websocket-reconnection-status", handleWebSocketReconnection);
})

// Watch for changes in conversations to reset pagination if needed
watch(filteredConversations, (newConversations) => {
  // If current page is beyond available pages, reset to last page
  const maxPage = Math.ceil(newConversations.length / itemsPerPage.value)
  if (currentPage.value > maxPage && maxPage > 0) {
    currentPage.value = maxPage
  }
})
</script>

<style scoped>
.chat-inbox-view {
  min-height: 100vh;
  background: var(--color-background);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Header Section */
.inbox-header {
  padding: 2rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
  margin: 1rem 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  padding: 0 0.5rem;
}

.header-title h1 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0 0 0.5rem 0;
}

.header-subtitle {
  color: var(--color-text-muted);
  font-size: 1rem;
  margin: 0;
}

.unread-indicator {
  color: var(--color-primary);
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

/* Connection Status Banner */
.connection-status-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.2);
  border-radius: var(--border-radius);
  color: #d32f2f;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}

.warning-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* Search and Filters */
.search-filters {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-bar {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: var(--color-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  transition: all var(--transition-fast);
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(150, 182, 197, 0.1);
}

.clear-search-btn {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: var(--color-text-muted);
  cursor: pointer;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.clear-search-btn:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.clear-search-btn svg {
  width: 16px;
  height: 16px;
}

.filter-controls {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(150, 182, 197, 0.1);
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.sort-btn:hover {
  background: var(--color-background-soft);
  border-color: var(--color-primary);
}

.sort-btn .icon {
  width: 16px;
  height: 16px;
}

/* Main Content */
.inbox-content {
  padding: 2rem 0;
}

/* Loading, Error, and Empty States */
.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  margin-bottom: 1.5rem;
  color: var(--color-primary);
  animation: spin 1s linear infinite;
}

.error-icon,
.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1.5rem;
  color: var(--color-text-muted);
  opacity: 0.6;
}

.error-state h3,
.empty-state h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.75rem 0;
}

.error-state p,
.empty-state p,
.loading-state p {
  color: var(--color-text-muted);
  font-size: 1rem;
  margin: 0 0 1.5rem 0;
  max-width: 400px;
  line-height: 1.5;
}

.empty-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

/* Conversations Container */
.conversations-container {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.pagination-btn:hover:not(:disabled) {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
  transform: translateY(-1px);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.pagination-btn .icon {
  width: 16px;
  height: 16px;
}

.pagination-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.page-numbers {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 0.875rem;
}

.total-count {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid transparent;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-primary {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-secondary);
  border-color: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-outline {
  background: transparent;
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-outline:hover:not(:disabled) {
  background: var(--color-background-soft);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn .icon {
  width: 16px;
  height: 16px;
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

/* Responsive Design */
@media (max-width: 1024px) {
  .container {
    padding: 0 1.5rem;
  }
  
  .search-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar {
    min-width: auto;
  }
  
  .filter-controls {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .search-bar {
    min-width: auto;
  }
  
  .filter-controls {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .pagination {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .pagination-info {
    order: -1;
  }
  
  .empty-actions {
    flex-direction: column;
    align-items: center;
  }
}

@media (max-width: 480px) {
  .inbox-header {
    padding: 1.5rem 0;
  }
  
  .inbox-content {
    padding: 1.5rem 0;
  }
  
  .header-title h1 {
    font-size: 1.5rem;
  }
  
  .search-input {
    padding: 0.625rem 1rem 0.625rem 2.5rem;
  }
  
  .search-icon {
    left: 0.75rem;
    width: 18px;
    height: 18px;
  }
  
  .btn {
    padding: 0.625rem 0.875rem;
    font-size: 0.8125rem;
  }
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner {
    animation: none;
  }
  
  .btn,
  .pagination-btn,
  .sort-btn {
    transition: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .connection-status-banner {
    border-width: 2px;
  }
  
  .conversations-container {
    border-width: 2px;
  }
}

/* Enhanced focus states for better accessibility */
.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(6, 147, 227, 0.1);
}

.sort-btn:focus-visible,
.clear-search-btn:focus-visible,
.btn:focus-visible,
.pagination-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(6, 147, 227, 0.1);
}

/* Enhanced hover states with consistent transitions */
.search-input:focus {
  transform: translateY(-1px);
  box-shadow: 0 0 0 3px rgba(6, 147, 227, 0.1), var(--shadow-sm);
}

.sort-btn:hover {
  background: var(--color-background-soft);
  border-color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.clear-search-btn:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
  transform: scale(1.1);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-secondary);
  border-color: var(--color-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-outline:hover:not(:disabled) {
  background: var(--color-background-soft);
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.pagination-btn:hover:not(:disabled) {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* Consistent spacing and typography */
.header-title h1 {
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.header-subtitle {
  font-size: 1rem;
  line-height: 1.4;
  letter-spacing: -0.005em;
}

.search-input {
  font-size: 1rem;
  line-height: 1.4;
  letter-spacing: -0.005em;
}

.filter-select,
.sort-btn {
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.btn {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* Enhanced visual hierarchy */
.inbox-header {
  background: linear-gradient(135deg, var(--color-background-soft), var(--color-background-mute));
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.connection-status-banner {
  background: linear-gradient(135deg, rgba(244, 67, 54, 0.1), rgba(244, 67, 54, 0.05));
  border: 1px solid rgba(244, 67, 54, 0.2);
  box-shadow: var(--shadow-sm);
}

.warning-icon {
  color: var(--color-error);
  filter: drop-shadow(0 1px 2px rgba(244, 67, 54, 0.2));
}

/* Improved search bar styling */
.search-bar {
  position: relative;
}

.search-input {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.search-icon {
  color: var(--color-text-muted);
  opacity: 0.7;
}

.clear-search-btn {
  background: transparent;
  border: none;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

/* Better filter controls styling */
.filter-select {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.sort-btn {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

/* Enhanced button styling */
.btn-primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  box-shadow: var(--shadow-sm);
}

.btn-outline {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

/* Improved state styling */
.loading-state,
.error-state,
.empty-state {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.error-icon,
.empty-icon {
  color: var(--color-text-muted);
  opacity: 0.6;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.error-state h3,
.empty-state h3 {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.error-state p,
.empty-state p {
  font-size: 1rem;
  line-height: 1.5;
  letter-spacing: -0.005em;
}

/* Better conversations container styling */
.conversations-container {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

/* Enhanced pagination styling */
.pagination {
  background: linear-gradient(135deg, var(--color-background-soft), var(--color-background-mute));
  border-top: 1px solid var(--color-border);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.pagination-btn {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.pagination-info {
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--border-radius);
  padding: 0.5rem 1rem;
}

.page-numbers {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.total-count {
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.01em;
}

/* Enhanced unread indicator */
.unread-indicator {
  color: var(--color-primary);
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(6, 147, 227, 0.2);
}

/* Improved loading spinner */
.loading-spinner {
  color: var(--color-primary);
  opacity: 0.8;
  filter: drop-shadow(0 2px 4px rgba(6, 147, 227, 0.2));
}

/* Better responsive design enhancements */
@media (max-width: 768px) {
  .inbox-header {
    padding: 1.5rem 1rem;
  }
  
  .header-title h1 {
    font-size: 1.75rem;
  }
  
  .search-input {
    font-size: 0.9375rem;
  }
  
  .btn {
    font-size: 0.8125rem;
    padding: 0.625rem 0.875rem;
  }
}

@media (max-width: 480px) {
  .header-title h1 {
    font-size: 1.5rem;
  }
  
  .search-input {
    padding: 0.625rem 1rem 0.625rem 2.5rem;
    font-size: 0.875rem;
  }
  
  .btn {
    font-size: 0.75rem;
    padding: 0.5rem 0.75rem;
  }
}
</style>