'''<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- User Type Selection -->
      <div v-if="!userType" class="user-type-selection animate-fade-in">
        <div class="auth-header">
          <h1 class="auth-title">Join LegalConnect</h1>
          <p class="auth-subtitle">
            Your journey towards streamlined legal solutions starts here. Let's personalize your
            experience.
          </p>
        </div>
        <div class="user-type-cards">
          <div class="user-type-card" @click="selectUserType('user')">
            <div class="user-type-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
              </svg>
            </div>
            <h3 class="user-type-title">I Need Legal Help</h3>
            <p class="user-type-description">
              Get AI-powered legal guidance and connect with expert lawyers.
            </p>
            <ul class="user-benefits">
              <li>Free AI legal assistant</li>
              <li>Connect with verified lawyers</li>
              <li>Secure document management</li>
            </ul>
          </div>
          <div class="user-type-card" @click="selectUserType('lawyer')">
            <div class="user-type-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
              </svg>
            </div>
            <h3 class="user-type-title">I'm a Lawyer</h3>
            <p class="user-type-description">
              Join our network of verified legal professionals.
            </p>
            <ul class="user-benefits">
              <li>Access to AI-powered tools</li>
              <li>Client management platform</li>
              <li>Grow your practice</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Registration Form -->
      <div v-else class="auth-card animate-fade-in">
        <div class="auth-header">
          <button class="back-button" @click="userType = ''">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            Back
          </button>
          <h1 class="auth-title">
            {{ userType === 'user' ? 'Create Your Account' : 'Lawyer Registration' }}
          </h1>
          <p class="auth-subtitle">Complete the form below to get started.</p>
        </div>

        <form class="auth-form" @submit.prevent="submitRegistration">
          <fieldset class="form-section">
            <legend>Basic Information</legend>
            <div class="grid grid-2">
              <div class="form-group">
                <label for="firstName">First Name</label>
                <input
                  type="text"
                  id="firstName"
                  v-model="form.firstName"
                  required
                  placeholder="John"
                />
              </div>
              <div class="form-group">
                <label for="lastName">Last Name</label>
                <input
                  type="text"
                  id="lastName"
                  v-model="form.lastName"
                  required
                  placeholder="Doe"
                />
              </div>
            </div>
            <div class="form-group">
              <label for="email">Email Address</label>
              <input
                type="email"
                id="email"
                v-model="form.email"
                required
                placeholder="name@example.com"
                @blur="validateEmail"
              />
              <p v-if="errors.email" class="error-message">{{ errors.email }}</p>
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
                  placeholder="Create a strong password"
                  @input="validatePassword"
                />
                <button 
                  type="button" 
                  class="password-toggle"
                  @click="showPassword = !showPassword"
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
              <p v-if="errors.password" class="error-message">{{ errors.password }}</p>
            </div>
            <div class="form-group">
              <label for="confirmPassword">Confirm Password</label>
              <div class="input-group">
                <span class="input-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                </span>
                <input
                  :type="showConfirmPassword ? 'text' : 'password'"
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  required
                  placeholder="Confirm your password"
                  @input="validateConfirmPassword"
                />
                <button 
                  type="button" 
                  class="password-toggle"
                  @click="showConfirmPassword = !showConfirmPassword"
                >
                  <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                </button>
              </div>
              <small v-if="confirmPasswordError" class="error-text">{{ confirmPasswordError }}</small>
            </div>
          </fieldset>

          <div class="form-group checkbox-group">
            <label class="checkbox-item">
              <input type="checkbox" v-model="form.agreeToTerms" required />
              <span class="checkmark"></span>
              I agree to the <a href="#" class="link">Terms of Service</a> and
              <a href="#" class="link">Privacy Policy</a>
            </label>
            <label class="checkbox-item" v-if="userType === 'lawyer'">
              <input type="checkbox" v-model="form.agreeToVerification" required />
              <span class="checkmark"></span>
              I consent to background and document verification.
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting || !isFormValid">
              <span v-if="isSubmitting" class="loading-spinner"></span>
              {{ isSubmitting ? 'Submitting...' : 'Create Account' }}
            </button>
          </div>
        </form>

        <div class="auth-footer">
          <p>
            Already have an account?
            <router-link to="/login" class="link font-weight-bold">Sign In</router-link>
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
import { showErrorAlert } from '../utils/errorHandler'

const router = useRouter()
const authStore = useAuthStore()
const userType = ref('')
const isSubmitting = ref(false)

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeToTerms: false,
  agreeToVerification: false,
})

const errors = reactive({
  email: '',
  password: '',
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordErrors = ref([])
const confirmPasswordError = ref('')

const selectUserType = (type) => {
  userType.value = type
}

const validateEmail = () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!form.email) {
    errors.email = 'Email is required.';
  } else if (!emailRegex.test(form.email)) {
    errors.email = 'Please enter a valid email address.';
  } else {
    errors.email = '';
  }
};

  const validatePassword = () => {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!*()_\-[\]{}|;:,.<>?])[A-Za-z\d@#$%^&+=!*()_\-[\]{}|;:,.<>?]{8,100}$/;
    if (!form.password) {
      errors.password = 'Password is required.';
    } else if (form.password.length < 8) {
      errors.password = 'Password must be at least 8 characters long.';
    } else if (form.password.length > 100) {
      errors.password = 'Password must be no more than 100 characters long.';
    } else if (!passwordRegex.test(form.password)) {
      errors.password =
        'Password must contain one uppercase, one lowercase, one number, and one special character.';
    } else {
      errors.password = '';
    }

    passwordErrors.value = []
    if (!form.password) {
      passwordErrors.value.push('Password is required.')
    }
    if (form.password.length < 8) {
      passwordErrors.value.push('Password must be at least 8 characters long.')
    }
    if (form.password.length > 100) {
      passwordErrors.value.push('No more than 100 characters')
    }
    if (!/(?=.*[a-z])/.test(form.password)) {
      passwordErrors.value.push('At least one lowercase letter')
    }
    if (!/(?=.*[A-Z])/.test(form.password)) {
      passwordErrors.value.push('At least one uppercase letter')
    }
    if (!/(?=.*\d)/.test(form.password)) {
      passwordErrors.value.push('At least one number')
    }
    if (!/[@#$%^&+=!*()_\-[\]{}|;:,.<>?]/.test(form.password)) {
      passwordErrors.value.push('At least one special character (@ # $ % ^ & + = ! * ( ) _ - [ ] { } | ; : , . < > ?)')
    }
    if (/\s/.test(form.password)) {
      passwordErrors.value.push('No spaces allowed')
    }
  };

const validateConfirmPassword = () => {
  if (form.password !== form.confirmPassword) {
    confirmPasswordError.value = 'Passwords do not match.'
  } else {
    confirmPasswordError.value = ''
  }
}

const isFormValid = computed(() => {
  return (
    !errors.email &&
    !errors.password &&
    form.firstName &&
    form.lastName &&
    form.email &&
    form.password &&
    form.confirmPassword &&
    form.agreeToTerms &&
    (userType.value === 'user' || form.agreeToVerification) &&
    !confirmPasswordError.value
  )
})

const submitRegistration = async () => {
  validateEmail()
  validatePassword()
  validateConfirmPassword()

  if (!isFormValid.value) {
    return
  }

  isSubmitting.value = true
  try {
    const userData = {
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      password: form.password,
      role: userType.value.toUpperCase(), // Convert to uppercase to match backend enum
    }

    const result = await authStore.register(userData)

    if (result.success) {
      const userInfo = authStore.getUserInfo()
      
      // Reset form
      form.firstName = ''
      form.lastName = ''
      form.email = ''
      form.password = ''
      form.confirmPassword = ''
      form.agreeToTerms = false
      form.agreeToVerification = false
      userType.value = ''

      // Check if email is verified
      if (!userInfo.emailVerified) {
        router.push('/email-verification')
      } else {
        // Redirect to appropriate dashboard
        if (authStore.isLawyer()) {
          router.push('/dashboard/lawyer')
        } else {
          router.push('/dashboard/user')
        }
      }
    } else {
      showErrorAlert(result.message || 'Registration failed. Please try again.')
    }
  } catch (error) {
    console.error('Registration error:', error)
    showErrorAlert('An error occurred during registration. Please try again.')
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
}

.auth-container {
  width: 100%;
  max-width: 800px;
}

/* User Type Selection */
.user-type-selection {
  text-align: center;
}

.user-type-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-top: 2.5rem;
}

.user-type-card {
  background: var(--color-background);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.user-type-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.user-type-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--vt-c-white);
  margin-bottom: 1.5rem;
}

.user-type-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.user-type-description {
  color: var(--color-text-muted);
  font-size: 1rem;
  line-height: 1.5;
  margin-bottom: 1.5rem;
}

.user-benefits {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: left;
  color: var(--color-text);
}

.user-benefits li {
  position: relative;
  padding-left: 1.5rem;
  margin-bottom: 0.75rem;
}

.user-benefits li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-success);
  font-weight: 600;
}

/* Auth Card */
.auth-card {
  background: var(--color-background);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  max-width: 520px;
  margin: 0 auto;
  position: relative;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.back-button {
  position: absolute;
  top: 1.5rem;
  left: 1.5rem;
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.back-button:hover {
  color: var(--color-primary);
}

.auth-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.auth-subtitle {
  color: var(--color-text-muted);
  max-width: 40ch;
  margin: 0 auto;
  line-height: 1.6;
}

/* Form Styles */
.form-section {
  border: none;
  padding: 0;
  margin: 0 0 2rem 0;
}

.form-section legend {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
  padding: 0;
}

.grid {
  display: grid;
  gap: 1.5rem;
}

.grid-2 {
  grid-template-columns: 1fr 1fr;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-heading);
  font-size: 0.875rem;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  color: var(--color-text);
  font-size: 1rem;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
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

.input-group input {
  padding-left: 3rem;
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

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary), 0.1);
}

.error-message {
  color: var(--color-error);
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin: 2rem 0;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 0.875rem;
  color: var(--color-text);
}

.checkbox-item input[type='checkbox'] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  margin-right: 0.75rem;
  position: relative;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.checkbox-item input[type='checkbox']:checked + .checkmark {
  background: var(--color-primary);
  border-color: var(--color-primary);
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

.auth-footer {
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

.font-weight-bold {
  font-weight: 700;
}

/* Animations */
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
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

@media (max-width: 768px) {
  .user-type-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .grid-2 {
    grid-template-columns: 1fr;
  }

  .auth-card,
  .user-type-card {
    padding: 1.5rem;
  }
}
</style>

''
