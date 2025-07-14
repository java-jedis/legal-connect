<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">Update Case Status</h3>
        <button @click="closeModal" class="modal-close-btn">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      
      <form @submit.prevent="handleSubmit" class="modal-body">
        <!-- Current Status Display -->
        <div class="current-status-section">
          <label class="form-label">Current Status</label>
          <div class="current-status" :class="getStatusClass(caseData.status)">
            {{ getStatusDisplayName(caseData.status) }}
          </div>
        </div>

        <!-- New Status Selection -->
        <div class="form-group">
          <label for="status" class="form-label">New Status *</label>
          <div class="status-options">
            <label 
              v-for="status in availableStatuses" 
              :key="status.value"
              class="status-option"
              :class="{ 'selected': formData.status === status.value, 'disabled': status.value === caseData.status }"
            >
              <input
                type="radio"
                v-model="formData.status"
                :value="status.value"
                :disabled="status.value === caseData.status"
                class="status-radio"
              />
              <div class="status-display" :class="getStatusClass(status.value)">
                <div class="status-icon">
                  <svg v-if="status.value === 'IN_PROGRESS'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 11.08V12A10 10 0 1 1 5.68 3.57" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <div class="status-info">
                  <div class="status-name">{{ status.label }}</div>
                  <div class="status-description">{{ status.description }}</div>
                </div>
              </div>
            </label>
          </div>
          <span v-if="errors.status" class="error-message">{{ errors.status }}</span>
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
          <button type="submit" class="btn btn-primary" :disabled="loading || !hasChanges">
            <span v-if="loading" class="loading-spinner-sm"></span>
            {{ loading ? 'Updating...' : 'Update Status' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useCaseStore } from '../stores/case'

// Props
const props = defineProps({
  caseData: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['close', 'status-updated'])

// Store
const caseStore = useCaseStore()

// Reactive data
const loading = ref(false)
const submitError = ref('')

const formData = reactive({
  status: ''
})

const errors = reactive({
  status: ''
})

// Available statuses
const availableStatuses = [
  {
    value: 'IN_PROGRESS',
    label: 'In Progress',
    description: 'Case is actively being worked on'
  },
  {
    value: 'RESOLVED',
    label: 'Resolved',
    description: 'Case has been completed and closed'
  }
]

// Computed
const hasChanges = computed(() => {
  return formData.status !== props.caseData.status && formData.status !== ''
})

// Methods
const getStatusClass = (status) => {
  return {
    'status-in-progress': status === 'IN_PROGRESS',
    'status-resolved': status === 'RESOLVED'
  }
}

const getStatusDisplayName = (status) => {
  return status === 'IN_PROGRESS' ? 'In Progress' : 'Resolved'
}

const validateForm = () => {
  let isValid = true
  
  // Reset errors
  Object.keys(errors).forEach(key => {
    errors[key] = ''
  })
  
  // Status validation
  if (!formData.status) {
    errors.status = 'Please select a status'
    isValid = false
  } else if (formData.status === props.caseData.status) {
    errors.status = 'Please select a different status'
    isValid = false
  }
  
  return isValid
}

const handleSubmit = async () => {
  if (!validateForm() || !hasChanges.value) {
    return
  }
  
  loading.value = true
  submitError.value = ''
  
  try {
    const statusData = {
      status: formData.status
    }
    
    const updatedCase = await caseStore.updateCaseStatus(props.caseData.caseId, statusData)
    
    // Emit success event
    emit('status-updated', updatedCase)
    
  } catch (error) {
    console.error('Error updating case status:', error)
    submitError.value = error.response?.data?.message || 'Failed to update case status. Please try again.'
  } finally {
    loading.value = false
  }
}

const closeModal = () => {
  emit('close')
}

const initializeForm = () => {
  formData.status = props.caseData.status || ''
}

// Handle escape key
const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    closeModal()
  }
}

// Lifecycle
onMounted(() => {
  initializeForm()
  document.addEventListener('keydown', handleKeydown)
})

import { onUnmounted } from 'vue'

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

.current-status-section {
  margin-bottom: 2rem;
}

.current-status {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  display: inline-block;
}

.status-in-progress {
  background: #fef3c7;
  color: #92400e;
}

.status-resolved {
  background: #d1fae5;
  color: #065f46;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
}

.status-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.status-option {
  cursor: pointer;
  display: block;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.status-option.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.status-option:not(.disabled):hover .status-display {
  border-color: var(--color-brand);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-option.selected .status-display {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 2px rgba(var(--color-brand-rgb), 0.2);
}

.status-radio {
  display: none;
}

.status-display {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-background);
  transition: all 0.2s ease;
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-in-progress .status-icon {
  background: #fef3c7;
  color: #92400e;
}

.status-resolved .status-icon {
  background: #d1fae5;
  color: #065f46;
}

.status-icon svg {
  width: 20px;
  height: 20px;
}

.status-info {
  flex: 1;
}

.status-name {
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.status-description {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.error-message {
  display: block;
  color: #ef4444;
  font-size: 0.75rem;
  margin-top: 0.5rem;
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
  
  .status-display {
    flex-direction: column;
    text-align: center;
    gap: 0.75rem;
  }
  
  .status-info {
    text-align: center;
  }
}
</style> 