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
              <input
                type="password"
                id="password"
                v-model="form.password"
                required
                placeholder="Create a strong password"
                @blur="validatePassword"
              />
              <p v-if="errors.password" class="error-message">{{ errors.password }}</p>
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
import { ref, reactive, computed } from 'vue'

const userType = ref('')
const isSubmitting = ref(false)

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  agreeToTerms: false,
  agreeToVerification: false,
})

const errors = reactive({
  email: '',
  password: '',
})

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
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  if (!form.password) {
    errors.password = 'Password is required.';
  } else if (form.password.length < 8) {
    errors.password = 'Password must be at least 8 characters long.';
  } else if (!passwordRegex.test(form.password)) {
    errors.password =
      'Password must contain one uppercase, one lowercase, one number, and one special character.';
  } else {
    errors.password = '';
  }
};

const isFormValid = computed(() => {
  return (
    !errors.email &&
    !errors.password &&
    form.firstName &&
    form.lastName &&
    form.email &&
    form.password &&
    form.agreeToTerms &&
    (userType.value === 'user' || form.agreeToVerification)
  )
})

const submitRegistration = async () => {
  validateEmail()
  validatePassword()

  if (!isFormValid.value) {
    return
  }

  isSubmitting.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 2000))
    alert(`Account created for ${form.firstName} as a ${userType.value}!`)
    // Reset form or redirect
  } catch (error) {
    alert('An error occurred.')
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
