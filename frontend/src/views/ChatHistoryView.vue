<template>
  <div class="chat-history-container">
    <div class="history-header">
      <div class="header-content">
        <div class="history-info">
          <div class="history-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="history-details">
            <h1 class="history-title">Chat History</h1>
            <p class="history-description">View and manage your AI chat sessions</p>
          </div>
        </div>
        
        <div class="history-actions">
          <button 
            @click="refreshHistory" 
            class="btn-action"
            title="Refresh history"
            :disabled="isLoading"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="23,4 23,10 17,10" stroke="currentColor" stroke-width="2"/>
              <polyline points="1,20 1,14 7,14" stroke="currentColor" stroke-width="2"/>
              <path d="M20.49,9A9,9 0 0,0 5.64,5.64L1,10m22,4l-4.64,4.36A9,9 0 0,1 3.51,15" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
          
          <button 
            @click="clearAllHistory" 
            class="btn-action danger"
            title="Clear all history"
            :disabled="isLoading || chatSessions.length === 0"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M3 6H5H21"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <div class="history-content">
      <!-- Search and Filter Section -->
      <div class="history-controls">
        <div class="search-container">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
          </svg>
          <input
            v-model="searchQuery"
            placeholder="Search chat history..."
            class="search-input"
          />
        </div>
        
        <div class="filter-container">
          <select v-model="dateFilter" class="filter-select">
            <option value="all">All time</option>
            <option value="today">Today</option>
            <option value="week">This week</option>
            <option value="month">This month</option>
          </select>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state">
        <div class="loading-content">
          <div class="loading-spinner-large"></div>
          <p>Loading chat history...</p>
        </div>
      </div>

      <!-- No History State -->
      <div v-else-if="filteredSessions.length === 0 && !isLoading" class="no-history-state">
        <div class="no-history-content">
          <svg class="no-history-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M21 11.5C21.0034 12.8199 20.6951 14.1219 20.1 15.3C19.3944 16.7118 18.3098 17.8992 16.9674 18.7293C15.6251 19.5594 14.0782 19.9994 12.5 20C11.1801 20.0035 9.87812 19.6951 8.7 19.1L3 21L4.9 15.3C4.30493 14.1219 3.99656 12.8199 4 11.5C4.00061 9.92179 4.44061 8.37488 5.27072 7.03258C6.10083 5.69028 7.28825 4.60557 8.7 3.9C9.87812 3.30493 11.1801 2.99656 12.5 3H13C15.0843 3.11499 17.053 3.99476 18.5291 5.47086C20.0052 6.94696 20.885 8.91565 21 11V11.5Z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
          <h3>No chat history found</h3>
          <p v-if="searchQuery">Try adjusting your search or filter criteria</p>
          <p v-else>Start a conversation with the AI assistant to see your chat history here</p>
          <router-link to="/ai-chat" class="btn">Start New Chat</router-link>
        </div>
      </div>

      <!-- Chat Sessions List -->
      <div v-else class="sessions-container">
        <div class="sessions-header">
          <h2>Chat Sessions</h2>
          <span class="sessions-count">{{ filteredSessions.length }} session{{ filteredSessions.length !== 1 ? 's' : '' }}</span>
        </div>
        
        <div class="sessions-list">
          <div 
            v-for="session in filteredSessions" 
            :key="session.id" 
            class="session-item"
          >
            <div class="session-header">
              <div class="session-info">
                <h3 class="session-title">{{ getSessionTitle(session) }}</h3>
                <p class="session-summary">{{ getSessionSummary(session) }}</p>
              </div>
              
              <div class="session-meta">
                <span class="session-date">{{ formatDate(session.lastActivity) }}</span>
                <span class="session-stats">{{ session.messageCount }} messages</span>
              </div>
            </div>
            
            <div class="session-preview">
              <div v-if="session.messages && session.messages.length > 0" class="messages-preview">
                <div 
                  v-for="(message, index) in session.messages.slice(-2)" 
                  :key="index"
                  class="preview-message"
                  :class="`preview-message--${message.type}`"
                >
                  <div class="preview-avatar">
                    <span v-if="message.type === 'user'">{{ userInitial }}</span>
                    <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
                  </div>
                  <div class="preview-content">
                    <span class="preview-sender">{{ message.type === 'user' ? 'You' : 'AI' }}</span>
                    <p class="preview-text">{{ truncateText(message.content, 100) }}</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="session-actions">
              <button 
                @click="viewSession(session)"
                class="action-button primary"
                title="View session"
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                </svg>
                View
              </button>
              
              <button 
                @click="continueSession(session)"
                class="action-button"
                title="Continue session"
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M21 11.5C21.0034 12.8199 20.6951 14.1219 20.1 15.3C19.3944 16.7118 18.3098 17.8992 16.9674 18.7293C15.6251 19.5594 14.0782 19.9994 12.5 20C11.1801 20.0035 9.87812 19.6951 8.7 19.1L3 21L4.9 15.3C4.30493 14.1219 3.99656 12.8199 4 11.5C4.00061 9.92179 4.44061 8.37488 5.27072 7.03258C6.10083 5.69028 7.28825 4.60557 8.7 3.9C9.87812 3.30493 11.1801 2.99656 12.5 3H13C15.0843 3.11499 17.053 3.99476 18.5291 5.47086C20.0052 6.94696 20.885 8.91565 21 11V11.5Z"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                Continue
              </button>
              
              <button 
                @click="deleteSession(session)"
                class="action-button danger"
                title="Delete session"
              >
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M3 6H5H21"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Session Detail Modal -->
    <div v-if="selectedSession" class="modal-overlay" @click="closeSessionDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ getSessionTitle(selectedSession) }}</h2>
          <button @click="closeSessionDetail" class="modal-close">×</button>
        </div>
        
        <div class="modal-body">
          <div class="session-metadata">
            <div class="metadata-row">
              <strong>Session ID:</strong> {{ selectedSession.id }}
            </div>
            <div class="metadata-row">
              <strong>Created:</strong> {{ formatDateTime(selectedSession.createdAt) }}
            </div>
            <div class="metadata-row">
              <strong>Last Activity:</strong> {{ formatDateTime(selectedSession.lastActivity) }}
            </div>
            <div class="metadata-row">
              <strong>Messages:</strong> {{ selectedSession.messageCount }}
            </div>
          </div>
          
          <div class="session-messages">
            <h3>Messages:</h3>
            <div class="messages-list">
              <div 
                v-for="(message, index) in selectedSession.messages" 
                :key="index"
                class="message-item"
                :class="`message-item--${message.type}`"
              >
                <div class="message-avatar">
                  <span v-if="message.type === 'user'">{{ userInitial }}</span>
                  <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
                </div>
                <div class="message-content">
                  <div class="message-header">
                    <span class="message-sender">{{ message.type === 'user' ? 'You' : 'AI Assistant' }}</span>
                    <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                  </div>
                  <p class="message-text">{{ message.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="continueSession(selectedSession)" class="btn btn-secondary">
            Continue Chat
          </button>
          <button @click="closeSessionDetail" class="btn">Close</button>
        </div>
      </div>
    </div>

    <!-- Error message -->
    <div v-if="errorMessage" class="error-message">
      <div class="error-content">
        <svg class="error-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
          <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
        </svg>
        <span>{{ errorMessage }}</span>
        <button @click="errorMessage = ''" class="error-close">×</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { aiChatService } from '../services/aiChatService'

const router = useRouter()
const authStore = useAuthStore()

// Reactive data
const chatSessions = ref([])
const selectedSession = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')
const searchQuery = ref('')
const dateFilter = ref('all')

// Computed properties
const userInitial = computed(() => {
  if (authStore.userInfo?.firstName) {
    return authStore.userInfo.firstName.charAt(0).toUpperCase()
  }
  if (authStore.userInfo?.email) {
    return authStore.userInfo.email.charAt(0).toUpperCase()
  }
  return 'U'
})

const filteredSessions = computed(() => {
  let filtered = [...chatSessions.value]
  
  // Apply search filter
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(session => 
      getSessionTitle(session).toLowerCase().includes(query) ||
      getSessionSummary(session).toLowerCase().includes(query) ||
      session.messages?.some(msg => msg.content.toLowerCase().includes(query))
    )
  }
  
  // Apply date filter
  if (dateFilter.value !== 'all') {
    const now = new Date()
    const filterDate = new Date()
    
    switch (dateFilter.value) {
      case 'today':
        filterDate.setHours(0, 0, 0, 0)
        break
      case 'week':
        filterDate.setDate(now.getDate() - 7)
        break
      case 'month':
        filterDate.setMonth(now.getMonth() - 1)
        break
    }
    
    filtered = filtered.filter(session => 
      new Date(session.lastActivity) >= filterDate
    )
  }
  
  // Sort by last activity (most recent first)
  return filtered.sort((a, b) => new Date(b.lastActivity) - new Date(a.lastActivity))
})

// Methods
const refreshHistory = async () => {
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    // Since the backend doesn't have session management yet,
    // we'll create mock data based on localStorage or generate sample data
    await loadChatHistory()
  } catch (error) {
    console.error('Error loading chat history:', error)
    errorMessage.value = 'Failed to load chat history. Please try again.'
  } finally {
    isLoading.value = false
  }
}

const loadChatHistory = async () => {
  try {
    // Try to load from backend first if user is authenticated
    if (authStore.userInfo?.id) {
      try {
        const response = await aiChatService.getUserSessions(authStore.userInfo.id, 50)
        if (response.sessions && response.sessions.length > 0) {
          chatSessions.value = response.sessions.map(session => ({
            id: session.id,
            messages: [], // Messages will be loaded when session is selected
            createdAt: new Date(session.created_at),
            lastActivity: new Date(session.updated_at),
            messageCount: session.message_count || 0,
            title: session.title || 'New Chat Session',
            isFromBackend: true,
            // Try to get preview from session data if available
            preview: session.preview || null
          }))
          
          // Load preview messages for better display
          await loadSessionPreviews()
          return
        }
      } catch (error) {
        console.warn('Failed to load sessions from backend, falling back to localStorage:', error)
      }
    }
    
    // Fallback to localStorage
    const storedSessions = localStorage.getItem('ai_chat_sessions')
    if (storedSessions) {
      chatSessions.value = JSON.parse(storedSessions)
    } else {
      chatSessions.value = []
    }
  } catch (error) {
    console.error('Error loading chat history:', error)
    errorMessage.value = 'Failed to load chat history. Please try again.'
  }
}

const loadSessionPreviews = async () => {
  // Load preview messages for backend sessions to show better titles and summaries
  const backendSessions = chatSessions.value.filter(session => session.isFromBackend && session.messageCount > 0)
  
  for (const session of backendSessions.slice(0, 5)) { // Only load for first 5 sessions to avoid too many requests
    try {
      const historyResponse = await aiChatService.getChatHistory(session.id)
      const backendMessages = historyResponse.messages || []
      
      if (backendMessages.length > 0) {
        // Load a few messages for preview
        session.messages = backendMessages.slice(0, 3).map(msg => ({
          type: msg.role === 'user' ? 'user' : 'ai',
          content: msg.content,
          timestamp: new Date(msg.created_at)
        }))
      }
    } catch (error) {
      console.warn(`Failed to load preview for session ${session.id}:`, error)
    }
  }
}

const clearAllHistory = async () => {
  if (confirm('Are you sure you want to clear all chat history? This action cannot be undone.')) {
    try {
      // Try to clear from backend if user is authenticated
      if (authStore.userInfo?.id) {
        // Note: This would need a backend API to clear all user sessions
        // For now, we'll just clear the local storage
      }
      
      chatSessions.value = []
      localStorage.removeItem('ai_chat_sessions')
    } catch (error) {
      console.error('Error clearing history:', error)
      errorMessage.value = 'Failed to clear history. Please try again.'
    }
  }
}

const viewSession = async (session) => {
  // If session is from backend and doesn't have messages loaded, load them
  if (session.isFromBackend && (!session.messages || session.messages.length === 0)) {
    try {
      const historyResponse = await aiChatService.getChatHistory(session.id)
      const backendMessages = historyResponse.messages || []
      session.messages = backendMessages.map(msg => ({
        type: msg.role === 'user' ? 'user' : 'ai',
        content: msg.content,
        timestamp: new Date(msg.created_at)
      }))
    } catch (error) {
      console.error('Error loading session messages:', error)
      errorMessage.value = 'Failed to load session details.'
      return
    }
  }
  
  selectedSession.value = session
}

const closeSessionDetail = () => {
  selectedSession.value = null
}

const continueSession = (session) => {
  // Navigate directly to the AI chat with session ID
  router.push(`/ai-chat/${session.id}`)
}

const deleteSession = async (session) => {
  if (confirm('Are you sure you want to delete this chat session?')) {
    try {
      // Try to delete from backend if it's a backend session
      if (session.isFromBackend || (authStore.userInfo?.id && session.id.includes('-'))) {
        await aiChatService.deleteSession(session.id)
      }
      
      // Remove from local state and localStorage
      chatSessions.value = chatSessions.value.filter(s => s.id !== session.id)
      localStorage.setItem('ai_chat_sessions', JSON.stringify(chatSessions.value.filter(s => !s.isFromBackend)))
      
      // If this was the selected session, close the modal
      if (selectedSession.value?.id === session.id) {
        selectedSession.value = null
      }
    } catch (error) {
      console.error('Error deleting session:', error)
      errorMessage.value = 'Failed to delete session. Please try again.'
    }
  }
}

const getSessionTitle = (session) => {
  // If session has a custom title, use it
  if (session.title && session.title !== 'New Chat Session') {
    return session.title
  }
  
  // Show first user message as title (like ChatGPT)
  if (session.messages && session.messages.length > 0) {
    const firstUserMessage = session.messages.find(msg => msg.type === 'user')
    if (firstUserMessage) {
      return truncateText(firstUserMessage.content, 60)
    }
  }
  
  // For backend sessions without loaded messages, try to create a meaningful title
  if (session.isFromBackend) {
    return `Chat Session`
  }
  
  // For UUID format, show a shorter identifier
  if (session.id.includes('-')) {
    return `Chat ${session.id.split('-')[0]}`
  }
  
  // For old format sessions
  return `Chat Session ${session.id.split('_').pop()}`
}

const getSessionSummary = (session) => {
  if (session.messages && session.messages.length > 0) {
    // Show the AI's response or the conversation flow
    const lastAIMessage = session.messages.slice().reverse().find(msg => msg.type === 'ai')
    if (lastAIMessage) {
      return truncateText(lastAIMessage.content, 100)
    }
    // Fallback to last message
    const lastMessage = session.messages[session.messages.length - 1]
    return truncateText(lastMessage.content, 100)
  }
  
  // For backend sessions without loaded messages
  if (session.isFromBackend) {
    return `${session.messageCount || 0} messages`
  }
  
  return 'Empty conversation'
}

const truncateText = (text, maxLength) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatDate = (date) => {
  const d = new Date(date)
  const now = new Date()
  const diffTime = Math.abs(now - d)
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
  const diffMinutes = Math.floor(diffTime / (1000 * 60))
  
  // Same day
  if (diffDays === 0) {
    if (diffHours === 0) {
      if (diffMinutes === 0) return 'Just now'
      return `${diffMinutes}m ago`
    }
    return `${diffHours}h ago`
  }
  
  // Yesterday
  if (diffDays === 1) return 'Yesterday'
  
  // This week
  if (diffDays <= 7) return `${diffDays}d ago`
  
  // This year
  const isThisYear = d.getFullYear() === now.getFullYear()
  if (isThisYear) {
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  }
  
  // Different year
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const formatDateTime = (date) => {
  return new Date(date).toLocaleString()
}

const formatTime = (date) => {
  return new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(date))
}

// Initialize component
onMounted(() => {
  refreshHistory()
})
</script>

<style scoped>
.chat-history-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--color-background);
}

.history-header {
  background: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  padding: 1.5rem;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.history-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.history-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-icon svg {
  width: 24px;
  height: 24px;
}

.history-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

.history-description {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.history-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-action {
  width: 40px;
  height: 40px;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-muted);
}

.btn-action:hover:not(:disabled) {
  background: var(--color-background-soft);
  color: var(--color-primary);
}

.btn-action.danger:hover:not(:disabled) {
  background: var(--color-danger);
  color: var(--color-background);
  border-color: var(--color-danger);
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action svg {
  width: 18px;
  height: 18px;
}

.history-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  width: 100%;
}

.history-controls {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  align-items: center;
}

.search-container {
  position: relative;
  flex: 1;
  max-width: 400px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--color-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: 1.5rem;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  outline: none;
  transition: all var(--transition-fast);
}

.search-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.filter-container {
  flex-shrink: 0;
}

.filter-select {
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: 0.75rem;
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  outline: none;
  transition: border-color var(--transition-fast);
}

.filter-select:focus {
  border-color: var(--color-primary);
}

.loading-state,
.no-history-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-content,
.no-history-content {
  text-align: center;
  max-width: 500px;
}

.loading-spinner-large {
  width: 60px;
  height: 60px;
  border: 4px solid var(--color-border);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.no-history-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  color: var(--color-text-muted);
}

.loading-content h3,
.no-history-content h3 {
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
  color: var(--color-heading);
}

.loading-content p,
.no-history-content p {
  margin: 0 0 1rem 0;
  color: var(--color-text);
  line-height: 1.6;
}

.sessions-container {
  animation: fadeIn 0.3s ease-out;
}

.sessions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.sessions-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--color-heading);
}

.sessions-count {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.session-item {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  transition: all var(--transition-fast);
}

.session-item:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.session-info {
  flex: 1;
}

.session-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  line-height: 1.4;
}

.session-summary {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  line-height: 1.4;
}

.session-meta {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  align-items: flex-end;
  flex-shrink: 0;
}

.session-date {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.session-stats {
  font-size: 0.75rem;
  color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.1);
  padding: 0.125rem 0.375rem;
  border-radius: 1rem;
}

.session-preview {
  margin-bottom: 1rem;
}

.messages-preview {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.preview-message {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.preview-message--user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.preview-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.preview-message--user .preview-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
}

.preview-message--ai .preview-avatar {
  background: var(--color-background);
  border: 2px solid var(--color-border);
  color: var(--color-primary);
}

.preview-message--ai .preview-avatar svg {
  width: 14px;
  height: 14px;
}

.preview-content {
  flex: 1;
  min-width: 0;
}

.preview-sender {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  display: block;
  margin-bottom: 0.25rem;
}

.preview-text {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text);
  line-height: 1.4;
}

.session-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
}

.action-button:hover {
  background: var(--color-background-soft);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.action-button.primary {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
}

.action-button.primary:hover {
  background: var(--color-primary-dark);
}

.action-button.danger:hover {
  background: var(--color-danger);
  color: var(--color-background);
  border-color: var(--color-danger);
}

.action-button svg {
  width: 16px;
  height: 16px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.error-message {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  max-width: 90%;
  width: 400px;
}

.error-content {
  background: var(--color-danger);
  color: var(--color-background);
  padding: 1rem;
  border-radius: var(--border-radius-lg);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  box-shadow: var(--shadow-lg);
}

.error-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.error-close {
  background: none;
  border: none;
  color: var(--color-background);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0;
  margin-left: auto;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background var(--transition-fast);
}

.error-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  max-width: 800px;
  max-height: 90vh;
  width: 100%;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--color-heading);
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-text-muted);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-body {
  padding: 1.5rem;
  max-height: 60vh;
  overflow-y: auto;
}

.session-metadata {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.metadata-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.metadata-row:last-child {
  margin-bottom: 0;
}

.metadata-row strong {
  color: var(--color-text-muted);
  min-width: 100px;
}

.session-messages h3 {
  margin: 0 0 1rem 0;
  font-size: 1.125rem;
  color: var(--color-heading);
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-item {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.message-item--user {
  margin-left: auto;
  flex-direction: row-reverse;
  max-width: 85%;
}

.message-item--ai {
  max-width: 85%;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
  flex-shrink: 0;
}

.message-item--user .message-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
}

.message-item--ai .message-avatar {
  background: var(--color-background-soft);
  border: 2px solid var(--color-border);
  color: var(--color-primary);
}

.message-item--ai .message-avatar svg {
  width: 16px;
  height: 16px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.75rem;
}

.message-sender {
  font-weight: 600;
  color: var(--color-text-muted);
}

.message-time {
  opacity: 0.7;
  color: var(--color-text-muted);
}

.message-text {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
  font-size: 0.875rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
}

/* Responsive design */
@media (max-width: 768px) {
  .history-content {
    padding: 1rem;
  }
  
  .history-controls {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .search-container {
    max-width: none;
  }
  
  .session-header {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }
  
  .session-meta {
    flex-direction: row;
    gap: 1rem;
    align-items: center;
  }
  
  .session-actions {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
  
  .modal-content {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }
}

@media (max-width: 480px) {
  .history-header {
    padding: 1rem;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .history-info {
    gap: 0.75rem;
  }
  
  .history-icon {
    width: 40px;
    height: 40px;
  }
  
  .history-title {
    font-size: 1.25rem;
  }
  
  .session-item {
    padding: 1rem;
  }
  
  .action-button {
    padding: 0.375rem 0.75rem;
    font-size: 0.8rem;
  }
  
  .action-button svg {
    width: 14px;
    height: 14px;
  }
}
</style>
