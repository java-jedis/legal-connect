<template>
  <div class="lawyer-dashboard">
    <!-- Header Section -->
    <section class="dashboard-header">
      <div class="container">
        <div class="header-content">
          <div class="welcome-section">
            <h1 class="welcome-title">Welcome back, {{ lawyerName }}!</h1>
            <p class="welcome-subtitle">Manage your practice and serve your clients effectively</p>
          </div>
          <div class="header-actions">
            <button class="btn btn-primary">New Client</button>
            <button class="btn btn-secondary">Schedule Consultation</button>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Overview -->
    <section class="stats-section section">
      <div class="container">
        <div class="stats-grid grid grid-4">
          <div class="stat-card">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M16 21V19A4 4 0 0 0 12 15H8A4 4 0 0 0 4 19V21"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.activeClients }}</h3>
              <p class="stat-label">Active Clients</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <polyline
                  points="14,2 14,8 20,8"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.activeCases }}</h3>
              <p class="stat-label">Active Cases</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect
                  x="3"
                  y="4"
                  width="18"
                  height="18"
                  rx="2"
                  ry="2"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2" />
                <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2" />
                <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.appointmentsToday }}</h3>
              <p class="stat-label">Today's Appointments</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M12 1V23M17 5H9.5A3.5 3.5 0 0 0 6 8.5V17A3.5 3.5 0 0 0 9.5 20.5H14.5A3.5 3.5 0 0 0 18 17V8.5A3.5 3.5 0 0 0 14.5 5H17Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-number">${{ stats.monthlyEarnings }}</h3>
              <p class="stat-label">Monthly Earnings</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="quick-actions-section section">
      <div class="container">
        <h2 class="section-title">Quick Actions</h2>
        <div class="quick-actions-grid grid grid-4">
          <div class="quick-action-card" @click="addNewClient">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M16 21V19A4 4 0 0 0 12 15H8A4 4 0 0 0 4 19V21"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" />
                <line x1="19" y1="8" x2="19" y2="14" stroke="currentColor" stroke-width="2" />
                <line x1="16" y1="11" x2="22" y2="11" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <h3>Add New Client</h3>
            <p>Register a new client</p>
          </div>

          <div class="quick-action-card" @click="createCase">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <polyline
                  points="14,2 14,8 20,8"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" />
                <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" />
                <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <h3>Create Case</h3>
            <p>Start a new legal case</p>
          </div>

          <div class="quick-action-card" @click="scheduleMeeting">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect
                  x="3"
                  y="4"
                  width="18"
                  height="18"
                  rx="2"
                  ry="2"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2" />
                <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2" />
                <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2" />
                <line x1="8" y1="14" x2="8" y2="14" stroke="currentColor" stroke-width="2" />
                <line x1="12" y1="14" x2="12" y2="14" stroke="currentColor" stroke-width="2" />
                <line x1="16" y1="14" x2="16" y2="14" stroke="currentColor" stroke-width="2" />
                <line x1="8" y1="18" x2="8" y2="18" stroke="currentColor" stroke-width="2" />
                <line x1="12" y1="18" x2="12" y2="18" stroke="currentColor" stroke-width="2" />
                <line x1="16" y1="18" x2="16" y2="18" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <h3>Schedule Meeting</h3>
            <p>Book client consultation</p>
          </div>

          <div class="quick-action-card" @click="generateDocument">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <polyline
                  points="14,2 14,8 20,8"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" />
                <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" />
                <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <h3>Generate Document</h3>
            <p>Create legal documents</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Dashboard Content -->
    <section class="dashboard-content section">
      <div class="container">
        <div class="dashboard-grid">
          <!-- Recent Clients -->
          <div class="dashboard-card">
            <div class="card-header">
              <h3>Recent Clients</h3>
              <button class="btn btn-outline btn-sm">View All</button>
            </div>
            <div class="clients-list">
              <div v-for="client in recentClients" :key="client.id" class="client-item">
                <div class="client-avatar">
                  <span>{{ client.name.charAt(0) }}</span>
                </div>
                <div class="client-info">
                  <h4 class="client-name">{{ client.name }}</h4>
                  <p class="client-case">{{ client.caseType }}</p>
                  <p class="client-status" :class="client.status">{{ client.status }}</p>
                </div>
                <div class="client-actions">
                  <button class="btn btn-outline btn-sm">View Profile</button>
                </div>
              </div>
              <div v-if="recentClients.length === 0" class="empty-state">
                <p>No clients yet</p>
                <button class="btn btn-primary btn-sm">Add Client</button>
              </div>
            </div>
          </div>

          <!-- Active Cases -->
          <div class="dashboard-card">
            <div class="card-header">
              <h3>Active Cases</h3>
              <button class="btn btn-outline btn-sm">View All</button>
            </div>
            <div class="cases-list">
              <div v-for="legalCase in activeCases" :key="legalCase.id" class="case-item">
                <div class="case-info">
                  <h4 class="case-title">{{ legalCase.title }}</h4>
                  <p class="case-client">Client: {{ legalCase.client }}</p>
                  <p class="case-status" :class="legalCase.status">{{ legalCase.status }}</p>
                </div>
                <div class="case-actions">
                  <button class="btn btn-outline btn-sm">View Details</button>
                </div>
              </div>
              <div v-if="activeCases.length === 0" class="empty-state">
                <p>No active cases</p>
                <button class="btn btn-primary btn-sm">Create Case</button>
              </div>
            </div>
          </div>

          <!-- Today's Appointments -->
          <div class="dashboard-card">
            <div class="card-header">
              <h3>Today's Appointments</h3>
              <button class="btn btn-outline btn-sm">Schedule New</button>
            </div>
            <div class="appointments-list">
              <div
                v-for="appointment in todaysAppointments"
                :key="appointment.id"
                class="appointment-item"
              >
                <div class="appointment-time">
                  <div class="time">{{ appointment.time }}</div>
                  <div class="duration">{{ appointment.duration }}</div>
                </div>
                <div class="appointment-info">
                  <h4 class="appointment-title">{{ appointment.title }}</h4>
                  <p class="appointment-client">with {{ appointment.client }}</p>
                </div>
                <button class="btn btn-outline btn-sm">Join</button>
              </div>
              <div v-if="todaysAppointments.length === 0" class="empty-state">
                <p>No appointments today</p>
                <button class="btn btn-primary btn-sm">Schedule Meeting</button>
              </div>
            </div>
          </div>

          <!-- Recent Documents -->
          <div class="dashboard-card">
            <div class="card-header">
              <h3>Recent Documents</h3>
              <button class="btn btn-outline btn-sm">View All</button>
            </div>
            <div class="documents-list">
              <div v-for="doc in recentDocuments" :key="doc.id" class="document-item">
                <div class="document-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <polyline
                      points="14,2 14,8 20,8"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
                <div class="document-info">
                  <h4 class="document-title">{{ doc.name }}</h4>
                  <p class="document-client">Client: {{ doc.client }}</p>
                  <p class="document-date">{{ doc.date }}</p>
                </div>
                <button class="btn btn-outline btn-sm">View</button>
              </div>
              <div v-if="recentDocuments.length === 0" class="empty-state">
                <p>No documents yet</p>
                <button class="btn btn-primary btn-sm">Generate Document</button>
              </div>
            </div>
          </div>

          <!-- Earnings Overview -->
          <div class="dashboard-card earnings-card">
            <div class="card-header">
              <h3>Earnings Overview</h3>
              <button class="btn btn-outline btn-sm">View Details</button>
            </div>
            <div class="earnings-content">
              <div class="earnings-chart">
                <div class="chart-bar" v-for="(earning, month) in monthlyEarnings" :key="month">
                  <div class="bar" :style="{ height: (earning / maxEarning) * 100 + '%' }"></div>
                  <span class="month">{{ month }}</span>
                </div>
              </div>
              <div class="earnings-summary">
                <div class="summary-item">
                  <span class="label">This Month</span>
                  <span class="amount">${{ stats.monthlyEarnings }}</span>
                </div>
                <div class="summary-item">
                  <span class="label">Last Month</span>
                  <span class="amount">${{ stats.lastMonthEarnings }}</span>
                </div>
                <div class="summary-item">
                  <span class="label">Total This Year</span>
                  <span class="amount">${{ stats.yearlyEarnings }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const lawyerName = computed(() => {
  if (authStore.userInfo?.firstName && authStore.userInfo?.lastName) {
    return `${authStore.userInfo.firstName} ${authStore.userInfo.lastName}`
  }
  return authStore.userInfo?.firstName || authStore.userInfo?.email || 'Lawyer'
})

const stats = ref({
  activeClients: 24,
  activeCases: 18,
  appointmentsToday: 5,
  monthlyEarnings: '12,450',
  lastMonthEarnings: '11,200',
  yearlyEarnings: '145,800',
})

const recentClients = ref([
  {
    id: 1,
    name: 'John Smith',
    caseType: 'Contract Dispute',
    status: 'active',
  },
  {
    id: 2,
    name: 'Emily Davis',
    caseType: 'Employment Law',
    status: 'pending',
  },
  {
    id: 3,
    name: 'Michael Brown',
    caseType: 'Real Estate',
    status: 'active',
  },
])

const activeCases = ref([
  {
    id: 1,
    title: 'Contract Dispute - ABC Corp',
    client: 'John Smith',
    status: 'active',
  },
  {
    id: 2,
    title: 'Employment Agreement Review',
    client: 'Emily Davis',
    status: 'pending',
  },
  {
    id: 3,
    title: 'Property Purchase Agreement',
    client: 'Michael Brown',
    status: 'active',
  },
])

const todaysAppointments = ref([
  {
    id: 1,
    title: 'Contract Review Consultation',
    client: 'John Smith',
    time: '10:00 AM',
    duration: '1 hour',
  },
  {
    id: 2,
    title: 'Employment Law Discussion',
    client: 'Emily Davis',
    time: '2:00 PM',
    duration: '45 min',
  },
  {
    id: 3,
    title: 'Property Closing Review',
    client: 'Michael Brown',
    time: '4:30 PM',
    duration: '1.5 hours',
  },
])

const recentDocuments = ref([
  {
    id: 1,
    name: 'Contract_Review_ABC_Corp.pdf',
    client: 'John Smith',
    date: '2 days ago',
  },
  {
    id: 2,
    name: 'Employment_Agreement_Draft.docx',
    client: 'Emily Davis',
    date: '1 week ago',
  },
  {
    id: 3,
    name: 'Property_Closing_Documents.pdf',
    client: 'Michael Brown',
    date: '2 weeks ago',
  },
])

const monthlyEarnings = ref({
  Jan: 8500,
  Feb: 9200,
  Mar: 10800,
  Apr: 11200,
  May: 12450,
  Jun: 11800,
})

const maxEarning = computed(() => {
  return Math.max(...Object.values(monthlyEarnings.value))
})

const addNewClient = () => {
  alert('Opening new client registration...')
}

const createCase = () => {
  alert('Opening case creation form...')
}

const scheduleMeeting = () => {
  alert('Opening appointment scheduler...')
}

const generateDocument = () => {
  alert('Opening document generator...')
}
</script>

<style scoped>
.lawyer-dashboard {
  min-height: 100vh;
  background: var(--color-background);
}

.dashboard-header {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  padding: 2rem 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.welcome-subtitle {
  font-size: 1.125rem;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.stats-section {
  background: var(--color-background-soft);
}

.stats-grid {
  margin-top: 2rem;
}

.stat-card {
  background: var(--color-background);
  padding: 1.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.quick-actions-section {
  background: var(--color-background);
}

.quick-actions-grid {
  margin-top: 2rem;
}

.quick-action-card {
  background: var(--color-background);
  padding: 1.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.quick-action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
  background: var(--color-background-soft);
}

.action-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 1rem;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  box-shadow: var(--shadow-sm);
}

.action-icon svg {
  width: 24px;
  height: 24px;
}

.quick-action-card h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.quick-action-card p {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.dashboard-content {
  background: var(--color-background);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
}

.dashboard-card {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.earnings-card {
  grid-column: span 2;
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
}

/* Lists */
.clients-list,
.cases-list,
.appointments-list,
.documents-list {
  padding: 1rem;
  background: var(--color-background);
}

.client-item,
.case-item,
.appointment-item,
.document-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid var(--color-border);
}

.client-item:last-child,
.case-item:last-child,
.appointment-item:last-child,
.document-item:last-child {
  border-bottom: none;
}

.client-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 0.75rem;
}

.client-name,
.case-title,
.appointment-title,
.document-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.client-case,
.case-client,
.appointment-client,
.document-client {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.client-status,
.case-status {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  text-transform: uppercase;
}

.client-status.active,
.case-status.active {
  background: #dcfce7;
  color: #166534;
}

.client-status.pending,
.case-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.appointment-time {
  text-align: center;
  margin-right: 1rem;
}

.time {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-heading);
}

.duration {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.document-icon {
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  margin-right: 0.75rem;
}

.document-icon svg {
  width: 16px;
  height: 16px;
}

.document-info {
  flex: 1;
}

.document-date {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-muted);
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  margin: 1rem;
}

.empty-state p {
  margin-bottom: 1rem;
}

/* Earnings Chart */
.earnings-content {
  padding: 1.5rem;
  background: var(--color-background);
}

.earnings-chart {
  display: flex;
  align-items: end;
  justify-content: space-between;
  height: 120px;
  margin-bottom: 2rem;
  padding: 0 1rem;
}

.chart-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 60px;
}

.bar {
  width: 100%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: all var(--transition-normal);
}

.chart-bar:hover .bar {
  opacity: 0.8;
}

.month {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-top: 0.5rem;
}

.earnings-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.summary-item {
  text-align: center;
  padding: 1rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
}

.summary-item .label {
  display: block;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-bottom: 0.5rem;
}

.summary-item .amount {
  display: block;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .earnings-card {
    grid-column: span 1;
  }

  .earnings-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .earnings-chart {
    height: 100px;
  }
}
</style>
