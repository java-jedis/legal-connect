<template>
  <div class="chat-document-upload">
    <!-- Upload Button -->
    <div class="upload-section">
      <button 
        @click="triggerFileUpload"
        class="upload-button"
        :disabled="isUploading"
        title="Upload Document"
      >
        <i class="fas fa-paperclip"></i>
        <span v-if="!isUploading">Upload Document</span>
        <span v-else>Uploading...</span>
      </button>
      
      <input
        ref="fileInput"
        type="file"
        accept=".pdf,.txt,.doc,.docx"
        @change="handleFileSelect"
        class="file-input-hidden"
      />
    </div>

    <!-- Upload Progress -->
    <div v-if="isUploading" class="upload-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <p class="progress-text">{{ uploadStatus }}</p>
    </div>

    <!-- Error Message -->
    <div v-if="uploadError" class="error-message">
      <i class="fas fa-exclamation-triangle"></i>
      <span>{{ uploadError }}</span>
      <button @click="clearError" class="close-button">×</button>
    </div>

    <!-- Success Message -->
    <div v-if="uploadSuccess" class="success-message">
      <i class="fas fa-check-circle"></i>
      <span>{{ uploadSuccess }}</span>
      <button @click="clearSuccess" class="close-button">×</button>
    </div>

    <!-- Uploaded Documents List -->
    <div v-if="sessionDocuments.length > 0" class="documents-list">
      <h4>Uploaded Documents</h4>
      <div class="documents-container">
        <div
          v-for="doc in sessionDocuments"
          :key="doc.id"
          class="document-item"
          :class="{ 'processing': doc.processing_status === 'processing' }"
        >
          <div class="document-info">
            <i :class="getFileIcon(doc.file_type)" class="file-icon"></i>
            <div class="document-details">
              <span class="filename">{{ doc.filename }}</span>
              <span class="file-meta">
                {{ formatFileSize(doc.file_size) }} • {{ doc.file_type.toUpperCase() }}
                <span v-if="doc.total_chunks" class="chunk-count">
                  • {{ doc.total_chunks }} chunks
                </span>
              </span>
              <span class="upload-time">{{ formatTime(doc.created_at) }}</span>
            </div>
          </div>
          
          <div class="document-status">
            <span 
              v-if="doc.processing_status === 'processing'"
              class="status processing"
            >
              <i class="fas fa-spinner fa-spin"></i>
              Processing...
            </span>
            <span 
              v-else-if="doc.processing_status === 'completed'"
              class="status completed"
            >
              <i class="fas fa-check"></i>
              Ready
            </span>
            <span 
              v-else-if="doc.processing_status === 'failed'"
              class="status failed"
              :title="doc.processing_error"
            >
              <i class="fas fa-exclamation-triangle"></i>
              Failed
            </span>
          </div>

          <div class="document-actions">
            <button
              @click="deleteDocument(doc.id)"
              class="delete-button"
              title="Delete document"
              :disabled="isDeleting === doc.id"
            >
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { aiChatService } from '../../services/aiChatService.js'

export default {
  name: 'ChatDocumentUpload',
  props: {
    sessionId: {
      type: String,
      required: true
    }
  },
  emits: ['document-uploaded', 'document-deleted'],
  data() {
    return {
      isUploading: false,
      uploadProgress: 0,
      uploadStatus: '',
      uploadError: null,
      uploadSuccess: null,
      sessionDocuments: [],
      isDeleting: null,
      maxFileSize: 10 * 1024 * 1024, // 10MB
      allowedTypes: ['pdf', 'txt', 'doc', 'docx']
    }
  },
  mounted() {
    this.loadSessionDocuments()
  },
  watch: {
    sessionId() {
      this.loadSessionDocuments()
    }
  },
  methods: {
    triggerFileUpload() {
      this.$refs.fileInput.click()
    },

    async handleFileSelect(event) {
      const file = event.target.files[0]
      if (!file) return

      // Reset input
      event.target.value = ''

      // Validate file
      const validation = this.validateFile(file)
      if (!validation.valid) {
        this.showError(validation.error)
        return
      }

      await this.uploadFile(file)
    },

    validateFile(file) {
      // Check file size
      if (file.size > this.maxFileSize) {
        return {
          valid: false,
          error: `File size exceeds 10MB limit. Current size: ${this.formatFileSize(file.size)}`
        }
      }

      if (file.size === 0) {
        return {
          valid: false,
          error: 'Cannot upload empty file'
        }
      }

      // Check file type
      const extension = file.name.split('.').pop()?.toLowerCase()
      if (!extension || !this.allowedTypes.includes(extension)) {
        return {
          valid: false,
          error: `Unsupported file type. Allowed: ${this.allowedTypes.join(', ')}`
        }
      }

      return { valid: true }
    },

    async uploadFile(file) {
      this.clearMessages()
      this.isUploading = true
      this.uploadProgress = 0
      this.uploadStatus = 'Preparing upload...'

      try {
        // Simulate progress
        const progressInterval = setInterval(() => {
          if (this.uploadProgress < 90) {
            this.uploadProgress += 10
            if (this.uploadProgress < 30) {
              this.uploadStatus = 'Uploading file...'
            } else if (this.uploadProgress < 60) {
              this.uploadStatus = 'Processing document...'
            } else {
              this.uploadStatus = 'Creating embeddings...'
            }
          }
        }, 500)

        const result = await aiChatService.uploadDocument(this.sessionId, file)

        clearInterval(progressInterval)
        this.uploadProgress = 100
        this.uploadStatus = 'Upload complete!'

        if (result.success) {
          this.showSuccess(`Document "${result.filename}" uploaded successfully!`)
          await this.loadSessionDocuments()
          this.$emit('document-uploaded', result)
        } else {
          throw new Error(result.error || 'Upload failed')
        }

      } catch (error) {
        this.showError(`Upload failed: ${error.message}`)
        console.error('Document upload error:', error)
      } finally {
        this.isUploading = false
        setTimeout(() => {
          this.uploadProgress = 0
          this.uploadStatus = ''
        }, 2000)
      }
    },

    async loadSessionDocuments() {
      if (!this.sessionId) return

      try {
        const result = await aiChatService.getSessionDocuments(this.sessionId)
        this.sessionDocuments = result.documents || []
      } catch (error) {
        console.error('Error loading session documents:', error)
      }
    },

    async deleteDocument(documentId) {
      if (!confirm('Are you sure you want to delete this document?')) return

      this.isDeleting = documentId

      try {
        await aiChatService.deleteSessionDocument(this.sessionId, documentId)
        await this.loadSessionDocuments()
        this.$emit('document-deleted', documentId)
        this.showSuccess('Document deleted successfully')
      } catch (error) {
        this.showError(`Failed to delete document: ${error.message}`)
        console.error('Document deletion error:', error)
      } finally {
        this.isDeleting = null
      }
    },

    getFileIcon(fileType) {
      const iconMap = {
        pdf: 'fas fa-file-pdf',
        txt: 'fas fa-file-alt',
        doc: 'fas fa-file-word',
        docx: 'fas fa-file-word'
      }
      return iconMap[fileType.toLowerCase()] || 'fas fa-file'
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    formatTime(dateString) {
      const date = new Date(dateString)
      return date.toLocaleString()
    },

    showError(message) {
      this.uploadError = message
      this.uploadSuccess = null
    },

    showSuccess(message) {
      this.uploadSuccess = message
      this.uploadError = null
    },

    clearError() {
      this.uploadError = null
    },

    clearSuccess() {
      this.uploadSuccess = null
    },

    clearMessages() {
      this.uploadError = null
      this.uploadSuccess = null
    }
  }
}
</script>

<style scoped>
.chat-document-upload {
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  padding: 16px;
  background: #f8f9fa;
  margin-bottom: 16px;
}

.upload-section {
  margin-bottom: 12px;
}

.upload-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 2px dashed #007bff;
  border-radius: 6px;
  background: white;
  color: #007bff;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.upload-button:hover:not(:disabled) {
  background: #e3f2fd;
  border-color: #0056b3;
}

.upload-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-input-hidden {
  display: none;
}

.upload-progress {
  margin-bottom: 12px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #007bff, #28a745);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #6c757d;
  margin: 0;
}

.error-message, .success-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 12px;
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.success-message {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.close-button {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  margin-left: auto;
  opacity: 0.7;
}

.close-button:hover {
  opacity: 1;
}

.documents-list h4 {
  margin: 0 0 12px 0;
  color: #343a40;
  font-size: 16px;
}

.documents-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.document-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.document-item:hover {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.document-item.processing {
  background: #fff3cd;
  border-color: #ffeaa7;
}

.document-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-icon {
  font-size: 24px;
  color: #6c757d;
}

.document-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.filename {
  font-weight: 500;
  color: #343a40;
  font-size: 14px;
}

.file-meta {
  font-size: 12px;
  color: #6c757d;
}

.chunk-count {
  color: #28a745;
  font-weight: 500;
}

.upload-time {
  font-size: 11px;
  color: #adb5bd;
}

.document-status {
  margin-right: 12px;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.status.processing {
  background: #fff3cd;
  color: #856404;
}

.status.completed {
  background: #d4edda;
  color: #155724;
}

.status.failed {
  background: #f8d7da;
  color: #721c24;
}

.document-actions {
  display: flex;
  gap: 4px;
}

.delete-button {
  padding: 6px 8px;
  border: none;
  background: #dc3545;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.2s ease;
}

.delete-button:hover:not(:disabled) {
  background: #c82333;
}

.delete-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive Design */
@media (max-width: 768px) {
  .document-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .document-status,
  .document-actions {
    margin-right: 0;
    align-self: flex-end;
  }
}
</style>
