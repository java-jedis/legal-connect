<template>
  <div class="register-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <h1 class="hero-title">Join LegalConnect</h1>
        <p class="hero-subtitle">
          Choose your path and start your legal journey with AI-powered assistance
        </p>
      </div>
    </section>

    <!-- User Type Selection -->
    <section class="user-type-section section">
      <div class="container">
        <div class="user-type-cards">
          <div
            class="user-type-card"
            :class="{ active: userType === 'user' }"
            @click="selectUserType('user')"
          >
            <div class="user-type-icon">
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
            <h3>I Need Legal Help</h3>
            <p>Get AI-powered legal guidance and connect with expert lawyers</p>
            <ul class="user-benefits">
              <li>Free AI legal assistant</li>
              <li>Connect with verified lawyers</li>
              <li>Secure document management</li>
              <li>24/7 support</li>
            </ul>
          </div>

          <div
            class="user-type-card"
            :class="{ active: userType === 'lawyer' }"
            @click="selectUserType('lawyer')"
          >
            <div class="user-type-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M12 22S20 18 20 12V5L12 2L4 5V12C4 18 12 22 12 22Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M9 12L11 14L15 10"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <h3>I'm a Lawyer</h3>
            <p>Join our network of verified legal professionals</p>
            <ul class="user-benefits">
              <li>Access to AI tools</li>
              <li>Client management platform</li>
              <li>Secure communication</li>
              <li>Practice management</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- Registration Form -->
    <section class="registration-section section" v-if="userType">
      <div class="container">
        <div class="registration-container">
          <div class="registration-header">
            <h2 class="section-title">
              {{ userType === 'user' ? 'Create Your Account' : 'Lawyer Registration' }}
            </h2>
            <p class="section-subtitle">
              {{
                userType === 'user'
                  ? 'Join thousands of users getting legal help with AI assistance'
                  : 'Complete your profile to join our network of verified lawyers'
              }}
            </p>
          </div>

          <form class="registration-form" @submit.prevent="submitRegistration">
            <!-- Basic Information -->
            <div class="form-section">
              <h3 class="form-section-title">Basic Information</h3>
              <div class="form-grid grid grid-2">
                <div class="form-group">
                  <label for="firstName">First Name *</label>
                  <input
                    type="text"
                    id="firstName"
                    v-model="form.firstName"
                    required
                    placeholder="Enter your first name"
                  />
                </div>

                <div class="form-group">
                  <label for="lastName">Last Name *</label>
                  <input
                    type="text"
                    id="lastName"
                    v-model="form.lastName"
                    required
                    placeholder="Enter your last name"
                  />
                </div>
              </div>

              <div class="form-group">
                <label for="email">Email Address *</label>
                <input
                  type="email"
                  id="email"
                  v-model="form.email"
                  required
                  placeholder="Enter your email address"
                />
              </div>

              <div class="form-group">
                <label for="phone">Phone Number</label>
                <input
                  type="tel"
                  id="phone"
                  v-model="form.phone"
                  placeholder="Enter your phone number"
                />
              </div>

              <div class="form-group">
                <label for="password">Password *</label>
                <input
                  type="password"
                  id="password"
                  v-model="form.password"
                  required
                  placeholder="Create a strong password"
                />
              </div>

              <div class="form-group">
                <label for="confirmPassword">Confirm Password *</label>
                <input
                  type="password"
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  required
                  placeholder="Confirm your password"
                />
              </div>
            </div>

            <!-- Lawyer-specific Information -->
            <div class="form-section" v-if="userType === 'lawyer'">
              <h3 class="form-section-title">Professional Information</h3>

              <div class="form-grid grid grid-2">
                <div class="form-group">
                  <label for="barNumber">Bar Number *</label>
                  <input
                    type="text"
                    id="barNumber"
                    v-model="form.barNumber"
                    required
                    placeholder="Enter your bar number"
                  />
                </div>

                <div class="form-group">
                  <label for="state">State of Practice *</label>
                  <select id="state" v-model="form.state" required>
                    <option value="">Select state</option>
                    <option value="CA">California</option>
                    <option value="NY">New York</option>
                    <option value="TX">Texas</option>
                    <option value="FL">Florida</option>
                    <option value="IL">Illinois</option>
                    <option value="PA">Pennsylvania</option>
                    <option value="OH">Ohio</option>
                    <option value="GA">Georgia</option>
                    <option value="NC">North Carolina</option>
                    <option value="MI">Michigan</option>
                    <option value="NJ">New Jersey</option>
                    <option value="VA">Virginia</option>
                    <option value="WA">Washington</option>
                    <option value="AZ">Arizona</option>
                    <option value="MA">Massachusetts</option>
                    <option value="TN">Tennessee</option>
                    <option value="IN">Indiana</option>
                    <option value="MO">Missouri</option>
                    <option value="MD">Maryland</option>
                    <option value="CO">Colorado</option>
                    <option value="MN">Minnesota</option>
                    <option value="WI">Wisconsin</option>
                    <option value="SC">South Carolina</option>
                    <option value="AL">Alabama</option>
                    <option value="LA">Louisiana</option>
                    <option value="KY">Kentucky</option>
                    <option value="OR">Oregon</option>
                    <option value="OK">Oklahoma</option>
                    <option value="CT">Connecticut</option>
                    <option value="UT">Utah</option>
                    <option value="IA">Iowa</option>
                    <option value="NV">Nevada</option>
                    <option value="AR">Arkansas</option>
                    <option value="MS">Mississippi</option>
                    <option value="KS">Kansas</option>
                    <option value="NE">Nebraska</option>
                    <option value="ID">Idaho</option>
                    <option value="WV">West Virginia</option>
                    <option value="HI">Hawaii</option>
                    <option value="NH">New Hampshire</option>
                    <option value="ME">Maine</option>
                    <option value="RI">Rhode Island</option>
                    <option value="MT">Montana</option>
                    <option value="DE">Delaware</option>
                    <option value="SD">South Dakota</option>
                    <option value="ND">North Dakota</option>
                    <option value="AK">Alaska</option>
                    <option value="VT">Vermont</option>
                    <option value="WY">Wyoming</option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label for="practiceAreas">Practice Areas *</label>
                <div class="checkbox-group">
                  <label class="checkbox-item" v-for="area in practiceAreas" :key="area.id">
                    <input type="checkbox" :value="area.id" v-model="form.practiceAreas" />
                    <span class="checkmark"></span>
                    {{ area.name }}
                  </label>
                </div>
              </div>

              <div class="form-group">
                <label for="experience">Years of Experience *</label>
                <select id="experience" v-model="form.experience" required>
                  <option value="">Select experience level</option>
                  <option value="0-2">0-2 years</option>
                  <option value="3-5">3-5 years</option>
                  <option value="6-10">6-10 years</option>
                  <option value="11-15">11-15 years</option>
                  <option value="16-20">16-20 years</option>
                  <option value="20+">20+ years</option>
                </select>
              </div>

              <div class="form-group">
                <label for="bio">Professional Bio</label>
                <textarea
                  id="bio"
                  v-model="form.bio"
                  rows="4"
                  placeholder="Tell us about your legal expertise and experience..."
                ></textarea>
              </div>
            </div>

            <!-- Document Upload (Lawyers Only) -->
            <div class="form-section" v-if="userType === 'lawyer'">
              <h3 class="form-section-title">Required Documents</h3>
              <p class="form-section-description">
                Please upload the following documents for verification. All documents will be kept
                secure and confidential.
              </p>

              <div class="document-upload-grid">
                <div class="document-upload-item">
                  <label for="license">Bar License *</label>
                  <div class="file-upload">
                    <input
                      type="file"
                      id="license"
                      @change="handleFileUpload('license', $event)"
                      accept=".pdf,.jpg,.jpeg,.png"
                      required
                    />
                    <div class="file-upload-content">
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
                      <span>{{
                        form.documents.license ? form.documents.license.name : 'Upload Bar License'
                      }}</span>
                    </div>
                  </div>
                </div>

                <div class="document-upload-item">
                  <label for="malpractice">Malpractice Insurance *</label>
                  <div class="file-upload">
                    <input
                      type="file"
                      id="malpractice"
                      @change="handleFileUpload('malpractice', $event)"
                      accept=".pdf,.jpg,.jpeg,.png"
                      required
                    />
                    <div class="file-upload-content">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path
                          d="M12 22S20 18 20 12V5L12 2L4 5V12C4 18 12 22 12 22Z"
                          stroke="currentColor"
                          stroke-width="2"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        />
                      </svg>
                      <span>{{
                        form.documents.malpractice
                          ? form.documents.malpractice.name
                          : 'Upload Insurance Certificate'
                      }}</span>
                    </div>
                  </div>
                </div>

                <div class="document-upload-item">
                  <label for="resume">Professional Resume</label>
                  <div class="file-upload">
                    <input
                      type="file"
                      id="resume"
                      @change="handleFileUpload('resume', $event)"
                      accept=".pdf,.doc,.docx"
                    />
                    <div class="file-upload-content">
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
                      <span>{{
                        form.documents.resume
                          ? form.documents.resume.name
                          : 'Upload Resume (Optional)'
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Terms and Conditions -->
            <div class="form-section">
              <div class="form-group">
                <label class="checkbox-item">
                  <input type="checkbox" v-model="form.agreeToTerms" required />
                  <span class="checkmark"></span>
                  I agree to the <a href="#" class="link">Terms of Service</a> and
                  <a href="#" class="link">Privacy Policy</a> *
                </label>
              </div>

              <div class="form-group" v-if="userType === 'lawyer'">
                <label class="checkbox-item">
                  <input type="checkbox" v-model="form.agreeToVerification" required />
                  <span class="checkmark"></span>
                  I consent to background verification and document review *
                </label>
              </div>
            </div>

            <!-- Submit Button -->
            <div class="form-actions">
              <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
                {{
                  isSubmitting
                    ? 'Creating Account...'
                    : userType === 'user'
                      ? 'Create Account'
                      : 'Submit Application'
                }}
              </button>
            </div>
          </form>

          <!-- Login Link -->
          <div class="login-link">
            <p>
              Already have an account?
              <router-link to="/login" class="link">Sign in here</router-link>
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const userType = ref('')
const isSubmitting = ref(false)

const practiceAreas = [
  { id: 'family', name: 'Family Law' },
  { id: 'criminal', name: 'Criminal Defense' },
  { id: 'business', name: 'Business Law' },
  { id: 'employment', name: 'Employment Law' },
  { id: 'property', name: 'Property Law' },
  { id: 'contract', name: 'Contract Law' },
  { id: 'personal-injury', name: 'Personal Injury' },
  { id: 'immigration', name: 'Immigration Law' },
  { id: 'tax', name: 'Tax Law' },
  { id: 'intellectual-property', name: 'Intellectual Property' },
  { id: 'bankruptcy', name: 'Bankruptcy' },
  { id: 'estate-planning', name: 'Estate Planning' },
]

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  // Lawyer-specific fields
  barNumber: '',
  state: '',
  practiceAreas: [],
  experience: '',
  bio: '',
  documents: {
    license: null,
    malpractice: null,
    resume: null,
  },
  agreeToTerms: false,
  agreeToVerification: false,
})

const selectUserType = (type) => {
  userType.value = type
  // Reset form when switching user types
  Object.assign(form, {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    barNumber: '',
    state: '',
    practiceAreas: [],
    experience: '',
    bio: '',
    documents: {
      license: null,
      malpractice: null,
      resume: null,
    },
    agreeToTerms: false,
    agreeToVerification: false,
  })
}

const handleFileUpload = (type, event) => {
  const file = event.target.files[0]
  if (file) {
    form.documents[type] = file
  }
}

const submitRegistration = async () => {
  if (form.password !== form.confirmPassword) {
    alert('Passwords do not match')
    return
  }

  isSubmitting.value = true

  try {
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 2000))

    if (userType.value === 'user') {
      alert('Account created successfully! Welcome to LegalAI.')
    } else {
      alert(
        'Application submitted successfully! We will review your documents and get back to you within 2-3 business days.',
      )
    }

    // Reset form
    selectUserType(userType.value)
  } catch {
    alert('An error occurred. Please try again.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(
    135deg,
    var(--color-background) 0%,
    var(--color-background-soft) 100%
  );
  padding: 3rem 0 2rem;
  text-align: center;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--color-text-muted);
  max-width: 600px;
  margin: 0 auto;
}

.user-type-section {
  background: var(--color-background);
}

.user-type-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.user-type-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 2px solid var(--color-border);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.user-type-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.user-type-card.active {
  border-color: var(--color-primary);
  background: var(--color-background);
  box-shadow: var(--shadow-lg);
}

.user-type-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
}

.user-type-icon svg {
  width: 40px;
  height: 40px;
}

.user-type-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.user-type-card p {
  color: var(--color-text-muted);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.user-benefits {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: left;
}

.user-benefits li {
  padding: 0.5rem 0;
  color: var(--color-text);
  position: relative;
  padding-left: 1.5rem;
}

.user-benefits li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

.registration-section {
  background: var(--color-background-soft);
}

.registration-container {
  max-width: 800px;
  margin: 0 auto;
}

.registration-header {
  text-align: center;
  margin-bottom: 3rem;
}

.form-section {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  margin-bottom: 2rem;
}

.form-section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1.5rem;
}

.form-section-description {
  color: var(--color-text-muted);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.registration-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-heading);
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  color: var(--color-text);
  font-size: 1rem;
  transition: border-color var(--transition-fast);
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 0.875rem;
  color: var(--color-text);
}

.checkbox-item input[type='checkbox'] {
  display: none;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  margin-right: 0.75rem;
  position: relative;
  transition: all var(--transition-fast);
}

.checkbox-item input[type='checkbox']:checked + .checkmark {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.checkbox-item input[type='checkbox']:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--color-background);
  font-size: 12px;
  font-weight: bold;
}

.document-upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.document-upload-item {
  margin-bottom: 1.5rem;
}

.file-upload {
  position: relative;
}

.file-upload input[type='file'] {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.file-upload-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 2px dashed var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background-soft);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.file-upload-content:hover {
  border-color: var(--color-primary);
  background: var(--color-background);
}

.file-upload-content svg {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.file-upload-content span {
  color: var(--color-text);
  font-size: 0.875rem;
}

.form-actions {
  text-align: center;
  margin-top: 2rem;
}

.login-link {
  text-align: center;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--color-border);
}

.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .user-type-cards {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .checkbox-group {
    grid-template-columns: 1fr;
  }

  .document-upload-grid {
    grid-template-columns: 1fr;
  }
}
</style>
