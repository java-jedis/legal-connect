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
                type="password"
                id="password"
                v-model="form.password"
                required
                placeholder="Enter your password"
              />
            </div>
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
    await new Promise((resolve) => setTimeout(resolve, 1500))

    const testCredentials = {
      'user@test.com': { password: 'password123', type: 'user', name: 'John Doe' },
      'client@legalai.com': { password: 'client123', type: 'user', name: 'Jane Smith' },
      'lawyer@test.com': { password: 'lawyer123', type: 'lawyer', name: 'Sarah Johnson' },
      'attorney@legalconnect.com.bd': { password: 'attorney123', type: 'lawyer', name: 'Michael Chen' },
    }

    const userCreds = testCredentials[form.email]

    if (userCreds && userCreds.password === form.password) {
      authStore.login({
        type: userCreds.type,
        name: userCreds.name,
        email: form.email,
      })

      alert(
        `Welcome back, ${userCreds.name}! You have been successfully signed in as a ${userCreds.type}.`,
      )

      form.email = ''
      form.password = ''
      form.rememberMe = false

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
  max-width: 420px;
}

.auth-card {
  background: var(--color-background);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  animation: fadeIn 0.5s ease-out;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
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

.form-group {
  margin-bottom: 1.5rem;
}

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
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  color: var(--color-text);
  font-size: 1rem;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary), 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  font-size: 0.875rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
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
</style>
