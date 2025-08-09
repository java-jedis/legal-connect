<template>
  <div class="lawyer-details-view">
    <!-- Back Navigation -->
    <div class="back-navigation">
      <button @click="handleBackToSearch" class="back-button">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Back to Search Results
      </button>
    </div>

    <!-- Loading state for initial page load -->
    <div v-if="isInitialLoading" class="loading-container">
      <div class="page-skeleton">
        <!-- Profile Section Skeleton -->
        <div class="skeleton-section">
          <div class="skeleton-header">
            <div class="skeleton-avatar"></div>
            <div class="skeleton-info">
              <div class="skeleton-line skeleton-name"></div>
              <div class="skeleton-line skeleton-firm"></div>
              <div class="skeleton-line skeleton-status"></div>
            </div>
          </div>
          <div class="skeleton-details">
            <div class="skeleton-line" v-for="i in 5" :key="i"></div>
          </div>
          <div class="skeleton-bio">
            <div class="skeleton-line" v-for="i in 3" :key="i"></div>
          </div>
          <div class="skeleton-tags">
            <div class="skeleton-tag" v-for="i in 4" :key="i"></div>
          </div>
        </div>

        <!-- Availability Section Skeleton -->
        <div class="skeleton-section">
          <div class="skeleton-section-title"></div>
          <div class="skeleton-availability">
            <div v-for="day in 3" :key="day" class="skeleton-day-group">
              <div class="skeleton-day-header"></div>
              <div class="skeleton-time-slots">
                <div v-for="slot in 2" :key="slot" class="skeleton-time-slot"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Reviews Section Skeleton -->
        <div class="skeleton-section">
          <div class="skeleton-section-title"></div>
          <div class="skeleton-rating">
            <div class="skeleton-stars"></div>
            <div class="skeleton-rating-text"></div>
          </div>
          <div class="skeleton-reviews">
            <div v-for="review in 3" :key="review" class="skeleton-review">
              <div class="skeleton-review-header">
                <div class="skeleton-client-avatar"></div>
                <div class="skeleton-client-info">
                  <div class="skeleton-line skeleton-client-name"></div>
                  <div class="skeleton-line skeleton-review-rating"></div>
                </div>
                <div class="skeleton-review-date"></div>
              </div>
              <div class="skeleton-review-content">
                <div class="skeleton-line" v-for="i in 2" :key="i"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main content when data is loaded -->
    <div v-else class="content-container">
      <!-- Profile Section -->
      <LawyerProfileDisplay 
        :profile="data.profile"
        :loading="loading.profile"
        :error="errors.profile"
        @retry="fetchLawyerProfile"
      />



      <!-- Availability Section -->
      <LawyerAvailabilitySection 
        :slots="data.availability"
        :loading="loading.availability"
        :error="errors.availability"
      />

      <!-- Reviews Section -->
      <LawyerReviewsSection 
        :reviews="data.reviews"
        :average-rating="data.averageRating"
        :loading="loading.reviews"
        :loading-more="loading.reviewsMore"
        :error="errors.reviews"
        :pagination="pagination"
        @retry="fetchReviews(0)"
        @load-page="handleLoadReviewsPage"
        @load-more="handleLoadMoreReviews"
      />
    </div>
  </div>
</template>

<script>
import LawyerAvailabilitySection from '@/components/lawyer/LawyerAvailabilitySection.vue'
import LawyerProfileDisplay from '@/components/lawyer/LawyerProfileDisplay.vue'
import LawyerReviewsSection from '@/components/lawyer/LawyerReviewsSection.vue'
import { lawyerAPI } from '@/services/api'

export default {
  name: 'LawyerDetailsView',
  components: {
    LawyerAvailabilitySection,
    LawyerProfileDisplay,
    LawyerReviewsSection
  },
  props: {
    email: {
      type: String,
      required: true
    }
  },

  data() {
    return {
      loading: {
        profile: false,
        availability: false,
        reviews: false,
        reviewsMore: false
      },
      data: {
        profile: null,
        availability: [],
        reviews: [],
        averageRating: 0
      },
      errors: {
        profile: null,
        availability: null,
        reviews: null
      },
      pagination: {
        currentPage: 0,
        totalPages: 0,
        hasMore: false
      }
    }
  },
  computed: {
    isInitialLoading() {
      return this.loading.profile && !this.data.profile && !this.errors.profile
    },
    decodedEmail() {
      try {
        return decodeURIComponent(this.email)
      } catch (error) {
        console.error('Error decoding email:', error)
        return this.email
      }
    }
  },
  async mounted() {
    await this.initializeData()
  },
  methods: {
    async initializeData() {
      // Fetch profile and availability first
      await Promise.all([
        this.fetchLawyerProfile(),
        this.fetchAvailabilitySlots()
      ])
      
      // Fetch reviews after profile is loaded to get lawyerId
      if (this.data.profile && this.data.profile.id) {
        await this.fetchReviews(0)
      }
    },

    async fetchLawyerProfile() {
      this.loading.profile = true
      this.errors.profile = null
      

      
      try {
        const response = await lawyerAPI.getLawyerInfo(this.decodedEmail)
        
        if (response && response.data) {
          this.data.profile = response.data
        } else {
          throw new Error('No profile data received')
        }
      } catch (error) {
        this.errors.profile = this.getErrorMessage(error, 'Failed to load lawyer profile')
      } finally {
        this.loading.profile = false
      }
    },

    async fetchAvailabilitySlots() {
      this.loading.availability = true
      this.errors.availability = null
      
      try {
        const response = await lawyerAPI.getAvailabilitySlots(this.decodedEmail)
        
        if (response && response.data) {
          // Handle different response structures
          let slots = []
          if (Array.isArray(response.data)) {
            slots = response.data
          } else if (response.data.slots && Array.isArray(response.data.slots)) {
            slots = response.data.slots
          } else if (response.data.data && response.data.data.slots && Array.isArray(response.data.data.slots)) {
            slots = response.data.data.slots
          } else if (response.data.data && Array.isArray(response.data.data)) {
            slots = response.data.data
          }
          
          this.data.availability = slots
        } else {
          this.data.availability = []
        }
      } catch (error) {
        this.errors.availability = this.getErrorMessage(error, 'Availability information unavailable')
      } finally {
        this.loading.availability = false
      }
    },

    async fetchReviews(page = 0) {
      if (!this.data.profile || !this.data.profile.id) {
        return
      }

      this.loading.reviews = true
      this.errors.reviews = null
      
      try {
        const response = await lawyerAPI.getLawyerReviews(this.data.profile.id, page, 10, 'DESC')
        
        if (response && response.data) {
          // Handle paginated response structure
          if (response.data.reviews && Array.isArray(response.data.reviews)) {
            // If first page, replace reviews; otherwise append for "load more" functionality
            if (page === 0) {
              this.data.reviews = response.data.reviews
            } else {
              this.data.reviews = [...this.data.reviews, ...response.data.reviews]
            }
            
            // Update pagination info
            this.pagination.currentPage = response.data.currentPage || page
            this.pagination.totalPages = response.data.totalPages || 0
            this.pagination.hasMore = response.data.hasNext || false
            
            // Calculate average rating
            this.calculateAverageRating()
          } else if (Array.isArray(response.data)) {
            // Handle direct array response
            this.data.reviews = page === 0 ? response.data : [...this.data.reviews, ...response.data]
            this.calculateAverageRating()
          } else {
            // No reviews found
            this.data.reviews = []
            this.data.averageRating = 0
          }
        } else {
          this.data.reviews = []
          this.data.averageRating = 0
        }
      } catch (error) {
        this.errors.reviews = this.getErrorMessage(error, 'Reviews unavailable')
        
        // Don't clear existing reviews on error if this was a "load more" request
        if (page === 0) {
          this.data.reviews = []
          this.data.averageRating = 0
        }
      } finally {
        this.loading.reviews = false
      }
    },

    calculateAverageRating() {
      if (!this.data.reviews || this.data.reviews.length === 0) {
        this.data.averageRating = 0
        return
      }
      
      const totalRating = this.data.reviews.reduce((sum, review) => sum + Number(review.rating), 0)
      this.data.averageRating = totalRating / this.data.reviews.length
    },

    getErrorMessage(error, defaultMessage) {
      if (error.response && error.response.data && error.response.data.message) {
        return error.response.data.message
      }
      if (error.message) {
        return error.message
      }
      return defaultMessage
    },

    getVerificationStatusClass(status) {
      return {
        'status-verified': status === 'VERIFIED',
        'status-pending': status === 'PENDING',
        'status-rejected': status === 'REJECTED'
      }
    },

    formatDate(dateString) {
      if (!dateString) return ''
      try {
        return new Date(dateString).toLocaleDateString()
      // eslint-disable-next-line no-unused-vars
      } catch (error) {
        return dateString
      }
    },

    async handleLoadReviewsPage(page) {
      if (!this.data.profile || !this.data.profile.id) {
        return
      }

      this.loading.reviewsMore = true
      this.errors.reviews = null
      
      try {
        const response = await lawyerAPI.getLawyerReviews(this.data.profile.id, page, 10, 'DESC')
        
        if (response && response.data) {
          // Handle paginated response structure
          if (response.data.reviews && Array.isArray(response.data.reviews)) {
            // Replace reviews for page navigation
            this.data.reviews = response.data.reviews
            
            // Update pagination info
            this.pagination.currentPage = response.data.currentPage || page
            this.pagination.totalPages = response.data.totalPages || 0
            this.pagination.hasMore = response.data.hasNext || false
            
            // Calculate average rating
            this.calculateAverageRating()
          } else if (Array.isArray(response.data)) {
            // Handle direct array response
            this.data.reviews = response.data
            this.calculateAverageRating()
          } else {
            // No reviews found
            this.data.reviews = []
            this.data.averageRating = 0
          }
        } else {
          this.data.reviews = []
          this.data.averageRating = 0
        }
      } catch (error) {
        this.errors.reviews = this.getErrorMessage(error, 'Failed to load reviews page')
      } finally {
        this.loading.reviewsMore = false
      }
    },

    async handleLoadMoreReviews() {
      if (!this.data.profile || !this.data.profile.id) {
        return
      }

      if (!this.pagination.hasMore) {
        return
      }

      const nextPage = this.pagination.currentPage + 1
      this.loading.reviewsMore = true
      this.errors.reviews = null
      
      try {
        const response = await lawyerAPI.getLawyerReviews(this.data.profile.id, nextPage, 10, 'DESC')
        
        if (response && response.data) {
          // Handle paginated response structure
          if (response.data.reviews && Array.isArray(response.data.reviews)) {
            // Append reviews for "load more" functionality
            this.data.reviews = [...this.data.reviews, ...response.data.reviews]
            
            // Update pagination info
            this.pagination.currentPage = response.data.currentPage || nextPage
            this.pagination.totalPages = response.data.totalPages || 0
            this.pagination.hasMore = response.data.hasNext || false
            
            // Calculate average rating
            this.calculateAverageRating()
          } else if (Array.isArray(response.data)) {
            // Handle direct array response
            this.data.reviews = [...this.data.reviews, ...response.data]
            this.calculateAverageRating()
          }
        }
      } catch (error) {
        this.errors.reviews = this.getErrorMessage(error, 'Failed to load more reviews')
      } finally {
        this.loading.reviewsMore = false
      }
    },

    handleBackToSearch() {
      // Try to preserve search state by going back in history
      // This works when user navigated here from FindLawyersView
      try {
        // Check if we have history to go back to and came from find-lawyer
        const canGoBack = window.history.length > 1
        const cameFromSearch = document.referrer.includes('/find-lawyer') || 
                              this.$route.query.from === 'search'
        
        if (canGoBack && cameFromSearch) {
          // Use router.go(-1) to maintain search filters and results
          this.$router.go(-1)
        } else {
          // Fallback: direct navigation to find-lawyer page
          this.$router.push('/find-lawyer')
        }
      } catch (error) {
        console.warn('Error with back navigation, using fallback:', error)
        // Fallback: navigate to find-lawyer page
        this.$router.push('/find-lawyer')
      }
    }
  }
}
</script>

<style scoped>
.lawyer-details-view {
  min-height: 100vh;
  background: linear-gradient(
    135deg,
    var(--color-background-soft) 0%,
    var(--color-background-mute) 100%
  );
  padding: 2rem 0;
}

.back-navigation {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  margin-bottom: 2rem;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--color-background);
  color: var(--color-text);
  border: 2px solid var(--color-border);
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
  box-shadow: var(--shadow-sm);
}

.back-button:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateX(-2px);
  box-shadow: var(--shadow-md);
}

.back-button:active {
  transform: translateX(0);
}

.back-icon {
  width: 18px;
  height: 18px;
  transition: transform var(--transition-fast);
}

.back-button:hover .back-icon {
  transform: translateX(-2px);
}

.loading-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-section,
.reviews-section {
  background: var(--color-background);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  position: relative;
  overflow: hidden;
}

.profile-section::before,
.reviews-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(
    90deg,
    var(--color-primary),
    var(--color-secondary)
  );
}

.reviews-section h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 1.5rem 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.section-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem;
}

.section-loading p {
  color: var(--color-text-muted);
  margin-top: 1rem;
  font-weight: 500;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-border);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-section {
  text-align: center;
  padding: 3rem;
}

.error-message {
  color: var(--color-error);
  font-weight: 500;
}

.error-message p {
  margin-bottom: 1rem;
  font-size: 1.125rem;
}

.retry-button {
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.retry-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.status-verified {
  color: var(--color-success);
  font-weight: 600;
}

.status-pending {
  color: var(--color-warning);
  font-weight: 600;
}

.status-rejected {
  color: var(--color-error);
  font-weight: 600;
}

.reviews-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.rating-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-heading);
}

.rating-stars {
  color: var(--color-warning);
  font-size: 1.5rem;
}

.review-item {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  transition: all var(--transition-fast);
}

.review-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
  border-color: var(--color-primary);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.client-name {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 1rem;
}

.rating {
  color: var(--color-warning);
  font-weight: 600;
  font-size: 0.875rem;
}

.review-text {
  color: var(--color-text);
  margin-bottom: 0.75rem;
  line-height: 1.6;
  font-size: 0.9375rem;
}

.review-date {
  color: var(--color-text-muted);
  font-size: 0.8125rem;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-muted);
}

.empty-state p {
  margin: 0;
  font-style: italic;
  font-size: 1.125rem;
}

/* Skeleton Loading Styles */
.page-skeleton {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-section {
  background: var(--color-background);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  position: relative;
  overflow: hidden;
}

.skeleton-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(
    90deg,
    var(--color-primary),
    var(--color-secondary)
  );
}

.skeleton-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.skeleton-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--color-background-soft);
  margin-right: 1.5rem;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
}

.skeleton-line {
  height: 16px;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  margin-bottom: 0.5rem;
}

.skeleton-name {
  width: 200px;
  height: 20px;
}

.skeleton-firm {
  width: 150px;
}

.skeleton-status {
  width: 100px;
}

.skeleton-details {
  margin-bottom: 2rem;
}

.skeleton-details .skeleton-line {
  width: 100%;
  margin-bottom: 0.75rem;
}

.skeleton-bio {
  margin-bottom: 2rem;
}

.skeleton-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.skeleton-tag {
  width: 80px;
  height: 32px;
  background: var(--color-background-soft);
  border-radius: 20px;
}

.skeleton-section-title {
  width: 200px;
  height: 24px;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  margin-bottom: 2rem;
}

.skeleton-availability {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.skeleton-day-group {
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.skeleton-day-header {
  height: 48px;
  background: var(--color-background-soft);
}

.skeleton-time-slots {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.skeleton-time-slot {
  height: 40px;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
}

.skeleton-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.skeleton-stars {
  width: 120px;
  height: 24px;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
}

.skeleton-rating-text {
  width: 80px;
  height: 16px;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
}

.skeleton-reviews {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.skeleton-review {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
}

.skeleton-review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.skeleton-client-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-background-mute);
  flex-shrink: 0;
}

.skeleton-client-info {
  flex: 1;
  margin-left: 0.75rem;
}

.skeleton-client-name {
  width: 120px;
  height: 16px;
}

.skeleton-review-rating {
  width: 80px;
  height: 14px;
}

.skeleton-review-date {
  width: 80px;
  height: 14px;
  background: var(--color-background-mute);
  border-radius: var(--border-radius);
}

.skeleton-review-content .skeleton-line {
  width: 100%;
  margin-bottom: 0.5rem;
  background: var(--color-background-mute);
}

.skeleton-review-content .skeleton-line:last-child {
  width: 75%;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* Responsive design */
@media (max-width: 768px) {
  .lawyer-details-view {
    padding: 1rem 0;
  }
  
  .back-navigation {
    padding: 0 1rem;
    margin-bottom: 1rem;
  }
  
  .back-button {
    padding: 0.625rem 1rem;
    font-size: 0.8125rem;
  }
  
  .back-icon {
    width: 16px;
    height: 16px;
  }

  .loading-container,
  .content-container {
    padding: 0 1rem;
  }
  
  .profile-section,
  .reviews-section {
    padding: 1.5rem;
  }

  .reviews-section h3 {
    font-size: 1.25rem;
  }
  
  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .average-rating {
    padding: 1rem;
  }

  .rating-value {
    font-size: 1.5rem;
  }

  .rating-stars {
    font-size: 1.25rem;
  }

  /* Skeleton responsive adjustments */
  .page-skeleton {
    gap: 1.5rem;
  }
  
  .skeleton-section {
    padding: 1.5rem;
  }
  
  .skeleton-header {
    margin-bottom: 1.5rem;
  }
  
  .skeleton-avatar {
    width: 60px;
    height: 60px;
    margin-right: 1rem;
  }
  
  .skeleton-name {
    width: 150px;
  }
  
  .skeleton-firm {
    width: 120px;
  }
  
  .skeleton-availability {
    gap: 0.75rem;
  }
  
  .skeleton-day-header {
    height: 40px;
  }
  
  .skeleton-time-slots {
    padding: 0.75rem;
  }
  
  .skeleton-review {
    padding: 1rem;
  }
  
  .skeleton-review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .skeleton-client-avatar {
    width: 40px;
    height: 40px;
  }
}
</style>