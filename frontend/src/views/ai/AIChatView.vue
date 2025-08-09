<template>
  <div class="ai-chat-layout">
    <!-- Chat History Sidebar -->
    <div class="chat-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="sidebar-header">
        <h3>Chat History</h3>
        <div class="sidebar-actions">
          <button 
            @click="refreshHistory" 
            class="sidebar-btn"
            title="Refresh history"
            :disabled="isLoadingHistory"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="23,4 23,10 17,10" stroke="currentColor" stroke-width="2"/>
              <polyline points="1,20 1,14 7,14" stroke="currentColor" stroke-width="2"/>
              <path d="M20.49,9A9,9 0 0,0 5.64,5.64L1,10m22,4l-4.64,4.36A9,9 0 0,1 3.51,15" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
          
          <button 
            @click="startNewChat" 
            class="sidebar-btn primary"
            title="New chat"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2"/>
              <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
          
          <button 
            @click="toggleSidebar" 
            class="sidebar-btn mobile-only"
            title="Close sidebar"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
      </div>
      
      <!-- Search History -->
      <div class="sidebar-search">
        <div class="search-container">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
          </svg>
          <input
            v-model="historySearchQuery"
            placeholder="Search history..."
            class="search-input"
          />
        </div>
      </div>
      
      <!-- History List -->
      <div class="sidebar-content">
        <div v-if="isLoadingHistory" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Loading history...</p>
        </div>
        
        <div v-else-if="filteredChatSessions.length === 0" class="no-history-state">
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
          <p>No chat history yet</p>
          <small>Start a conversation to see your history here</small>
        </div>
        
        <div v-else class="history-list">
          <div 
            v-for="session in filteredChatSessions" 
            :key="session.id" 
            class="history-item"
            :class="{ 'active': currentSessionId === session.id }"
            @click="loadSession(session)"
          >
            <div class="history-item-content">
              <h4 class="history-title">{{ getSessionTitle(session) }}</h4>
              <!-- <p class="history-preview">{{ getSessionPreview(session) }}</p> -->
              <span class="history-date">{{ formatHistoryDate(session.lastActivity) }}</span>
            </div>
            
            <div class="history-actions">
              <button 
                @click.stop="deleteSession(session)"
                class="history-action-btn"
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
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Chat Area -->
    <div class="chat-main">
      <!-- Mobile Header -->
      <div class="mobile-header">
        <button @click="toggleSidebar" class="mobile-menu-btn">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="2"/>
            <line x1="3" y1="12" x2="21" y2="12" stroke="currentColor" stroke-width="2"/>
            <line x1="3" y1="18" x2="21" y2="18" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
        <h1>Legal Connect AI Assistant</h1>
        <div class="mobile-actions">
          <button 
            @click="navigateToDocumentSearch" 
            class="mobile-clear-btn"
            title="Search Documents"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <polyline
                points="14,2 14,8 20,8"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="11.5" cy="12.5" r="2.5" stroke="currentColor" stroke-width="2"/>
              <path d="m13.5 14.5 1.5 1.5" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
          
          <button 
            @click="navigateToChatHistory" 
            class="mobile-clear-btn"
            title="Chat History"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z"
                stroke="currentColor"
                stroke-width="2"
              />
              <path
                d="M12 7V12L15 15"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
          
          <button 
            @click="clearConversation" 
            class="mobile-clear-btn"
            :disabled="messages.length === 0"
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

      <!-- Desktop Header -->
      <div class="chat-header desktop-only">
        <div class="header-content">
          <div class="ai-info">
            <div class="ai-avatar">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
            <div class="ai-details">
              <h1 class="ai-name">Legal Connect AI Assistant</h1>
              <p class="ai-description">Ask me anything about Bangladesh legal documents and laws</p>
            </div>
          </div>
          
          <div v-if="messages.length > 0" class="chat-actions">
            <button 
              @click="navigateToDocumentSearch" 
              class="btn-action"
              title="Search Documents"
            >
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <polyline
                  points="14,2 14,8 20,8"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle cx="11.5" cy="12.5" r="2.5" stroke="currentColor" stroke-width="2"/>
                <path d="m13.5 14.5 1.5 1.5" stroke="currentColor" stroke-width="2"/>
              </svg>
            </button>
            
            <button 
              @click="navigateToChatHistory" 
              class="btn-action"
              title="Chat History"
            >
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <path
                  d="M12 7V12L15 15"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
            
            <!-- <button 
              @click="clearConversation" 
              class="btn-action"
              title="Clear conversation"
              :disabled="messages.length === 0"
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
            </button> -->
          </div>
        </div>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <!-- Welcome message -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-content">
            <h3>Welcome to Legal Connect AI Assistant</h3>
            <p>Get instant legal guidance by choosing a topic below or ask your own question:</p>
            
            <!-- Quick Legal Topic Buttons -->
            <div class="quick-prompts">
              <button 
                @click="startPromptConversation('What are the key provisions of civil law in Bangladesh?')"
                class="prompt-btn"
              >
                What are the key provisions of civil law in Bangladesh?
              </button>

              <button 
                @click="startPromptConversation('Explain the criminal law system in Bangladesh')"
                class="prompt-btn"
              >
                Explain the criminal law system in Bangladesh
              </button>

              <button 
                @click="startPromptConversation('What are the family law provisions in Bangladesh?')"
                class="prompt-btn"
              >
                What are the family law provisions in Bangladesh?
              </button>

              <button 
                @click="startPromptConversation('Explain labor law and workers rights in Bangladesh')"
                class="prompt-btn"
              >
                Explain labor law and workers rights in Bangladesh
              </button>

              <button 
                @click="startPromptConversation('What are the property law regulations in Bangladesh?')"
                class="prompt-btn"
              >
                What are the property law regulations in Bangladesh?
              </button>

              <button 
                @click="startPromptConversation('Explain business law and company formation in Bangladesh')"
                class="prompt-btn"
              >
                Explain business law and company formation in Bangladesh
              </button>
            </div>

            <p class="welcome-note">
              {{ chatSessions.length > 0 
                ? 'Or continue with a previous conversation from the history above.' 
                : 'You can also type your own legal question below.' 
              }}
            </p>
          </div>
        </div>

        <!-- Chat messages -->
        <div v-for="(message, index) in messages" :key="index" class="message-wrapper">
          <div class="message" :class="`message--${message.type}`">
            <div class="message-avatar">
              <div v-if="message.type === 'user'" class="user-avatar">
                {{ userInitial }}
              </div>
              <div v-else class="ai-avatar">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
            </div>
            
            <div class="message-content">
              <div class="message-header">
                <span class="message-sender">{{ message.type === 'user' ? 'You' : 'Legal Connect AI Assistant' }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
              
              <div class="message-text" v-html="formatMessage(message.content)"></div>
            </div>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="isTyping" class="message-wrapper">
          <div class="message message--ai">
            <div class="message-avatar">
              <div class="ai-avatar">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-container">
        <div class="input-wrapper">
          <textarea
            ref="messageInput"
            v-model="currentMessage"
            @keydown="handleKeydown"
            @input="adjustTextareaHeight"
            placeholder="Ask me anything about Bangladesh legal matters..."
            class="message-input"
            rows="1"
            :disabled="isLoading"
          ></textarea>
          
          <button 
            @click="sendMessage" 
            class="send-button"
            :disabled="!currentMessage.trim() || isLoading"
          >
            <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line
                x1="22"
                y1="2"
                x2="11"
                y2="13"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <polygon
                points="22,2 15,22 11,13 2,9 22,2"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <svg v-else class="loading-spinner" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-dasharray="31.416"
                stroke-dashoffset="31.416"
              />
            </svg>
          </button>
        </div>
        
        <div class="input-footer">
          <p class="disclaimer">
            AI responses are for informational purposes only and should not be considered as legal advice.
          </p>
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

    <!-- Right Actions Sidebar -->
    <div class="right-actions-sidebar">
      <div class="actions-header">
        <h4>Quick Actions</h4>
      </div>
      
      <div class="action-buttons">
        <button 
          @click="navigateToDocumentSearch" 
          class="action-btn document-search-btn"
          title="Search Legal Documents"
        >
          <div class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <polyline
                points="14,2 14,8 20,8"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="11.5" cy="12.5" r="2.5" stroke="currentColor" stroke-width="2"/>
              <path d="m13.5 14.5 1.5 1.5" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="action-text">
            <span class="action-title">Search Documents</span>
            <span class="action-subtitle">Browse legal documents and laws</span>
          </div>
        </button>
        
        <button 
          @click="navigateToChatHistory" 
          class="action-btn chat-history-btn"
          title="View Chat History"
        >
          <div class="action-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z"
                stroke="currentColor"
                stroke-width="2"
              />
              <path
                d="M12 7V12L15 15"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
          <div class="action-text">
            <span class="action-title">Chat History</span>
            <span class="action-subtitle">View past conversations</span>
          </div>
        </button>
      </div>
    </div>

    <!-- Sidebar Overlay for Mobile -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="toggleSidebar"></div>
  </div>
</template>

<script setup>
import { aiChatService } from '@/services/aiChatService'
import { useAuthStore } from '@/stores/auth'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// Reactive data
const messages = ref([])
const currentMessage = ref('')
const isLoading = ref(false)
const isTyping = ref(false)
const errorMessage = ref('')
const sessionId = ref('')
const currentSessionId = ref('')
const messagesContainer = ref(null)
const messageInput = ref(null)

// History-related data
const chatSessions = ref([])
const isLoadingHistory = ref(false)
const historySearchQuery = ref('')
const sidebarOpen = ref(false)

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

const filteredChatSessions = computed(() => {
  if (!historySearchQuery.value.trim()) {
    return chatSessions.value
  }
  
  const query = historySearchQuery.value.toLowerCase()
  return chatSessions.value.filter(session => 
    getSessionTitle(session).toLowerCase().includes(query) ||
    getSessionPreview(session).toLowerCase().includes(query) ||
    session.messages?.some(msg => msg.content.toLowerCase().includes(query))
  )
})

// Watch for route changes to handle session navigation
watch(() => route.params.sessionId, async (newSessionId, oldSessionId) => {
  if (newSessionId && newSessionId !== oldSessionId) {
    if (newSessionId !== currentSessionId.value) {
      await loadSessionFromBackend(newSessionId)
    }
  } else if (!newSessionId && oldSessionId) {
    // Navigated to base AI chat route, show empty state without creating session
    currentSessionId.value = ''
    sessionId.value = ''
    messages.value = []
  }
}, { immediate: true })

// Initialize session
onMounted(async () => {
  checkServiceHealth()
  await loadChatHistory()
  
  // Handle session from route
  const routeSessionId = route.params.sessionId
  if (routeSessionId) {
    await loadSessionFromBackend(routeSessionId)
  } else {
    // Check for continuation from history page
    const continueSessionId = localStorage.getItem('continue_session_id')
    if (continueSessionId) {
      localStorage.removeItem('continue_session_id')
      // Navigate to the session URL
      await router.push(`/ai-chat/${continueSessionId}`)
    } else {
      // Show empty state without creating a session
      // Load the most recent session if available and user wants to continue
      // But don't auto-navigate - let user choose from sidebar or start new chat
      currentSessionId.value = ''
      sessionId.value = ''
      messages.value = []
    }
  }
})

// Methods
const navigateToDocumentSearch = () => {
  router.push('/document-search')
}

const navigateToChatHistory = () => {
  router.push('/chat-history')
}

// Start conversation with predefined prompt
const startPromptConversation = async (prompt) => {
  try {
    // Create new session first if needed
    if (!sessionId.value) {
      await createNewSession()
    }
    
    // Set the prompt as current message and send it
    currentMessage.value = prompt
    await sendMessage()
    
    // Focus on the input for follow-up questions
    await nextTick()
    messageInput.value?.focus()
  } catch (error) {
    console.error('Error starting prompt conversation:', error)
    errorMessage.value = 'Failed to start conversation. Please try again.'
  }
}

const checkServiceHealth = async () => {
  try {
    await aiChatService.checkHealth()
  } catch (error) {
    errorMessage.value = 'AI Chat service is currently unavailable. Please try again later.'
  }
}

const loadChatHistory = async () => {
  isLoadingHistory.value = true
  
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
            isFromBackend: true
          }))
          
          // Load preview content for better display
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
  } finally {
    isLoadingHistory.value = false
  }
}

// Load actual message content for better previews
const loadSessionPreviews = async () => {
  for (const session of chatSessions.value) {
    if (session.isFromBackend && session.messages.length === 0) {
      try {
        const historyResponse = await aiChatService.getChatHistory(session.id)
        if (historyResponse.messages && historyResponse.messages.length > 0) {
          session.messages = historyResponse.messages.map(msg => ({
            type: msg.type || msg.role,
            content: msg.content,
            timestamp: new Date(msg.timestamp || msg.created_at)
          }))
        }
      } catch (error) {
        console.warn(`Failed to load preview for session ${session.id}:`, error)
      }
    }
  }
}

const createNewSession = async () => {
  try {
    // Clear current messages
    messages.value = []
    
    // Create session on backend if user is authenticated
    if (authStore.userInfo?.id) {
      const response = await aiChatService.createSession(authStore.userInfo.id, 'New Chat Session')
      sessionId.value = response.session_id
      currentSessionId.value = response.session_id
      
      // Navigate to the new session URL
      await router.push(`/ai-chat/${response.session_id}`)
    } else {
      // Create local session for anonymous users
      sessionId.value = aiChatService.generateSessionId()
      currentSessionId.value = sessionId.value
      await router.push(`/ai-chat/${sessionId.value}`)
    }
  } catch (error) {
    console.error('Error creating new session:', error)
    // Fallback to local session
    sessionId.value = aiChatService.generateSessionId()
    currentSessionId.value = sessionId.value
    messages.value = []
    await router.push(`/ai-chat/${sessionId.value}`)
  }
}

const loadSessionFromBackend = async (sessionIdToLoad) => {
  try {
    // Clear current messages first
    messages.value = []
    
    // Try to get session info from backend
    const sessionInfo = await aiChatService.getSessionInfo(sessionIdToLoad)
    
    // Get chat history for this session
    const historyResponse = await aiChatService.getChatHistory(sessionIdToLoad)
    
    // Convert backend messages to frontend format
    const backendMessages = historyResponse.messages || []
    const convertedMessages = backendMessages.map(msg => ({
      type: msg.role === 'user' ? 'user' : 'ai',
      content: msg.content,
      sources: msg.metadata?.sources || [],
      metadata: msg.metadata || {},
      timestamp: new Date(msg.created_at)
    }))
    
    // Set current session
    sessionId.value = sessionIdToLoad
    currentSessionId.value = sessionIdToLoad
    messages.value = convertedMessages
    
    // Update URL if needed
    if (route.params.sessionId !== sessionIdToLoad) {
      await router.replace(`/ai-chat/${sessionIdToLoad}`)
    }
    
    // Scroll to bottom
    await nextTick()
    scrollToBottom()
    
  } catch (error) {
    console.error('Error loading session from backend:', error)
    
    // Try to load from localStorage as fallback
    const storedSessions = JSON.parse(localStorage.getItem('ai_chat_sessions') || '[]')
    const localSession = storedSessions.find(s => s.id === sessionIdToLoad)
    
    if (localSession) {
      loadSession(localSession)
    } else {
      // Session not found, redirect to new chat
      errorMessage.value = 'Session not found. Starting a new chat.'
      await createNewSession()
    }
  }
}

const refreshHistory = () => {
  loadChatHistory()
}

const saveCurrentSession = () => {
  if (messages.value.length === 0) return
  
  const existingSessionIndex = chatSessions.value.findIndex(s => s.id === currentSessionId.value)
  const sessionData = {
    id: currentSessionId.value,
    messages: [...messages.value],
    createdAt: existingSessionIndex >= 0 ? chatSessions.value[existingSessionIndex].createdAt : new Date(),
    lastActivity: new Date(),
    messageCount: messages.value.length
  }
  
  if (existingSessionIndex >= 0) {
    chatSessions.value[existingSessionIndex] = sessionData
  } else {
    chatSessions.value.unshift(sessionData)
  }
  
  // Keep only last 50 sessions
  chatSessions.value = chatSessions.value.slice(0, 50)
  
  localStorage.setItem('ai_chat_sessions', JSON.stringify(chatSessions.value))
}

const startNewChat = async () => {
  if (messages.value.length > 0) {
    saveCurrentSession()
  }
  
  await createNewSession()
  errorMessage.value = ''
  sidebarOpen.value = false
}

const loadSession = async (session) => {
  if (messages.value.length > 0 && currentSessionId.value !== session.id) {
    saveCurrentSession()
  }
  
  // If session is from backend, load it properly
  if (session.isFromBackend) {
    await loadSessionFromBackend(session.id)
  } else {
    // Local session
    messages.value = [...session.messages]
    sessionId.value = session.id
    currentSessionId.value = session.id
    
    // Navigate to session URL
    if (route.params.sessionId !== session.id) {
      await router.push(`/ai-chat/${session.id}`)
    }
    
    nextTick(() => {
      scrollToBottom()
    })
  }
  
  sidebarOpen.value = false
}

const deleteSession = async (session) => {
  if (confirm('Are you sure you want to delete this chat session?')) {
    try {
      // Try to delete from backend if it's a backend session
      if (session.isFromBackend || (authStore.userInfo?.id && session.id.includes('-'))) {
        await aiChatService.deleteSession(session.id)
      }
      
      // Remove from local storage and state
      chatSessions.value = chatSessions.value.filter(s => s.id !== session.id)
      localStorage.setItem('ai_chat_sessions', JSON.stringify(chatSessions.value.filter(s => !s.isFromBackend)))
      
      // If we're deleting the current session, redirect to main AI chat page
      if (currentSessionId.value === session.id) {
        // Clear current session data
        messages.value = []
        currentSessionId.value = ''
        sessionId.value = ''
        
        // Navigate to main AI chat page without creating new session
        await router.push('/ai-chat')
      }
    } catch (error) {
      console.error('Error deleting session:', error)
      errorMessage.value = 'Failed to delete session. Please try again.'
    }
  }
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
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
      return truncateText(firstUserMessage.content, 45)
    }
  }
  
  // For backend sessions without loaded messages
  if (session.isFromBackend) {
    return `Chat Session`
  }
  
  // For UUID format, show a shorter identifier
  if (session.id.includes('-')) {
    return `Chat ${session.id.split('-')[0]}`
  }
  
  // For old format sessions
  return `Chat ${session.id.split('_').pop()}`
}

const getSessionPreview = (session) => {
  if (session.messages && session.messages.length > 0) {
    // Show the AI's response or the conversation flow
    const lastAIMessage = session.messages.slice().reverse().find(msg => msg.type === 'ai')
    if (lastAIMessage) {
      return truncateText(lastAIMessage.content, 60)
    }
    // Fallback to last message
    const lastMessage = session.messages[session.messages.length - 1]
    return truncateText(lastMessage.content, 60)
  }
  
  // For backend sessions without loaded messages
  if (session.isFromBackend) {
    return `${session.messageCount || 0} messages`
  }
  
  return 'Empty conversation'
}

const formatHistoryDate = (date) => {
  if (!date) return ''
  
  // Add 6 hours to the date
  const d = new Date(date)
  d.setHours(d.getHours() + 6)
  
  const now = new Date()
  const diffTime = Math.abs(now - d)
  const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffHours < 1) return 'Just now'
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays === 1) return 'Yesterday'
  if (diffDays < 7) return `${diffDays}d ago`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}w ago`
  
  return d.toLocaleDateString()
}

const truncateText = (text, maxLength) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const sendMessage = async () => {
  if (!currentMessage.value.trim() || isLoading.value) return
  
  const messageText = currentMessage.value.trim()
  
  // If no session exists, create one before sending the message
  if (!sessionId.value) {
    try {
      // Create session on backend if user is authenticated
      if (authStore.userInfo?.id) {
        const response = await aiChatService.createSession(authStore.userInfo.id, 'New Chat Session')
        sessionId.value = response.session_id
        currentSessionId.value = response.session_id
        
        // Navigate to the new session URL
        await router.push(`/ai-chat/${response.session_id}`)
      } else {
        // Create local session for anonymous users
        sessionId.value = aiChatService.generateSessionId()
        currentSessionId.value = sessionId.value
        await router.push(`/ai-chat/${sessionId.value}`)
      }
    } catch (error) {
      console.error('Error creating session for message:', error)
      // Fallback to local session
      sessionId.value = aiChatService.generateSessionId()
      currentSessionId.value = sessionId.value
      await router.push(`/ai-chat/${sessionId.value}`)
    }
  }
  
  // Add user message to chat
  messages.value.push({
    type: 'user',
    content: messageText,
    timestamp: new Date()
  })
  
  // Clear input and reset height
  currentMessage.value = ''
  adjustTextareaHeight()
  
  // Scroll to bottom
  await nextTick()
  scrollToBottom()
  
  // Show typing indicator
  isLoading.value = true
  isTyping.value = true
  errorMessage.value = ''
  
  try {
    // Send message to AI service
    const response = await aiChatService.sendMessage(
      messageText, 
      sessionId.value, 
      authStore.userInfo?.id,
      5 // context limit
    )
    
    // Add AI response to chat
    messages.value.push({
      type: 'ai',
      content: response.response,
      sources: response.sources || [],
      metadata: response.metadata || {},
      timestamp: new Date()
    })
    
    // Save session after each exchange
    saveCurrentSession()
    
  } catch (error) {
    console.error('Error sending message:', error)
    errorMessage.value = error.message || 'Failed to get AI response. Please try again.'
    
    // Add error message to chat
    messages.value.push({
      type: 'ai',
      content: 'I apologize, but I encountered an error while processing your request. Please try again.',
      timestamp: new Date()
    })
  } finally {
    isLoading.value = false
    isTyping.value = false
    
    // Scroll to bottom
    await nextTick()
    scrollToBottom()
    
    // Focus input
    messageInput.value?.focus()
  }
}

const clearConversation = async () => {
  if (messages.value.length > 0) {
    saveCurrentSession()
  }
  
  // Navigate to base AI chat route to show empty state
  await router.push('/ai-chat')
}

const handleKeydown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const adjustTextareaHeight = () => {
  const textarea = messageInput.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (date) => {
  return new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatMessage = (content) => {
  // Simple formatting for better readability
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}
</script>

<style scoped>
.ai-chat-layout {
  display: flex;
  height: 100vh;
  background: var(--color-background);
  position: relative;
  overflow: hidden;
}

/* Sidebar Styles */
.chat-sidebar {
  width: 370px;
  background: linear-gradient(135deg, var(--color-background-soft), var(--color-background));
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 1.5rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(147, 51, 234, 0.03));
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-heading);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-actions {
  display: flex;
  gap: 0.75rem;
}

.sidebar-btn {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(59, 130, 246, 0.2);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(147, 51, 234, 0.05));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-primary);
  position: relative;
  overflow: hidden;
}

.sidebar-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.sidebar-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(147, 51, 234, 0.1));
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.2);
}

.sidebar-btn:hover::before {
  opacity: 0.1;
}

.sidebar-btn.primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  border-color: var(--color-primary);
}

.sidebar-btn.primary:hover {
  background: linear-gradient(135deg, var(--color-secondary), var(--color-primary));
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.3);
}

.sidebar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sidebar-btn svg {
  width: 14px;
  height: 14px;
}

.sidebar-search {
  padding: 1.25rem;
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.03), rgba(6, 182, 212, 0.02));
}

.search-container {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--color-primary);
  pointer-events: none;
  transition: color var(--transition-fast);
}

.search-input {
  width: 100%;
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: var(--border-radius-lg);
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8), rgba(59, 130, 246, 0.02));
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  outline: none;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.search-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1), 0 4px 12px rgba(59, 130, 246, 0.15);
  background: var(--color-background);
}

.search-input:focus + .search-icon {
  color: var(--color-secondary);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.loading-state,
.no-history-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1.5rem;
  text-align: center;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.no-history-icon {
  width: 48px;
  height: 48px;
  color: var(--color-primary);
  margin-bottom: 1rem;
  opacity: 0.7;
}

.no-history-state p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-heading);
}

.no-history-state small {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.history-item {
  background: linear-gradient(135deg, var(--color-background), rgba(59, 130, 246, 0.02));
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: var(--border-radius-lg);
  padding: 1rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.history-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  transform: scaleY(0);
  transition: transform var(--transition-fast);
}

.history-item:hover {
  background: linear-gradient(135deg, var(--color-background-soft), rgba(59, 130, 246, 0.05));
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.15);
}

.history-item:hover::before {
  transform: scaleY(1);
}

.history-item.active {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(147, 51, 234, 0.05));
  border-color: var(--color-primary);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.2);
}

.history-item.active::before {
  transform: scaleY(1);
}

.history-item-content {
  flex: 1;
  min-width: 0;
}

.history-title {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-heading);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-preview {
  margin: 0 0 0.5rem 0;
  font-size: 0.8rem;
  color: var(--color-text-muted);
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.history-date {
  font-size: 0.75rem;
  color: var(--color-primary);
  font-weight: 500;
}

.history-actions {
  flex-shrink: 0;
  margin-left: 0.5rem;
}

.history-action-btn {
  width: 28px;
  height: 28px;
  border: 1px solid rgba(239, 68, 68, 0.2);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.05), rgba(220, 38, 38, 0.03));
  color: var(--color-danger);
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  opacity: 0;
  transform: scale(0.8);
}

.history-item:hover .history-action-btn {
  opacity: 1;
  transform: scale(1);
}

.history-action-btn:hover {
  background: linear-gradient(135deg, var(--color-danger), #dc2626);
  color: var(--color-background);
  border-color: var(--color-danger);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.history-action-btn svg {
  width: 12px;
  height: 12px;
}

/* Main Chat Area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
}

.mobile-header {
  display: none;
  background: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  padding: 1rem;
  align-items: center;
  justify-content: space-between;
}

.mobile-menu-btn,
.mobile-clear-btn {
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

.mobile-menu-btn:hover,
.mobile-clear-btn:hover:not(:disabled) {
  background: var(--color-background-soft);
  color: var(--color-primary);
}

.mobile-clear-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mobile-header h1 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
}

.mobile-actions {
  display: flex;
  gap: 0.5rem;
}

.mobile-menu-btn svg,
.mobile-clear-btn svg {
  width: 18px;
  height: 18px;
}

.chat-header {
  background: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  padding: 1rem 1.5rem;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 800px;
  margin: 0 auto;
}

.ai-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.ai-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar svg {
  width: 24px;
  height: 24px;
}

.ai-details h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
}

.ai-details p {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.chat-actions {
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

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action svg {
  width: 18px;
  height: 18px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1rem 2rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  min-height: 0;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 300px;
  padding: 3rem 2rem 2rem;
}

.welcome-content {
  text-align: center;
  max-width: 900px;
  width: 100%;
}

.welcome-content h3 {
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
  color: var(--color-heading);
}

.welcome-content p {
  margin: 0 0 1rem 0;
  color: var(--color-text);
  line-height: 1.6;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 1rem 0;
  text-align: left;
}

.feature-list li {
  padding: 0.5rem 0;
  color: var(--color-text);
  position: relative;
  padding-left: 1.5rem;
}

.feature-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

.welcome-note {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  font-style: italic;
}

/* Quick Prompts Styling */
.quick-prompts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 32px;
  width: 100%;
  max-width: 800px;
}

.prompt-btn {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  text-align: center;
  line-height: 1.4;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.prompt-btn:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.prompt-btn:active {
  transform: translateY(0);
}

.message-wrapper {
  display: flex;
  justify-content: flex-start;
}

.message {
  display: flex;
  gap: 1rem;
  max-width: 85%;
  width: 100%;
}

.message--user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message--user .message-content {
  background: var(--color-primary);
  color: var(--color-background);
  border-radius: 1.25rem 1.25rem 0.5rem 1.25rem;
}

.message--ai .message-content {
  background: var(--color-background-soft);
  color: var(--color-text);
  border-radius: 1.25rem 1.25rem 1.25rem 0.5rem;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.message--ai .ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-background-soft);
  border: 2px solid var(--color-border);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.message--ai .ai-avatar svg {
  width: 20px;
  height: 20px;
}

.message-content {
  padding: 0.75rem 1rem;
  border-radius: 1.25rem;
  position: relative;
  word-wrap: break-word;
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.75rem;
}

.message--user .message-header {
  color: rgba(255, 255, 255, 0.8);
}

.message--ai .message-header {
  color: var(--color-text-muted);
}

.message-sender {
  font-weight: 600;
}

.message-time {
  opacity: 0.7;
}

.message-text {
  line-height: 1.5;
  font-size: 0.95rem;
}

.message-text :deep(strong) {
  font-weight: 600;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875em;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-text-muted);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chat-input-container {
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  padding: 1rem 1.5rem;
  position: sticky;
  bottom: 0;
  z-index: 10;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 1.25rem;
  padding: 0.75rem 1rem;
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.95rem;
  resize: none;
  outline: none;
  transition: all var(--transition-fast);
  min-height: 44px;
  max-height: 120px;
}

.message-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.message-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-button {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.send-button svg {
  width: 20px;
  height: 20px;
}

.loading-spinner {
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

.input-footer {
  max-width: 800px;
  margin: 0.5rem auto 0;
  text-align: center;
}

.disclaimer {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin: 0;
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

/* Right Actions Sidebar */
.right-actions-sidebar {
  width: 320px;
  background: var(--color-background-soft);
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.actions-header {
  padding: 1.5rem 1.25rem 1rem;
  border-bottom: 1px solid var(--color-border);
}

.actions-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.action-buttons {
  padding: 1.5rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: left;
  position: relative;
  overflow: hidden;
}

.action-btn:hover {
  background: var(--color-background-mute);
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--color-primary);
  transform: scaleY(0);
  transition: transform var(--transition-fast);
}

.action-btn:hover::before {
  transform: scaleY(1);
}

.document-search-btn {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(147, 51, 234, 0.05));
  border-color: rgba(59, 130, 246, 0.15);
}

.document-search-btn:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(147, 51, 234, 0.1));
  border-color: var(--color-primary);
}

.chat-history-btn {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.05), rgba(6, 182, 212, 0.05));
  border-color: rgba(16, 185, 129, 0.15);
}

.chat-history-btn:hover {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(6, 182, 212, 0.1));
  border-color: rgb(16, 185, 129);
}

.action-btn .action-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.action-btn:hover .action-icon {
  transform: scale(1.1);
}

.action-icon svg {
  width: 22px;
  height: 22px;
}

.chat-history-btn .action-icon {
  background: linear-gradient(135deg, rgb(16, 185, 129), rgb(6, 182, 212));
}

.action-text {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.action-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  line-height: 1.2;
}

.action-subtitle {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  line-height: 1.3;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
  display: none;
}

/* Responsive Design */
.mobile-only {
  display: none;
}

.desktop-only {
  display: block;
}

/* Tablet responsive */
@media (max-width: 1024px) {
  .quick-prompts {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(3, 1fr);
    max-width: 700px;
    gap: 1.25rem;
  }
}

@media (max-width: 768px) {
  .ai-chat-layout {
    flex-direction: column;
  }
  
  .chat-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: 320px;
    z-index: 999;
    transform: translateX(-100%);
  }
  
  .chat-sidebar.sidebar-open {
    transform: translateX(0);
  }
  
  .sidebar-overlay {
    display: block;
  }
  
  .mobile-only {
    display: flex;
  }
  
  .desktop-only {
    display: none;
  }
  
  .mobile-header {
    display: flex;
  }
  
  .chat-messages {
    padding: 1rem 0.75rem 2rem 0.75rem;
  }
  
  .chat-input-container {
    padding: 1rem;
    position: sticky;
    bottom: 0;
  }
  
  .message {
    max-width: 95%;
  }
  
  .welcome-content {
    padding: 0 1rem;
  }
  
  .right-actions-sidebar {
    display: none;
  }
  
  .welcome-message {
    flex-direction: column;
    gap: 2rem;
    padding: 1.5rem 1rem;
    min-height: auto;
  }
  
  .quick-prompts {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .prompt-btn {
    padding: 16px;
    font-size: 13px;
    min-height: 70px;
  }
}

@media (max-width: 480px) {
  .chat-sidebar {
    width: 100vw;
  }
  
  .mobile-header {
    padding: 0.75rem;
  }
  
  .mobile-header h1 {
    font-size: 1rem;
  }
  
  .chat-messages {
    padding: 1rem 0.5rem 2rem 0.5rem;
  }
  
  .chat-input-container {
    padding: 0.75rem;
    position: sticky;
    bottom: 0;
  }
  
  .input-wrapper {
    gap: 0.5rem;
  }
  
  .message-input {
    padding: 0.625rem 0.875rem;
  }
  
  .send-button {
    width: 40px;
    height: 40px;
  }
  
  .send-button svg {
    width: 18px;
    height: 18px;
  }
  
  /* Mobile prompt buttons */
  .quick-prompts {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(3, 1fr);
    gap: 0.75rem;
    margin: 1.5rem 0;
  }
  
  .prompt-btn {
    padding: 1rem;
  }
  
  .prompt-icon {
    width: 40px;
    height: 40px;
  }
  
  .prompt-icon svg {
    width: 20px;
    height: 20px;
  }
  
  .prompt-title {
    font-size: 0.875rem;
  }
  
  .prompt-subtitle {
    font-size: 0.75rem;
  }
}
</style>
