<template>
  <div class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <div class="modal-header-content">
          <div class="modal-icon" :class="`icon-${form.type}`">
            <svg viewBox="0 0 20 20" fill="currentColor"><path d="M5.75 3.5a.75.75 0 00-1.5 0v1.5H3a.75.75 0 000 1.5h1.25v1.5a.75.75 0 001.5 0v-1.5h1.5a.75.75 0 000-1.5H5.75V3.5zM11.5 6.25a.75.75 0 00-1.5 0v1.5h-1.5a.75.75 0 000 1.5h1.5v1.5a.75.75 0 001.5 0v-1.5h1.5a.75.75 0 000-1.5h-1.5V6.25zM4 10.75a.75.75 0 01.75-.75h10.5a.75.75 0 010 1.5H4.75a.75.75 0 01-.75-.75zM4.75 14a.75.75 0 000 1.5h6.5a.75.75 0 000-1.5h-6.5z"></path></svg>
          </div>
          <h3 class="modal-title">{{ mode === 'edit' ? 'Edit Event' : 'Create Event' }}</h3>
        </div>
        <button class="modal-close" @click="closeModal">&times;</button>
      </div>
      <form @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-group">
          <label for="title">Title *</label>
          <input id="title" v-model="form.title" required maxlength="255" class="form-input" />
        </div>
        <div class="form-group">
          <label for="type">Type *</label>
          <select id="type" v-model="form.type" required class="form-select">
            <option v-for="type in scheduleTypes" :key="type.value" :value="type.value">
              {{ type.label }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" v-model="form.description" maxlength="2000" class="form-textarea" />
        </div>
        <div class="form-group">
          <label for="date">Date *</label>
          <input id="date" type="date" v-model="form.date" required class="form-input" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="startTime">Start Time *</label>
            <div class="time-picker-group">
              <select v-model="startHour" class="time-select">
                <option v-for="h in 12" :key="h" :value="h">{{ h }}</option>
              </select>
              :
              <select v-model="startMinute" class="time-select">
                <option v-for="m in minuteOptions" :key="m" :value="m">{{ m.toString().padStart(2, '0') }}</option>
              </select>
              <select v-model="startPeriod" class="time-select">
                <option value="AM">AM</option>
                <option value="PM">PM</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label for="endTime">End Time *</label>
            <div class="time-picker-group">
              <select v-model="endHour" class="time-select">
                <option v-for="h in 12" :key="h" :value="h">{{ h }}</option>
              </select>
              :
              <select v-model="endMinute" class="time-select">
                <option v-for="m in minuteOptions" :key="m" :value="m">{{ m.toString().padStart(2, '0') }}</option>
              </select>
              <select v-model="endPeriod" class="time-select">
                <option value="AM">AM</option>
                <option value="PM">PM</option>
              </select>
            </div>
          </div>
        </div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div class="modal-actions">
          <button v-if="mode === 'edit'" type="button" class="btn btn-danger" @click="emitDelete" :disabled="loading">
            Delete
          </button>
          <div class="main-actions">
            <button type="button" class="btn btn-outline" @click="closeModal">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ mode === 'edit' ? 'Update Event' : 'Create Event' }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { scheduleAPI } from '../services/api'

const props = defineProps({
  mode: { type: String, required: true },
  eventData: { type: Object, default: null },
  caseId: { type: String, required: true }
})
const emit = defineEmits(['event-saved', 'close', 'delete-event'])

const loading = ref(false)
const error = ref('')

const scheduleTypes = [
  { value: 'MEETING', label: 'Meeting' },
  { value: 'COURT_HEARING', label: 'Court Hearing' },
  { value: 'DOCUMENT_SUBMISSION_DEADLINE', label: 'Document Submission Deadline' },
  { value: 'PAYMENT_DUE_DATE', label: 'Payment Due Date' },
  { value: 'FOLLOW_UP_CALL', label: 'Follow Up Call' },
  { value: 'MEDIATION_SESSION', label: 'Mediation Session' },
  { value: 'ARBITRATION_SESSION', label: 'Arbitration Session' },
  { value: 'LEGAL_NOTICE_RESPONSE_DEADLINE', label: 'Legal Notice Response Deadline' },
  { value: 'CONTRACT_SIGNING', label: 'Contract Signing' },
  { value: 'DISCOVERY_DATE', label: 'Discovery Date' },
  { value: 'DEPOSITION_DATE', label: 'Deposition Date' },
  { value: 'EVIDENCE_COLLECTION_REMINDER', label: 'Evidence Collection Reminder' },
  { value: 'LEGAL_ADVICE_SESSION', label: 'Legal Advice Session' },
  { value: 'BAIL_HEARING', label: 'Bail Hearing' },
  { value: 'PROBATION_MEETING', label: 'Probation Meeting' },
  { value: 'PAROLE_MEETING', label: 'Parole Meeting' },
  { value: 'COMPLIANCE_DEADLINE', label: 'Compliance Deadline' }
]

const form = ref({
  title: '',
  type: scheduleTypes[0].value,
  description: '',
  date: new Date().toISOString().split('T')[0],
})

// Time picker state
const minuteOptions = [0, 15, 30, 45]
const startHour = ref(9)
const startMinute = ref(0)
const startPeriod = ref('AM')
const endHour = ref(10)
const endMinute = ref(0)
const endPeriod = ref('AM')

const to24Hour = (hour, minute, period) => {
  let h = parseInt(hour)
  if (period === 'PM' && h !== 12) h += 12
  if (period === 'AM' && h === 12) h = 0
  return `${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}

const startTime24 = computed(() => to24Hour(startHour.value, startMinute.value, startPeriod.value))
const endTime24 = computed(() => to24Hour(endHour.value, endMinute.value, endPeriod.value))

watch(() => props.eventData, (val) => {
  if (props.mode === 'edit' && val && val.start) {
    const startDate = new Date(val.start)
    const endDate = val.end ? new Date(val.end) : new Date(startDate.getTime() + 60 * 60 * 1000)

    form.value = {
      title: val.title || '',
      type: val.extendedProps?.type || scheduleTypes[0].value,
      description: val.extendedProps?.description || '',
      date: startDate.toISOString().split('T')[0],
    }

    const startH = startDate.getHours()
    startMinute.value = startDate.getMinutes()
    startPeriod.value = startH >= 12 ? 'PM' : 'AM'
    startHour.value = startH % 12 || 12

    const endH = endDate.getHours()
    endMinute.value = endDate.getMinutes()
    endPeriod.value = endH >= 12 ? 'PM' : 'AM'
    endHour.value = endH % 12 || 12

  } else {
    const now = new Date()
    now.setDate(now.getDate() + 1)
    form.value = {
      title: '',
      type: scheduleTypes[0].value,
      description: '',
      date: now.toISOString().split('T')[0],
    }
    startHour.value = 9
    startMinute.value = 0
    startPeriod.value = 'AM'
    endHour.value = 10
    endMinute.value = 0
    endPeriod.value = 'AM'
  }
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
      modal.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
}, { immediate: true, deep: true })

function closeModal() {
  emit('close')
}

async function handleSubmit() {
  error.value = ''
  loading.value = true

  const startDateTime = new Date(`${form.value.date}T${startTime24.value}`)
  const endDateTime = new Date(`${form.value.date}T${endTime24.value}`)

  if (endDateTime <= startDateTime) {
    error.value = 'End time must be after start time.'
    loading.value = false
    return
  }

  try {
    const payload = {
      title: form.value.title,
      type: form.value.type,
      description: form.value.description,
      date: form.value.date,
      startTime: startDateTime.toISOString(),
      endTime: endDateTime.toISOString()
    }

    if (props.mode === 'create') {
      await scheduleAPI.createSchedule({ ...payload, caseId: props.caseId })
    } else {
      await scheduleAPI.updateSchedule(props.eventData.id, payload)
    }
    emit('event-saved')
    closeModal()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to save event'
  } finally {
    loading.value = false
  }
}

function emitDelete() {
  emit('delete-event', props.eventData)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
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
  max-width: 550px; /* Increased width */
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
}
.modal-header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.modal-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
/* Colors for different event types */
.icon-MEETING { background: #3b82f6; }
.icon-COURT_HEARING { background: #ef4444; }
.icon-DOCUMENT_SUBMISSION_DEADLINE { background: #f97316; }
.icon-PAYMENT_DUE_DATE { background: #84cc16; }
.icon-FOLLOW_UP_CALL { background: #14b8a6; }
.icon-MEDIATION_SESSION { background: #6366f1; }
.icon-ARBITRATION_SESSION { background: #8b5cf6; }
.icon-LEGAL_NOTICE_RESPONSE_DEADLINE { background: #d946ef; }
.icon-CONTRACT_SIGNING { background: #06b6d4; }
.icon-DISCOVERY_DATE { background: #f59e0b; }
.icon-DEPOSITION_DATE { background: #6b7280; }
.icon-EVIDENCE_COLLECTION_REMINDER { background: #10b981; }
.icon-LEGAL_ADVICE_SESSION { background: #a855f7; }
.icon-BAIL_HEARING { background: #ec4899; }
.icon-PROBATION_MEETING { background: #78716c; }
.icon-PAROLE_MEETING { background: #4ade80; }
.icon-COMPLIANCE_DEADLINE { background: #f43f5e; }

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}
.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  line-height: 1;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: background-color 0.2s;
}
.modal-close:hover {
  background-color: var(--color-background-soft);
}
.modal-form {
  padding: 1.5rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
.form-group {
  margin-bottom: 1.25rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-text);
}
.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 1rem;
  background: var(--color-background-soft);
  color: var(--color-text);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(var(--color-brand-rgb), 0.2);
}
.form-textarea {
  min-height: 100px;
  resize: vertical;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}
.time-picker-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.time-select {
  flex: 1;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  font-size: 1rem;
  background: var(--color-background-soft);
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
}
.modal-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto; /* Pushes to the bottom */
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border);
}
.main-actions {
  display: flex;
  gap: 1rem;
}
.btn {
  padding: 0.6rem 1.25rem;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary {
  background: var(--color-primary);
  color: var(--vt-c-white);
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  filter: brightness(1.1);
}
.btn-outline {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
.btn-outline:hover { background: var(--color-background-soft); }
.btn-danger {
  background: #fee2e2;
  color: #ef4444;
  border: 1px solid #fee2e2;
}
.btn-danger:hover { background: #fecaca; }
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error-message {
  color: #ef4444;
  background: #fee2e2;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  margin-bottom: 1.25rem;
  text-align: center;
  font-weight: 500;
}

/* Light mode specific styles */
@media (prefers-color-scheme: light) {
  .btn-primary {
    color: var(--vt-c-white);
  }
}
</style> 