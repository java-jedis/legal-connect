<template>
  <div class="schedule-calendar">
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
        <span class="placeholder-note">Schedule events will appear here when created</span>
      </div>
      

      <ScheduleEventModal
        v-if="showEventModal"
        :mode="modalMode"
        :eventData="selectedEvent"
        :caseId="caseId"
        @event-saved="handleEventSaved"
        @close="closeEventModal"
        @delete-event="openDeleteModal"
      />
      <DeleteConfirmationModal
        v-if="showDeleteModal"
        :eventTitle="selectedEvent?.title"
        @confirm="deleteEvent"
        @cancel="closeDeleteModal"
      />
    </div>
  </div>
</template>

<script setup>
import { scheduleAPI } from '@/services/api'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import FullCalendar from '@fullcalendar/vue3'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import DeleteConfirmationModal from '../DeleteConfirmationModal.vue'
import ScheduleEventModal from './ScheduleEventModal.vue'

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
const fullCalendar = ref(null)
const calendarKey = ref(0)
const isMounted = ref(false)

const showEventModal = ref(false)
const showDeleteModal = ref(false)
const modalMode = ref('create') // 'create' or 'edit'
const selectedEvent = ref(null)

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
      // Has both start and end times - treat as timed event
      startDate = new Date(schedule.startTime)
      endDate = new Date(schedule.endTime)
      allDay = false
    } else if (schedule.startTime) {
      // Has only start time - treat as timed event with 1 hour duration
      startDate = new Date(schedule.startTime)
      endDate = new Date(new Date(schedule.startTime).getTime() + 60 * 60 * 1000) // 1 hour later
      allDay = false
    } else {
      // No start time - treat as all-day event
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
    // This callback is called when the calendar view changes
    console.log('Calendar dates set:', dateInfo)
  },
  eventClick: (info) => {
    openEditModal(info.event)
  },
  eventDidMount: (info) => {
    // Add tooltip or custom styling
    const event = info.event
    const type = event.extendedProps.typeDisplay
    const description = event.extendedProps.description
    
    if (description) {
      info.el.title = `${type}: ${description}`
    } else {
      info.el.title = type
    }
  },
  dayMaxEvents: true, // Show "more" link when too many events
  moreLinkClick: 'popover', // Show popover when "more" is clicked
  editable: false, // Disable drag and drop for now
  selectable: false, // Disable date selection for now
  selectMirror: true,
  weekends: true,
  firstDay: 1, // Start week on Monday
  slotMinTime: '06:00:00', // Extended time range
  slotMaxTime: '22:00:00', // Extended time range
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
  // Ensure events are properly displayed in time grid views
  eventDisplay: 'block',
  eventOverlap: false,
  slotEventOverlap: false
}))

// Lifecycle
onMounted(() => {
  isMounted.value = true
  loadSchedules()
})

// Watch for changes in calendar events and refresh calendar
watch(calendarEvents, () => {
  // Force calendar to refresh when events change
  if (fullCalendar.value) {
    nextTick(() => {
      fullCalendar.value.getApi().refetchEvents()
    })
  }
}, { deep: true })

// Watch for loading state changes to force re-render
watch(loading, (newLoading, oldLoading) => {
  if (oldLoading && !newLoading) {
    // Loading finished, force calendar re-render
    nextTick(() => {
      calendarKey.value++
      if (fullCalendar.value) {
        fullCalendar.value.getApi().render()
      }
    })
  }
})

function openCreateModal() {
  modalMode.value = 'create'
  selectedEvent.value = null
  showEventModal.value = true
}

function openEditModal(event) {
  modalMode.value = 'edit'
  selectedEvent.value = {
    id: event.id,
    title: event.title,
    start: event.startStr,
    end: event.endStr,
    allDay: event.allDay,
    extendedProps: event.extendedProps
  }
  showEventModal.value = true
}

function closeEventModal() {
  showEventModal.value = false
  selectedEvent.value = null
}

function handleEventSaved() {
  closeEventModal()
  loadSchedules()
}

function openDeleteModal(event) {
  selectedEvent.value = event
  showDeleteModal.value = true
}

function closeDeleteModal() {
  showDeleteModal.value = false
  selectedEvent.value = null
}

async function deleteEvent() {
  if (!selectedEvent.value?.id) return
  try {
    await scheduleAPI.deleteSchedule(selectedEvent.value.id)
    closeDeleteModal()
    loadSchedules()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to delete event')
  }
}

defineExpose({
  openCreateModal
})
</script>

<style scoped>
.schedule-calendar {
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

.calendar-container {
  background: var(--color-background);
  border-radius: 8px;
  overflow: hidden;
  position: relative; /* Added for fab positioning */
}

.calendar-placeholder {
  text-align: center;
  padding: 3rem 1rem;
  background: var(--color-background);
  border-radius: 8px;
  border: 2px dashed var(--color-border);
}

.calendar-placeholder .placeholder-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-text-muted);
}

.calendar-placeholder p {
  color: var(--color-text-muted);
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.calendar-placeholder .placeholder-note {
  font-size: 0.875rem;
  opacity: 0.7;
  color: var(--color-text-muted);
}

.fab {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--color-brand);
  color: #fff;
  font-size: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  cursor: pointer;
  z-index: 100;
  border: none;
  outline: none;
  transition: background 0.2s;
}
.fab:hover {
  background: var(--color-brand-dark);
}

/* FullCalendar custom styles */
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