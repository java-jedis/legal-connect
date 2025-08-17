<template>
  <div class="schedule-list">
    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading schedules...</p>
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
      <p>{{ error }}</p>
      <button @click="loadSchedules" class="btn btn-primary btn-sm">Try Again</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="schedules.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
          <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
          <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
          <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <p>No scheduled events found</p>
      <span class="empty-note">Schedule events will appear here when created</span>
    </div>

    <!-- Schedule List -->
    <div v-else class="schedules-container">
      <div class="schedule-item" v-for="schedule in schedules" :key="schedule.id">
        <div class="schedule-header">
          <div class="schedule-title">
            <h4>{{ schedule.title }}</h4>
            <span class="schedule-type" :class="getScheduleTypeClass(schedule.type)">
              {{ getScheduleTypeDisplayName(schedule.type) }}
            </span>
          </div>
          <div class="schedule-date">
            <div class="date-info">
              <span class="date-label">Date:</span>
              <span class="date-value">{{ formatDate(schedule.date) }}</span>
            </div>
            <div v-if="schedule.startTime" class="time-info">
              <span class="time-label">Time:</span>
              <span class="time-value">{{ formatTime(schedule.startTime) }} - {{ formatTime(schedule.endTime) }}</span>
            </div>
          </div>
        </div>
        
        <div v-if="schedule.description" class="schedule-description">
          <p>{{ schedule.description }}</p>
        </div>

        <div class="schedule-meta">
          <div class="meta-item">
            <span class="meta-label">Case:</span>
            <span class="meta-value">{{ schedule.caseInfo.title }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Created:</span>
            <span class="meta-value">{{ formatDateTime(schedule.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { scheduleAPI } from '@/services/api'
import { onMounted, ref } from 'vue'

// Props
const props = defineProps({
  caseId: {
    type: String,
    required: true
  }
})

// Reactive data
const schedules = ref([])
const loading = ref(false)
const error = ref('')

// Methods
const loadSchedules = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await scheduleAPI.getAllSchedulesForCase(props.caseId)
    // Extract schedules from ApiResponse format: response.data.schedules
    schedules.value = response.data?.schedules || []
  } catch (err) {
    console.error('Error loading schedules:', err)
    error.value = err.response?.data?.message || 'Failed to load schedules'
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatTime = (timeString) => {
  return new Date(timeString).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDateTime = (dateTimeString) => {
  return new Date(dateTimeString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getScheduleTypeClass = (type) => {
  return {
    'type-court': type === 'COURT_HEARING',
    'type-meeting': type === 'CLIENT_MEETING',
    'type-deadline': type === 'DEADLINE',
    'type-other': type === 'OTHER'
  }
}

const getScheduleTypeDisplayName = (type) => {
  const typeMap = {
    'COURT_HEARING': 'Court Hearing',
    'CLIENT_MEETING': 'Client Meeting',
    'DEADLINE': 'Deadline',
    'OTHER': 'Other'
  }
  return typeMap[type] || type
}

// Lifecycle
onMounted(() => {
  loadSchedules()
})
</script>

<style scoped>
.schedule-list {
  width: 100%;
}

.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 2rem 1rem;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top: 3px solid var(--color-brand);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon, .empty-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 1rem;
  color: var(--color-text-muted);
}

.error-state p, .empty-state p {
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}

.empty-note {
  font-size: 0.875rem;
  opacity: 0.7;
}

.schedules-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.schedule-item {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.2s ease;
}

.schedule-item:hover {
  border-color: var(--color-brand);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 1rem;
}

.schedule-title h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.schedule-type {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.type-court {
  background: #fef3c7;
  color: #92400e;
}

.type-meeting {
  background: #dbeafe;
  color: #1e40af;
}

.type-deadline {
  background: #fecaca;
  color: #991b1b;
}

.type-other {
  background: #f3f4f6;
  color: #374151;
}

.schedule-date {
  text-align: right;
  font-size: 0.875rem;
}

.date-info, .time-info {
  margin-bottom: 0.25rem;
}

.date-label, .time-label {
  color: var(--color-text-muted);
  font-weight: 500;
}

.date-value, .time-value {
  color: var(--color-text);
  font-weight: 600;
}

.schedule-description {
  margin-bottom: 0.75rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: 6px;
}

.schedule-description p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
}

.schedule-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.meta-label {
  font-weight: 500;
}

.meta-value {
  color: var(--color-text);
}

@media (max-width: 768px) {
  .schedule-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .schedule-date {
    text-align: left;
  }

  .schedule-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style> 