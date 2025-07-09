<template>
  <div class="lawyer-profile-section">
    <div class="section-divider">
      <span class="divider-text">Lawyer Profile</span>
    </div>
    
    <!-- Loading State -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading your profile information...</p>
    </div>

    <!-- No Profile State -->
    <div v-else-if="!hasProfile" class="no-profile-state">
      <div class="info-card">
        <div class="info-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M16 21V19A4 4 0 0 0 12 15H8A4 4 0 0 0 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            <path d="M12 11V17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M9 14L12 11L15 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3>No Lawyer Profile</h3>
        <p>You haven't created your lawyer profile yet. Create your profile to get started.</p>
        <button @click="createProfile" class="btn btn-primary">
          Create Profile
        </button>
      </div>
    </div>

    <!-- Profile Display State -->
    <div v-else class="profile-display">
      <!-- Verification Status Banner -->
      <div v-if="!isVerified" class="verification-banner">
        <div class="banner-content">
          <svg class="banner-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <div class="banner-text">
            <h4>Profile Under Review</h4>
            <p>Your profile is currently being reviewed. You cannot make changes until it's verified.</p>
          </div>
        </div>
      </div>

      <!-- Profile Information -->
      <div class="profile-info-grid">
        <div class="info-card">
          <h3>Professional Information</h3>
          <div class="info-item">
            <span class="info-label">Law Firm:</span>
            <span class="info-value">{{ lawyerInfo?.firm || 'Not specified' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Years of Experience:</span>
            <span class="info-value">{{ lawyerInfo?.yearsOfExperience || 'Not specified' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Bar Certificate Number:</span>
            <span class="info-value">{{ lawyerInfo?.barCertificateNumber || 'Not specified' }}</span>
          </div>
        </div>

                <div class="info-card">
          <h3>Practice Location</h3>
          <div class="info-item">
            <span class="info-label">Practicing Court:</span>
            <span class="info-value">{{ lawyerInfo?.practicingCourtDisplayName || 'Not specified' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Division:</span>
            <span class="info-value">{{ lawyerInfo?.divisionDisplayName || 'Not specified' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">District:</span>
            <span class="info-value">{{ lawyerInfo?.districtDisplayName || 'Not specified' }}</span>
          </div>
        </div>

        <div v-if="lawyerInfo?.bio" class="info-card full-width">
          <h3>Professional Bio</h3>
          <p class="bio-text">{{ lawyerInfo.bio }}</p>
        </div>

        <div class="info-card">
          <h3>Specializations</h3>
          <div class="specializations-list">
            <template v-if="lawyerInfo?.specializationDisplayNames?.length">
              <span class="specialization-tag" v-for="spec in lawyerInfo.specializationDisplayNames" :key="spec">
                {{ spec }}
              </span>
            </template>
            <span v-else class="no-specializations">No specializations specified</span>
          </div>
        </div>

        <div class="info-card">
          <h3>Verification Status</h3>
          <div class="status-item">
            <span class="status-label">Status:</span>
            <span class="status-value" :class="verificationStatusClass">
              {{ lawyerInfo?.verificationStatusDisplayName || 'Unknown' }}
            </span>
          </div>
          <div class="status-item" v-if="lawyerInfo?.lawyerCreatedAt">
            <span class="status-label">Profile Created:</span>
            <span class="status-value">{{ formatDate(lawyerInfo.lawyerCreatedAt) }}</span>
          </div>
          <div class="status-item" v-if="lawyerInfo?.lawyerUpdatedAt">
            <span class="status-label">Last Updated:</span>
            <span class="status-value">{{ formatDate(lawyerInfo.lawyerUpdatedAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="profile-actions">
        <button 
          v-if="isVerified" 
          @click="editProfile" 
          class="btn btn-primary"
        >
          Edit Profile
        </button>
        <button 
          v-if="isVerified" 
          @click="viewCredentials" 
          class="btn btn-outline"
        >
          View Bar Certificate
        </button>
        <button 
          v-if="!isVerified" 
          @click="refreshStatus" 
          class="btn btn-outline"
        >
          Refresh Status
        </button>
      </div>
    </div>

    <!-- Edit Profile Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Edit Lawyer Profile</h3>
          <button @click="closeEditModal" class="modal-close">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        
        <form @submit.prevent="submitUpdate" class="edit-form">
          <!-- Professional Information -->
          <div class="form-section">
            <h4>Professional Information</h4>
            <div class="form-row">
              <div class="form-group">
                <label for="edit-firm">Law Firm Name *</label>
                <input
                  id="edit-firm"
                  v-model="editForm.firm"
                  type="text"
                  class="form-input"
                  :class="{ 'error': editErrors.firm }"
                  placeholder="Enter your law firm name"
                  required
                />
                <span v-if="editErrors.firm" class="error-message">{{ editErrors.firm }}</span>
              </div>

              <div class="form-group">
                <label for="edit-yearsOfExperience">Years of Experience *</label>
                <input
                  id="edit-yearsOfExperience"
                  v-model.number="editForm.yearsOfExperience"
                  type="number"
                  min="0"
                  max="50"
                  class="form-input"
                  :class="{ 'error': editErrors.yearsOfExperience }"
                  placeholder="Enter years of experience"
                  required
                />
                <span v-if="editErrors.yearsOfExperience" class="error-message">{{ editErrors.yearsOfExperience }}</span>
              </div>
            </div>

            <div class="form-group">
              <label for="edit-barCertificateNumber">Bar Certificate Number *</label>
              <input
                id="edit-barCertificateNumber"
                v-model="editForm.barCertificateNumber"
                type="text"
                class="form-input"
                :class="{ 'error': editErrors.barCertificateNumber }"
                placeholder="Enter your bar certificate number"
                required
                readonly
              />
              <small class="help-text">Bar certificate number cannot be changed after profile creation</small>
            </div>
          </div>

          <!-- Location Information -->
          <div class="form-section">
            <h4>Practice Location</h4>
            <div class="form-row">
              <div class="form-group">
                <label for="edit-practicingCourt">Practicing Court *</label>
                <select
                  id="edit-practicingCourt"
                  v-model="editForm.practicingCourt"
                  class="form-select"
                  :class="{ 'error': editErrors.practicingCourt }"
                  required
                >
                  <option value="">Select practicing court</option>
                  <option v-for="court in practicingCourts" :key="court.value" :value="court.value">
                    {{ court.label }}
                  </option>
                </select>
                <span v-if="editErrors.practicingCourt" class="error-message">{{ editErrors.practicingCourt }}</span>
              </div>

              <div class="form-group">
                <label for="edit-division">Division *</label>
                <select
                  id="edit-division"
                  v-model="editForm.division"
                  class="form-select"
                  :class="{ 'error': editErrors.division }"
                  required
                >
                  <option value="">Select division</option>
                  <option v-for="div in divisions" :key="div.value" :value="div.value">
                    {{ div.label }}
                  </option>
                </select>
                <span v-if="editErrors.division" class="error-message">{{ editErrors.division }}</span>
              </div>
            </div>

            <div class="form-group">
              <label for="edit-district">District *</label>
              <select
                id="edit-district"
                v-model="editForm.district"
                class="form-select"
                :class="{ 'error': editErrors.district }"
                required
              >
                <option value="">Select district</option>
                <option v-for="dist in districts" :key="dist.value" :value="dist.value">
                  {{ dist.label }}
                </option>
              </select>
              <span v-if="editErrors.district" class="error-message">{{ editErrors.district }}</span>
            </div>
          </div>

          <!-- Specializations -->
          <div class="form-section">
            <h4>Specializations *</h4>
            <p class="section-description">Select all areas of law you specialize in</p>
            
            <div class="specializations-grid">
              <label
                v-for="spec in specializations"
                :key="spec.value"
                class="specialization-checkbox"
                :class="{ 'checked': editForm.specializations.includes(spec.value) }"
              >
                <input
                  type="checkbox"
                  :value="spec.value"
                  v-model="editForm.specializations"
                  class="checkbox-input"
                />
                <span class="checkbox-label">{{ spec.label }}</span>
              </label>
            </div>
            <span v-if="editErrors.specializations" class="error-message">{{ editErrors.specializations }}</span>
          </div>

          <!-- Bio -->
          <div class="form-section">
            <h4>Professional Bio</h4>
            <p class="section-description">Tell clients about your expertise and experience (optional)</p>
            
            <div class="form-group">
              <textarea
                v-model="editForm.bio"
                class="form-textarea"
                :class="{ 'error': editErrors.bio }"
                placeholder="Describe your legal expertise, notable cases, and professional achievements..."
                rows="6"
                maxlength="2000"
              ></textarea>
              <div class="textarea-footer">
                <span v-if="editErrors.bio" class="error-message">{{ editErrors.bio }}</span>
                <span class="char-count">{{ editForm.bio.length }}/2000</span>
              </div>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button type="button" @click="closeEditModal" class="btn btn-outline">
              Cancel
            </button>
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="isUpdating"
            >
              <span v-if="isUpdating" class="loading-spinner"></span>
              {{ isUpdating ? 'Updating...' : 'Update Profile' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Bar Certificate Modal -->
    <div v-if="showCertificateModal" class="modal-overlay" @click="closeCertificateModal">
      <div class="certificate-modal-content" @click.stop>
        <div class="modal-header">
          <h3>Bar Certificate</h3>
          <div class="modal-actions">
            <button @click="downloadCertificate" class="btn btn-outline btn-sm">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="7,10 12,15 17,10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Download
            </button>
            <button @click="closeCertificateModal" class="modal-close">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
        
        <div class="certificate-viewer">
          <div v-if="isLoadingCertificate" class="certificate-loading">
            <div class="loading-spinner"></div>
            <p>Loading certificate...</p>
          </div>
          <div v-else-if="certificateError" class="certificate-error">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="48" height="48">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h4>Unable to load certificate</h4>
            <p>{{ certificateError }}</p>
            <button @click="viewCredentials" class="btn btn-primary">Try Again</button>
          </div>
          <iframe 
            v-else-if="certificateUrl" 
            :src="certificateUrl" 
            class="certificate-iframe"
            title="Bar Certificate"
          ></iframe>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { lawyerAPI } from '../services/api'
import { useLawyerStore } from '../stores/lawyer'

const router = useRouter()
const lawyerStore = useLawyerStore()

// State
const isLoading = ref(false)
const showEditModal = ref(false)
const isUpdating = ref(false)
const editErrors = reactive({})
const showCertificateModal = ref(false)
const isLoadingCertificate = ref(false)
const certificateError = ref('')
const certificateUrl = ref('')

// Computed properties
const hasProfile = computed(() => lawyerStore.hasProfile)
const isVerified = computed(() => lawyerStore.isVerified)
const lawyerInfo = computed(() => lawyerStore.lawyerInfo)

const verificationStatusClass = computed(() => {
  const status = lawyerInfo.value?.verificationStatus
  if (status === 'APPROVED') return 'verified'
  if (status === 'PENDING') return 'pending'
  if (status === 'REJECTED') return 'rejected'
  return 'unknown'
})

// Edit form data
const editForm = reactive({
  firm: '',
  yearsOfExperience: null,
  barCertificateNumber: '',
  practicingCourt: '',
  division: '',
  district: '',
  bio: '',
  specializations: []
})

// Enums data (same as in LawyerProfileCreation)
const practicingCourts = [
  { value: 'SUPREME_COURT', label: 'Supreme Court' },
  { value: 'HIGH_COURT_DIVISION', label: 'High Court Division' },
  { value: 'APPELLATE_DIVISION', label: 'Appellate Division' },
  { value: 'DISTRICT_COURT', label: 'District Court' },
  { value: 'SESSIONS_COURT', label: 'Sessions Court' },
  { value: 'ADMINISTRATIVE_TRIBUNAL', label: 'Administrative Tribunal' },
  { value: 'LABOUR_COURT', label: 'Labour Court' },
  { value: 'FAMILY_COURT', label: 'Family Court' },
  { value: 'MAGISTRATE_COURT', label: 'Magistrate Court' },
  { value: 'SPECIAL_TRIBUNAL', label: 'Special Tribunal' }
]

const divisions = [
  { value: 'DHAKA', label: 'Dhaka' },
  { value: 'CHATTOGRAM', label: 'Chattogram' },
  { value: 'RAJSHAHI', label: 'Rajshahi' },
  { value: 'KHULNA', label: 'Khulna' },
  { value: 'BARISHAL', label: 'Barishal' },
  { value: 'SYLHET', label: 'Sylhet' },
  { value: 'RANGPUR', label: 'Rangpur' },
  { value: 'MYMENSINGH', label: 'Mymensingh' }
]

const districts = [
  { value: 'DHAKA', label: 'Dhaka' },
  { value: 'CHATTOGRAM', label: 'Chattogram' },
  { value: 'RAJSHAHI', label: 'Rajshahi' },
  { value: 'KHULNA', label: 'Khulna' },
  { value: 'BARISHAL', label: 'Barishal' },
  { value: 'SYLHET', label: 'Sylhet' },
  { value: 'RANGPUR', label: 'Rangpur' },
  { value: 'MYMENSINGH', label: 'Mymensingh' },
  { value: 'COMILLA', label: 'Comilla' },
  { value: 'GAZIPUR', label: 'Gazipur' },
  { value: 'NARAYANGANJ', label: 'Narayanganj' },
  { value: 'TANGAIL', label: 'Tangail' },
  { value: 'NARSINGDI', label: 'Narsingdi' },
  { value: 'MUNSHIGANJ', label: 'Munshiganj' },
  { value: 'MANIKGANJ', label: 'Manikganj' },
  { value: 'MADARIPUR', label: 'Madaripur' },
  { value: 'SHARIATPUR', label: 'Shariatpur' },
  { value: 'RAJBARI', label: 'Rajbari' },
  { value: 'GOPALGANJ', label: 'Gopalganj' },
  { value: 'FARIDPUR', label: 'Faridpur' },
  { value: 'KISHOREGANJ', label: 'Kishoreganj' },
  { value: 'NETROKONA', label: 'Netrokona' },
  { value: 'JAMALPUR', label: 'Jamalpur' },
  { value: 'SHERPUR', label: 'Sherpur' },
  { value: 'MYMENSINGH', label: 'Mymensingh' },
  { value: 'BOGRA', label: 'Bogra' },
  { value: 'JOYPURHAT', label: 'Joypurhat' },
  { value: 'NAOGAON', label: 'Naogaon' },
  { value: 'NATORE', label: 'Natore' },
  { value: 'CHAPAI_NAWABGANJ', label: 'Chapai Nawabganj' },
  { value: 'PABNA', label: 'Pabna' },
  { value: 'SIRAJGANJ', label: 'Sirajganj' },
  { value: 'DINAJPUR', label: 'Dinajpur' },
  { value: 'THAKURGAON', label: 'Thakurgaon' },
  { value: 'PANCHAGARH', label: 'Panchagarh' },
  { value: 'NILPHAMARI', label: 'Nilphamari' },
  { value: 'LALMONIRHAT', label: 'Lalmonirhat' },
  { value: 'KURIGRAM', label: 'Kurigram' },
  { value: 'GAIBANDHA', label: 'Gaibandha' },
  { value: 'RANGPUR', label: 'Rangpur' },
  { value: 'COXS_BAZAR', label: 'Cox\'s Bazar' },
  { value: 'BANDARBAN', label: 'Bandarban' },
  { value: 'RANGAMATI', label: 'Rangamati' },
  { value: 'KHAGRACHHARI', label: 'Khagrachhari' },
  { value: 'FENI', label: 'Feni' },
  { value: 'LAKSHMIPUR', label: 'Lakshmipur' },
  { value: 'CHANDPUR', label: 'Chandpur' },
  { value: 'NOAKHALI', label: 'Noakhali' },
  { value: 'CHATTOGRAM', label: 'Chattogram' },
  { value: 'BRAHMANBARIA', label: 'Brahmanbaria' },
  { value: 'COMILLA', label: 'Comilla' },
  { value: 'CHITTAGONG', label: 'Chittagong' },
  { value: 'JESSORE', label: 'Jessore' },
  { value: 'SATKHIRA', label: 'Satkhira' },
  { value: 'MEHERPUR', label: 'Meherpur' },
  { value: 'NARAIL', label: 'Narail' },
  { value: 'CHUADANGA', label: 'Chuadanga' },
  { value: 'KUSHTIA', label: 'Kushtia' },
  { value: 'MAGURA', label: 'Magura' },
  { value: 'JHENAIDAH', label: 'Jhenaidah' },
  { value: 'KHULNA', label: 'Khulna' },
  { value: 'BAGERHAT', label: 'Bagerhat' },
  { value: 'PIROJPUR', label: 'Pirojpur' },
  { value: 'JHALOKATHI', label: 'Jhalokathi' },
  { value: 'BHOLA', label: 'Bhola' },
  { value: 'PATUAKHALI', label: 'Patuakhali' },
  { value: 'BARGUNA', label: 'Barguna' },
  { value: 'BARISHAL', label: 'Barishal' },
  { value: 'SUNAMGANJ', label: 'Sunamganj' },
  { value: 'HABIGANJ', label: 'Habiganj' },
  { value: 'MOULVIBAZAR', label: 'Moulvibazar' },
  { value: 'SYLHET', label: 'Sylhet' }
]

const specializations = [
  { value: 'CRIMINAL_LAW', label: 'Criminal Law' },
  { value: 'CIVIL_LAW', label: 'Civil Law' },
  { value: 'FAMILY_LAW', label: 'Family Law' },
  { value: 'LABOUR_LAW', label: 'Labour Law' },
  { value: 'CORPORATE_LAW', label: 'Corporate Law' },
  { value: 'CONSTITUTIONAL_LAW', label: 'Constitutional Law' },
  { value: 'TAX_LAW', label: 'Tax Law' },
  { value: 'ENVIRONMENTAL_LAW', label: 'Environmental Law' },
  { value: 'INTELLECTUAL_PROPERTY_LAW', label: 'Intellectual Property Law' },
  { value: 'BANKING_LAW', label: 'Banking and Finance Law' },
  { value: 'PROPERTY_LAW', label: 'Property and Real Estate Law' },
  { value: 'HUMAN_RIGHTS_LAW', label: 'Human Rights Law' },
  { value: 'INTERNATIONAL_LAW', label: 'International Law' },
  { value: 'CYBER_LAW', label: 'Cyber and ICT Law' },
  { value: 'CONTRACT_LAW', label: 'Contract Law' },
  { value: 'ADMINISTRATIVE_LAW', label: 'Administrative Law' },
  { value: 'IMMIGRATION_LAW', label: 'Immigration Law' },
  { value: 'CONSUMER_LAW', label: 'Consumer Protection Law' },
  { value: 'INSURANCE_LAW', label: 'Insurance Law' },
  { value: 'MARITIME_LAW', label: 'Maritime Law' },
  { value: 'EDUCATION_LAW', label: 'Education Law' }
]

// Methods
const createProfile = () => {
  router.push({ name: 'lawyer-profile-creation' })
}

const editProfile = () => {
  // Populate edit form with current data
  editForm.firm = lawyerInfo.value?.firm || ''
  editForm.yearsOfExperience = lawyerInfo.value?.yearsOfExperience || null
  editForm.barCertificateNumber = lawyerInfo.value?.barCertificateNumber || ''
  editForm.practicingCourt = lawyerInfo.value?.practicingCourt || ''
  editForm.division = lawyerInfo.value?.division || ''
  editForm.district = lawyerInfo.value?.district || ''
  editForm.bio = lawyerInfo.value?.bio || ''
  editForm.specializations = lawyerInfo.value?.specializations?.map(s => s) || []
  
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  // Clear errors
  Object.keys(editErrors).forEach(key => delete editErrors[key])
}

const validateEditForm = () => {
  // Clear previous errors
  Object.keys(editErrors).forEach(key => delete editErrors[key])

  if (!editForm.firm.trim()) {
    editErrors.firm = 'Firm name is required'
  }

  if (!editForm.yearsOfExperience || editForm.yearsOfExperience < 0 || editForm.yearsOfExperience > 50) {
    editErrors.yearsOfExperience = 'Years of experience must be between 0 and 50'
  }

  if (!editForm.barCertificateNumber.trim()) {
    editErrors.barCertificateNumber = 'Bar certificate number is required'
  }

  if (!editForm.practicingCourt) {
    editErrors.practicingCourt = 'Practicing court is required'
  }

  if (!editForm.division) {
    editErrors.division = 'Division is required'
  }

  if (!editForm.district) {
    editErrors.district = 'District is required'
  }

  if (editForm.specializations.length === 0) {
    editErrors.specializations = 'At least one specialization is required'
  }

  return Object.keys(editErrors).length === 0
}

const submitUpdate = async () => {
  if (!validateEditForm()) {
    return
  }

  isUpdating.value = true

  try {
    await lawyerStore.updateLawyerProfile({
      firm: editForm.firm,
      yearsOfExperience: editForm.yearsOfExperience,
      barCertificateNumber: editForm.barCertificateNumber,
      practicingCourt: editForm.practicingCourt,
      division: editForm.division,
      district: editForm.district,
      bio: editForm.bio,
      specializations: editForm.specializations
    })

    closeEditModal()
    alert('Profile updated successfully!')
  } catch (error) {
    console.error('Error updating lawyer profile:', error)
    
    if (error.response?.data?.error?.message) {
      alert(`Error: ${error.response.data.error.message}`)
    } else {
      alert('An error occurred while updating your profile. Please try again.')
    }
  } finally {
    isUpdating.value = false
  }
}

const viewCredentials = async () => {
  showCertificateModal.value = true
  isLoadingCertificate.value = true
  certificateError.value = ''
  certificateUrl.value = ''
  
  try {
    const response = await lawyerAPI.viewCredentials()
    // Create a blob URL for viewing in the browser
    const blob = new Blob([response], { type: 'application/pdf' })
    certificateUrl.value = window.URL.createObjectURL(blob)
  } catch (error) {
    console.error('Error viewing credentials:', error)
    certificateError.value = 'Unable to load bar certificate. Please try again.'
  } finally {
    isLoadingCertificate.value = false
  }
}

const closeCertificateModal = () => {
  showCertificateModal.value = false
  // Clean up the blob URL to prevent memory leaks
  if (certificateUrl.value) {
    window.URL.revokeObjectURL(certificateUrl.value)
    certificateUrl.value = ''
  }
  certificateError.value = ''
}

const downloadCertificate = async () => {
  try {
    const response = await lawyerAPI.viewCredentials()
    // Create a blob URL and download the file
    const blob = new Blob([response], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'bar-certificate.pdf'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (error) {
    console.error('Error downloading credentials:', error)
    alert('Unable to download bar certificate. Please try again.')
  }
}

const refreshStatus = async () => {
  try {
    await lawyerStore.fetchLawyerInfo()
  } catch {
    // Error is handled by the store
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'N/A'
  }
}

// Load lawyer info on mount
onMounted(async () => {
  isLoading.value = true
  try {
    await lawyerStore.fetchLawyerInfo()
  } catch {
    // Error is handled by the store
  } finally {
    isLoading.value = false
  }
})
</script>

<style scoped>
.lawyer-profile-section {
  margin-top: 2rem;
}

.loading-state {
  text-align: center;
  padding: 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-border);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-profile-state {
  text-align: center;
  padding: 2rem;
}

.info-card {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1.5rem;
  margin-bottom: 1rem;
}

.info-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 1rem;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.info-icon svg {
  width: 24px;
  height: 24px;
}

.verification-banner {
  background: linear-gradient(135deg, #ffc107, #ff9800);
  color: white;
  padding: 1rem;
  border-radius: var(--border-radius);
  margin-bottom: 1.5rem;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.banner-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.banner-text h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
}

.banner-text p {
  margin: 0;
  font-size: 0.875rem;
  opacity: 0.9;
}

.profile-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

  .info-card.full-width {
    grid-column: 1 / -1;
  }



.info-card h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border);
}

.info-item, .status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.info-item:last-child, .status-item:last-child {
  margin-bottom: 0;
}

.info-label, .status-label {
  font-weight: 500;
  color: var(--color-text);
}

.info-value, .status-value {
  font-weight: 600;
  color: var(--color-heading);
}

.status-value.verified {
  color: #28a745;
}

.status-value.pending {
  color: #ffc107;
}

.status-value.rejected {
  color: #dc3545;
}

.status-value.unknown {
  color: var(--color-text-muted);
}

  .specializations-list {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .specialization-tag {
    background: var(--color-primary);
    color: white;
    padding: 0.25rem 0.75rem;
    border-radius: var(--border-radius);
    font-size: 0.875rem;
    font-weight: 500;
  }

.no-specializations {
  color: var(--color-text-muted);
  font-style: italic;
}

.bio-text {
  line-height: 1.6;
  color: var(--color-text);
}

.profile-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* Modal Styles */
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
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  max-width: 800px;
  width: 100%;
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
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.edit-form {
  padding: 1.5rem;
}

.form-section {
  margin-bottom: 2rem;
}

.form-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.section-description {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.form-input.error,
.form-select.error,
.form-textarea.error {
  border-color: var(--color-error);
}

.form-input[readonly] {
  background: var(--color-background-soft);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

.help-text {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.textarea-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
}

.char-count {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.specializations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
}

.specialization-checkbox {
  display: flex;
  align-items: center;
  padding: 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.specialization-checkbox:hover {
  border-color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.05);
}

.specialization-checkbox.checked {
  border-color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.1);
}

.checkbox-input {
  margin-right: 0.5rem;
  width: 1rem;
  height: 1rem;
}

.checkbox-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
}

  .form-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    margin-top: 2rem;
    padding-top: 1rem;
    border-top: 1px solid var(--color-border);
  }

  /* Certificate Modal Styles */
  .certificate-modal-content {
    background: var(--color-background);
    border-radius: var(--border-radius-lg);
    box-shadow: var(--shadow-lg);
    max-width: 90vw;
    width: 100%;
    height: 90vh;
    max-height: 90vh;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .modal-actions {
    display: flex;
    gap: 0.5rem;
    align-items: center;
  }

  .btn-sm {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
  }

  .certificate-viewer {
    flex: 1;
    min-height: 600px;
    height: calc(90vh - 80px);
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-background-soft);
  }

  .certificate-loading,
  .certificate-error {
    text-align: center;
    padding: 2rem;
  }

  .certificate-error svg {
    color: var(--color-error);
    margin-bottom: 1rem;
  }

  .certificate-error h4 {
    margin: 0 0 0.5rem 0;
    color: var(--color-heading);
  }

  .certificate-error p {
    margin: 0 0 1rem 0;
    color: var(--color-text-muted);
  }

  .certificate-iframe {
    width: 100%;
    height: 100%;
    border: none;
    background: white;
  }

@media (max-width: 768px) {
  .profile-info-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .specializations-grid {
    grid-template-columns: 1fr;
  }
  
  .profile-actions {
    flex-direction: column;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style> 