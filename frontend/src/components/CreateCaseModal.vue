<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">Create New Case</h3>
        <button @click="closeModal" class="modal-close-btn">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      
      <form @submit.prevent="handleSubmit" class="modal-body">
        <!-- Title Field -->
        <div class="form-group">
          <label for="title" class="form-label">Case Title *</label>
          <input
            id="title"
            v-model="formData.title"
            type="text"
            class="form-input"
            :class="{ 'error': errors.title }"
            placeholder="Enter case title"
            maxlength="255"
            required
          />
          <span v-if="errors.title" class="error-message">{{ errors.title }}</span>
        </div>

        <!-- Client Email Field -->
        <div class="form-group">
          <label for="clientEmail" class="form-label">Client Email *</label>
          <input
            id="clientEmail"
            v-model="formData.clientEmail"
            type="email"
            class="form-input"
            :class="{ 'error': errors.clientEmail }"
            placeholder="Enter client's email address"
            required
          />
          <span v-if="errors.clientEmail" class="error-message">{{ errors.clientEmail }}</span>
          <span class="form-help">The client must have an existing account on the platform</span>
        </div>

        <!-- Description Field -->
        <div class="form-group">
          <label for="description" class="form-label">Case Description</label>
          <textarea
            id="description"
            v-model="formData.description"
            class="form-textarea"
            :class="{ 'error': errors.description }"
            placeholder="Enter case description (optional)"
            rows="4"
            maxlength="2000"
          ></textarea>
          <span v-if="errors.description" class="error-message">{{ errors.description }}</span>
          <span class="form-help">{{ formData.description.length }}/2000 characters</span>
        </div>

        <!-- Error Display -->
        <div v-if="submitError" class="error-alert">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span>{{ submitError }}</span>
        </div>

        <!-- Form Actions -->
        <div class="modal-actions">
          <button type="button" @click="closeModal" class="btn btn-secondary" :disabled="loading">
            Cancel
          </button>
          <button type="submit" class="btn btn-primary" :disabled="loading || !isFormValid">
            <span v-if="loading" class="loading-spinner-sm"></span>
            {{ loading ? 'Creating...' : 'Create Case' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useCaseStore } from '../stores/case'

// Emits
const emit = defineEmits(['close', 'case-created'])

// Store
const caseStore = useCaseStore()

// Reactive data
const loading = ref(false)
const submitError = ref('')

const formData = reactive({
  title: '',
  clientEmail: '',
  description: ''
})

const errors = reactive({
  title: '',
  clientEmail: '',
  description: ''
})

// Computed
const isFormValid = computed(() => {
  return formData.title.trim().length >= 2 && 
         formData.clientEmail.trim() !== '' && 
         isValidEmail(formData.clientEmail) &&
         formData.description.length <= 2000
})

// Methods
const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const validateForm = () => {
  let isValid = true
  
  // Reset errors
  Object.keys(errors).forEach(key => {
    errors[key] = ''
  })
  
  // Title validation
  if (!formData.title.trim()) {
    errors.title = 'Title is required'
    isValid = false
  } else if (formData.title.trim().length < 2) {
    errors.title = 'Title must be at least 2 characters long'
    isValid = false
  } else if (formData.title.length > 255) {
    errors.title = 'Title cannot exceed 255 characters'
    isValid = false
  }
  
  // Client email validation
  if (!formData.clientEmail.trim()) {
    errors.clientEmail = 'Client email is required'
    isValid = false
  } else if (!isValidEmail(formData.clientEmail)) {
    errors.clientEmail = 'Please enter a valid email address'
    isValid = false
  }
  
  // Description validation
  if (formData.description.length > 2000) {
    errors.description = 'Description cannot exceed 2000 characters'
    isValid = false
  }
  
  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  submitError.value = ''
  
  try {
    const caseData = {
      title: formData.title.trim(),
      clientEmail: formData.clientEmail.trim(),
      description: formData.description.trim() || undefined
    }
    
    const newCase = await caseStore.createCase(caseData)
    
    // Emit success event
    emit('case-created', newCase)
    
    // Reset form
    Object.keys(formData).forEach(key => {
      formData[key] = ''
    })
    
  } catch (error) {
    console.error('Error creating case:', error)
    submitError.value = error.response?.data?.message || 'Failed to create case. Please try again.'
  } finally {
    loading.value = false
  }
}

const closeModal = () => {
  emit('close')
}

// Handle escape key
const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    closeModal()
  }
}

// Add event listener when component mounts
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  // Focus the first input
  setTimeout(() => {
    const titleInput = document.getElementById('title')
    if (titleInput) {
      titleInput.focus()
    }
  }, 100)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
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
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 0;
  margin-bottom: 1.5rem;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.modal-close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  color: var(--color-text-muted);
  border-radius: 6px;
  transition: all 0.2s ease;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close-btn:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-close-btn svg {
  width: 18px;
  height: 18px;
}

.modal-body {
  padding: 0 1.5rem 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: var(--color-background);
  color: var(--color-text);
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(var(--color-brand-rgb), 0.1);
}

.form-input.error, .form-textarea.error {
  border-color: #ef4444;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-help {
  display: block;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.error-message {
  display: block;
  color: #ef4444;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.error-alert {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}

.error-alert svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.loading-spinner-sm {
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

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .error-alert {
    background: rgba(239, 68, 68, 0.1);
    border-color: rgba(239, 68, 68, 0.2);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 0.5rem;
  }
  
  .modal-content {
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 1rem 1rem 0;
    margin-bottom: 1rem;
  }
  
  .modal-body {
    padding: 0 1rem 1rem;
  }
  
  .modal-actions {
    flex-direction: column-reverse;
  }
  
  .modal-actions .btn {
    width: 100%;
  }
}
</style> 