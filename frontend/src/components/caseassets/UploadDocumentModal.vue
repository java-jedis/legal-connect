<template>
  <div class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Upload Document</h3>
        <button class="modal-close" @click="closeModal">&times;</button>
      </div>
      <form @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-group">
          <label for="title">Title *</label>
          <input id="title" v-model="form.title" required maxlength="255" class="form-input" />
        </div>
        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" v-model="form.description" maxlength="2000" class="form-textarea"></textarea>
        </div>
        <div class="form-group">
          <label for="privacy">Privacy *</label>
          <select id="privacy" v-model="form.privacy" required class="form-select">
            <option value="SHARED">Shared with all case members</option>
            <option value="PRIVATE">Private (only visible to you)</option>
          </select>
        </div>
        <div class="form-group">
          <label for="file">File *</label>
          <input id="file" type="file" @change="handleFileChange" required class="form-file-input" />
        </div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div class="modal-actions">
          <button type="button" class="btn btn-outline" @click="closeModal">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? 'Uploading...' : 'Upload' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { caseAssetsAPI } from '../../services/api'

const props = defineProps({
  caseId: { type: String, required: true }
})
const emit = defineEmits(['document-uploaded', 'close'])

const loading = ref(false)
const error = ref('')
const form = ref({
  title: '',
  description: '',
  privacy: 'SHARED',
  file: null
})

const handleFileChange = (event) => {
  form.value.file = event.target.files[0]
}

const closeModal = () => {
  emit('close')
}

const handleSubmit = async () => {
  if (!form.value.file) {
    error.value = 'Please select a file to upload.'
    return
  }

  error.value = ''
  loading.value = true

  try {
    const documentData = { ...form.value, caseId: props.caseId };
    await caseAssetsAPI.uploadDocument(documentData, form.value.file)
    emit('document-uploaded')
    closeModal()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to upload document.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}
.modal-content {
  background: var(--color-background);
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}
.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}
.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--color-text-muted);
  cursor: pointer;
}
.modal-form {
  padding: 1.5rem;
}
.form-group {
  margin-bottom: 1.25rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}
.form-input, .form-select, .form-textarea, .form-file-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 1rem;
  background: var(--color-background-soft);
}
.form-file-input {
  padding: 0.5rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}
.btn {
  padding: 0.6rem 1.25rem;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary {
  background: var(--color-primary);
  color: var(--vt-c-white);
}
.btn-outline {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
.error-message {
  color: #ef4444;
  background: #fee2e2;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  margin-bottom: 1.25rem;
  text-align: center;
}
</style>
