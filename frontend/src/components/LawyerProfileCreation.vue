<template>
  <div class="lawyer-profile-creation">
    <div class="container">
      <div class="profile-form-container">
        <div class="form-header">
          <h1 class="form-title">Complete Your Lawyer Profile</h1>
          <p class="form-subtitle">
            Please provide your professional information and upload your bar certificate to complete your profile verification.
          </p>
        </div>

        <form @submit.prevent="submitProfile" class="profile-form">
          <!-- Basic Information -->
          <div class="form-section">
            <h2 class="section-title">Professional Information</h2>
            
            <div class="form-row">
              <div class="form-group">
                <label for="firm" class="form-label">Law Firm Name *</label>
                <input
                  id="firm"
                  v-model="formData.firm"
                  type="text"
                  class="form-input"
                  :class="{ 'error': errors.firm }"
                  placeholder="Enter your law firm name"
                  required
                />
                <span v-if="errors.firm" class="error-message">{{ errors.firm }}</span>
              </div>

              <div class="form-group">
                <label for="yearsOfExperience" class="form-label">Years of Experience *</label>
                <input
                  id="yearsOfExperience"
                  v-model.number="formData.yearsOfExperience"
                  type="number"
                  min="0"
                  max="50"
                  class="form-input"
                  :class="{ 'error': errors.yearsOfExperience }"
                  placeholder="Enter years of experience"
                  required
                />
                <span v-if="errors.yearsOfExperience" class="error-message">{{ errors.yearsOfExperience }}</span>
              </div>
            </div>

            <div class="form-group">
              <label for="barCertificateNumber" class="form-label">Bar Certificate Number *</label>
              <input
                id="barCertificateNumber"
                v-model="formData.barCertificateNumber"
                type="text"
                class="form-input"
                :class="{ 'error': errors.barCertificateNumber }"
                placeholder="Enter your bar certificate number"
                required
              />
              <span v-if="errors.barCertificateNumber" class="error-message">{{ errors.barCertificateNumber }}</span>
            </div>
          </div>

          <!-- Location Information -->
          <div class="form-section">
            <h2 class="section-title">Practice Location</h2>
            
            <div class="form-row">
              <div class="form-group">
                <label for="practicingCourt" class="form-label">Practicing Court *</label>
                <select
                  id="practicingCourt"
                  v-model="formData.practicingCourt"
                  class="form-select"
                  :class="{ 'error': errors.practicingCourt }"
                  required
                >
                  <option value="">Select practicing court</option>
                  <option v-for="court in practicingCourts" :key="court.value" :value="court.value">
                    {{ court.label }}
                  </option>
                </select>
                <span v-if="errors.practicingCourt" class="error-message">{{ errors.practicingCourt }}</span>
              </div>

              <div class="form-group">
                <label for="division" class="form-label">Division *</label>
                <select
                  id="division"
                  v-model="formData.division"
                  class="form-select"
                  :class="{ 'error': errors.division }"
                  required
                >
                  <option value="">Select division</option>
                  <option v-for="div in divisions" :key="div.value" :value="div.value">
                    {{ div.label }}
                  </option>
                </select>
                <span v-if="errors.division" class="error-message">{{ errors.division }}</span>
              </div>
            </div>

            <div class="form-group">
              <label for="district" class="form-label">District *</label>
              <select
                id="district"
                v-model="formData.district"
                class="form-select"
                :class="{ 'error': errors.district }"
                required
              >
                <option value="">Select district</option>
                <option v-for="dist in districts" :key="dist.value" :value="dist.value">
                  {{ dist.label }}
                </option>
              </select>
              <span v-if="errors.district" class="error-message">{{ errors.district }}</span>
            </div>
          </div>

          <!-- Specializations -->
          <div class="form-section">
            <h2 class="section-title">Specializations *</h2>
            <p class="section-description">Select all areas of law you specialize in</p>
            
            <div class="specializations-grid">
              <label
                v-for="spec in specializations"
                :key="spec.value"
                class="specialization-checkbox"
                :class="{ 'checked': formData.specializations.includes(spec.value) }"
              >
                <input
                  type="checkbox"
                  :value="spec.value"
                  v-model="formData.specializations"
                  class="checkbox-input"
                />
                <span class="checkbox-label">{{ spec.label }}</span>
              </label>
            </div>
            <span v-if="errors.specializations" class="error-message">{{ errors.specializations }}</span>
          </div>

          <!-- Bio -->
          <div class="form-section">
            <h2 class="section-title">Professional Bio</h2>
            <p class="section-description">Tell clients about your expertise and experience (optional)</p>
            
            <div class="form-group">
              <textarea
                v-model="formData.bio"
                class="form-textarea"
                :class="{ 'error': errors.bio }"
                placeholder="Describe your legal expertise, notable cases, and professional achievements..."
                rows="6"
                maxlength="2000"
              ></textarea>
              <div class="textarea-footer">
                <span v-if="errors.bio" class="error-message">{{ errors.bio }}</span>
                <span class="char-count">{{ formData.bio.length }}/2000</span>
              </div>
            </div>
          </div>

          <!-- Bar Certificate Upload -->
          <div class="form-section">
            <h2 class="section-title">Bar Certificate Upload *</h2>
            <p class="section-description">Upload a clear copy of your bar certificate for verification</p>
            
            <div class="file-upload-container">
              <div
                class="file-upload-area"
                :class="{ 'error': errors.barCertificate, 'has-file': selectedFile }"
                @click="triggerFileInput"
                @dragover.prevent
                @drop.prevent="handleFileDrop"
              >
                <div v-if="!selectedFile" class="upload-placeholder">
                  <svg class="upload-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15V19A2 2 0 0 1 19 21H5A2 2 0 0 1 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <polyline points="7,10 12,15 17,10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <p class="upload-text">Click to upload or drag and drop</p>
                  <p class="upload-hint">PDF, JPG, PNG up to 10MB</p>
                </div>
                <div v-else class="file-info">
                  <svg class="file-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <div class="file-details">
                    <p class="file-name">{{ selectedFile.name }}</p>
                    <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
                  </div>
                  <button type="button" class="remove-file" @click.stop="removeFile">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
              <input
                ref="fileInput"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                @change="handleFileSelect"
                class="hidden-input"
              />
              <span v-if="errors.barCertificate" class="error-message">{{ errors.barCertificate }}</span>
            </div>
          </div>

          <!-- Submit Button -->
          <div class="form-actions">
            <button
              type="submit"
              class="btn btn-primary btn-lg"
              :disabled="isSubmitting"
            >
              <span v-if="isSubmitting" class="loading-spinner"></span>
              {{ isSubmitting ? 'Submitting...' : 'Submit Profile for Verification' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLawyerStore } from '../stores/lawyer'

const router = useRouter()
const lawyerStore = useLawyerStore()

// Form data
const formData = reactive({
  firm: '',
  yearsOfExperience: null,
  barCertificateNumber: '',
  practicingCourt: '',
  division: '',
  district: '',
  bio: '',
  specializations: []
})

// File upload
const fileInput = ref(null)
const selectedFile = ref(null)

// Form state
const isSubmitting = ref(false)
const errors = reactive({})

// Enums data
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
const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    validateAndSetFile(file)
  }
}

const handleFileDrop = (event) => {
  const file = event.dataTransfer.files[0]
  if (file) {
    validateAndSetFile(file)
  }
}

const validateAndSetFile = (file) => {
  const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png']
  const maxSize = 10 * 1024 * 1024 // 10MB

  if (!allowedTypes.includes(file.type)) {
    errors.barCertificate = 'Please upload a PDF, JPG, or PNG file'
    return
  }

  if (file.size > maxSize) {
    errors.barCertificate = 'File size must be less than 10MB'
    return
  }

  selectedFile.value = file
  errors.barCertificate = ''
}

const removeFile = () => {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  errors.barCertificate = ''
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const validateForm = () => {
  // Clear previous errors
  Object.keys(errors).forEach(key => delete errors[key])

  if (!formData.firm.trim()) {
    errors.firm = 'Firm name is required'
  }

  if (!formData.yearsOfExperience || formData.yearsOfExperience < 0 || formData.yearsOfExperience > 50) {
    errors.yearsOfExperience = 'Years of experience must be between 0 and 50'
  }

  if (!formData.barCertificateNumber.trim()) {
    errors.barCertificateNumber = 'Bar certificate number is required'
  }

  if (!formData.practicingCourt) {
    errors.practicingCourt = 'Practicing court is required'
  }

  if (!formData.division) {
    errors.division = 'Division is required'
  }

  if (!formData.district) {
    errors.district = 'District is required'
  }

  if (formData.specializations.length === 0) {
    errors.specializations = 'At least one specialization is required'
  }

  if (!selectedFile.value) {
    errors.barCertificate = 'Bar certificate file is required'
  }

  return Object.keys(errors).length === 0
}

const submitProfile = async () => {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    // First, create the lawyer profile
    await lawyerStore.createLawyerProfile({
      firm: formData.firm,
      yearsOfExperience: formData.yearsOfExperience,
      barCertificateNumber: formData.barCertificateNumber,
      practicingCourt: formData.practicingCourt,
      division: formData.division,
      district: formData.district,
      bio: formData.bio,
      specializations: formData.specializations
    })

    // Then upload the bar certificate
    if (selectedFile.value) {
      await lawyerStore.uploadCredentials(selectedFile.value)
    }

    // Redirect to dashboard with success message
    router.push({
      name: 'lawyer-dashboard',
      query: { profileCreated: 'true' }
    })

  } catch (error) {
    console.error('Error creating lawyer profile:', error)
    
    if (error.response?.data?.error?.message) {
      alert(`Error: ${error.response.data.error.message}`)
    } else {
      alert('An error occurred while creating your profile. Please try again.')
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.lawyer-profile-creation {
  min-height: 100vh;
  background: var(--color-background-soft);
  padding: 2rem 0;
}

.profile-form-container {
  max-width: 800px;
  margin: 0 auto;
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.form-header {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  padding: 2rem;
  text-align: center;
}

.form-title {
  font-size: 1.875rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.form-subtitle {
  font-size: 1.125rem;
  opacity: 0.9;
}

.profile-form {
  padding: 2rem;
}

.form-section {
  margin-bottom: 2.5rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.section-description {
  color: var(--color-text-muted);
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
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
  gap: 1rem;
}

.specialization-checkbox {
  display: flex;
  align-items: center;
  padding: 0.75rem;
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
  margin-right: 0.75rem;
  width: 1.25rem;
  height: 1.25rem;
}

.checkbox-label {
  font-weight: 500;
  color: var(--color-text);
}

.file-upload-container {
  margin-top: 1rem;
}

.file-upload-area {
  border: 2px dashed var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.file-upload-area:hover {
  border-color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.05);
}

.file-upload-area.error {
  border-color: var(--color-error);
  background: rgba(var(--color-error-rgb), 0.05);
}

.file-upload-area.has-file {
  border-color: var(--color-success);
  background: rgba(var(--color-success-rgb), 0.05);
}

.upload-icon {
  width: 48px;
  height: 48px;
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}

.upload-text {
  font-size: 1.125rem;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.upload-hint {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.file-icon {
  width: 32px;
  height: 32px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.file-details {
  flex: 1;
  text-align: left;
}

.file-name {
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.file-size {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.remove-file {
  background: none;
  border: none;
  color: var(--color-error);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

.remove-file:hover {
  background: rgba(var(--color-error-rgb), 0.1);
}

.remove-file svg {
  width: 20px;
  height: 20px;
}

.hidden-input {
  display: none;
}

.form-actions {
  margin-top: 2rem;
  text-align: center;
}

.btn-lg {
  padding: 1rem 2rem;
  font-size: 1.125rem;
  font-weight: 600;
}

.loading-spinner {
  display: inline-block;
  width: 1rem;
  height: 1rem;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .specializations-grid {
    grid-template-columns: 1fr;
  }
  
  .profile-form {
    padding: 1.5rem;
  }
  
  .form-header {
    padding: 1.5rem;
  }
}
</style> 