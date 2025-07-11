<template>
  <div v-if="show" class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Lawyer Details</h2>
        <div class="modal-actions">
          <button v-if="certificateUrl" @click="downloadCertificate" class="btn btn-outline btn-sm">
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

      <div v-if="loading" class="modal-body loading">
        <div class="spinner"></div>
        <p>Loading lawyer details...</p>
      </div>

      <div v-else-if="error" class="modal-body error">
        <p>{{ error }}</p>
        <button @click="loadLawyerDetails" class="retry-btn">Retry</button>
      </div>

      <div v-else-if="lawyer" class="modal-body">
        <!-- Personal Information -->
        <div class="detail-section">
          <h3>Personal Information</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">Full Name:</span>
              <span class="value">{{ lawyer.user.firstName }} {{ lawyer.user.lastName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Email:</span>
              <span class="value">{{ lawyer.user.email }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Join Date:</span>
              <span class="value">{{ formatDate(lawyer.user.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Bar Certificate No:</span>
              <span class="value">{{ lawyer.lawyer.barCertificateNumber || 'N/A' }}</span>
            </div>
          </div>
        </div>

        <!-- Professional Information -->
        <div class="detail-section">
          <h3>Professional Information</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">Law Firm:</span>
              <span class="value">{{ lawyer.lawyer.firm || 'N/A' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Years of Experience:</span>
              <span class="value">{{ lawyer.lawyer.yearsOfExperience || 'N/A' }} years</span>
            </div>
            <div class="detail-item">
              <span class="label">Practicing Court:</span>
              <span class="value">{{ lawyer.lawyer.practicingCourtDisplayName || lawyer.lawyer.practicingCourt || 'N/A' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Division:</span>
              <span class="value">{{ lawyer.lawyer.divisionDisplayName || lawyer.lawyer.division || 'N/A' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">District:</span>
              <span class="value">{{ lawyer.lawyer.districtDisplayName || lawyer.lawyer.district || 'N/A' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Verification Status:</span>
              <span :class="['status-badge', `status-${lawyer.lawyer.verificationStatus?.toLowerCase()}`]">
                {{ lawyer.lawyer.verificationStatusDisplayName || lawyer.lawyer.verificationStatus || 'N/A' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Specializations -->
        <div class="detail-section">
          <h3>Specializations</h3>
          <div class="specializations-list">
            <template v-if="lawyer.lawyer.specializationDisplayNames && lawyer.lawyer.specializationDisplayNames.length > 0">
              <span v-for="spec in lawyer.lawyer.specializationDisplayNames" 
                    :key="spec" 
                    class="specialization-tag">
                {{ spec }}
              </span>
            </template>
            <span v-else class="no-specializations">No specializations specified</span>
          </div>
        </div>

        <!-- Bio -->
        <div v-if="lawyer.lawyer.bio" class="detail-section">
          <h3>Professional Bio</h3>
          <div class="bio-content">
            {{ lawyer.lawyer.bio }}
          </div>
        </div>

        <!-- Bar Certificate -->
        <div class="detail-section">
          <div class="certificate-section">
            <button v-if="lawyer.barCertificateFileUrl" @click="viewCertificate" class="certificate-link btn btn-outline btn-sm">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              View Certificate
            </button>
            <div v-else class="certificate-placeholder">
              <p>No certificate available</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Document Viewer for Certificate -->
    <DocumentViewer
      :show="showCertificateModal"
      title="Bar Certificate"
      :document-data="certificateData"
      document-type="application/pdf"
      :file-name="`bar-certificate-${lawyer?.user?.lastName || 'lawyer'}`"
      :auto-load="true"
      :show-load-button="true"
      load-button-text="Load Certificate"
      @close="closeCertificateModal"
      @load="loadCertificate"
      @download="downloadCertificate"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { lawyerAPI } from '../services/api'
import DocumentViewer from './DocumentViewer.vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  lawyer: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const loading = ref(false)
const error = ref(null)
const showCertificateModal = ref(false)
const certificateData = ref(null)

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const closeModal = () => {
  emit('close')
}

const loadLawyerDetails = async () => {
  if (props.lawyer) {
    return
  }

  loading.value = true
  error.value = null

  try {
    // For now, we'll use the lawyer data passed as prop
    // In the future, you can add an API call here to fetch lawyer details by ID
    // const response = await adminAPI.getLawyerDetails(props.lawyerId)
    // lawyer.value = response.data
    
    error.value = 'Lawyer details API not implemented yet'
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load lawyer details'
    console.error('Error loading lawyer details:', err)
  } finally {
    loading.value = false
  }
}

const viewCertificate = () => {
  showCertificateModal.value = true
}

const closeCertificateModal = () => {
  showCertificateModal.value = false
  // Clean up certificate data
  certificateData.value = null
}

const loadCertificate = async () => {
  if (!props.lawyer?.user?.email) {
    return
  }

  try {
    // Use the email parameter for admin viewing
    const response = await lawyerAPI.viewCredentials(props.lawyer.user.email)
    certificateData.value = response
  } catch (err) {
    console.error('Error viewing certificate:', err)
    // Set error in certificateData to trigger error display
    certificateData.value = { error: 'Unable to load bar certificate. Please try again.' }
  }
}

const downloadCertificate = async () => {
  if (!certificateData.value) {
    await loadCertificate()
  }

  if (certificateData.value) {
    try {
      const blob = new Blob([certificateData.value], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `bar-certificate-${props.lawyer?.user?.lastName || 'lawyer'}.pdf`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (error) {
      console.error('Error downloading certificate:', error)
      alert('Unable to download bar certificate. Please try again.')
    }
  }
}

// Watch for changes in props
watch(() => props.show, (newVal) => {
  if (newVal && props.lawyer) {
    loadLawyerDetails()
  }
})

watch(() => props.lawyer, (newVal) => {
  if (newVal && props.show) {
    loadLawyerDetails()
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
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
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
  border-color: #007bff;
  color: #007bff;
}

.btn-outline:hover {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  color: var(--color-text);
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.btn:hover {
  background: var(--color-background-soft);
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
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

.modal-body {
  padding: 1.5rem;
  max-height: calc(90vh - 80px);
  overflow-y: auto;
}

.modal-body.loading,
.modal-body.error {
  text-align: center;
  padding: 3rem 1.5rem;
}

.spinner {
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

.retry-btn {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
}

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: 6px;
}

.label {
  font-weight: 500;
  color: var(--color-text-light);
}

.value {
  font-weight: 600;
  color: var(--color-text);
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-approved {
  background: #d4edda;
  color: #155724;
}

.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.specializations-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.specialization-tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.bio-content {
  background: var(--color-background-soft);
  padding: 1rem;
  border-radius: 6px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.certificate-section {
  background: var(--color-background-soft);
  border-radius: 6px;
  padding: 1rem;
}

.btn-view {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-view:hover {
  background: var(--color-primary-dark);
}

.certificate-placeholder {
  text-align: center;
  padding: 1rem;
  color: var(--color-text-light);
}

.certificate-link {
  color: #2196f3;
  text-decoration: none;
  font-weight: 500;
}

.certificate-link:hover {
  background: #007bff;
  color: white;
  border-color: #007bff;
  text-decoration: none;
}



.no-specializations {
  color: var(--color-text-light);
  font-style: italic;
  padding: 0.5rem;
}

@media (max-width: 768px) {
  .modal-content {
    margin: 0.5rem;
    max-height: 95vh;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style> 