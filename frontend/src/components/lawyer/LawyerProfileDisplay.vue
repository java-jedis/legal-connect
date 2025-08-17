<template>
  <div class="lawyer-profile-display">
    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="profile-skeleton">
        <div class="skeleton-header">
          <div class="skeleton-avatar"></div>
          <div class="skeleton-info">
            <div class="skeleton-line skeleton-name"></div>
            <div class="skeleton-line skeleton-firm"></div>
            <div class="skeleton-line skeleton-status"></div>
          </div>
        </div>
        <div class="skeleton-details">
          <div class="skeleton-line" v-for="i in 5" :key="i"></div>
        </div>
        <div class="skeleton-bio">
          <div class="skeleton-line" v-for="i in 3" :key="i"></div>
        </div>
        <div class="skeleton-tags">
          <div class="skeleton-tag" v-for="i in 4" :key="i"></div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
          <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <h4>Profile unavailable</h4>
      <p>{{ error }}</p>
      <button @click="$emit('retry')" class="retry-button" type="button">
        Try Again
      </button>
    </div>

    <!-- Profile Content -->
    <div v-else-if="profile" class="profile-content">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="lawyer-avatar">
          {{ getInitial(profile.firstName, profile.lastName) }}
        </div>
        <div class="lawyer-info">
          <h2 class="lawyer-name">
            {{ getDisplayName(profile.firstName, profile.lastName) }}
          </h2>
          <div class="lawyer-email">{{ getEmail(profile.email) }}</div>
          <div class="lawyer-firm">{{ getFirm(profile.firm) }}</div>
          <div class="verified-status">
            <svg class="status-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C3 7.02944 3 12C3 16.9706 7.02944 21 12 21C16.9706 21 21 16.9706 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="status-text">Verified Lawyer</span>
          </div>
        </div>
        
        <!-- Chat Action Button -->
        <div class="profile-actions">
          <button 
            @click="handleStartChat" 
            class="chat-button"
            :disabled="isStartingChat || !canStartChat"
            :aria-label="`Start chat with ${getDisplayName(profile.firstName, profile.lastName)}`"
            type="button"
          >
            <svg 
              v-if="!isStartingChat" 
              class="chat-icon" 
              viewBox="0 0 24 24" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
              aria-hidden="true"
            >
              <path 
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" 
                stroke="currentColor" 
                stroke-width="2" 
                stroke-linecap="round" 
                stroke-linejoin="round"
              />
            </svg>
            <svg 
              v-else 
              class="loading-spinner" 
              viewBox="0 0 24 24" 
              aria-hidden="true"
            >
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
            <span class="chat-button-text">
              {{ isStartingChat ? 'Starting...' : 'Start Chat' }}
            </span>
          </button>
        </div>
      </div>

      <!-- Profile Details Row (all in one row) -->
      <div class="profile-details-row">
        <!-- Professional Information -->
        <div class="detail-card">
          <h3>Professional Information</h3>
          <div class="detail-item">
            <span class="detail-label">Years of Experience:</span>
            <span class="detail-value">{{ profile.yearsOfExperience || 'Not specified' }} years</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Bar Certificate:</span>
            <span class="detail-value">{{ profile.barCertificateNumber || 'Not specified' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Hourly Charge (BDT):</span>
            <span class="detail-value">
              {{ profile.hourlyCharge != null
                ? Number(profile.hourlyCharge).toLocaleString('en-BD', { style: 'currency', currency: 'BDT', minimumFractionDigits: 2 })
                : 'Not specified' }}
            </span>
          </div>
        </div>

        <!-- Practice Location -->
        <div class="detail-card">
          <h3>Practice Location</h3>
          <div class="detail-item">
            <span class="detail-label">Court:</span>
            <span class="detail-value">{{ profile.practicingCourtDisplayName || 'Not specified' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Division:</span>
            <span class="detail-value">{{ profile.divisionDisplayName || 'Not specified' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">District:</span>
            <span class="detail-value">{{ profile.districtDisplayName || 'Not specified' }}</span>
          </div>
        </div>

        <!-- Specializations -->
        <div class="detail-card">
          <h3>Specializations</h3>
          <div class="specializations-container">
            <template v-if="profile.specializationDisplayNames && profile.specializationDisplayNames.length > 0">
              <span 
                v-for="specialization in profile.specializationDisplayNames" 
                :key="specialization" 
                class="specialization-tag"
              >
                {{ specialization }}
              </span>
            </template>
            <span v-else class="no-specializations">No specializations specified</span>
          </div>
        </div>
      </div>

      <!-- Professional Bio -->
      <div v-if="profile.bio" class="detail-card full-width">
        <h3>Professional Bio</h3>
        <div class="bio-content">
          <p class="bio-text" :class="{ 'expanded': bioExpanded }">{{ profile.bio }}</p>
          <button 
            v-if="profile.bio && profile.bio.length > 300" 
            @click="bioExpanded = !bioExpanded" 
            class="bio-toggle"
            type="button"
          >
            {{ bioExpanded ? 'Show Less' : 'Show More' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M16 21V19A4 4 0 0 0 12 15H8A4 4 0 0 0 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <h4>No profile information</h4>
      <p>Profile information is not available for this lawyer.</p>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'LawyerProfileDisplay',
  props: {
    profile: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    },
    error: {
      type: String,
      default: null
    }
  },
  emits: ['retry'],
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const chatStore = useChatStore()
    
    // Chat state
    const isStartingChat = ref(false)
    
    // Computed properties
    const canStartChat = computed(() => {
      return authStore.isLoggedIn && authStore.userInfo?.id
    })
    
    // Chat methods
    const startChat = async (profile) => {
      // Check authentication first
      if (!authStore.isLoggedIn) {
        // Redirect to login
        router.push('/login')
        return
      }
      
      if (!authStore.userInfo?.id) {
        console.error('User information not available')
        return
      }
      
      if (!profile?.id) {
        console.error('Lawyer profile information not available')
        return
      }
      
      // Prevent starting chat with yourself
      if (authStore.userInfo.id === profile.id) {
        console.warn('Cannot start chat with yourself')
        return
      }
      
      isStartingChat.value = true
      
      try {
        // Start conversation with initial message
        const initialMessage = `Hi ${profile.firstName || 'there'}, I would like to discuss my legal needs with you.`
        
        const result = await chatStore.startConversation(profile.id, initialMessage)
        
        if (result.success && result.data) {
          // Navigate to the conversation
          const conversationId = result.data.conversationId
          if (conversationId) {
            // Try to navigate to specific conversation, fallback to inbox if route doesn't exist
            try {
              await router.push(`/chat/${conversationId}`)
            } catch (routeError) {
              console.warn('Chat conversation route not available, navigating to inbox')
              try {
                await router.push('/chat')
              } catch (inboxError) {
                console.warn('Chat inbox route not available yet')
                // For now, just log success - routes will be implemented in later tasks
                console.log('Chat conversation started successfully. Conversation ID:', conversationId)
              }
            }
          } else {
            // If no conversation ID, try to navigate to inbox
            try {
              await router.push('/chat')
            } catch (routeError) {
              console.warn('Chat inbox route not available yet')
              console.log('Chat conversation started successfully')
            }
          }
        } else {
          console.error('Failed to start conversation:', result.message)
          // Could show a toast notification here
        }
      } catch (error) {
        console.error('Error starting chat:', error)
        // Could show a toast notification here
      } finally {
        isStartingChat.value = false
      }
    }
    
    return {
      isStartingChat,
      canStartChat,
      startChat
    }
  },
  data() {
    return {
      bioExpanded: false
    }
  },
  methods: {
    getInitial(firstName, lastName) {
      if (firstName && lastName) {
        return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
      } else if (firstName) {
        return firstName.charAt(0).toUpperCase()
      } else if (lastName) {
        return lastName.charAt(0).toUpperCase()
      }
      return '?'
    },
    getDisplayName(firstName, lastName) {
      if (firstName && lastName) {
        return `${firstName} ${lastName}`
      } else if (firstName) {
        return firstName
      } else if (lastName) {
        return lastName
      }
      return 'Lawyer Name Not Available'
    },
    getEmail(email) {
      return email || 'Not specified'
    },
    getFirm(firm) {
      return firm || 'Not specified'
    },
    // Chat method that uses the setup function
    async handleStartChat() {
      await this.startChat(this.profile)
    }
  }
}
</script>

<style scoped>
.lawyer-profile-display {
  background: var(--color-background);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  position: relative;
  overflow: hidden;
}

.lawyer-profile-display::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(
    90deg,
    var(--color-primary),
    var(--color-secondary)
  );
}

/* Loading State */
.loading-state {
  padding: 2rem;
}

.profile-skeleton {
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.skeleton-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--color-background-soft);
  margin-right: 1.5rem;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
}

.skeleton-line {
  height: 16px;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  margin-bottom: 0.5rem;
}

.skeleton-name {
  width: 200px;
  height: 20px;
}

.skeleton-firm {
  width: 150px;
}

.skeleton-status {
  width: 100px;
}

.skeleton-details {
  margin-bottom: 2rem;
}

.skeleton-details .skeleton-line {
  width: 100%;
  margin-bottom: 0.75rem;
}

.skeleton-bio {
  margin-bottom: 2rem;
}

.skeleton-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.skeleton-tag {
  width: 80px;
  height: 32px;
  background: var(--color-background-soft);
  border-radius: 20px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.error-icon {
  width: 48px;
  height: 48px;
  color: var(--color-error);
  margin-bottom: 1rem;
}

.error-state h4 {
  margin: 0 0 0.5rem 0;
  color: var(--color-error);
  font-size: 1.125rem;
  font-weight: 600;
}

.error-state p {
  margin: 0 0 1rem 0;
  color: var(--color-text-muted);
  font-size: 0.95rem;
}

.retry-button {
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.retry-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}

.empty-state h4 {
  margin: 0 0 0.5rem 0;
  color: var(--color-heading);
  font-size: 1.125rem;
  font-weight: 600;
}

.empty-state p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 0.95rem;
  font-style: italic;
}

/* Profile Content */
.profile-content {
  width: 100%;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.lawyer-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  margin-right: 1.5rem;
  flex-shrink: 0;
  box-shadow: var(--shadow-md);
}

.lawyer-info {
  flex: 1;
}

.lawyer-name {
  margin: 0 0 0.25rem 0;
  color: var(--color-heading);
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.2;
}

.lawyer-email {
  color: var(--color-primary);
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  word-break: break-all;
}

.lawyer-firm {
  color: var(--color-text-muted);
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
}

.verified-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-success);
  font-weight: 600;
  font-size: 1rem;
  margin-top: 0.25rem;
}

.status-icon {
  width: 18px;
  height: 18px;
}

/* Profile Actions */
.profile-actions {
  margin-left: 1.5rem;
  flex-shrink: 0;
}

.chat-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: var(--border-radius-lg);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
  min-width: 140px;
  justify-content: center;
}

.chat-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.chat-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.chat-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: var(--shadow-sm);
}

.chat-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  animation: spin 1s linear infinite;
}

.chat-button-text {
  font-size: 0.95rem;
  font-weight: 600;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.profile-details-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.detail-card {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  flex: 1 1 300px;
  min-width: 260px;
  transition: all var(--transition-fast);
}

.detail-card h3 {
  margin: 0 0 1rem 0;
  color: var(--color-heading);
  font-size: 1.125rem;
  font-weight: 600;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-weight: 500;
  color: var(--color-text);
}

.detail-value {
  font-weight: 600;
  color: var(--color-heading);
  text-align: right;
}

.specializations-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.specialization-tag {
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius-lg);
  font-size: 0.875rem;
  font-weight: 600;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.specialization-tag:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.no-specializations {
  color: var(--color-text-muted);
  font-style: italic;
  font-size: 0.95rem;
}

@media (max-width: 900px) {
  .profile-details-row {
    flex-direction: column;
    gap: 1rem;
  }
}

.bio-content {
  position: relative;
}

.bio-text {
  margin: 0;
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
}

.bio-text:not(.expanded) {
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bio-toggle {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 0.5rem;
  padding: 0;
  transition: color var(--transition-fast);
}

.bio-toggle:hover {
  color: var(--color-secondary);
}

/* Responsive Design */
@media (max-width: 768px) {
  .lawyer-profile-display {
    padding: 1.5rem;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
    margin-bottom: 1.5rem;
  }

  .lawyer-avatar {
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
    margin-right: 0;
    margin-bottom: 1rem;
  }

  .lawyer-name {
    font-size: 1.5rem;
  }

  .lawyer-firm {
    font-size: 1rem;
  }

  .profile-actions {
    margin-left: 0;
    margin-top: 1rem;
    width: 100%;
    display: flex;
    justify-content: center;
  }

  .chat-button {
    min-width: 160px;
    padding: 0.875rem 1.5rem;
  }

  .profile-details-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .detail-card {
    padding: 1rem;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .detail-value {
    text-align: left;
  }

  .specializations-container {
    justify-content: center;
  }

  /* Skeleton responsive adjustments */
  .skeleton-header {
    flex-direction: column;
    text-align: center;
    margin-bottom: 1.5rem;
  }

  .skeleton-avatar {
    width: 60px;
    height: 60px;
    margin-right: 0;
    margin-bottom: 1rem;
  }

  .skeleton-name {
    width: 150px;
  }

  .skeleton-firm {
    width: 120px;
  }
}

@media (max-width: 480px) {
  .lawyer-profile-display {
    padding: 1rem;
  }

  .lawyer-avatar {
    width: 50px;
    height: 50px;
    font-size: 1.25rem;
  }

  .lawyer-name {
    font-size: 1.25rem;
  }

  .lawyer-firm {
    font-size: 0.95rem;
  }

  .verification-status {
    padding: 0.375rem 0.75rem;
    font-size: 0.8125rem;
  }

  .status-icon {
    width: 14px;
    height: 14px;
  }

  .chat-button {
    min-width: 140px;
    padding: 0.75rem 1rem;
    font-size: 0.875rem;
  }

  .chat-icon,
  .loading-spinner {
    width: 16px;
    height: 16px;
  }

  .chat-button-text {
    font-size: 0.875rem;
  }

  .detail-card h3 {
    font-size: 1rem;
  }

  .detail-label,
  .detail-value {
    font-size: 0.875rem;
  }

  .specialization-tag {
    padding: 0.375rem 0.75rem;
    font-size: 0.8125rem;
  }

  .bio-text {
    font-size: 0.875rem;
  }

  .bio-toggle {
    font-size: 0.8125rem;
  }
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .chat-button,
  .loading-spinner {
    transition: none;
    animation: none;
  }

  .chat-button:hover:not(:disabled) {
    transform: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .chat-button {
    border: 2px solid var(--color-background);
  }

  .chat-button:disabled {
    border-color: var(--color-text-muted);
  }
}
</style>