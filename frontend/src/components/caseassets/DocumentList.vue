<template>
  <div class="document-list">
    <div class="list-header">
      <h3>Documents</h3>
      <button @click="showUploadModal = true; scrollToModal();" class="btn btn-primary btn-sm">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path d="M9.25 13.25a.75.75 0 001.5 0V4.79l2.97 2.97a.75.75 0 001.06-1.06l-4.25-4.25a.75.75 0 00-1.06 0L5.22 6.72a.75.75 0 001.06 1.06l2.97-2.97v8.46z" />
          <path d="M3.5 12.75a.75.75 0 00-1.5 0v2.5A2.75 2.75 0 004.75 18h10.5A2.75 2.75 0 0018 15.25v-2.5a.75.75 0 00-1.5 0v2.5c0 .69-.56 1.25-1.25 1.25H4.75c-.69 0-1.25-.56-1.25-1.25v-2.5z" />
        </svg>
        Upload Document
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading documents...</p>
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
      <button @click="loadDocuments" class="btn btn-primary btn-sm">Try Again</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="documents.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2"/>
          <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2"/>
          <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2"/>
          <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2"/>
          <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <p>No documents found</p>
      <span class="empty-note">Documents will appear here when uploaded</span>
    </div>

    <!-- Document List -->
    <div v-else class="documents-container">
      <div class="document-item" v-for="document in documents" :key="document.documentId">
        <div class="document-header">
          <div class="document-title">
            <h4>{{ document.title }}</h4>
            <span class="document-privacy" :class="getPrivacyClass(document.privacy)">
              {{ getPrivacyDisplayName(document.privacy) }}
            </span>
          </div>
          <div class="document-actions">
            <button @click="viewDocument(document)" class="btn btn-sm btn-outline">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              View
            </button>
            <button v-if="isOwner(document)" @click="openEditModal(document)" class="btn btn-sm btn-outline">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Edit
            </button>
            <button v-if="isOwner(document)" @click="openDeleteModal(document)" class="btn btn-sm btn-danger">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Delete
            </button>
          </div>
        </div>
        
        <div v-if="document.description" class="document-description">
          <p>{{ document.description }}</p>
        </div>

        <div class="document-meta">
          <div class="meta-item">
            <span class="meta-label">Uploaded by:</span>
            <span class="meta-value">{{ document.uploadedByName }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Uploaded:</span>
            <span class="meta-value">{{ formatDateTime(document.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <UploadDocumentModal
      v-if="showUploadModal"
      :case-id="caseId"
      @close="showUploadModal = false"
      @document-uploaded="handleDocumentUploaded"
    />

    <EditDocumentModal
      v-if="showEditModal"
      :document="selectedDocument"
      @close="showEditModal = false"
      @document-updated="handleDocumentUpdated"
    />

    <DocumentViewer
      :show="showViewer"
      :title="viewedDocumentTitle"
      :document-data="viewedDocumentData"
      @close="closeViewer"
      :auto-load="true"
    />

    <DeleteDocumentConfirmationModal
      v-if="showDeleteModal"
      :document-title="selectedDocument?.title"
      @confirm="deleteDocument"
      @cancel="closeDeleteModal"
    />
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { caseAssetsAPI } from '../../services/api'
import { useAuthStore } from '../../stores/auth'
import DeleteDocumentConfirmationModal from './DeleteDocumentConfirmationModal.vue'
import DocumentViewer from './DocumentViewer.vue'
import EditDocumentModal from './EditDocumentModal.vue'
import UploadDocumentModal from './UploadDocumentModal.vue'

// Props
const props = defineProps({
  caseId: {
    type: String,
    required: true
  }
})

// Reactive data
const documents = ref([])
const loading = ref(false)
const error = ref('')
const showUploadModal = ref(false)
const showEditModal = ref(false)
const showViewer = ref(false)
const showDeleteModal = ref(false)
const selectedDocument = ref(null)
const viewedDocumentData = ref(null)
const viewedDocumentTitle = ref('')
const authStore = useAuthStore()

const scrollToModal = () => {
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
      modal.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
};

// Methods
const loadDocuments = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await caseAssetsAPI.getAllDocumentsForCase(props.caseId)
    documents.value = response.data?.documents || []
  } catch (err) {
    console.error('Error loading documents:', err)
    error.value = err.response?.data?.message || 'Failed to load documents'
  } finally {
    loading.value = false
  }
}

const viewDocument = async (document) => {
  viewedDocumentTitle.value = document.title
  showViewer.value = true
  scrollToModal()
  try {
    const blob = await caseAssetsAPI.viewDocument(document.documentId)
    viewedDocumentData.value = blob
  } catch (err) {
    console.error('Error viewing document:', err)
    viewedDocumentData.value = { error: 'Failed to load document.' }
  }
}

const openEditModal = (document) => {
  selectedDocument.value = document
  showEditModal.value = true
  scrollToModal()
}

const openDeleteModal = (document) => {
  selectedDocument.value = document
  showDeleteModal.value = true
  scrollToModal()
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  selectedDocument.value = null
}

const deleteDocument = async () => {
  if (!selectedDocument.value) return
  try {
    await caseAssetsAPI.deleteDocument(selectedDocument.value.documentId)
    closeDeleteModal()
    loadDocuments()
  } catch (err) {
    console.error('Error deleting document:', err)
    alert(err.response?.data?.message || 'Failed to delete document.')
  }
}

const closeViewer = () => {
  showViewer.value = false
  viewedDocumentData.value = null
  viewedDocumentTitle.value = ''
}

const handleDocumentUploaded = () => {
  showUploadModal.value = false
  loadDocuments()
}

const handleDocumentUpdated = () => {
  showEditModal.value = false
  loadDocuments()
}

const isOwner = (document) => {
  return authStore.userInfo?.id === document.uploadedById
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

// Lifecycle
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.document-list {
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

.documents-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.document-item {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.2s ease;
}

.document-item:hover {
  border-color: var(--color-brand);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.document-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 1rem;
}

.document-title h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.document-privacy {
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

.document-actions {
  display: flex;
  gap: 0.5rem;
}

.document-description {
  margin-bottom: 0.75rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: 6px;
}

.document-description p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
}

.document-meta {
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
  .document-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .document-meta {
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