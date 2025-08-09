<template>
  <div class="settings-page">
    <div class="settings-container">
      <div class="settings-card">
        <div class="settings-header">
          <h1 class="settings-title">Settings</h1>
          <p class="settings-subtitle">Manage your account and change your password.</p>
        </div>
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
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { computed, reactive, ref } from 'vue'

const authStore = useAuthStore()
const isSubmitting = ref(false)
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const formError = ref('')
const formSuccess = ref('')

const form = reactive({
  oldPassword: '',
  password: '',
  confirmPassword: ''
})

const passwordErrors = ref([])
const confirmPasswordError = ref('')

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
</script>

<style scoped>
.settings-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--color-background-soft);
  padding: 2rem;
}
.settings-container {
  width: 100%;
  max-width: 420px;
}
.settings-card {
  background: var(--color-background);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}
.settings-header {
  text-align: center;
  margin-bottom: 2rem;
}
.settings-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}
.settings-subtitle {
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
</style> 