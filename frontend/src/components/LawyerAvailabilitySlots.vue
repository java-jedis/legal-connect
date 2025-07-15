<template>
  <div class="availability-slots-section redesigned">
    <div class="section-header">
      <h3>Availability Slots</h3>
      <p class="section-description">Manage your available time slots for client consultations</p>
    </div>

    <!-- Add New Slot Button -->
    <div class="add-slot-section redesigned">
      <button @click="showAddModal = true" class="btn btn-primary">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
          <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        Add Availability Slot
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading availability slots...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
          <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <h4>Unable to load availability slots</h4>
      <p>{{ error }}</p>
      <button @click="fetchSlots" class="btn btn-primary">Try Again</button>
    </div>

    <!-- Content -->
    <div v-else class="slots-content redesigned">
      <!-- Slots List -->
      <div v-if="slots.length === 0" class="no-slots redesigned">
        <div class="no-slots-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h4>No availability slots</h4>
        <p>You haven't added any availability slots yet. Add your first slot to start receiving consultation requests.</p>
      </div>

      <div v-else class="slots-grid redesigned">
        <div v-for="slot in slots" :key="slot.id" class="slot-card redesigned">
          <div class="slot-header redesigned">
            <div class="slot-day">
              <span class="day-icon"><svg width="20" height="20" fill="none" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/><path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></span>
              <span class="day-name">{{ getDayDisplayName(slot.day) }}</span>
            </div>
            <div class="slot-actions">
              <button @click="editSlot(slot)" class="btn-icon" title="Edit slot">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              <button @click="deleteSlot(slot.id)" class="btn-icon delete" title="Delete slot">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <polyline points="3,6 5,6 21,6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>
          <div class="slot-time redesigned">
            <div class="time-range">
              <span class="time-icon"><svg width="18" height="18" fill="none" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></span>
              <span class="time-label">Time:</span>
              <span class="time-value">{{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</span>
            </div>
            <div class="duration">
              <span class="duration-label">Duration:</span>
              <span class="duration-value">{{ calculateDuration(slot.startTime, slot.endTime) }}</span>
            </div>
          </div>
        </div>
        <!-- Ghost cards for grid balance -->
        <div v-for="n in ghostCards" :key="'ghost-'+n" class="slot-card ghost"></div>
      </div>
    </div>

    <!-- Add/Edit Slot Modal -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showEditModal ? 'Edit' : 'Add' }} Availability Slot</h3>
          <button @click="closeModal" class="modal-close">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        
        <form @submit.prevent="submitSlot" class="slot-form">
          <div class="form-group">
            <label for="day">Day of Week *</label>
            <select
              id="day"
              v-model="slotForm.day"
              class="form-select"
              :class="{ 'error': formErrors.day }"
              required
            >
              <option value="">Select a day</option>
              <option v-for="day in daysOfWeek" :key="day.value" :value="day.value">
                {{ day.label }}
              </option>
            </select>
            <span v-if="formErrors.day" class="error-message">{{ formErrors.day }}</span>
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
              <span v-if="formErrors.startTime" class="error-message">{{ formErrors.startTime }}</span>
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
              <span v-if="formErrors.endTime" class="error-message">{{ formErrors.endTime }}</span>
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
              {{ isSubmitting ? (showEditModal ? 'Updating...' : 'Creating...') : (showEditModal ? 'Update Slot' : 'Create Slot') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content delete-modal" @click.stop>
        <div class="modal-header">
          <h3>Delete Availability Slot</h3>
          <button @click="closeDeleteModal" class="modal-close">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        
        <div class="delete-content">
          <div class="delete-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <h4>Are you sure?</h4>
          <p>This will permanently delete the availability slot for <strong>{{ getDayDisplayName(slotToDelete?.day) }}</strong> from <strong>{{ formatTime(slotToDelete?.startTime) }}</strong> to <strong>{{ formatTime(slotToDelete?.endTime) }}</strong>.</p>
          <p>This action cannot be undone.</p>
          
          <div class="delete-actions">
            <button @click="closeDeleteModal" class="btn btn-outline">
              Cancel
            </button>
            <button @click="confirmDelete" class="btn btn-danger" :disabled="isDeleting">
              <span v-if="isDeleting" class="loading-spinner"></span>
              {{ isDeleting ? 'Deleting...' : 'Delete Slot' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { lawyerAPI } from '../services/api'

// State
const slots = ref([])
const isLoading = ref(false)
const error = ref('')
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const isSubmitting = ref(false)
const isDeleting = ref(false)
const slotToDelete = ref(null)
const slotToEdit = ref(null)
const formErrors = reactive({})

// Form data
const slotForm = reactive({
  day: '',
  startTime: '',
  endTime: ''
})

// Days of week data
const daysOfWeek = [
  { value: 'SAT', label: 'Saturday' },
  { value: 'SUN', label: 'Sunday' },
  { value: 'MON', label: 'Monday' },
  { value: 'TUE', label: 'Tuesday' },
  { value: 'WED', label: 'Wednesday' },
  { value: 'THU', label: 'Thursday' },
  { value: 'FRI', label: 'Friday' }
]

// Time picker state
const minuteOptions = [0, 15, 30, 45]
const startHour = ref(9)
const startMinute = ref(0)
const startPeriod = ref('AM')
const endHour = ref(10)
const endMinute = ref(0)
const endPeriod = ref('AM')

// Watchers to update slotForm.startTime and slotForm.endTime in 24h format
watch([startHour, startMinute, startPeriod], ([h, m, p]) => {
  slotForm.startTime = to24Hour(h, m, p)
})
watch([endHour, endMinute, endPeriod], ([h, m, p]) => {
  slotForm.endTime = to24Hour(h, m, p)
})

function to24Hour(hour, minute, period) {
  let h = parseInt(hour)
  if (period === 'PM' && h !== 12) h += 12
  if (period === 'AM' && h === 12) h = 0
  return `${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}

// When editing, sync dropdowns with slotForm values
function syncTimePickers() {
  if (slotForm.startTime) {
    const [h, m] = slotForm.startTime.split(':')
    let hour = parseInt(h)
    let period = 'AM'
    if (hour === 0) hour = 12
    else if (hour === 12) period = 'PM'
    else if (hour > 12) { hour -= 12; period = 'PM' }
    startHour.value = hour
    startMinute.value = parseInt(m)
    startPeriod.value = period
  }
  if (slotForm.endTime) {
    const [h, m] = slotForm.endTime.split(':')
    let hour = parseInt(h)
    let period = 'AM'
    if (hour === 0) hour = 12
    else if (hour === 12) period = 'PM'
    else if (hour > 12) { hour -= 12; period = 'PM' }
    endHour.value = hour
    endMinute.value = parseInt(m)
    endPeriod.value = period
  }
}

watch([showAddModal, showEditModal], ([add, edit]) => {
  if (add || edit) syncTimePickers()
})

// Computed properties
const timeConflict = computed(() => {
  if (!slotForm.startTime || !slotForm.endTime) return false
  return slotForm.startTime >= slotForm.endTime
})

// Responsive ghost cards for grid balance
const gridColumns = computed(() => {
  if (window.innerWidth < 600) return 1
  if (window.innerWidth < 900) return 2
  return 3
})
const ghostCards = computed(() => {
  const count = slots.value.length
  const cols = gridColumns.value
  return count > 0 && count < cols ? cols - count : 0
})
window.addEventListener('resize', () => { gridColumns.value })

// Methods
const fetchSlots = async () => {
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await lawyerAPI.getAvailabilitySlots()
    slots.value = response.data.slots || []
  } catch (err) {
    console.error('Error fetching availability slots:', err)
    error.value = err.response?.data?.message || 'Failed to load availability slots'
  } finally {
    isLoading.value = false
  }
}

const editSlot = (slot) => {
  slotToEdit.value = slot
  slotForm.day = slot.day
  slotForm.startTime = slot.startTime
  slotForm.endTime = slot.endTime
  showEditModal.value = true
}

const deleteSlot = (slotId) => {
  slotToDelete.value = slots.value.find(slot => slot.id === slotId)
  showDeleteModal.value = true
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  slotToEdit.value = null
  resetForm()
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  slotToDelete.value = null
}

const resetForm = () => {
  slotForm.day = ''
  slotForm.startTime = ''
  slotForm.endTime = ''
  Object.keys(formErrors).forEach(key => delete formErrors[key])
}

const validateForm = () => {
  Object.keys(formErrors).forEach(key => delete formErrors[key])

  if (!slotForm.day) {
    formErrors.day = 'Day is required'
  }

  if (!slotForm.startTime) {
    formErrors.startTime = 'Start time is required'
  }

  if (!slotForm.endTime) {
    formErrors.endTime = 'End time is required'
  }

  if (timeConflict.value) {
    formErrors.endTime = 'End time must be after start time'
  }

  return Object.keys(formErrors).length === 0
}

const submitSlot = async () => {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    if (showEditModal.value) {
      // Update existing slot
      if (slotToEdit.value?.id) {
        await lawyerAPI.updateAvailabilitySlot(slotToEdit.value.id, {
          day: slotForm.day,
          startTime: slotForm.startTime,
          endTime: slotForm.endTime
        })
      }
    } else {
      // Create new slot
      await lawyerAPI.createAvailabilitySlot({
        day: slotForm.day,
        startTime: slotForm.startTime,
        endTime: slotForm.endTime
      })
    }

    closeModal()
    await fetchSlots()
  } catch (err) {
    console.error('Error saving availability slot:', err)
    
    if (err.response?.data?.message) {
      alert(`Error: ${err.response.data.message}`)
    } else {
      alert('An error occurred while saving the availability slot. Please try again.')
    }
  } finally {
    isSubmitting.value = false
  }
}

const confirmDelete = async () => {
  if (!slotToDelete.value) return

  isDeleting.value = true

  try {
    await lawyerAPI.deleteAvailabilitySlot(slotToDelete.value.id)
    closeDeleteModal()
    await fetchSlots()
  } catch (err) {
    console.error('Error deleting availability slot:', err)
    
    if (err.response?.data?.message) {
      alert(`Error: ${err.response.data.message}`)
    } else {
      alert('An error occurred while deleting the availability slot. Please try again.')
    }
  } finally {
    isDeleting.value = false
  }
}

const getDayDisplayName = (day) => {
  const dayObj = daysOfWeek.find(d => d.value === day)
  return dayObj ? dayObj.label : day
}

const formatTime = (time) => {
  if (!time) return ''
  
  // Convert 24-hour format to 12-hour format with AM/PM
  const [hours, minutes] = time.split(':')
  const hour = parseInt(hours, 10)
  const ampm = hour >= 12 ? 'PM' : 'AM'
  const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour
  
  return `${displayHour}:${minutes} ${ampm}`
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return ''
  
  const start = new Date(`2000-01-01T${startTime}`)
  const end = new Date(`2000-01-01T${endTime}`)
  const diffMs = end - start
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  if (diffHours === 0) {
    return `${diffMinutes} min`
  } else if (diffMinutes === 0) {
    return `${diffHours} hr`
  } else {
    return `${diffHours} hr ${diffMinutes} min`
  }
}

// Load slots on mount
onMounted(() => {
  fetchSlots()
})
</script>

<style scoped>
.availability-slots-section.redesigned {
  width: 100%;
  max-width: 900px;
  margin: 2rem auto 0 auto;
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 2.5rem 1.5rem 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Remove vertical centering from body and #app */
body, #app {
  min-height: 100vh;
}

.section-header {
  margin-bottom: 2rem;
  text-align: center;
}
.add-slot-section.redesigned {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}
.slots-content.redesigned {
  width: 100%;
}
.slots-grid.redesigned {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  width: 100%;
}
@media (max-width: 900px) {
  .slots-grid.redesigned {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 600px) {
  .slots-grid.redesigned {
    grid-template-columns: 1fr;
  }
}
.slot-card.redesigned {
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 1.5rem 1.25rem;
  border: 1px solid var(--color-border);
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: box-shadow 0.2s;
}
.slot-card.redesigned:hover {
  box-shadow: 0 4px 24px rgba(var(--color-primary-rgb), 0.12);
  border-color: var(--color-primary);
}
.slot-card.ghost {
  background: transparent;
  border: none;
  box-shadow: none;
  pointer-events: none;
}
.slot-header.redesigned {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
/* Replace the day icon with a calendar icon */
.slot-day .day-icon {
  margin-right: 0.5rem;
  color: var(--color-primary);
  display: flex;
  align-items: center;
}
.slot-time.redesigned {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 0.5rem;
}
.time-range .time-icon {
  margin-right: 0.25rem;
  color: var(--color-primary);
}
.no-slots.redesigned {
  text-align: center;
  padding: 4rem 2rem 3rem 2rem;
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  margin: 2rem 0;
}
.slot-card {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  transition: all 0.2s ease;
}

.slot-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(var(--color-primary-rgb), 0.1);
}

.slot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.slot-day {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.day-name {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 1.125rem;
}

.slot-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

.btn-icon:hover {
  background: var(--color-background);
  color: var(--color-text);
}

.btn-icon.delete:hover {
  background: var(--color-error);
  color: white;
}

.slot-time {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.time-range,
.duration {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-label,
.duration-label {
  font-weight: 500;
  color: var(--color-text);
}

.time-value,
.duration-value {
  font-weight: 600;
  color: var(--color-heading);
}

/* Modal Styles */
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
  max-width: 500px;
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

.slot-form {
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

.form-label {
  display: block;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.form-input,
.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.form-input.error,
.form-select.error {
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

.time-picker-group {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
.time-select {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  border: 1px solid var(--color-border);
  font-size: 1rem;
  background: var(--color-background);
}

/* Delete Modal Styles */
.delete-modal {
  max-width: 400px;
}

.delete-content {
  padding: 1.5rem;
  text-align: center;
}

.delete-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-error);
}

.delete-content h4 {
  margin: 0 0 1rem 0;
  color: var(--color-heading);
}

.delete-content p {
  margin: 0 0 1rem 0;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.delete-content p:last-of-type {
  margin-bottom: 2rem;
}

.delete-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-danger {
  background: var(--color-error);
  color: white;
  border: 1px solid var(--color-error);
}

.btn-danger:hover {
  background: #c53030;
  border-color: #c53030;
}

@media (max-width: 768px) {
  .slots-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .delete-actions {
    flex-direction: column;
  }
}
</style> 