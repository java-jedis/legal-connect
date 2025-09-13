<template>
  <div>
    <!-- Main Navbar should be here (not shown in this file) -->
    <ProfileTopBar :is-lawyer="authStore.isLawyer()" />
    <div class="profile-page">
      <div class="profile-container">
        <div class="profile-card">
          <div id="profile" class="profile-header">
            <h1 class="profile-title">My Profile</h1>
            <p class="profile-subtitle">Manage your account and change your password.</p>
          </div>
          <div class="user-info-section">
            <div class="user-avatar-section">
              <div class="user-profile-container">
                <div class="user-profile-image-container">
                  <div class="user-avatar-large" v-if="!authStore.userInfo?.profilePicture?.fullPictureUrl">
                    <span>{{ userInitial }}</span>
                  </div>
                  <img 
                    v-else
                    :src="authStore.userInfo.profilePicture.fullPictureUrl" 
                    :alt="userFullName"
                    :key="authStore.userInfo.profilePicture.fullPictureUrl"
                    class="user-profile-image-square"
                  />
                </div>
                <button class="upload-picture-btn-large" @click="triggerFileInput" :disabled="uploadingPicture">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z"></path>
                    <circle cx="12" cy="13" r="3"></circle>
                  </svg>
                  {{ uploadingPicture ? 'Uploading...' : 'Change Photo' }}
                </button>
                <input 
                  ref="fileInput" 
                  type="file" 
                  accept="image/*" 
                  @change="handleFileSelect" 
                  style="display: none;"
                />
              </div>
              <div class="user-name-display">
                <h2>{{ userFullName }}</h2>
                <p class="user-email">{{ authStore.userInfo?.email }}</p>
              </div>
            </div>
            
            <!-- Profile Picture Upload Messages -->
            <div v-if="pictureError" class="form-error">
              <p class="error-message">{{ pictureError }}</p>
            </div>
            <div v-if="pictureSuccess" class="form-success">
              <p class="success-message">{{ pictureSuccess }}</p>
            </div>
            
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                    <polyline points="22,6 12,13 2,6"></polyline>
                  </svg>
                </div>
                <div class="stat-content">
                  <span class="stat-label">Email Status</span>
                  <span class="stat-value" :class="{ 'verified': authStore.userInfo?.emailVerified, 'unverified': !authStore.userInfo?.emailVerified }">
                    {{ authStore.userInfo?.emailVerified ? 'Verified' : 'Unverified' }}
                  </span>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                  </svg>
                </div>
                <div class="stat-content">
                  <span class="stat-label">Member Since</span>
                  <span class="stat-value">{{ formattedCreatedAt }}</span>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                  </svg>
                </div>
                <div class="stat-content">
                  <span class="stat-label">Account Type</span>
                  <span class="stat-value">{{ userTypeDisplay }}</span>
                </div>
              </div>
            </div>
          </div>
          <!-- Lawyer Profile Section -->
        <div id="lawyer-profile">
          <div class="section-divider" v-if="authStore.isLawyer()">
            <span class="divider-text">Lawyer Profile</span>
          </div>
          <LawyerProfileSection v-if="authStore.isLawyer()" />
        </div>

        <div class="section-divider">
          <span class="divider-text">Security</span>
        </div>
          <div id="security" class="security-section">
          <form class="change-password-form" @submit.prevent="submitChangePassword">
            <div class="form-group">
              <label for="oldPassword">Current Password</label>
              <div class="input-group">
                <input
                  :type="showOldPassword ? 'text' : 'password'"
                  id="oldPassword"
                  v-model="form.oldPassword"
                  required
                  placeholder="Enter current password"
                  :disabled="isSubmitting"
                />
                <button type="button" class="password-toggle" @click="showOldPassword = !showOldPassword" :disabled="isSubmitting">
                  <svg v-if="showOldPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                </button>
              </div>
            </div>
            <div class="form-group">
              <label for="password">New Password</label>
              <div class="input-group">
                <input
                  :type="showNewPassword ? 'text' : 'password'"
                  id="password"
                  v-model="form.password"
                  required
                  placeholder="Enter new password"
                  :disabled="isSubmitting"
                  @input="validatePassword"
                />
                <button type="button" class="password-toggle" @click="showNewPassword = !showNewPassword" :disabled="isSubmitting">
                  <svg v-if="showNewPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                </button>
              </div>
              <div class="password-requirements" v-if="passwordErrors.length > 0">
                <small class="error-text">Password must meet the following requirements:</small>
                <ul class="requirements-list">
                  <li v-for="error in passwordErrors" :key="error" class="requirement-item">
                    {{ error }}
                  </li>
                </ul>
              </div>
            </div>
            <div class="form-group">
              <label for="confirmPassword">Confirm New Password</label>
              <div class="input-group">
                <input
                  :type="showConfirmPassword ? 'text' : 'password'"
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  required
                  placeholder="Confirm new password"
                  :disabled="isSubmitting"
                  @input="validateConfirmPassword"
                />
                <button type="button" class="password-toggle" @click="showConfirmPassword = !showConfirmPassword" :disabled="isSubmitting">
                  <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                </button>
              </div>
              <small v-if="confirmPasswordError" class="error-text">{{ confirmPasswordError }}</small>
            </div>
            <div v-if="formError" class="form-error">
              <p class="error-message">{{ formError }}</p>
            </div>
            <div v-if="formSuccess" class="form-success">
              <p class="success-message">{{ formSuccess }}</p>
            </div>
            <div class="form-actions">
              <button type="submit" class="btn btn-primary" :disabled="isSubmitting || !isFormValid">
                <span v-if="isSubmitting" class="loading-spinner"></span>
                {{ isSubmitting ? 'Changing...' : 'Change Password' }}
              </button>
            </div>
          </form>
        </div>
        <div class="section-divider">
          <span class="divider-text">Notification Preferences</span>
        </div>
        <div id="notifications" class="notification-section">
          <div class="notification-preferences-container">
            <h2 class="section-title">Notification Preferences</h2>
            <p class="section-subtitle">Manage how you receive notifications from the system.</p>
            <NotificationPreferences />
          </div>
        </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import LawyerProfileSection from "@/components//lawyer/LawyerProfileSection.vue";
import NotificationPreferences from "@/components//notification/NotificationPreferences.vue";
import ProfileTopBar from "@/components//user/ProfileTopBar.vue";
import { useAuthStore } from '@/stores/auth';
import { computed, reactive, ref } from 'vue';

const authStore = useAuthStore()
const isSubmitting = ref(false)
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const formError = ref('')
const formSuccess = ref('')
const uploadingPicture = ref(false)
const fileInput = ref(null)
const pictureError = ref('')
const pictureSuccess = ref('')

const form = reactive({
  oldPassword: '',
  password: '',
  confirmPassword: ''
})

const passwordErrors = ref([])
const confirmPasswordError = ref('')

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

const userTypeDisplay = computed(() => {
  if (authStore.isLawyer()) return 'Lawyer'
  if (authStore.isUser()) return 'Client'
  return 'User'
})

const formattedCreatedAt = computed(() => {
  if (!authStore.userInfo?.createdAt) return ''
  return new Date(authStore.userInfo.createdAt).toLocaleDateString('en-US', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
})

const validatePassword = () => {
  const errors = []
  if (form.password.length < 8) {
    errors.push('At least 8 characters long')
  }
  if (form.password.length > 100) {
    errors.push('No more than 100 characters')
  }
  if (!/(?=.*[a-z])/.test(form.password)) {
    errors.push('At least one lowercase letter')
  }
  if (!/(?=.*[A-Z])/.test(form.password)) {
    errors.push('At least one uppercase letter')
  }
  if (!/(?=.*\d)/.test(form.password)) {
    errors.push('At least one number')
  }
  if (!/[@#$%^&+=!*()_\-[\]{}|;:,.<>?]/.test(form.password)) {
    errors.push('At least one special character (@ # $ % ^ & + = ! * ( ) _ - [ ] { } | ; : , . < > ?)')
  }
  if (/\s/.test(form.password)) {
    errors.push('No spaces allowed')
  }
  passwordErrors.value = errors
  validateConfirmPassword()
}

const validateConfirmPassword = () => {
  if (form.confirmPassword && form.password !== form.confirmPassword) {
    confirmPasswordError.value = 'Passwords do not match'
  } else {
    confirmPasswordError.value = ''
  }
}

const isFormValid = computed(() => {
  return (
    form.oldPassword &&
    form.password &&
    form.confirmPassword &&
    passwordErrors.value.length === 0 &&
    !confirmPasswordError.value
  )
})

const submitChangePassword = async () => {
  formError.value = ''
  formSuccess.value = ''
  if (!isFormValid.value) return
  isSubmitting.value = true
  try {
    const result = await authStore.changePassword({
      oldPassword: form.oldPassword,
      password: form.password
    })
    if (result.success) {
      formSuccess.value = result.message
      form.oldPassword = ''
      form.password = ''
      form.confirmPassword = ''
    } else {
      formError.value = result.message
    }
  } catch {
    formError.value = 'An unexpected error occurred.'
  } finally {
    isSubmitting.value = false
  }
}

const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

const handleFileSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    pictureError.value = 'Please select a valid image file.'
    return
  }

  // Validate file size (5MB limit)
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    pictureError.value = 'File size must be less than 5MB.'
    return
  }

  pictureError.value = ''
  pictureSuccess.value = ''
  uploadingPicture.value = true

  try {
    console.log('Starting profile picture upload...')
    const result = await authStore.uploadProfilePicture(file)
    console.log('Upload result:', result)
    
    if (result.success) {
      pictureSuccess.value = result.message || 'Profile picture uploaded successfully!'
      // Clear the file input
      event.target.value = ''
      
      console.log('Profile picture updated successfully:', result.data)
    } else {
      pictureError.value = result.message || 'Failed to upload profile picture.'
      console.error('Upload failed:', result)
    }
  } catch (error) {
    console.error('Upload error:', error)
    pictureError.value = `Failed to upload profile picture: ${error.message || 'Please try again.'}`
  } finally {
    uploadingPicture.value = false
  }
}
</script>

<style scoped>
/* Remove any overflow: hidden/auto from .profile-page or parent containers */
.profile-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--color-background-soft);
  padding: 2rem;
}

/* No overflow property here! */

/* Optionally, you can add a margin-top to .profile-page if your sticky bars overlap content */
.profile-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  padding-top: 1rem;
}
.profile-card {
  background: var(--color-background);
  padding: 3rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}
.profile-header {
  text-align: center;
  margin-bottom: 2rem;
  padding-top: 1rem;
}
.profile-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}
.profile-subtitle {
  color: var(--color-text-muted);
}
.change-password-form {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}
.form-group {
  margin-bottom: 1.5rem;
}
.input-group {
  position: relative;
}
.input-group input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.input-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary), 0.1);
  background: var(--color-background);
}
.input-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.password-toggle {
  position: absolute;
  top: 50%;
  right: 1rem;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--border-radius-sm);
  transition: all 0.2s ease;
}
.password-toggle:hover {
  color: var(--color-text);
  background: var(--color-background-soft);
}
.password-toggle:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.password-requirements {
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  border-left: 3px solid var(--color-warning);
}
.requirements-list {
  margin: 0.5rem 0 0 0;
  padding-left: 1.25rem;
  list-style: none;
}
.requirement-item {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}
.form-error {
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: rgba(var(--color-error), 0.05);
  border: 1px solid rgba(var(--color-error), 0.2);
  border-radius: var(--border-radius);
}
.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  text-align: center;
  margin: 0;
}
.form-success {
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: rgba(var(--color-success), 0.05);
  border: 1px solid rgba(var(--color-success), 0.2);
  border-radius: var(--border-radius);
}
.success-message {
  color: var(--color-success);
  font-size: 0.875rem;
  text-align: center;
  margin: 0;
}
.btn {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--border-radius);
  border: none;
  background: var(--color-primary);
  color: var(--color-background);
  cursor: pointer;
  transition: background 0.2s;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-primary {
  background: var(--color-primary);
}
.loading-spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid var(--color-background);
  border-top: 2px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 0.5em;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
/* User Info Section */
.user-info-section {
  margin-bottom: 2.5rem;
  padding: 2.5rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}
.user-avatar-section {
  display: flex;
  align-items: flex-start;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
  gap: 2rem;
}

.user-profile-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.user-profile-image-container {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  border: 3px solid var(--color-border);
  background: var(--color-background-soft);
}

.user-avatar-large {
  width: 100%;
  height: 100%;
  border-radius: var(--border-radius-lg);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  font-weight: 700;
}

.user-profile-image-square {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-picture-btn-large {
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: var(--shadow-md);
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 140px;
  justify-content: center;
}

.upload-picture-btn-large:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.upload-picture-btn-large:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.upload-picture-btn-large svg {
  flex-shrink: 0;
}

/* Keep original styles for backward compatibility */
.user-avatar-container {
  position: relative;
  margin-right: 2rem;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  box-shadow: var(--shadow-md);
}

.user-profile-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: var(--shadow-md);
  border: 3px solid var(--color-background);
}

.upload-picture-btn {
  position: absolute;
  bottom: -8px;
  right: -8px;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  padding: 0.5rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  box-shadow: var(--shadow-md);
  transition: all 0.2s ease;
  white-space: nowrap;
}

.upload-picture-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.upload-picture-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.upload-picture-btn svg {
  flex-shrink: 0;
}
.user-name-display h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0 0 0.5rem 0;
}
.user-email {
  color: var(--color-text-muted);
  font-size: 1rem;
  margin: 0;
}
.user-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}
.stat-item {
  display: flex;
  align-items: center;
  padding: 1.25rem;
  background: var(--color-background);
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
  transition: all 0.2s ease;
}
.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1.25rem;
  flex-shrink: 0;
}
.stat-content {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}
.stat-value {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
}
.stat-value.verified {
  color: #10b981;
}
.stat-value.unverified {
  color: #ef4444;
}
.section-divider {
  display: flex;
  align-items: center;
  margin: 2rem 0;
  text-align: center;
}
.section-divider::before,
.section-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}
.divider-text {
  padding: 0 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.security-section {
  max-width: 420px;
  margin: 0 auto;
  padding-top: 2rem;
}
#lawyer-profile {
  padding-top: 2rem;
}

.notification-section {
  max-width: 600px;
  margin: 0 auto;
  padding-top: 2rem;
}

.notification-preferences-container {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
  text-align: center;
}

.section-subtitle {
  color: var(--color-text-muted);
  text-align: center;
  margin-bottom: 2rem;
}
</style> 