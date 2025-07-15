<template>
  <div class="case-detail">
    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading case details...</p>
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
      <h3>Failed to load case</h3>
      <p>{{ error }}</p>
      <div class="error-actions">
        <button @click="loadCase" class="btn btn-primary">Try Again</button>
        <button @click="goBack" class="btn btn-secondary">Go Back</button>
      </div>
    </div>

    <!-- Case Content -->
    <div v-else-if="currentCase" class="case-content">
      <!-- Case Header -->
      <div class="case-header">
        <div class="header-main">
          <div class="header-left">
            <button @click="goBack" class="back-btn">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Back to Cases
            </button>
            <div class="case-title-section">
              <h1 class="case-title">{{ currentCase.title }}</h1>
              <span class="case-id">Case #{{ currentCase.caseId.substring(0, 8) }}</span>
            </div>
          </div>
          <div class="header-right">
            <div class="case-status" :class="getStatusClass(currentCase.status)">
              {{ getStatusDisplayName(currentCase.status) }}
            </div>
            <div v-if="isLawyer" class="case-actions">
              <button @click="showEditModal = true" class="btn btn-secondary btn-sm">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M11 4H4A2 2 0 0 0 2 6V20A2 2 0 0 0 4 22H18A2 2 0 0 0 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M18.5 2.5A2.121 2.121 0 0 1 21 5L12 14L8 15L9 11L18.5 2.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Edit Case
              </button>
              <button @click="openStatusModal" class="btn btn-primary btn-sm">
                Update Status
              </button>
            </div>
          </div>
        </div>
        
        <!-- Case Meta Information -->
        <div class="case-meta">
          <div class="meta-grid">
            <div class="meta-item">
              <span class="meta-label">Created</span>
              <span class="meta-value">{{ formatDate(currentCase.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Last Updated</span>
              <span class="meta-value">{{ formatDate(currentCase.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Case Body -->
      <div class="case-body">
        <div class="case-sections">
          <!-- Case Description Section (Expandable) -->
          <div class="info-section expandable-section">
            <div class="expandable-header" @click="toggleDescription">
              <h3 class="section-title">Case Description</h3>
              <div class="expand-icon" :class="{ 'expanded': showDescription }">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <div class="expandable-content" v-show="showDescription">
              <div class="description-content">
                <p v-if="currentCase.description">{{ currentCase.description }}</p>
                <p v-else class="no-description">No description provided for this case.</p>
              </div>
            </div>
          </div>

          <!-- Case Participants Section (Expandable) -->
          <div class="info-section expandable-section">
            <div class="expandable-header" @click="toggleParticipants">
              <h3 class="section-title">Case Participants</h3>
              <div class="expand-icon" :class="{ 'expanded': showParticipants }">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <div class="expandable-content" v-show="showParticipants">
              <div class="participants-grid">
                <div class="participant-card">
                  <div class="participant-header">
                    <div class="participant-icon lawyer-icon">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M20 21V19A4 4 0 0 0 16 15H8A4 4 0 0 0 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </div>
                    <div class="participant-info">
                      <h4 class="participant-role">Lawyer</h4>
                      <p class="participant-name">{{ currentCase.lawyer.firstName }} {{ currentCase.lawyer.lastName }}</p>
                    </div>
                  </div>
                  <div class="participant-details">
                    <div class="detail-item">
                      <span class="detail-label">Email:</span>
                      <span class="detail-value">{{ currentCase.lawyer.email }}</span>
                    </div>
                    <div v-if="currentCase.lawyer.firm" class="detail-item">
                      <span class="detail-label">Firm:</span>
                      <span class="detail-value">{{ currentCase.lawyer.firm }}</span>
                    </div>
                  </div>
                </div>

                <div class="participant-card">
                  <div class="participant-header">
                    <div class="participant-icon client-icon">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M20 21V19A4 4 0 0 0 16 15H8A4 4 0 0 0 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </div>
                    <div class="participant-info">
                      <h4 class="participant-role">Client</h4>
                      <p class="participant-name">{{ currentCase.client.firstName }} {{ currentCase.client.lastName }}</p>
                    </div>
                  </div>
                  <div class="participant-details">
                    <div class="detail-item">
                      <span class="detail-label">Email:</span>
                      <span class="detail-value">{{ currentCase.client.email }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Upcoming Dates Section (Expandable) -->
          <div class="info-section expandable-section">
            <div class="expandable-header" @click="toggleUpcomingDates">
              <div class="expandable-header-left">
                <h3 class="section-title">Upcoming Dates</h3>
              </div>
              <div class="expandable-header-right">
                <button @click.stop="openCreateEventModal" class="btn btn-sm btn-primary">
                  <svg viewBox="0 0 20 20" fill="currentColor" class="w-5 h-5">
                    <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
                  </svg>
                  Add Event
                </button>
                <div class="expand-icon" :class="{ 'expanded': showUpcomingDates }">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
            </div>
            <div class="expandable-content" v-show="showUpcomingDates">
              <ScheduleCalendar :case-id="currentCase.caseId" ref="scheduleCalendar" />
            </div>
          </div>

          <!-- Documents Section (Expandable) -->
          <div class="info-section expandable-section">
            <div class="expandable-header" @click="toggleDocuments">
              <h3 class="section-title">Documents</h3>
              <div class="expand-icon" :class="{ 'expanded': showDocuments }">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <div class="expandable-content" v-show="showDocuments">
              <DocumentList :case-id="currentCase.caseId" />
            </div>
          </div>

          <!-- Notes Section (Expandable) -->
          <div class="info-section expandable-section">
            <div class="expandable-header" @click="toggleNotes">
              <h3 class="section-title">Case Notes</h3>
              <div class="expand-icon" :class="{ 'expanded': showNotes }">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
                          <div class="expandable-content" v-show="showNotes">
                <NoteList :case-id="currentCase.caseId" :case-data="currentCase" />
              </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Case Modal -->
    <EditCaseModal 
      v-if="showEditModal" 
      :case-data="currentCase"
      @close="showEditModal = false"
      @case-updated="onCaseUpdated"
    />

    <!-- Status Update Modal -->
    <UpdateStatusModal 
      v-if="showStatusModal" 
      :case-data="currentCase"
      @close="showStatusModal = false"
      @status-updated="onStatusUpdated"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCaseStore } from '../stores/case'
import DocumentList from './DocumentList.vue'
import EditCaseModal from './EditCaseModal.vue'
import NoteList from './NoteList.vue'
import ScheduleCalendar from './ScheduleCalendar.vue'
import UpdateStatusModal from './UpdateStatusModal.vue'

// Stores and routing
const caseStore = useCaseStore()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

// Reactive data
const showEditModal = ref(false)
const showStatusModal = ref(false)
const statusModalRef = ref(null)
const scheduleCalendar = ref(null)
const showDescription = ref(true)
const showParticipants = ref(true)
const showUpcomingDates = ref(true) // Changed to true
const showDocuments = ref(false)
const showNotes = ref(false)

// Computed properties
const currentCase = computed(() => caseStore.currentCase)
const loading = computed(() => caseStore.loading)
const error = computed(() => caseStore.error)
const isLawyer = computed(() => authStore.isLawyer())

// Methods
const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusClass = (status) => {
  return {
    'status-in-progress': status === 'IN_PROGRESS',
    'status-resolved': status === 'RESOLVED'
  }
}

const getStatusDisplayName = (status) => {
  return status === 'IN_PROGRESS' ? 'In Progress' : 'Resolved'
}

const loadCase = async () => {
  const caseId = route.params.id
  if (caseId) {
    try {
      await caseStore.getCaseById(caseId)
    } catch (err) {
      console.error('Failed to load case:', err)
    }
  }
}

const goBack = () => {
  router.push('/cases')
}

const onCaseUpdated = () => {
  showEditModal.value = false
  // The store automatically updates the current case
}

const onStatusUpdated = () => {
  showStatusModal.value = false
  // The store automatically updates the current case
}

const toggleDescription = () => {
  showDescription.value = !showDescription.value
}

const toggleParticipants = () => {
  showParticipants.value = !showParticipants.value
}

const toggleUpcomingDates = () => {
  showUpcomingDates.value = !showUpcomingDates.value
}

const toggleDocuments = () => {
  showDocuments.value = !showDocuments.value
}

const toggleNotes = () => {
  showNotes.value = !showNotes.value
}

function openStatusModal() {
  showStatusModal.value = true
  nextTick(() => {
    if (statusModalRef.value) {
      statusModalRef.value.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  })
}

function openCreateEventModal() {
  if (scheduleCalendar.value) {
    scheduleCalendar.value.openCreateModal()
  }
}

// Lifecycle
onMounted(() => {
  loadCase()
})
</script>

<style scoped>
.case-detail {
  padding: 2rem 0;
  min-height: 60vh;
}

/* Loading and Error States */
.loading-state, .error-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
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

.error-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-text-muted);
}

.error-state h3 {
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.error-state p {
  color: var(--color-text-muted);
  margin-bottom: 2rem;
}

.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

/* Case Header */
.case-header {
  margin-bottom: 2rem;
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  gap: 2rem;
}

.header-left {
  flex: 1;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: var(--color-brand);
  cursor: pointer;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  padding: 0.5rem 0;
  transition: opacity 0.2s ease;
}

.back-btn:hover {
  opacity: 0.8;
}

.back-btn svg {
  width: 16px;
  height: 16px;
}

.case-title-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.case-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0;
  line-height: 1.2;
}

.case-id {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  font-family: monospace;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1rem;
}

.case-status {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-in-progress {
  background: #fef3c7;
  color: #92400e;
}

.status-resolved {
  background: #d1fae5;
  color: #065f46;
}

.case-actions {
  display: flex;
  gap: 0.75rem;
}

.case-meta {
  padding: 1.5rem;
  background: var(--color-background-soft);
  border-radius: 12px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.meta-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.meta-value {
  font-size: 0.875rem;
  color: var(--color-text);
  font-weight: 500;
}

/* Case Body */
.case-sections {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-section {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.description-content {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
}

.description-content p {
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
}

.no-description {
  color: var(--color-text-muted);
  font-style: italic;
}

/* Participants */
.participants-grid {
  display: grid;
  gap: 1.5rem;
}

.participant-card {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
}

.participant-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.participant-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.lawyer-icon {
  background: #e0f2fe;
  color: #0277bd;
}

.client-icon {
  background: #f3e5f5;
  color: #7b1fa2;
}

.participant-icon svg {
  width: 24px;
  height: 24px;
}

.participant-role {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 0.25rem 0;
}

.participant-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.participant-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.detail-label {
  font-weight: 500;
  color: var(--color-text-muted);
  min-width: 50px;
}

.detail-value {
  color: var(--color-text);
}

/* Expandable Sections */
.expandable-section {
  margin-bottom: 1.5rem;
}

.expandable-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 1rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.expandable-header-left {
  flex-grow: 1;
}

.expandable-header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.expandable-header .btn-sm {
    padding: 0.25rem 0.75rem;
    font-size: 0.875rem;
}

.expandable-header .btn-sm svg {
    width: 16px;
    height: 16px;
    margin-right: 0.25rem;
    display: inline-block;
    vertical-align: middle;
}

.expandable-header:hover {
  background: var(--color-background-soft);
  border-color: var(--color-brand);
}

.expandable-header .section-title {
  margin: 0;
  font-size: 1.1rem;
}

.expand-icon {
  width: 24px;
  height: 24px;
  transition: transform 0.2s ease;
  color: var(--color-text-muted);
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.expandable-content {
  margin-top: 0.5rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
}

/* Sidebar - keeping for potential future use */
.sidebar-section {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.sidebar-section:last-child {
  margin-bottom: 0;
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

/* Placeholders */
.placeholder-content {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--color-text-muted);
}

.placeholder-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 1rem;
  opacity: 0.5;
}

.placeholder-content p {
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.placeholder-note {
  font-size: 0.75rem;
  opacity: 0.7;
}

/* Responsive */
@media (max-width: 768px) {
  .case-sections {
    gap: 1rem;
  }
  
  .expandable-header {
    padding: 0.75rem;
  }
  
  .expandable-content {
    padding: 1rem;
  }
}

@media (max-width: 768px) {
  .case-detail {
    padding: 1rem 0;
  }

  .header-main {
    flex-direction: column;
    gap: 1.5rem;
  }

  .header-right {
    align-items: stretch;
  }

  .case-actions {
    flex-direction: column;
  }

  .case-title {
    font-size: 2rem;
  }

  .meta-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .participants-grid {
    gap: 1rem;
  }

  .participant-header {
    flex-direction: column;
    text-align: center;
    align-items: center;
  }
}
</style> 