<template>
  <div class="my-calendar-section dashboard-card">
    <div class="card-header">
      <h3>My Calendar</h3>
      <button v-if="!hasGoogleCalendar" @click="connectGoogleCalendar" class="btn btn-outline btn-sm">
        Connect Google Calendar
      </button>
      <span v-else class="google-calendar-connected">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M22 11.08V12A10 10 0 1 1 5.68 3.57" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        Google Calendar Connected
      </span>
    </div>
    <div class="card-content">
      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Loading calendar...</p>
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

      <!-- Calendar View -->
      <div v-else class="calendar-container">
        <FullCalendar
          v-if="!loading && schedules.length > 0 && isMounted"
          :options="calendarOptions"
          ref="fullCalendar"
          :key="calendarKey"
        />
        <div v-else-if="!loading && schedules.length === 0" class="calendar-placeholder">
          <div class="placeholder-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
              <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
              <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
              <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <p>No scheduled events found</p>
          <span class="placeholder-note">Schedule events will appear here</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import FullCalendar from '@fullcalendar/vue3'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { scheduleAPI, authAPI } from '../services/api' // Assuming authAPI has the oauth status check

// Reactive data
const schedules = ref([])
const loading = ref(false)
const error = ref('')
const fullCalendar = ref(null)
const calendarKey = ref(0)
const isMounted = ref(false)
const hasGoogleCalendar = ref(false)

// Methods
const loadSchedules = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await scheduleAPI.getAllUserSchedules() // Use the new API endpoint
    schedules.value = response.data?.schedules || []
  } catch (err) {
    console.error('Error loading schedules:', err)
    error.value = err.response?.data?.message || 'Failed to load schedules'
  } finally {
    loading.value = false
  }
}

const checkGoogleCalendarStatus = async () => {
  try {
    const response = await authAPI.checkGoogleCalendarStatus() // Assuming this API call exists
    hasGoogleCalendar.value = response.data // Assuming the response data is a boolean
  } catch (err) {
    console.error('Error checking Google Calendar status:', err)
    hasGoogleCalendar.value = false
  }
}

const connectGoogleCalendar = async () => {
  try {
    const response = await authAPI.authorizeGoogleCalendar()
    if (response.data) {
      window.location.href = response.data
    }
  } catch (err) {
    console.error('Error initiating Google Calendar authorization:', err)
    alert('Failed to connect Google Calendar. Please try again.')
  }
}

const getScheduleTypeColor = (type) => {
  const colorMap = {
    'COURT_HEARING': '#92400e', // Amber
    'CLIENT_MEETING': '#1e40af', // Blue
    'DEADLINE': '#991b1b', // Red
    'OTHER': '#374151' // Gray
  }
  return colorMap[type] || '#374151'
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

// Computed properties
const calendarEvents = computed(() => {
  return schedules.value.map(schedule => {
    let startDate, endDate, allDay = false

    if (schedule.startTime && schedule.endTime) {
      startDate = new Date(schedule.startTime)
      endDate = new Date(schedule.endTime)
      allDay = false
    } else if (schedule.startTime) {
      startDate = new Date(schedule.startTime)
      endDate = new Date(new Date(schedule.startTime).getTime() + 60 * 60 * 1000) // 1 hour later
      allDay = false
    } else {
      startDate = new Date(schedule.date)
      endDate = new Date(schedule.date + 'T23:59:59')
      allDay = true
    }

    return {
      id: schedule.id,
      title: schedule.title,
      start: startDate,
      end: endDate,
      allDay: allDay,
      backgroundColor: getScheduleTypeColor(schedule.type),
      borderColor: getScheduleTypeColor(schedule.type),
      textColor: '#ffffff',
      extendedProps: {
        type: schedule.type,
        typeDisplay: getScheduleTypeDisplayName(schedule.type),
        description: schedule.description,
        caseTitle: schedule.caseInfo?.title || 'Unknown Case'
      }
    }
  })
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  height: 'auto',
  events: calendarEvents.value,
  datesSet: (dateInfo) => {
    console.log('Calendar dates set:', dateInfo)
  },
  eventClick: (info) => {
    // No edit/delete for now, just log or show details
    console.log('Event clicked:', info.event.extendedProps)
  },
  eventDidMount: (info) => {
    const event = info.event
    const type = event.extendedProps.typeDisplay
    const description = event.extendedProps.description

    if (description) {
      info.el.title = `${type}: ${description}`
    } else {
      info.el.title = type
    }
  },
  dayMaxEvents: true,
  moreLinkClick: 'popover',
  editable: false,
  selectable: false,
  selectMirror: true,
  weekends: true,
  firstDay: 1,
  slotMinTime: '06:00:00',
  slotMaxTime: '22:00:00',
  allDaySlot: true,
  slotDuration: '00:30:00',
  slotLabelInterval: '01:00:00',
  nowIndicator: true,
  scrollTime: '08:00:00',
  expandRows: true,
  aspectRatio: 1.35,
  eventTimeFormat: {
    hour: '2-digit',
    minute: '2-digit',
    meridiem: 'short'
  },
  eventDisplay: 'block',
  eventOverlap: false,
  slotEventOverlap: false
}))

// Lifecycle
onMounted(() => {
  isMounted.value = true
  loadSchedules()
  checkGoogleCalendarStatus()
})

// Watch for changes in calendar events and refresh calendar
watch(calendarEvents, () => {
  if (fullCalendar.value) {
    nextTick(() => {
      fullCalendar.value.getApi().refetchEvents()
    })
  }
}, { deep: true })

// Watch for loading state changes to force re-render
watch(loading, (newLoading, oldLoading) => {
  if (oldLoading && !newLoading) {
    nextTick(() => {
      calendarKey.value++
      if (fullCalendar.value) {
        fullCalendar.value.getApi().render()
      }
    })
  }
})
</script>

<style scoped>
.my-calendar-section {
  grid-column: span 2; /* Occupy full width in the grid */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-background-soft);
}

.card-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.card-content {
  padding: 1.5rem;
  background: var(--color-background);
}

.loading-state, .error-state, .calendar-placeholder {
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

.error-icon, .placeholder-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 1rem;
  color: var(--color-text-muted);
}

.error-state p, .calendar-placeholder p {
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}

.calendar-placeholder .placeholder-note {
  font-size: 0.875rem;
  opacity: 0.7;
  color: var(--color-text-muted);
}

.calendar-container {
  background: var(--color-background);
  border-radius: 8px;
  overflow: hidden;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
}

.google-calendar-connected {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #10b981; /* Green color for success */
  font-weight: 500;
  font-size: 0.875rem;
}

.google-calendar-connected svg {
  width: 18px;
  height: 18px;
}

/* FullCalendar custom styles - copied from ScheduleCalendar.vue */
:deep(.fc) {
  font-family: inherit;
}

:deep(.fc-toolbar) {
  background: var(--color-background-soft);
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
}

:deep(.fc-toolbar-title) {
  color: var(--color-heading);
  font-weight: 600;
}

:deep(.fc-button) {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

:deep(.fc-button:hover) {
  background: var(--color-background-soft);
  border-color: var(--color-brand);
  color: var(--color-text);
}

:deep(.fc-button-active) {
  background: var(--color-brand);
  border-color: var(--color-brand);
  color: white;
}

:deep(.fc-daygrid-day) {
  background: var(--color-background);
}

:deep(.fc-daygrid-day-number) {
  color: var(--color-text);
}

:deep(.fc-daygrid-day.fc-day-today) {
  background: var(--color-background-soft);
}

:deep(.fc-daygrid-day.fc-day-past) {
  background: var(--color-background-mute);
}

:deep(.fc-event) {
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.fc-event:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

:deep(.fc-event-title) {
  font-weight: 600;
}

:deep(.fc-event-time) {
  font-weight: 500;
}

:deep(.fc-col-header-cell) {
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
}

:deep(.fc-col-header-cell-cushion) {
  color: var(--color-heading);
  font-weight: 600;
  text-decoration: none;
}

:deep(.fc-daygrid-day-frame) {
  border: 1px solid var(--color-border);
}

:deep(.fc-daygrid-day-events) {
  margin-top: 0.25rem;
}

:deep(.fc-more-link) {
  color: var(--color-brand);
  font-weight: 500;
  text-decoration: none;
}

:deep(.fc-more-link:hover) {
  text-decoration: underline;
}

/* Time grid specific styles */
:deep(.fc-timegrid-slot) {
  border-bottom: 1px solid var(--color-border);
}

:deep(.fc-timegrid-slot-label) {
  color: var(--color-text-muted);
  font-size: 0.75rem;
}

:deep(.fc-timegrid-now-indicator-line) {
  border-color: var(--color-brand);
}

:deep(.fc-timegrid-now-indicator-arrow) {
  border-color: var(--color-brand);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  :deep(.fc-toolbar) {
    flex-direction: column;
    gap: 1rem;
  }
  
  :deep(.fc-toolbar-chunk) {
    display: flex;
    justify-content: center;
  }
  
  :deep(.fc-button) {
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
  }
}
</style>