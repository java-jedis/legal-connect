<template>
  <div class="login-page">
    <!-- Login Form -->
    <section class="login-section section">
      <div class="container">
        <div class="login-container">
          <div class="login-header">
            <h2 class="section-title">Sign In</h2>
            <p class="section-subtitle">
              Access your account and get back to managing your legal matters
            </p>
          </div>

          <form class="login-form" @submit.prevent="submitLogin">
            <div class="form-section">
              <div class="form-group">
                <label for="email">Email Address</label>
                <input
                  type="email"
                  id="email"
                  v-model="form.email"
                  required
                  placeholder="Enter your email address"
                />
              </div>

              <div class="form-group">
                <label for="password">Password</label>
                <input
                  type="password"
                  id="password"
                  v-model="form.password"
                  required
                  placeholder="Enter your password"
                />
              </div>

              <div class="form-options">
                <label class="checkbox-item">
                  <input type="checkbox" v-model="form.rememberMe" />
                  <span class="checkmark"></span>
                  Remember me
                </label>
                <a href="#" class="link">Forgot password?</a>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
                  {{ isSubmitting ? 'Signing In...' : 'Sign In' }}
                </button>
              </div>
            </div>
          </form>

          <!-- Register Link -->
          <div class="register-link">
            <p>
              Don't have an account?
              <router-link to="/register" class="link">Sign up here</router-link>
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isSubmitting = ref(false)

const form = reactive({
  email: '',
  password: '',
  rememberMe: false,
})

const submitLogin = async () => {
  isSubmitting.value = true

  try {
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1500))

    // Hardcoded test credentials
    const testCredentials = {
      // Normal user credentials
      'user@test.com': { password: 'password123', type: 'user', name: 'John Doe' },
      'client@legalai.com': { password: 'client123', type: 'user', name: 'Jane Smith' },

      // Lawyer credentials
      'lawyer@test.com': { password: 'lawyer123', type: 'lawyer', name: 'Sarah Johnson' },
      'attorney@legalai.com': { password: 'attorney123', type: 'lawyer', name: 'Michael Chen' },
    }

    const userCreds = testCredentials[form.email]

    if (userCreds && userCreds.password === form.password) {
      // Store user data in auth store
      authStore.login({
        type: userCreds.type,
        name: userCreds.name,
        email: form.email,
      })

      alert(
        `Welcome back, ${userCreds.name}! You have been successfully signed in as a ${userCreds.type}.`,
      )

      // Reset form
      form.email = ''
      form.password = ''
      form.rememberMe = false

      // Redirect to appropriate dashboard
      if (userCreds.type === 'lawyer') {
        router.push('/dashboard/lawyer')
      } else {
        router.push('/dashboard/user')
      }
    } else {
      alert('Invalid email or password. Please try again.')
    }
  } catch {
    alert('An error occurred. Please try again.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
}

.login-section {
  background: var(--color-background-soft);
}

.login-container {
  max-width: 500px;
  margin: 0 auto;
}

.login-header {
  text-align: center;
  margin-bottom: 3rem;
}

.form-section {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-heading);
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  color: var(--color-text);
  font-size: 1rem;
  transition: border-color var(--transition-fast);
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
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
  margin-right: 0.5rem;
  position: relative;
  transition: all var(--transition-fast);
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

.form-actions {
  text-align: center;
}

.register-link {
  text-align: center;
  padding-top: 2rem;
}

.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .form-options {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}
</style>
