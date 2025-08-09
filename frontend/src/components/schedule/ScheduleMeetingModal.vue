<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>Schedule Meeting</h3>
        <button @click="closeModal" class="modal-close">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      
      <form @submit.prevent="submitMeeting" class="meeting-form">
        <div class="form-group">
          <label for="clientEmail">Client Email *</label>
          <input
            id="clientEmail"
            v-model="meetingForm.email"
            type="email"
            class="form-input"
            :class="{ 'error': formErrors.email }"
            placeholder="Enter client's email address"
            required
          />
          <span v-if="formErrors.email" class="error-message">{{ formErrors.email }}</span>
        </div>

        <div class="form-group">
          <label for="title">Meeting Title *</label>
          <input
            id="title"
            v-model="meetingForm.title"
            type="text"
            class="form-input"
            :class="{ 'error': formErrors.title }"
            placeholder="Enter meeting title"
            required
          />
          <span v-if="formErrors.title" class="error-message">{{ formErrors.title }}</span>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startDate">Start Date *</label>
            <input
              id="startDate"
              v-model="startDate"
              type="date"
              class="form-input"
              :class="{ 'error': formErrors.startDateTime }"
              :min="minDate"
              required
            />
          </div>

          <div class="form-group">
            <label for="startTime">Start Time *</label>
            <input
              id="startTime"
              v-model="startTime"
              type="time"
              class="form-input"
              :class="{ 'error': formErrors.startDateTime }"
              required
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="endDate">End Date *</label>
            <input
              id="endDate"
              v-model="endDate"
              type="date"
              class="form-input"
              :class="{ 'error': formErrors.endDateTime }"
              :min="minDate"
              required
            />
          </div>

          <div class="form-group">
            <label for="endTime">End Time *</label>
            <input
              id="endTime"
              v-model="endTime"
              type="time"
              class="form-input"
              :class="{ 'error': formErrors.endDateTime }"
              required
            />
          </div>
        </div>

        <div v-if="timeConflict" class="time-conflict-warning">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>End time must be after start time</span>
        </div>

        <!-- Server Error Banner -->
        <div v-if="submitError" class="error-alert">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span>{{ submitError }}</span>
        </div>

        <div class="form-actions">
          <button type="button" @click="closeModal" class="btn btn-outline">
            Cancel
          </button>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="isSubmitting || timeConflict"
          >
            <span v-if="isSubmitting" class="loading-spinner"></span>
            {{ isSubmitting ? 'Scheduling...' : 'Schedule Meeting' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { meetingAPI } from '@/services/api'
import { computed, reactive, ref, watch } from 'vue'

const emit = defineEmits(['close', 'meeting-scheduled'])

// State
const isSubmitting = ref(false)
const formErrors = reactive({})
const submitError = ref('')

// Form data
const meetingForm = reactive({
  email: '',
  title: '',
  startDateTime: '',
  endDateTime: ''
})

// Separate date and time inputs for better UX
const startDate = ref('')
const startTime = ref('')
const endDate = ref('')
const endTime = ref('')

// Computed properties
const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const timeConflict = computed(() => {
  if (!meetingForm.startDateTime || !meetingForm.endDateTime) return false
  return new Date(meetingForm.startDateTime) >= new Date(meetingForm.endDateTime)
})

// Watchers to combine date and time inputs
watch([startDate, startTime], ([date, time]) => {
  if (date && time) {
    meetingForm.startDateTime = `${date}T${time}:00`
  }
})

watch([endDate, endTime], ([date, time]) => {
  if (date && time) {
    meetingForm.endDateTime = `${date}T${time}:00`
  }
})

// Methods
const closeModal = () => {
  emit('close')
}

const validateForm = () => {
  Object.keys(formErrors).forEach(key => delete formErrors[key])

  if (!meetingForm.email) {
    formErrors.email = 'Client email is required'
  }

  if (!meetingForm.title) {
    formErrors.title = 'Meeting title is required'
  }

  if (!meetingForm.startDateTime) {
    formErrors.startDateTime = 'Start date and time are required'
  }

  if (!meetingForm.endDateTime) {
    formErrors.endDateTime = 'End date and time are required'
  }

  if (timeConflict.value) {
    formErrors.endDateTime = 'End time must be after start time'
  }

  // Check if meeting is in the past
  if (meetingForm.startDateTime && new Date(meetingForm.startDateTime) <= new Date()) {
    formErrors.startDateTime = 'Meeting cannot be scheduled in the past'
  }

  return Object.keys(formErrors).length === 0
}

const submitMeeting = async () => {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true
  submitError.value = ''

  try {
    // Convert to ISO format for API
    const meetingData = {
      email: meetingForm.email,
      title: meetingForm.title,
      startDateTime: new Date(meetingForm.startDateTime).toISOString(),
      endDateTime: new Date(meetingForm.endDateTime).toISOString()
    }

    await meetingAPI.scheduleMeeting(meetingData)
    emit('meeting-scheduled')
  } catch (err) {
    console.error('Error scheduling meeting:', err)
    const api = err?.response?.data
    const backendMessage = api?.error?.message || api?.message || err?.message || 'Failed to schedule meeting. Please try again.'

    // Map backend errors to inline field errors first
    const status = err?.response?.status
    const lowerMsg = (backendMessage || '').toLowerCase()
    if (status === 404 && lowerMsg.includes('client')) {
      formErrors.email = backendMessage
    } else if (lowerMsg.includes('email')) {
      formErrors.email = backendMessage
    } else if (lowerMsg.includes('time')) {
      formErrors.startDateTime = backendMessage
    } else {
      submitError.value = backendMessage
    }
  } finally {
    isSubmitting.value = false
  }
}

// Initialize with default values
const initializeForm = () => {
  const now = new Date()
  const tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  // Set default start time to 9 AM tomorrow
  const startDateTime = new Date(tomorrow)
  startDateTime.setHours(9, 0, 0, 0)
  
  // Set default end time to 10 AM tomorrow
  const endDateTime = new Date(tomorrow)
  endDateTime.setHours(10, 0, 0, 0)
  
  startDate.value = startDateTime.toISOString().split('T')[0]
  startTime.value = '09:00'
  endDate.value = endDateTime.toISOString().split('T')[0]
  endTime.value = '10:00'
  
  meetingForm.title = 'Legal Consultation'
}

// Initialize form when component mounts
initializeForm()
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
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.meeting-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

label {
  display: block;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.form-input.error {
  border-color: var(--color-error);
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.time-conflict-warning {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(var(--color-error-rgb), 0.1);
  border: 1px solid var(--color-error);
  border-radius: var(--border-radius);
  color: var(--color-error);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}

.loading-spinner {
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

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
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
  margin-bottom: 1rem;
}

.error-alert svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}
</style>