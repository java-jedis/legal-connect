<template>
  <div class="document-viewer">
    <!-- Document Viewer Modal -->
    <div v-if="show" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ title || 'Document Viewer' }}</h3>
          <div class="modal-actions">
            <button v-if="internalDocumentUrl || documentData" @click="openInNewTab" class="btn btn-outline btn-sm">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="15,3 21,3 21,9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="10" y1="14" x2="21" y2="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Open in New Tab
            </button>
            <button v-if="internalDocumentUrl || documentData" @click="downloadDocument" class="btn btn-outline btn-sm">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="7,10 12,15 17,10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Download
            </button>
            <button @click="closeModal" class="close-btn">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
        
        <div class="document-content">
          <div v-if="shouldShowLoading" class="document-loading">
            <div class="loading-spinner"></div>
            <p>Loading document...</p>
          </div>
          
          <div v-else-if="error" class="document-error">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="48" height="48">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h4>Unable to load document</h4>
            <p>{{ error }}</p>
            <button @click="loadDocument" class="btn btn-primary">Try Again</button>
          </div>
          
          <iframe 
            v-else-if="internalDocumentUrl" 
            :src="internalDocumentUrl" 
            class="document-iframe"
            :title="title || 'Document'"
          ></iframe>
          
          <div v-else-if="showLoadButton && !autoLoad" class="document-placeholder">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="64" height="64">
              <path d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h4>Document not loaded</h4>
            <p>{{ placeholderText || 'Click the button below to load the document.' }}</p>
            <button @click="loadDocument" class="btn btn-primary">
              {{ loadButtonText || 'Load Document' }}
            </button>
          </div>
          
          <div v-else class="document-placeholder">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="64" height="64">
              <path d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h4>No document available</h4>
            <p>{{ placeholderText || 'No document has been uploaded yet.' }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Document Viewer'
  },
  documentUrl: {
    type: String,
    default: null
  },
  documentData: {
    type: [Blob, ArrayBuffer, Object],
    default: null
  },
  documentType: {
    type: String,
    default: 'application/pdf'
  },
  fileName: {
    type: String,
    default: 'document'
  },
  placeholderText: {
    type: String,
    default: 'No document has been uploaded yet.'
  },
  showLoadButton: {
    type: Boolean,
    default: false
  },
  loadButtonText: {
    type: String,
    default: 'Load Document'
  },
  autoLoad: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'load', 'download'])

const isLoading = ref(false)
const error = ref('')
const internalDocumentUrl = ref('')

// Computed property to determine if we should show loading state
const shouldShowLoading = computed(() => {
  return isLoading.value || (props.autoLoad && !props.documentData && !internalDocumentUrl.value && !error.value)
})

const closeModal = () => {
  emit('close')
  // Clean up the blob URL to prevent memory leaks
  if (internalDocumentUrl.value) {
    window.URL.revokeObjectURL(internalDocumentUrl.value)
    internalDocumentUrl.value = ''
  }
  error.value = ''
  isLoading.value = false
}

const loadDocument = async () => {
  isLoading.value = true
  error.value = ''
  
  try {
    if (props.documentData) {
      // Check if documentData contains an error
      if (props.documentData.error) {
        error.value = props.documentData.error
        return
      }
      // If document data is provided directly
      const blob = new Blob([props.documentData], { type: props.documentType })
      internalDocumentUrl.value = window.URL.createObjectURL(blob)
    } else if (props.documentUrl) {
      // If document URL is provided
      internalDocumentUrl.value = props.documentUrl
    } else {
      // Emit load event for parent to handle
      emit('load')
      // Don't set isLoading to false here, let the parent handle it
      return
    }
  } catch (err) {
    error.value = 'Unable to load document. Please try again.'
    console.error('Error loading document:', err)
  } finally {
    isLoading.value = false
  }
}

const openInNewTab = async () => {
  try {
    let urlToOpen = internalDocumentUrl.value

    // If we have document data but no URL yet, create one
    if (!urlToOpen && props.documentData) {
      const blob = new Blob([props.documentData], { type: props.documentType })
      urlToOpen = window.URL.createObjectURL(blob)
    }

    if (urlToOpen) {
      // Open the document in a new tab using the browser's native viewer
      window.open(urlToOpen, '_blank', 'noopener,noreferrer')
    } else {
      console.error('No document URL available to open in new tab')
      alert('Unable to open document in new tab. Please try again.')
    }
  } catch (error) {
    console.error('Error opening document in new tab:', error)
    alert('Unable to open document in new tab. Please try again.')
  }
}

const downloadDocument = async () => {
  if (props.documentData) {
    try {
      const blob = new Blob([props.documentData], { type: props.documentType })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${props.fileName}.${getFileExtension(props.documentType)}`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (error) {
      console.error('Error downloading document:', error)
      alert('Unable to download document. Please try again.')
    }
  } else {
    // Emit download event for parent to handle
    emit('download')
  }
}

const getFileExtension = (mimeType) => {
  const extensions = {
    'application/pdf': 'pdf',
    'image/jpeg': 'jpg',
    'image/png': 'png',
    'image/gif': 'gif',
    'text/plain': 'txt',
    'application/msword': 'doc',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx'
  }
  return extensions[mimeType] || 'pdf'
}

// Watch for changes in props
watch(() => props.show, (newVal) => {
  if (newVal && props.autoLoad) {
    // Set loading state immediately when modal opens with autoLoad
    if (!props.documentData && !props.documentUrl) {
      isLoading.value = true
    }
    loadDocument()
  }
})

watch(() => props.documentData, (newVal) => {
  if (newVal && props.show) {
    // If we have document data and autoLoad is true, load immediately
    if (props.autoLoad) {
      loadDocument()
    }
  }
})

watch(() => props.documentUrl, (newVal) => {
  if (newVal && props.show) {
    loadDocument()
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: var(--color-background);
  border-radius: 12px;
  max-width: 95vw;
  width: 100%;
  max-height: 95vh;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.modal-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  color: var(--color-text);
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.btn:hover {
  background: var(--color-background-soft);
}

.btn-outline {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-outline:hover {
  background: var(--color-primary);
  color: white;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  border: 1px solid var(--color-primary);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.8rem;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  color: var(--color-text);
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: var(--color-background-soft);
}

.close-btn svg {
  width: 20px;
  height: 20px;
}

.document-content {
  padding: 1rem;
  max-height: calc(95vh - 70px);
  overflow-y: auto;
}

.document-loading,
.document-error,
.document-placeholder {
  text-align: center;
  padding: 2rem 1rem;
}

.loading-spinner {
  border: 3px solid var(--color-border);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.document-error h4,
.document-placeholder h4 {
  margin: 1rem 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.document-error p,
.document-placeholder p {
  margin: 0 0 1rem 0;
  color: var(--color-text-light);
}

.document-iframe {
  width: 100%;
  height: calc(95vh - 140px);
  min-height: 600px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
}

.document-placeholder svg {
  color: var(--color-text-light);
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .modal-content {
    margin: 0.5rem;
    max-height: 98vh;
    max-width: 98vw;
  }
  
  .document-iframe {
    height: calc(98vh - 120px);
    min-height: 400px;
  }
  
  .modal-header {
    padding: 0.75rem 1rem;
  }
  
  .document-content {
    padding: 0.75rem;
    max-height: calc(98vh - 65px);
  }
}
</style> 