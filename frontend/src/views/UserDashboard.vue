<template>
  <div class="user-dashboard">
    <!-- Header Section -->
    <section class="dashboard-header">
      <div class="container">
        <div class="header-content">
          <div class="welcome-section">
            <h1 class="welcome-title">Welcome back, {{ userName }}!</h1>
            <p class="welcome-subtitle">How can we help with your legal matters today?</p>
          </div>
          <div class="header-actions">
            <button @click="router.push('/cases')" class="btn btn-primary">My Cases</button>
            <button class="btn btn-secondary">Schedule Consultation</button>
          </div>
        </div>
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="quick-actions-section section">
      <div class="container">
        <h2 class="section-title">Quick Actions</h2>
        <div class="quick-actions-grid grid grid-4">
          <div class="quick-action-card stagger-item" @click="openAIChat">
            <div class="action-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M12 2L2 7L12 12L22 7L12 2Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M2 17L12 22L22 17"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M2 12L12 17L22 12"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <h3>AI Legal Assistant</h3>
            <p>Get instant legal guidance</p>
          </div>

          <div class="quick-action-card stagger-item" @click="findLawyer">
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
              </svg>
            </div>
            <h3>Find a Lawyer</h3>
            <p>Connect with legal experts</p>
          </div>

          <div class="quick-action-card stagger-item" @click="uploadDocument">
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
              </svg>
            </div>
            <h3>Upload Documents</h3>
            <p>Share legal documents</p>
          </div>

          <div class="quick-action-card stagger-item" @click="scheduleAppointment">
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
              </svg>
            </div>
            <h3>Schedule Meeting</h3>
            <p>Book consultation time</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Dashboard Content -->
    <section class="dashboard-content section">
      <div class="container">
        <div class="dashboard-grid">
          <!-- AI Chat Section -->
          <div class="dashboard-card chat-section">
            <div class="card-header">
              <h3>AI Legal Assistant</h3>
              <div class="status-indicator">
                <span class="status-dot online"></span>
                Online
              </div>
            </div>
            <div class="chat-messages" ref="chatMessages">
              <div class="message ai-message">
                <div class="message-avatar">AI</div>
                <div class="message-content">
                  Hello! I'm here to help with your legal questions. What would you like to know?
                </div>
              </div>
              <div class="message user-message" v-for="message in chatHistory" :key="message.id">
                <div class="message-content">{{ message.text }}</div>
                <div class="message-avatar">You</div>
              </div>
            </div>
            <div class="chat-input">
              <input
                type="text"
                v-model="newMessage"
                @keyup.enter="sendMessage"
                placeholder="Type your legal question..."
              />
              <button class="send-btn" @click="sendMessage">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </div>
          </div>

          <!-- Active Cases -->
          <div class="dashboard-card">
            <div class="card-header">
              <h3>Active Cases</h3>
              <button @click="router.push('/cases')" class="btn btn-outline btn-sm">View All</button>
            </div>
            <div class="cases-list">
              <div v-for="legalCase in activeCases" :key="legalCase.id" class="case-item">
                <div class="case-info">
                  <h4 class="case-title">{{ legalCase.title }}</h4>
                  <p class="case-type">{{ legalCase.type }}</p>
                  <p class="case-status" :class="legalCase.status">{{ legalCase.status }}</p>
                </div>
                <div class="case-actions">
                  <button class="btn btn-outline btn-sm">View Details</button>
                </div>
              </div>
              <div v-if="activeCases.length === 0" class="empty-state">
                <p>No active cases yet</p>
                <button class="btn btn-primary btn-sm">Start a Case</button>
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
                  <p class="document-date">{{ doc.date }}</p>
                </div>
                <button class="btn btn-outline btn-sm">View</button>
              </div>
              <div v-if="recentDocuments.length === 0" class="empty-state">
                <p>No documents uploaded yet</p>
                <button class="btn btn-primary btn-sm">Upload Document</button>
              </div>
            </div>
          </div>

          <!-- Upcoming Appointments -->
          <div class="dashboard-card">
            <div class="card-header">
              <h3>Upcoming Appointments</h3>
              <button class="btn btn-outline btn-sm">Schedule New</button>
            </div>
            <div class="appointments-list">
              <div
                v-for="appointment in upcomingAppointments"
                :key="appointment.id"
                class="appointment-item"
              >
                <div class="appointment-time">
                  <div class="time">{{ appointment.time }}</div>
                  <div class="date">{{ appointment.date }}</div>
                </div>
                <div class="appointment-info">
                  <h4 class="appointment-title">{{ appointment.title }}</h4>
                  <p class="appointment-lawyer">with {{ appointment.lawyer }}</p>
                </div>
                <button class="btn btn-outline btn-sm">Join</button>
              </div>
              <div v-if="upcomingAppointments.length === 0" class="empty-state">
                <p>No upcoming appointments</p>
                <button class="btn btn-primary btn-sm">Schedule Meeting</button>
              </div>
            </div>
          </div>

          <!-- My Calendar -->
          <MyCalendarSection />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import MyCalendarSection from '../components/MyCalendarSection.vue'

const authStore = useAuthStore()
const router = useRouter()
const userName = computed(() => {
  if (authStore.userInfo?.firstName && authStore.userInfo?.lastName) {
    return `${authStore.userInfo.firstName} ${authStore.userInfo.lastName}`
  }
  return authStore.userInfo?.firstName || authStore.userInfo?.email || 'User'
})
const newMessage = ref('')
const chatHistory = ref([])
const chatMessages = ref(null)

const activeCases = ref([
  {
    id: 1,
    title: 'Contract Dispute - ABC Corp',
    type: 'Business Law',
    status: 'active',
  },
  {
    id: 2,
    title: 'Employment Agreement Review',
    type: 'Employment Law',
    status: 'pending',
  },
])

const recentDocuments = ref([
  {
    id: 1,
    name: 'Contract_ABC_Corp.pdf',
    date: '2 days ago',
  },
  {
    id: 2,
    name: 'Employment_Agreement.docx',
    date: '1 week ago',
  },
  {
    id: 3,
    name: 'Legal_Consultation_Notes.pdf',
    date: '2 weeks ago',
  },
])

const upcomingAppointments = ref([
  {
    id: 1,
    title: 'Contract Review Consultation',
    lawyer: 'Sarah Johnson',
    date: 'Tomorrow',
    time: '2:00 PM',
  },
  {
    id: 2,
    title: 'Employment Law Discussion',
    lawyer: 'Michael Chen',
    date: 'Dec 15',
    time: '10:00 AM',
  },
])

const sendMessage = async () => {
  if (!newMessage.value.trim()) return

  const message = {
    id: Date.now(),
    text: newMessage.value,
  }

  chatHistory.value.push(message)
  newMessage.value = ''

  // Simulate AI response
  setTimeout(() => {
    const aiResponse = {
      id: Date.now() + 1,
      text: "Thank you for your question. I'm analyzing your request and will provide you with relevant legal information shortly.",
      isAI: true,
    }
    chatHistory.value.push(aiResponse)
    scrollToBottom()
  }, 1000)

  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}

const openAIChat = () => {
  // Focus on chat input
  const chatInput = document.querySelector('.chat-input input')
  if (chatInput) chatInput.focus()
}

const findLawyer = () => {
  alert('Redirecting to lawyer search...')
}

const uploadDocument = () => {
  alert('Opening document upload...')
}

const scheduleAppointment = () => {
  alert('Opening appointment scheduler...')
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.user-dashboard {
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

.quick-actions-section {
  background: var(--color-background-soft);
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
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.dashboard-card {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
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

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: #10b981;
}

/* Chat Section */
.chat-section {
  grid-row: span 2;
}

.chat-messages {
  height: 300px;
  overflow-y: auto;
  padding: 1rem;
  background: var(--color-background);
}

.message {
  display: flex;
  margin-bottom: 1rem;
  gap: 0.75rem;
}

.message.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.message-content {
  background: var(--color-background-soft);
  padding: 0.75rem 1rem;
  border-radius: var(--border-radius);
  max-width: 70%;
  font-size: 0.875rem;
  line-height: 1.4;
  border: 1px solid var(--color-border);
}

.message.user-message .message-content {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
}

.chat-input {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid var(--color-border);
  background: var(--color-background-soft);
}

.chat-input input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
}

.chat-input input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(150, 182, 197, 0.1);
}

.send-btn {
  width: 40px;
  height: 40px;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.send-btn:hover {
  background: var(--color-secondary);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.send-btn svg {
  width: 16px;
  height: 16px;
}

/* Lists */
.cases-list,
.documents-list,
.appointments-list {
  padding: 1rem;
  background: var(--color-background);
}

.case-item,
.document-item,
.appointment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid var(--color-border);
}

.case-item:last-child,
.document-item:last-child,
.appointment-item:last-child {
  border-bottom: none;
}

.case-title,
.document-title,
.appointment-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.case-type,
.document-date,
.appointment-lawyer {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.case-status {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  text-transform: uppercase;
}

.case-status.active {
  background: #dcfce7;
  color: #166534;
}

.case-status.pending {
  background: #fef3c7;
  color: #92400e;
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

.appointment-time {
  text-align: center;
  margin-right: 1rem;
}

.time {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-heading);
}

.date {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.appointment-info {
  flex: 1;
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

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .chat-section {
    grid-row: span 1;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chat-messages {
    height: 250px;
  }
}
</style>
