<template>
  <div class="lawyer-availability-section redesigned">
    <div class="section-header">
      <h3>Availability</h3>
      <p class="section-description">Available consultation time slots</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
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
      <h4>Availability information unavailable</h4>
      <p>{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="!slots || slots.length === 0" class="no-slots redesigned">
      <div class="no-slots-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h4>No availability information</h4>
      <p>This lawyer hasn't set up their availability schedule yet.</p>
    </div>

    <!-- Availability Content -->
    <div v-else class="slots-content redesigned">
      <div class="slots-grid redesigned">
        <div v-for="slot in slots" :key="slot.id" class="slot-card redesigned">
          <div class="slot-header redesigned">
            <div class="slot-day">
              <span class="day-icon">
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24">
                  <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <span class="day-name">{{ getDayDisplayName(slot.day) }}</span>
            </div>
          </div>
          <div class="slot-time redesigned">
            <div class="time-range">
              <span class="time-icon">
                <svg width="18" height="18" fill="none" viewBox="0 0 24 24">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
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
  </div>
</template>

<script>
export default {
  name: 'LawyerAvailabilitySection',
  props: {
    slots: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    error: {
      type: String,
      default: null
    }
  },
  computed: {
    ghostCards() {
      const remainder = this.slots.length % 3
      return remainder === 0 ? 0 : 3 - remainder
    }
  },
  methods: {
    getDayDisplayName(day) {
      const dayNames = {
        'SAT': 'Saturday',
        'SUN': 'Sunday', 
        'MON': 'Monday',
        'TUE': 'Tuesday',
        'WED': 'Wednesday',
        'THU': 'Thursday',
        'FRI': 'Friday'
      }
      return dayNames[day] || day
    },

    formatTime(time) {
      if (!time) return ''
      
      try {
        // Convert 24-hour format to 12-hour format with AM/PM
        const [hours, minutes] = time.split(':')
        const hour = parseInt(hours, 10)
        const ampm = hour >= 12 ? 'PM' : 'AM'
        const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour
        
        return `${displayHour}:${minutes} ${ampm}`
      } catch (error) {
        return time
      }
    },

    calculateDuration(startTime, endTime) {
      if (!startTime || !endTime) return ''
      
      try {
        const start = new Date(`2000-01-01T${startTime}`)
        const end = new Date(`2000-01-01T${endTime}`)
        const diffMs = end - start
        
        if (diffMs <= 0) return ''
        
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
        const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
        
        if (diffHours === 0) {
          return `${diffMinutes} min`
        } else if (diffMinutes === 0) {
          return `${diffHours} hr`
        } else {
          return `${diffHours} hr ${diffMinutes} min`
        }
      } catch (error) {
        return ''
      }
    }
  }
}
</script>

<style scoped>
.lawyer-availability-section.redesigned {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.section-header {
  margin-bottom: 24px;
  text-align: center;
}

.section-header h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 600;
}

.section-description {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.error-icon {
  width: 48px;
  height: 48px;
  color: #dc3545;
  margin-bottom: 16px;
}

.error-state h4 {
  margin: 0 0 8px 0;
  color: #dc3545;
  font-size: 1.125rem;
  font-weight: 600;
}

.error-state p {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

/* Empty State */
.no-slots.redesigned {
  text-align: center;
  padding: 60px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin: 24px 0;
}

.no-slots-icon {
  width: 48px;
  height: 48px;
  color: #6c757d;
  margin-bottom: 16px;
}

.no-slots.redesigned h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 1.125rem;
  font-weight: 600;
}

.no-slots.redesigned p {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
  font-style: italic;
}

/* Slots Content */
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
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 1.5rem 1.25rem;
  border: 1px solid #e9ecef;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: all 0.2s ease;
}

.slot-card.redesigned:hover {
  box-shadow: 0 4px 24px rgba(0, 123, 255, 0.12);
  border-color: #007bff;
  transform: translateY(-1px);
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

.slot-day {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.day-icon {
  color: #007bff;
  display: flex;
  align-items: center;
}

.day-name {
  font-weight: 600;
  color: #333;
  font-size: 1.125rem;
}

.slot-time.redesigned {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.time-range,
.duration {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.time-icon {
  color: #007bff;
  display: flex;
  align-items: center;
}

.time-label,
.duration-label {
  font-weight: 500;
  color: #666;
  font-size: 0.875rem;
  min-width: 60px;
}

.time-value,
.duration-value {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .lawyer-availability-section.redesigned {
    padding: 16px;
  }
  
  .section-header h3 {
    font-size: 1.25rem;
  }
  
  .slots-grid.redesigned {
    gap: 1rem;
  }
  
  .slot-card.redesigned {
    padding: 1.25rem 1rem;
  }
}
</style>