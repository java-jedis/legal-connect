<template>
  <div class="verification-page">
    <div class="verification-container">
      <div class="verification-card">
        <div class="verification-header">
          <div class="verification-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
              <polyline points="22,6 12,13 2,6"></polyline>
            </svg>
          </div>
          <h1 class="verification-title">Verify Your Email</h1>
          <p class="verification-subtitle">
            We've sent a verification code to <strong>{{ userEmail }}</strong>
          </p>
        </div>

        <!-- Step 1: Request OTP -->
        <div v-if="!otpRequested" class="verification-step">
          <p class="step-description">
            To complete your registration, please verify your email address. We'll send you a 6-digit verification code.
          </p>
          <div class="form-actions">
            <button 
              @click="requestOTP" 
              class="btn btn-primary" 
              :disabled="isRequesting"
            >
              <span v-if="isRequesting" class="loading-spinner"></span>
              {{ isRequesting ? 'Sending...' : 'Send Verification Code' }}
            </button>
          </div>
        </div>

        <!-- Step 2: Enter OTP -->
        <div v-else class="verification-step">
          <p class="step-description">
            Enter the 6-digit verification code sent to your email address.
          </p>
          
          <form @submit.prevent="verifyEmail" class="verification-form">
            <div class="otp-input-group">
              <label for="otp" class="form-label">Verification Code</label>
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
                  :disabled="isVerifying"
                />
              </div>
              <p v-if="otpError" class="error-message">{{ otpError }}</p>
            </div>

            <div class="form-actions">
              <button 
                type="submit" 
                class="btn btn-primary" 
                :disabled="isVerifying || !isOtpComplete"
              >
                <span v-if="isVerifying" class="loading-spinner"></span>
                {{ isVerifying ? 'Verifying...' : 'Verify Email' }}
              </button>
            </div>
          </form>

          <div class="verification-options">
            <p class="resend-text">
              Didn't receive the code?
              <button 
                @click="resendOTP" 
                class="resend-link"
                :disabled="isResending || countdown > 0"
              >
                {{ countdown > 0 ? `Resend in ${countdown}s` : 'Resend Code' }}
              </button>
            </p>
          </div>
        </div>

        <div class="verification-footer">
          <p>
            Having trouble? 
            <a href="#" class="link">Contact Support</a>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { authAPI } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { showErrorAlert } from '@/utils/errorHandler'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
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
      
      // Redirect to appropriate dashboard
      if (authStore.isLawyer()) {
        router.push('/dashboard/lawyer')
      } else {
        router.push('/dashboard/user')
      }
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

// Lifecycle
onMounted(() => {
  // Auto-request OTP if user is logged in but email not verified
  if (authStore.isLoggedIn && !authStore.userInfo?.emailVerified) {
    requestOTP()
  }
})
</script>

<style scoped>
.verification-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--color-background-soft);
  padding: 2rem;
}

.verification-container {
  width: 100%;
  max-width: 480px;
}

.verification-card {
  background: var(--color-background);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  animation: fadeIn 0.5s ease-out;
}

.verification-header {
  text-align: center;
  margin-bottom: 2rem;
}

.verification-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--vt-c-white);
  margin-bottom: 1.5rem;
}

.verification-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.verification-subtitle {
  color: var(--color-text-muted);
  line-height: 1.6;
}

.verification-step {
  margin-bottom: 2rem;
}

.step-description {
  color: var(--color-text);
  line-height: 1.6;
  margin-bottom: 1.5rem;
  text-align: center;
}

.verification-form {
  margin-bottom: 1.5rem;
}

.otp-input-group {
  margin-bottom: 2rem;
}

.form-label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 500;
  color: var(--color-heading);
  text-align: center;
}

.otp-inputs {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.otp-input {
  width: 50px;
  height: 50px;
  text-align: center;
  font-size: 1.25rem;
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
  text-align: center;
  margin-top: 0.5rem;
}

.form-actions {
  text-align: center;
}

.form-actions .btn {
  min-width: 200px;
  padding: 0.875rem 1.5rem;
  font-size: 1rem;
  position: relative;
  overflow: hidden;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.verification-options {
  text-align: center;
  margin-bottom: 1.5rem;
}

.resend-text {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.resend-link {
  background: none;
  border: none;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  margin: 0;
}

.resend-link:hover:not(:disabled) {
  text-decoration: underline;
}

.resend-link:disabled {
  color: var(--color-text-muted);
  cursor: not-allowed;
}

.verification-footer {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border);
  font-size: 0.875rem;
}

.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 480px) {
  .verification-card {
    padding: 1.5rem;
  }
  
  .otp-inputs {
    gap: 0.5rem;
  }
  
  .otp-input {
    width: 45px;
    height: 45px;
    font-size: 1.125rem;
  }
}
</style> 