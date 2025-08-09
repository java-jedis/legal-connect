<template>
  <div v-if="isVisible" class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">Email Verification Required</h2>
        <button class="modal-close" @click="closeModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <div class="modal-body">
        <div class="verification-content">
          <div class="verification-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
              <polyline points="22,6 12,13 2,6"></polyline>
            </svg>
          </div>
          
          <p class="verification-message">
            Please verify your email address to access this feature. We'll send a verification code to <strong>{{ userEmail }}</strong>.
          </p>

          <!-- Step 1: Request OTP -->
          <div v-if="!otpRequested" class="verification-step">
            <button 
              @click="requestOTP" 
              class="btn btn-primary" 
              :disabled="isRequesting"
            >
              {{ isRequesting ? 'Sending...' : 'Send Verification Code' }}
            </button>
          </div>

          <!-- Step 2: Enter OTP -->
          <div v-else class="verification-step">
            <p class="otp-instruction">Enter the 6-digit verification code:</p>
            
            <div class="otp-inputs">
              <input
                v-for="(digit, index) in 6"
                :key="index"
                :ref="el => otpInputs[index] = el"
                type="text"
                maxlength="1"
                class="otp-input"
                :class="{ 'otp-input--error': otpError }"
                @input="handleOtpInput($event, index)"
                @keydown="handleOtpKeydown($event, index)"
                @paste="handleOtpPaste"
                @focus="handleOtpFocus(index)"
              />
            </div>
            
            <p v-if="otpError" class="error-message">{{ otpError }}</p>
            
            <div class="verification-actions">
              <button 
                @click="verifyEmail" 
                class="btn btn-primary" 
                :disabled="isVerifying || !isOtpComplete"
              >
                {{ isVerifying ? 'Verifying...' : 'Verify Email' }}
              </button>
              
              <button 
                @click="resendOTP" 
                class="btn btn-secondary"
                :disabled="isResending || countdown > 0"
              >
                {{ countdown > 0 ? `Resend in ${countdown}s` : 'Resend Code' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { authAPI } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { showErrorAlert } from '@/utils/errorHandler'
import { computed, ref, watch } from 'vue'

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'verified'])

const authStore = useAuthStore()

// State
const otpRequested = ref(false)
const isRequesting = ref(false)
const isVerifying = ref(false)
const isResending = ref(false)
const otpError = ref('')
const countdown = ref(0)
const otpInputs = ref([])
const otp = ref(['', '', '', '', '', ''])

// Computed
const userEmail = computed(() => authStore.userInfo?.email || 'your email')
const isOtpComplete = computed(() => otp.value.every(digit => digit !== ''))

// Methods
const closeModal = () => {
  emit('close')
  resetForm()
}

const resetForm = () => {
  otpRequested.value = false
  otpError.value = ''
  otp.value = ['', '', '', '', '', '']
  otpInputs.value.forEach(input => {
    if (input) input.value = ''
  })
}

const requestOTP = async () => {
  if (!authStore.userInfo?.email) {
    showErrorAlert(new Error('Email not found. Please login again.'))
    return
  }

  isRequesting.value = true
  try {
    const response = await authAPI.sendVerificationCode(authStore.userInfo.email)
    
    if (response.status >= 200 && response.status < 300) {
      otpRequested.value = true
      startCountdown()
    } else {
      showErrorAlert(new Error(response.message || 'Failed to send verification code'))
    }
  } catch (error) {
    showErrorAlert(error)
  } finally {
    isRequesting.value = false
  }
}

const verifyEmail = async () => {
  if (!isOtpComplete.value) {
    otpError.value = 'Please enter the complete verification code'
    return
  }

  isVerifying.value = true
  otpError.value = ''

  try {
    const otpString = otp.value.join('')
    const response = await authAPI.verifyEmail({
      email: authStore.userInfo.email,
      otp: otpString
    })

    if (response.status >= 200 && response.status < 300) {
      // Update user info to reflect email verification
      const updatedUserInfo = { ...authStore.userInfo, emailVerified: true }
      authStore.userInfo = updatedUserInfo
      localStorage.setItem('auth_userInfo', JSON.stringify(updatedUserInfo))
      
      emit('verified')
      closeModal()
    } else {
      otpError.value = response.message || 'Invalid verification code'
    }
  } catch (error) {
    showErrorAlert(error)
  } finally {
    isVerifying.value = false
  }
}

const resendOTP = async () => {
  if (countdown.value > 0) return

  isResending.value = true
  try {
    const response = await authAPI.sendVerificationCode(authStore.userInfo.email)
    
    if (response.status >= 200 && response.status < 300) {
      startCountdown()
    } else {
      showErrorAlert(new Error(response.message || 'Failed to resend verification code'))
    }
  } catch (error) {
    showErrorAlert(error)
  } finally {
    isResending.value = false
  }
}

const handleOtpInput = (event, index) => {
  const value = event.target.value
  if (value.length > 1) {
    event.target.value = value[0]
  }
  
  otp.value[index] = value
  
  // Move to next input if value entered
  if (value && index < 5) {
    otpInputs.value[index + 1]?.focus()
  }
  
  otpError.value = ''
}

const handleOtpKeydown = (event, index) => {
  // Handle backspace
  if (event.key === 'Backspace' && !otp.value[index] && index > 0) {
    otpInputs.value[index - 1]?.focus()
  }
}

const handleOtpPaste = (event) => {
  event.preventDefault()
  const pastedData = event.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
  
  if (pastedData.length === 6) {
    otp.value = pastedData.split('')
    otpInputs.value.forEach((input, index) => {
      if (input) input.value = otp.value[index]
    })
    otpInputs.value[5]?.focus()
  }
}

const handleOtpFocus = (index) => {
  // Select all text when focusing on input
  otpInputs.value[index]?.select()
}

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// Watch for modal visibility changes
watch(() => props.isVisible, (newValue) => {
  if (newValue) {
    resetForm()
  }
})
</script>

<style scoped>
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
  box-shadow: var(--shadow-xl);
  max-width: 480px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease-out;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 1.5rem 0;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-body {
  padding: 1.5rem;
}

.verification-content {
  text-align: center;
}

.verification-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--vt-c-white);
  margin-bottom: 1rem;
}

.verification-message {
  color: var(--color-text);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.verification-step {
  margin-bottom: 1rem;
}

.otp-instruction {
  color: var(--color-text);
  margin-bottom: 1rem;
  font-weight: 500;
}

.otp-inputs {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  margin-bottom: 1rem;
}

.otp-input {
  width: 40px;
  height: 40px;
  text-align: center;
  font-size: 1rem;
  font-weight: 600;
  border: 2px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  color: var(--color-text);
  transition: all var(--transition-fast);
}

.otp-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary), 0.1);
}

.otp-input--error {
  border-color: var(--color-error);
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.verification-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.verification-actions .btn {
  width: 100%;
  padding: 0.75rem 1rem;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 480px) {
  .modal-content {
    margin: 0.5rem;
  }
  
  .modal-header,
  .modal-body {
    padding: 1rem;
  }
  
  .otp-inputs {
    gap: 0.375rem;
  }
  
  .otp-input {
    width: 35px;
    height: 35px;
    font-size: 0.875rem;
  }
}
</style> 