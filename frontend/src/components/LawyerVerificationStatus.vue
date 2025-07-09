<template>
  <div class="lawyer-verification-status">
    <div class="container">
      <div class="status-container">
        <!-- Loading State -->
        <div v-if="isLoading" class="loading-state">
          <div class="loading-spinner"></div>
          <h2>Loading your profile...</h2>
          <p>Please wait while we fetch your information.</p>
        </div>

        <!-- No Profile Created -->
        <div v-else-if="!hasProfile" class="no-profile-state">
          <div class="status-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M16 21V19A4 4 0 0 0 12 15H8A4 4 0 0 0 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M12 11V17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M9 14L12 11L15 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>Complete Your Lawyer Profile</h2>
          <p>To access your dashboard, you need to create your lawyer profile and upload your bar certificate for verification.</p>
          <div class="action-buttons">
            <button @click="createProfile" class="btn btn-primary btn-lg">
              Create Profile
            </button>
          </div>
        </div>

        <!-- Pending Verification -->
        <div v-else-if="isPending" class="pending-state">
          <div class="status-icon pending">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>Profile Under Review</h2>
          <p>Your lawyer profile has been submitted and is currently under review by our verification team. This process typically takes 1-3 business days.</p>
          <div class="verification-details">
            <div class="detail-item">
              <span class="detail-label">Status:</span>
              <span class="detail-value pending">Pending Verification</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Submitted:</span>
              <span class="detail-value">{{ formatDate(lawyerInfo?.lawyerCreatedAt) }}</span>
            </div>
          </div>
          <div class="action-buttons">
            <button @click="refreshStatus" class="btn btn-outline">
              Refresh Status
            </button>
            <button @click="viewProfile" class="btn btn-secondary">
              View Profile
            </button>
          </div>
        </div>

        <!-- Rejected -->
        <div v-else-if="isRejected" class="rejected-state">
          <div class="status-icon rejected">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>Profile Verification Failed</h2>
          <p>Your lawyer profile verification was not approved. Please review the requirements and update your profile.</p>
          <div class="verification-details">
            <div class="detail-item">
              <span class="detail-label">Status:</span>
              <span class="detail-value rejected">Rejected</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Last Updated:</span>
              <span class="detail-value">{{ formatDate(lawyerInfo?.lawyerUpdatedAt) }}</span>
            </div>
          </div>
          <div class="action-buttons">
            <button @click="updateProfile" class="btn btn-primary">
              Update Profile
            </button>
            <button @click="viewProfile" class="btn btn-outline">
              View Profile
            </button>
          </div>
        </div>

        <!-- Verified -->
        <div v-else-if="isVerified" class="verified-state">
          <div class="status-icon verified">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22 11.08V12A10 10 0 1 1 5.68 3.57" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>Profile Verified!</h2>
          <p>Congratulations! Your lawyer profile has been verified and approved. You now have full access to your dashboard.</p>
          <div class="verification-details">
            <div class="detail-item">
              <span class="detail-label">Status:</span>
              <span class="detail-value verified">Verified</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Verified:</span>
              <span class="detail-value">{{ formatDate(lawyerInfo?.lawyerUpdatedAt) }}</span>
            </div>
          </div>
          <div class="action-buttons">
            <button @click="goToDashboard" class="btn btn-primary">
              Go to Dashboard
            </button>
            <button @click="viewProfile" class="btn btn-outline">
              View Profile
            </button>
          </div>
        </div>

        <!-- Error State -->
        <div v-else class="error-state">
          <div class="status-icon error">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>Something went wrong</h2>
          <p>{{ error || 'Unable to load your verification status. Please try again.' }}</p>
          <div class="action-buttons">
            <button @click="retry" class="btn btn-primary">
              Try Again
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLawyerStore } from '../stores/lawyer'

const router = useRouter()
const lawyerStore = useLawyerStore()

// Computed properties
const isLoading = computed(() => lawyerStore.isLoading)
const hasProfile = computed(() => lawyerStore.hasProfile)
const isVerified = computed(() => lawyerStore.isVerified)
const isPending = computed(() => lawyerStore.isPending)
const isRejected = computed(() => lawyerStore.isRejected)
const lawyerInfo = computed(() => lawyerStore.lawyerInfo)
const error = computed(() => lawyerStore.error)

// Methods
const createProfile = () => {
  router.push({ name: 'lawyer-profile-creation' })
}

const updateProfile = () => {
  router.push({ name: 'lawyer-profile-creation' })
}

const viewProfile = () => {
  router.push({ name: 'view-profile' })
}

const goToDashboard = () => {
  router.push({ name: 'lawyer-dashboard' })
}

const refreshStatus = async () => {
  try {
    await lawyerStore.fetchLawyerInfo()
  } catch {
    // Error is handled by the store
  }
}

const retry = async () => {
  lawyerStore.clearError()
  await refreshStatus()
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'N/A'
  }
}

// Load lawyer info on mount
onMounted(async () => {
  await lawyerStore.fetchLawyerInfo()
})
</script>

<style scoped>
.lawyer-verification-status {
  min-height: 100vh;
  background: var(--color-background-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.status-container {
  max-width: 600px;
  width: 100%;
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 3rem 2rem;
  text-align: center;
}

.status-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background-soft);
  color: var(--color-text-muted);
}

.status-icon svg {
  width: 40px;
  height: 40px;
}

.status-icon.pending {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.status-icon.verified {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.status-icon.rejected {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.status-icon.error {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-border);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 2rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

p {
  font-size: 1.125rem;
  color: var(--color-text-muted);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.verification-details {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: left;
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
}

.detail-value.pending {
  color: #ffc107;
}

.detail-value.verified {
  color: #28a745;
}

.detail-value.rejected {
  color: #dc3545;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-lg {
  padding: 1rem 2rem;
  font-size: 1.125rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .status-container {
    margin: 1rem;
    padding: 2rem 1.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .btn {
    width: 100%;
  }
}
</style> 