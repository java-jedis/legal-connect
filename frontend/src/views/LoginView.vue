<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1 class="auth-title">Welcome Back</h1>
          <p class="auth-subtitle">Sign in to continue to your dashboard.</p>
        </div>

        <form class="auth-form" @submit.prevent="submitLogin">
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
                :class="{ 'input-error': hasError }"
                @input="clearError"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <div class="input-group">
              <span class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
              </span>
              <input
                :type="showPassword ? 'text' : 'password'"
                id="password"
                v-model="form.password"
                required
                placeholder="Enter your password"
                :disabled="isSubmitting"
                :class="{ 'input-error': hasError }"
                @input="clearError"
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
          </div>

          <!-- Generic error message for credentials -->
          <div v-if="hasError" class="credentials-error">
            <p class="error-message">Invalid email address or wrong password</p>
          </div>

          <div class="form-options">
            <label class="checkbox-item">
              <input type="checkbox" v-model="form.rememberMe" :disabled="isSubmitting" />
              <span class="checkmark"></span>
              Remember me
            </label>
            <router-link to="/forgot-password" class="forgot-password-link">
              Forgot password?
            </router-link>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="loading-spinner"></span>
              {{ isSubmitting ? 'Signing In...' : 'Sign In' }}
            </button>
          </div>
        </form>

        <div class="auth-footer">
          <p>
            Don't have an account?
            <router-link to="/register" class="link font-weight-bold">Sign up</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isSubmitting = ref(false)
const showPassword = ref(false)
const hasError = ref(false)

const form = reactive({
  email: '',
  password: '',
  rememberMe: false,
})

const clearError = () => {
  hasError.value = false
}

const submitLogin = async () => {
  // Clear previous errors
  hasError.value = false
  
  isSubmitting.value = true

  try {
    const result = await authStore.login({
      email: form.email,
      password: form.password,
    })

    if (result.success) {
      const userInfo = authStore.getUserInfo()

      form.email = ''
      form.password = ''
      form.rememberMe = false

      if (!userInfo.emailVerified) {
        router.push('/email-verification')
      } else {
        if (authStore.isLawyer()) {
          router.push('/dashboard/lawyer')
        } else {
          router.push('/dashboard/user')
        }
      }
    } else {
      // Show generic error for any login failure
      hasError.value = true
    }
  } catch (error) {
    console.error('Login error:', error)
    // Handle network or unexpected errors
    hasError.value = true
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

.form-group:nth-child(1) { animation-delay: 0.3s; }
.form-group:nth-child(2) { animation-delay: 0.4s; }

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-heading);
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
  background: var(--color-background-soft);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary), 0.1);
  background: var(--color-background);
}

.form-group input:focus + .input-icon {
  color: var(--color-primary);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-group input.input-error {
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px rgba(var(--color-error), 0.1);
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-top: 0.5rem;
  animation: fadeIn 0.3s ease-in;
}

.credentials-error {
  margin-bottom: 1.5rem;
  padding: 0.75rem;
  background: rgba(var(--color-error), 0.05);
  border: 1px solid rgba(var(--color-error), 0.2);
  border-radius: var(--border-radius);
  animation: fadeIn 0.3s ease-in;
}

.credentials-error .error-message {
  margin: 0;
  text-align: center;
  font-weight: 500;
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  font-size: 0.875rem;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.5s both;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--color-text);
  transition: opacity 0.2s ease;
}

.checkbox-item:has(input:disabled) {
  opacity: 0.6;
  cursor: not-allowed;
}

.checkbox-item input[type='checkbox'] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  margin-right: 0.5rem;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.checkbox-item input[type='checkbox']:checked + .checkmark {
  background: var(--color-primary);
  border-color: var(--color-primary);
  transform: scale(1.1);
}

.checkbox-item input[type='checkbox']:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--color-background);
  font-size: 10px;
  font-weight: bold;
}

.forgot-password-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.forgot-password-link:hover {
  text-decoration: underline;
  color: var(--color-secondary);
}

.form-actions {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.6s both;
}

.form-actions .btn {
  width: 100%;
  padding: 0.875rem;
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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-footer {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border);
  font-size: 0.875rem;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.7s both;
}

.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.link:hover {
  text-decoration: underline;
  color: var(--color-secondary);
}

.font-weight-bold {
  font-weight: 700;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(50px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 480px) {
  .auth-page {
    padding: 1rem;
  }
  
  .auth-card {
    padding: 1.5rem;
  }
  
  .auth-title {
    font-size: 1.75rem;
  }
}
</style>
