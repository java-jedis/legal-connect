<template>
  <div class="note-list">
    <div class="list-header">
      <h3>Notes</h3>
      <button @click="showCreateModal = true" class="btn btn-primary btn-sm">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
        </svg>
        Add Note
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading notes...</p>
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
      <button @click="loadNotes" class="btn btn-primary btn-sm">Try Again</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="notes.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2"/>
          <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2"/>
          <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2"/>
          <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2"/>
          <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <p>No notes found</p>
      <span class="empty-note">Notes will appear here when created</span>
    </div>

    <!-- Note List -->
    <div v-else class="notes-container">
      <div class="note-item" v-for="note in notes" :key="note.noteId">
        <div class="note-header">
          <div class="note-title">
            <h4>{{ note.title }}</h4>
            <span class="note-privacy" :class="getPrivacyClass(note.privacy)">
              {{ getPrivacyDisplayName(note.privacy) }}
            </span>
          </div>
          <div class="note-actions">
            <button @click="openViewModal(note)" class="btn btn-sm btn-outline">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              View
            </button>
            <button v-if="isOwner(note)" @click="openEditModal(note)" class="btn btn-sm btn-outline">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Edit
            </button>
            <button v-if="isOwner(note)" @click="openDeleteModal(note)" class="btn btn-sm btn-danger">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Delete
            </button>
          </div>
        </div>
        
        

        <div class="note-meta">
          <div class="meta-item">
            <span class="meta-label">Created by:</span>
            <span class="meta-value">{{ getUploaderName(note.ownerId) }}</span>
          </div>
        </div>
      </div>
    </div>

    <CreateNoteModal
      v-if="showCreateModal"
      :case-id="caseId"
      @close="showCreateModal = false"
      @note-created="handleNoteCreated"
    />

    <EditNoteModal
      v-if="showEditModal"
      :note="selectedNote"
      @close="showEditModal = false"
      @note-updated="handleNoteUpdated"
    />

    <DeleteNoteConfirmationModal
      v-if="showDeleteModal"
      :note-title="selectedNote?.title"
      @confirm="deleteNote"
      @cancel="closeDeleteModal"
    />

    <ViewNoteModal
      v-if="showViewModal"
      :note="selectedNote"
      @close="showViewModal = false"
    />
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { caseAssetsAPI } from '../../services/api'
import { useAuthStore } from '../../stores/auth'
import CreateNoteModal from './CreateNoteModal.vue'
import DeleteNoteConfirmationModal from './DeleteNoteConfirmationModal.vue'
import EditNoteModal from './EditNoteModal.vue'
import ViewNoteModal from './ViewNoteModal.vue'

// Props
const props = defineProps({
  caseId: {
    type: String,
    required: true
  },
  caseData: {
    type: Object,
    required: true
  }
})

// Reactive data
const notes = ref([])
const loading = ref(false)
const error = ref('')
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const showViewModal = ref(false)
const selectedNote = ref(null)
const authStore = useAuthStore()

// Methods
const loadNotes = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await caseAssetsAPI.getAllNotesForCase(props.caseId)
    notes.value = response.data?.notes || []
  } catch (err) {
    console.error('Error loading notes:', err)
    error.value = err.response?.data?.message || 'Failed to load notes'
  } finally {
    loading.value = false
  }
}

const openViewModal = (note) => {
  selectedNote.value = note
  showViewModal.value = true
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
      modal.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
}

const openEditModal = (note) => {
  selectedNote.value = note
  showEditModal.value = true
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
      modal.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
}

const openDeleteModal = (note) => {
  selectedNote.value = note
  showDeleteModal.value = true
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
      modal.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  selectedNote.value = null
}

const deleteNote = async () => {
  if (!selectedNote.value) return
  try {
    await caseAssetsAPI.deleteNote(selectedNote.value.noteId)
    closeDeleteModal()
    loadNotes()
  } catch (err) {
    console.error('Error deleting note:', err)
    alert(err.response?.data?.message || 'Failed to delete note.')
  }
}

const handleNoteCreated = () => {
  showCreateModal.value = false
  loadNotes()
}

const handleNoteUpdated = () => {
  showEditModal.value = false
  loadNotes()
}

const isOwner = (note) => {
  return authStore.userInfo?.id === note.ownerId
}

const getPrivacyClass = (privacy) => {
  return {
    'privacy-public': privacy === 'PUBLIC',
    'privacy-private': privacy === 'PRIVATE',
    'privacy-shared': privacy === 'SHARED'
  }
}

const getPrivacyDisplayName = (privacy) => {
  const privacyMap = {
    'PUBLIC': 'Public',
    'PRIVATE': 'Private',
    'SHARED': 'Shared'
  }
  return privacyMap[privacy] || privacy
}

const getUploaderName = (ownerId) => {
  if (!props.caseData) return 'Unknown'
  
  // If ownerId equals client ID, show client name, otherwise show lawyer name
  if (props.caseData.client && props.caseData.client.id === ownerId) {
    return `${props.caseData.client.firstName} ${props.caseData.client.lastName}`
  } else {
    return `${props.caseData.lawyer.firstName} ${props.caseData.lawyer.lastName}`
  }
}

// Lifecycle
onMounted(() => {
  loadNotes()
})
</script>

<style scoped>
.note-list {
  width: 100%;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.list-header h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.list-header .btn-sm {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

.notes-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.note-item {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.2s ease;
}

.note-item:hover {
  border-color: var(--color-brand);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 1rem;
}

.note-title h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.note-privacy {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.privacy-public {
  background: #dcfce7;
  color: #166534;
}

.privacy-private {
  background: #fef3c7;
  color: #92400e;
}

.privacy-shared {
  background: #dbeafe;
  color: #1e40af;
}

.note-actions {
  display: flex;
  gap: 0.5rem; /* Added spacing */
}

.note-content {
  margin-bottom: 0.75rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: 6px;
}

.note-content p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
  white-space: pre-wrap;
}

.note-meta {
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
  .note-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .note-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

.btn-danger {
  background-color: #fee2e2;
  color: #ef4444;
  border: 1px solid #fee2e2;
}

.btn-danger:hover {
  background-color: #fecaca;
}
</style> 