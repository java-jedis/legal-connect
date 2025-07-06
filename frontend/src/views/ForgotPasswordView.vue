<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1 class="auth-title">{{ currentStep === 'request' ? 'Forgot Password' : 'Reset Password' }}</h1>
          <p class="auth-subtitle">
            {{ currentStep === 'request' 
              ? 'Enter your email to receive a verification code.' 
              : 'Enter the verification code and your new password.' 
            }}
          </p>
        </div>

        <!-- Step 1: Request OTP -->
        <form v-if="currentStep === 'request'" class="auth-form" @submit.prevent="requestOTP">
          <div class="form-group">
            <label for="email">Email Address</label>
            <div class="input-group">
              <span class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
              </span>
              <input
                type="email"
                id="email"
                v-model="form.email"
                required
                placeholder="e.g., name@example.com"
                :disabled="isSubmitting"
              />
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="loading-spinner"></span>
              {{ isSubmitting ? 'Sending...' : 'Send Verification Code' }}
            </button>
          </div>
        </form>

        <!-- Step 2: Reset Password -->
        <form v-if="currentStep === 'reset'" class="auth-form" @submit.prevent="resetPassword">
          <div class="form-group">
            <label for="otp">Verification Code</label>
            <div class="input-group">
              <span class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><circle cx="12" cy="16" r="1"></circle><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
              </span>
              <input
                type="text"
                id="otp"
                v-model="form.otp"
                required
                maxlength="6"
                placeholder="Enter 6-digit code"
                :disabled="isSubmitting"
                @input="validateOTP"
              />
            </div>
            <small class="form-help">Enter the 6-digit code sent to your email</small>
            <p v-if="otpError" class="error-message">{{ otpError }}</p>
          </div>

          <div class="form-group">
            <label for="newPassword">New Password</label>
            <div class="input-group">
              <span class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
              </span>
              <input
                :type="showPassword ? 'text' : 'password'"
                id="newPassword"
                v-model="form.newPassword"
                required
                placeholder="Enter new password"
                :disabled="isSubmitting"
                @input="validatePassword"
              />
              <button 
                type="button" 
                class="password-toggle"
                @click="showPassword = !showPassword"
                :disabled="isSubmitting"
              >
                <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
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
              <span class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
              </span>
              <input
                :type="showConfirmPassword ? 'text' : 'password'"
                id="confirmPassword"
                v-model="form.confirmPassword"
                required
                placeholder="Confirm new password"
                :disabled="isSubmitting"
                @input="validateConfirmPassword"
              />
              <button 
                type="button" 
                class="password-toggle"
                @click="showConfirmPassword = !showConfirmPassword"
                :disabled="isSubmitting"
              >
                <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
              </button>
            </div>
            <small v-if="confirmPasswordError" class="error-text">{{ confirmPasswordError }}</small>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting || !isFormValid">
              <span v-if="isSubmitting" class="loading-spinner"></span>
              {{ isSubmitting ? 'Resetting...' : 'Reset Password' }}
            </button>
          </div>

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
        </form>

        <div class="auth-footer">
          <p v-if="currentStep === 'request'">
            Remember your password?
            <router-link to="/login" class="link font-weight-bold">Sign in</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { showErrorAlert, showSuccessAlert } from '../utils/errorHandler'

const router = useRouter()
const authStore = useAuthStore()
const isSubmitting = ref(false)
const isResending = ref(false)
const currentStep = ref('request')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordErrors = ref([])
const confirmPasswordError = ref('')
const otpError = ref('')
const countdown = ref(0)

const form = reactive({
  email: '',
  otp: '',
  newPassword: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  if (currentStep.value === 'request') {
    return form.email && form.email.includes('@')
  }
  
  if (currentStep.value === 'reset') {
    return form.otp && 
           form.otp.length === 6 && 
           form.newPassword && 
           form.confirmPassword && 
           passwordErrors.value.length === 0 && 
           !confirmPasswordError.value
  }
  
  return false
})

const validateOTP = () => {
  form.otp = form.otp.replace(/\D/g, '').slice(0, 6)
}

const validatePassword = () => {
  const errors = []
  
  if (form.newPassword.length < 8) {
    errors.push('At least 8 characters long')
  }
  
  if (!/(?=.*[a-z])/.test(form.newPassword)) {
    errors.push('At least one lowercase letter')
  }
  
  if (!/(?=.*[A-Z])/.test(form.newPassword)) {
    errors.push('At least one uppercase letter')
  }
  
  if (!/(?=.*\d)/.test(form.newPassword)) {
    errors.push('At least one number')
  }
  
  if (!/(?=.*[@$!%*?&])/.test(form.newPassword)) {
    errors.push('At least one special character (@$!%*?&)')
  }
  
  passwordErrors.value = errors
  validateConfirmPassword()
}

const validateConfirmPassword = () => {
  if (form.confirmPassword && form.newPassword !== form.confirmPassword) {
    confirmPasswordError.value = 'Passwords do not match'
  } else {
    confirmPasswordError.value = ''
  }
}

const requestOTP = async () => {
  isSubmitting.value = true

  try {
    const result = await authStore.sendPasswordResetOTP(form.email)

    if (result.success) {
      showSuccessAlert('Verification code sent to your email')
      currentStep.value = 'reset'
      startCountdown()
    } else {
      showErrorAlert(new Error(result.message || 'Failed to send verification code'))
    }
  } catch (error) {
    console.error('Request OTP error:', error)
    showErrorAlert(error)
  } finally {
    isSubmitting.value = false
  }
}

const resendOTP = async () => {
  if (countdown.value > 0) return

  isResending.value = true
  try {
    const result = await authStore.sendPasswordResetOTP(form.email)

    if (result.success) {
      showSuccessAlert('Verification code resent to your email')
      startCountdown()
    } else {
      showErrorAlert(new Error(result.message || 'Failed to resend verification code'))
    }
  } catch (error) {
    console.error('Resend OTP error:', error)
    showErrorAlert(error)
  } finally {
    isResending.value = false
  }
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

const resetPassword = async () => {
  if (!isFormValid.value) {
    otpError.value = 'Please enter the complete verification code'
    return
  }

  isSubmitting.value = true
  otpError.value = ''

  try {
    const resetData = {
      email: form.email,
      otp: form.otp,
      password: form.newPassword
    }

    const result = await authStore.resetPassword(resetData)

    if (result.success) {
      showSuccessAlert('Password reset successfully! You can now sign in with your new password.')
      router.push('/login')
    } else {
      otpError.value = result.message || 'Failed to reset password'
    }
  } catch (error) {
    console.error('Reset password error:', error)
    otpError.value = 'An unexpected error occurred. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--color-background-soft);
  padding: 2rem;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-container {
  width: 100%;
  max-width: 420px;
}

.auth-card {
  background: var(--color-background);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  animation: slideInUp 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.1s both;
}

.auth-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.auth-subtitle {
  color: var(--color-text-muted);
}

.auth-form {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

.form-group {
  margin-bottom: 1.5rem;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-heading);
}

.form-label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 500;
  color: var(--color-heading);
  text-align: center;
}

.input-group {
  position: relative;
}

.input-icon {
  position: absolute;
  top: 50%;
  left: 1rem;
  transform: translateY(-50%);
  color: var(--color-text-muted);
  pointer-events: none;
  transition: color 0.2s ease;
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-alpha);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-help {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  text-align: center;
  margin-top: 0.5rem;
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

.requirement-item::before {
  content: "•";
  color: var(--color-warning);
  font-weight: bold;
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

.error-text {
  color: var(--color-error);
  font-size: 0.875rem;
}

.form-actions {
  margin-top: 2rem;
}

.btn {
  width: 100%;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: var(--border-radius);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.verification-options {
  text-align: center;
  margin-top: 1.5rem;
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

.auth-footer {
  margin-top: 2rem;
  text-align: center;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.3s both;
}

.auth-footer p {
  color: var(--color-text-muted);
  margin: 0;
}

.link {
  color: var(--color-primary);
  text-decoration: none;
  transition: color 0.2s ease;
}

.link:hover {
  color: var(--color-primary-dark);
  text-decoration: underline;
}

.font-weight-bold {
  font-weight: 600;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive adjustments */
@media (max-width: 480px) {
  .auth-page {
    padding: 1rem;
  }
  
  .auth-card {
    padding: 2rem 1.5rem;
  }
  
  .auth-title {
    font-size: 1.75rem;
  }
}
</style> 