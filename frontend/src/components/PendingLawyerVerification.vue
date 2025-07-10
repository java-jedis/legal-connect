<template>
  <div class="pending-lawyer-verification">
    <div class="header">
      <div class="filters">
        <select v-model="selectedStatus" @change="loadLawyers">
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>Loading lawyers...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadLawyers" class="retry-btn">Retry</button>
    </div>

    <div v-else-if="lawyers.length === 0" class="empty-state">
      <p>No lawyers found with the selected status.</p>
    </div>

    <div v-else class="lawyers-table">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Bar Certificate No.</th>
            <th>Bar Certificate</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="lawyer in lawyers" :key="lawyer.lawyerId">
            <td>
              <div class="lawyer-name">
                <strong>{{ lawyer.user.firstName }} {{ lawyer.user.lastName }}</strong>
              </div>
            </td>
            <td>{{ lawyer.user.email }}</td>
            <td>{{ lawyer.lawyer.barCertificateNumber || 'N/A' }}</td>
            <td>
              <button v-if="lawyer.barCertificateFileUrl"
                      @click="viewLawyerCertificate(lawyer)"
                      class="certificate-link btn btn-outline btn-sm">
                View Certificate
              </button>
              <span v-else class="no-certificate">No certificate</span>
            </td>
            <td>
              <span :class="['status-badge', `status-${lawyer.lawyer.verificationStatus.toLowerCase()}`]">
                {{ lawyer.lawyer.verificationStatus }}
              </span>
            </td>
            <td>
              <div class="actions">
                <button @click="viewLawyerDetails(lawyer)" class="btn-details">
                  View Details
                </button>
                <button v-if="lawyer.lawyer.verificationStatus === 'PENDING'"
                        @click="approveLawyer(lawyer.lawyerId)"
                        :disabled="processingLawyer === lawyer.lawyerId"
                        class="btn-approve">
                  {{ processingLawyer === lawyer.lawyerId ? 'Approving...' : 'Approve' }}
                </button>
                <button v-if="lawyer.lawyer.verificationStatus === 'PENDING'"
                        @click="rejectLawyer(lawyer.lawyerId)"
                        :disabled="processingLawyer === lawyer.lawyerId"
                        class="btn-reject">
                  {{ processingLawyer === lawyer.lawyerId ? 'Rejecting...' : 'Reject' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 0"
          class="page-btn">
          Previous
        </button>
        
        <span class="page-info">
          Page {{ currentPage + 1 }} of {{ totalPages }}
        </span>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage >= totalPages - 1"
          class="page-btn">
          Next
        </button>
      </div>
    </div>

    <LawyerDetailsModal
      :show="showLawyerModal"
      :lawyer="selectedLawyer"
      @close="closeLawyerModal"
    />

    <!-- Certificate Viewer Modal -->
    <DocumentViewer
      :show="showCertificateModal"
      title="Bar Certificate"
      :document-data="certificateData"
      document-type="application/pdf"
      :file-name="`bar-certificate-${selectedLawyer?.user?.lastName || 'lawyer'}`"
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
import { onMounted, ref } from 'vue'
import { adminAPI, lawyerAPI } from '../services/api'
import DocumentViewer from './DocumentViewer.vue'
import LawyerDetailsModal from './LawyerDetailsModal.vue'

const lawyers = ref([])
const loading = ref(false)
const error = ref(null)
const currentPage = ref(0)
const totalPages = ref(0)
const selectedStatus = ref('PENDING')
const processingLawyer = ref(null)
const showLawyerModal = ref(false)
const selectedLawyer = ref(null)
const showCertificateModal = ref(false)
const certificateData = ref(null)

const loadLawyers = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await adminAPI.getLawyersByStatus(selectedStatus.value, currentPage.value, 10)
    
    lawyers.value = response.data.content || response.data.lawyers || []
    totalPages.value = response.data.totalPages || 1
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load lawyers'
    console.error('Error loading lawyers:', err)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  currentPage.value = page
  loadLawyers()
}

const approveLawyer = async (lawyerId) => {
  processingLawyer.value = lawyerId
  
  try {
    await adminAPI.approveLawyer(lawyerId)
    // Reload the list to reflect the change
    await loadLawyers()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to approve lawyer'
    console.error('Error approving lawyer:', err)
  } finally {
    processingLawyer.value = null
  }
}

const rejectLawyer = async (lawyerId) => {
  processingLawyer.value = lawyerId
  
  try {
    await adminAPI.rejectLawyer(lawyerId)
    // Reload the list to reflect the change
    await loadLawyers()
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to reject lawyer'
    console.error('Error rejecting lawyer:', err)
  } finally {
    processingLawyer.value = null
  }
}

const viewLawyerDetails = (lawyer) => {
  selectedLawyer.value = lawyer
  showLawyerModal.value = true
}

const viewLawyerCertificate = (lawyer) => {
  selectedLawyer.value = lawyer
  showCertificateModal.value = true
  certificateData.value = null // Clear previous data
}

const closeLawyerModal = () => {
  showLawyerModal.value = false
  selectedLawyer.value = null
}

const closeCertificateModal = () => {
  showCertificateModal.value = false
  certificateData.value = null
}

const loadCertificate = async () => {
  if (!selectedLawyer.value?.user?.email) {
    return
  }

  try {
    const response = await lawyerAPI.viewCredentials(selectedLawyer.value.user.email)
    certificateData.value = response
  } catch (err) {
    console.error('Error viewing certificate:', err)
    certificateData.value = { error: 'Unable to load bar certificate. Please try again.' }
  }
}

const downloadCertificate = async () => {
  try {
    let response
    if (certificateData.value) {
      response = certificateData.value
    } else {
      response = await lawyerAPI.viewCredentials(selectedLawyer.value.user.email)
    }
    
    const blob = new Blob([response], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `bar-certificate-${selectedLawyer.value?.user?.lastName || 'lawyer'}.pdf`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (error) {
    console.error('Error downloading certificate:', error)
    alert('Unable to download bar certificate. Please try again.')
  }
}

onMounted(() => {
  loadLawyers()
})
</script>

<style scoped>
.pending-lawyer-verification {
  width: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.filters select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.spinner {
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
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
  background: #3498db;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
}

.lawyers-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.lawyer-name {
  font-weight: 500;
}

.specializations {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.specialization-tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.certificate-link {
  color: #2196f3;
  text-decoration: none;
  font-weight: 500;
}

.certificate-link:hover {
  text-decoration: underline;
}

.no-certificate {
  color: #999;
  font-style: italic;
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

.actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn-approve, .btn-reject {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-details {
  padding: 0.5rem 1rem;
  border: 1px solid #007bff;
  background: transparent;
  color: #007bff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
  margin-bottom: 0.5rem;
}

.btn-details:hover {
  background: #007bff;
  color: white;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover:not(:disabled) {
  background: #218838;
}

.btn-reject {
  background: #dc3545;
  color: white;
}

.btn-reject:hover:not(:disabled) {
  background: #c82333;
}

.btn-approve:disabled, .btn-reject:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 500;
  color: #666;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  table {
    font-size: 0.9rem;
  }
  
  th, td {
    padding: 0.75rem 0.5rem;
  }
  
  .actions {
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .btn-approve, .btn-reject {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }
}
</style> 