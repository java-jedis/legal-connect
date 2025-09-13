<template>
  <div class="profile-picture-prompt" v-if="!authStore.userInfo?.profilePicture?.fullPictureUrl">
    <div class="prompt-card">
      <div class="prompt-header">
        <div class="prompt-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z"></path>
            <circle cx="12" cy="13" r="3"></circle>
          </svg>
        </div>
        <div class="prompt-content">
          <h3>Complete Your Profile</h3>
          <p>Add a profile picture to personalize your account and help others recognize you.</p>
        </div>
      </div>
      
      <div class="prompt-actions">
        <button 
          class="btn btn-primary" 
          @click="triggerFileInput" 
          :disabled="uploadingPicture"
        >
          <svg v-if="!uploadingPicture" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z"></path>
            <circle cx="12" cy="13" r="3"></circle>
          </svg>
          <span v-if="uploadingPicture" class="loading-spinner"></span>
          {{ uploadingPicture ? 'Uploading...' : 'Upload Photo' }}
        </button>
      </div>
      
      <!-- Upload Messages -->
      <div v-if="pictureError" class="upload-message error">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="15" y1="9" x2="9" y2="15"></line>
          <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>
        {{ pictureError }}
      </div>
      <div v-if="pictureSuccess" class="upload-message success">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 11.08V12A10 10 0 1 1 5.68 3.57"></path>
          <path d="M22 4L12 14.01L9 11.01"></path>
        </svg>
        {{ pictureSuccess }}
      </div>
    </div>
    
    <!-- Hidden file input -->
    <input 
      ref="fileInput" 
      type="file" 
      accept="image/*" 
      @change="handleFileSelect" 
      style="display: none;"
    />
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { ref } from 'vue'

const authStore = useAuthStore()
const fileInput = ref(null)
const uploadingPicture = ref(false)
const pictureError = ref('')
const pictureSuccess = ref('')

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
    console.log('Starting profile picture upload from dashboard...')
    const result = await authStore.uploadProfilePicture(file)
    console.log('Upload result:', result)
    
    if (result.success) {
      pictureSuccess.value = result.message || 'Profile picture uploaded successfully!'
      // Clear the file input
      event.target.value = ''
      
      console.log('Profile picture updated successfully:', result.data)
      
      // Auto-dismiss success message after 3 seconds
      setTimeout(() => {
        pictureSuccess.value = ''
      }, 3000)
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
.profile-picture-prompt {
  margin-bottom: 2rem;
}

.prompt-card {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  padding: 1.5rem;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.prompt-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.prompt-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(10px);
}

.prompt-icon svg {
  width: 24px;
  height: 24px;
}

.prompt-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  color: var(--color-background);
}

.prompt-content p {
  font-size: 0.875rem;
  margin: 0;
  opacity: 0.9;
  line-height: 1.5;
}

.prompt-actions {
  display: flex;
  justify-content: center;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: none;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--color-background);
  color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.btn-primary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}


.loading-spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid var(--color-primary);
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-message {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.upload-message.error {
  background: rgba(239, 68, 68, 0.1);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.upload-message.success {
  background: rgba(16, 185, 129, 0.1);
  color: #6ee7b7;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.upload-message svg {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .prompt-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .prompt-actions {
    width: 100%;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
