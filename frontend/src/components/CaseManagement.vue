<template>
  <div class="case-management">
    <!-- Header Section -->
    <div class="case-header">
      <div class="header-content">
        <div class="title-section">
          <h2 class="case-title">Case Management</h2>
          <p class="case-subtitle">Manage and track your legal cases</p>
        </div>
        <div class="header-actions" v-if="isLawyer">
          <button @click="showCreateCaseModal = true" class="btn btn-success btn-sm">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            New Case
          </button>
        </div>
      </div>
    </div>

    <!-- Filters Section -->
    <div class="case-filters">
      <div class="filter-tabs">
        <button 
          @click="filterByStatus(null)" 
          :class="['filter-tab', { active: currentFilter === null }]"
        >
          All Cases ({{ totalCases }})
        </button>
        <button 
          @click="filterByStatus('IN_PROGRESS')" 
          :class="['filter-tab', { active: currentFilter === 'IN_PROGRESS' }]"
        >
          In Progress ({{ inProgressCount }})
        </button>
        <button 
          @click="filterByStatus('RESOLVED')" 
          :class="['filter-tab', { active: currentFilter === 'RESOLVED' }]"
        >
          Resolved ({{ resolvedCount }})
        </button>
      </div>
      <div class="filter-actions">
        <button @click="refreshCases" class="btn btn-secondary btn-sm" :disabled="loading">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 3V9H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 12A9 9 0 0 0 6 3L3 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 21V15H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3 12A9 9 0 0 0 18 21L21 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Refresh
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading && cases.length === 0" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading cases...</p>
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
      <h3>Failed to load cases</h3>
      <p>{{ error }}</p>
      <button @click="refreshCases" class="btn btn-primary">Try Again</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="cases.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h3>No cases found</h3>
      <p v-if="isLawyer">Start by creating your first case to manage client matters.</p>
      <p v-else>You don't have any active cases yet.</p>
      <button v-if="isLawyer" @click="showCreateCaseModal = true" class="btn btn-primary">
        Create First Case
      </button>
    </div>

    <!-- Cases List -->
    <div v-else class="cases-list">
      <div class="case-card" v-for="caseItem in cases" :key="caseItem.caseId" @click="viewCase(caseItem.caseId)">
        <div class="case-card-header">
          <div class="case-title-section">
            <h3 class="case-card-title">{{ caseItem.title }}</h3>
            <span class="case-id">Case #{{ caseItem.caseId.substring(0, 8) }}</span>
          </div>
          <div class="case-status" :class="getStatusClass(caseItem.status)">
            {{ getStatusDisplayName(caseItem.status) }}
          </div>
        </div>
        
        <div class="case-card-content">
          <p class="case-description">{{ caseItem.description || 'No description provided' }}</p>
          
          <div class="case-participants">
            <div class="participant">
              <span class="participant-label">Lawyer:</span>
              <span class="participant-name">{{ caseItem.lawyer.firstName }} {{ caseItem.lawyer.lastName }}</span>
              <span v-if="caseItem.lawyer.firm" class="participant-firm">({{ caseItem.lawyer.firm }})</span>
            </div>
            <div class="participant">
              <span class="participant-label">Client:</span>
              <span class="participant-name">{{ caseItem.client.firstName }} {{ caseItem.client.lastName }}</span>
            </div>
          </div>
        </div>
        
        <div class="case-card-footer">
          <div class="case-dates">
            <span class="date-item">
              Created: {{ formatDate(caseItem.createdAt) }}
            </span>
            <span class="date-item">
              Updated: {{ formatDate(caseItem.updatedAt) }}
            </span>
          </div>
          <div class="case-actions">
            <button @click.stop="viewCase(caseItem.caseId)" class="btn btn-sm btn-primary">
              View Details
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Load More Button -->
    <div v-if="hasMorePages && !loading" class="load-more-section">
      <button @click="loadMoreCases" class="btn btn-secondary">
        Load More Cases
      </button>
    </div>

    <!-- Create Case Modal -->
    <CreateCaseModal 
      v-if="showCreateCaseModal" 
      @close="showCreateCaseModal = false"
      @case-created="onCaseCreated"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCaseStore } from '../stores/case'
import CreateCaseModal from './CreateCaseModal.vue'

// Stores
const caseStore = useCaseStore()
const authStore = useAuthStore()
const router = useRouter()

// Reactive data
const showCreateCaseModal = ref(false)
const currentFilter = ref(null)

// Computed properties
const cases = computed(() => caseStore.cases)
const loading = computed(() => caseStore.loading)
const error = computed(() => caseStore.error)
const totalCases = computed(() => caseStore.totalCases)
const hasMorePages = computed(() => caseStore.hasMorePages)
const isLawyer = computed(() => authStore.isLawyer())

const inProgressCount = computed(() => 
  cases.value.filter(c => c.status === 'IN_PROGRESS').length
)

const resolvedCount = computed(() => 
  cases.value.filter(c => c.status === 'RESOLVED').length
)

// Watch for cases changes
watch(cases, () => {
  // Cases updated
}, { deep: true })

// Methods
const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
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

const filterByStatus = async (status) => {
  currentFilter.value = status
  await caseStore.filterCasesByStatus(status)
}

const refreshCases = async () => {
  await caseStore.refreshCases()
}

const loadMoreCases = async () => {
  await caseStore.loadMoreCases()
}

const viewCase = (caseId) => {
  router.push(`/case/${caseId}`)
}

const onCaseCreated = () => {
  showCreateCaseModal.value = false
  // The store will automatically update the cases list
}

// Lifecycle
onMounted(async () => {
  await caseStore.getAllUserCases()
})
</script>

<style scoped>
.case-management {
  padding: 2rem 0;
}

.case-header {
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.case-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0 0 0.5rem 0;
}

.case-subtitle {
  color: var(--color-text-muted);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-success {
  background-color: #4ade80;
  color: white;
  border: 1px solid #4ade80;
}

.btn-success:hover {
  background-color: #22c55e;
  border-color: #22c55e;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

.btn-sm svg {
  width: 16px;
  height: 16px;
}

/* Filters */
.case-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1rem;
  background: var(--color-background-soft);
  border-radius: 12px;
}

.filter-tabs {
  display: flex;
  gap: 0.5rem;
}

.filter-tab {
  padding: 0.5rem 1rem;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  color: var(--color-text-muted);
  font-weight: 500;
  transition: all 0.2s ease;
}

.filter-tab:hover {
  background: var(--color-background-mute);
  color: var(--color-text);
}

.filter-tab.active {
  background: #e0f2fe;
  color: #0369a1 !important;
  font-weight: 600;
}

.filter-actions {
  display: flex;
  gap: 1rem;
}

/* States */
.loading-state, .error-state, .empty-state {
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

.error-icon, .empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-text-muted);
}

.error-state h3, .empty-state h3 {
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.error-state p, .empty-state p {
  color: var(--color-text-muted);
  margin-bottom: 2rem;
}

/* Cases List */
.cases-list {
  display: grid;
  gap: 1.5rem;
}

.case-card {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.case-card:hover {
  border-color: var(--color-brand);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.case-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.case-title-section {
  flex: 1;
}

.case-card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.25rem 0;
}

.case-id {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  font-family: monospace;
}

.case-status {
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
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

.case-card-content {
  margin-bottom: 1.5rem;
}

.case-description {
  color: var(--color-text);
  margin-bottom: 1rem;
  line-height: 1.5;
}

.case-participants {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.participant {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  font-size: 0.875rem;
}

.participant-label {
  font-weight: 600;
  color: var(--color-text-muted);
  min-width: 60px;
}

.participant-name {
  color: var(--color-text);
  font-weight: 500;
}

.participant-firm {
  color: var(--color-text-muted);
  font-style: italic;
}

.case-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}

.case-dates {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.date-item {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.case-actions {
  display: flex;
  gap: 0.5rem;
}

/* Load More */
.load-more-section {
  text-align: center;
  margin-top: 2rem;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }

  .case-filters {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .filter-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }

  .case-card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .case-card-footer {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .case-participants {
    font-size: 0.8rem;
  }
}
</style> 