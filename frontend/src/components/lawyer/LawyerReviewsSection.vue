<template>
  <div class="lawyer-reviews-section">
    <div class="section-header">
      <h3>Client Reviews</h3>
      <div v-if="averageRating > 0" class="average-rating">
        <div class="star-rating">
          <span 
            v-for="star in 5" 
            :key="star" 
            class="star"
            :class="{ 'filled': star <= Math.round(Number(averageRating)) }"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" 
                    :fill="star <= Math.round(Number(averageRating)) ? '#ffc107' : 'none'"
                    stroke="currentColor" 
                    stroke-width="2" 
                    stroke-linecap="round" 
                    stroke-linejoin="round"/>
            </svg>
          </span>
        </div>
        <span class="rating-text">{{ averageRating.toFixed(1) }} out of 5</span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="reviews-skeleton">
        <div class="skeleton-rating">
          <div class="skeleton-stars"></div>
          <div class="skeleton-rating-text"></div>
        </div>
        <div class="skeleton-reviews-list">
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

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
          <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
        </svg>
      </div>
      <h4>Reviews unavailable</h4>
      <p>{{ error }}</p>
      <button @click="$emit('retry')" class="retry-button" type="button">
        Try Again
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="!reviews || reviews.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" 
                stroke="currentColor" 
                stroke-width="2" 
                stroke-linecap="round" 
                stroke-linejoin="round"/>
        </svg>
      </div>
      <h4>No reviews yet</h4>
      <p>This lawyer hasn't received any client reviews yet.</p>
    </div>

    <!-- Reviews Content -->
    <div v-else class="reviews-content">
      <div class="reviews-list">
        <div 
          v-for="review in reviews" 
          :key="review.id" 
          class="review-card"
        >
          <div class="review-header">
            <div class="client-info">
              <div class="client-avatar">{{ getClientInitial(review.clientName) }}</div>
              <div class="client-details">
                <h5 class="client-name">{{ review.clientName }}</h5>
                <div class="review-rating">
                  <span 
                    v-for="star in 5" 
                    :key="star" 
                    class="star"
                    :class="{ 'filled': star <= Number(review.rating) }"
                  >
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" 
                            :fill="star <= Number(review.rating) ? '#ffc107' : 'none'"
                            stroke="currentColor" 
                            stroke-width="2" 
                            stroke-linecap="round" 
                            stroke-linejoin="round"/>
                    </svg>
                  </span>
                </div>
              </div>
            </div>
            <div class="review-date">
              {{ formatDate(review.createdAt) }}
            </div>
          </div>
          
          <div v-if="review.review" class="review-content">
            <p class="review-text">{{ review.review }}</p>
          </div>
        </div>
      </div>

      <!-- Pagination Controls -->
      <div v-if="pagination && pagination.totalPages > 1" class="pagination-controls">
        <button 
          @click="$emit('load-page', pagination.currentPage - 1)"
          :disabled="pagination.currentPage === 0 || loadingMore"
          class="pagination-button"
          type="button"
        >
          <div v-if="loadingMore" class="loading-spinner small"></div>
          <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ loadingMore ? 'Loading...' : 'Previous' }}
        </button>

        <div class="pagination-info">
          <span class="page-numbers">
            Page {{ pagination.currentPage + 1 }} of {{ pagination.totalPages }}
          </span>
        </div>

        <button 
          @click="$emit('load-page', pagination.currentPage + 1)"
          :disabled="pagination.currentPage >= pagination.totalPages - 1 || loadingMore"
          class="pagination-button"
          type="button"
        >
          <div v-if="loadingMore" class="loading-spinner small"></div>
          <span>{{ loadingMore ? 'Loading...' : 'Next' }}</span>
          <svg v-if="!loadingMore" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <!-- Load More Button -->
      <div v-if="pagination && pagination.hasMore" class="load-more-section">
        <button 
          @click="$emit('load-more')"
          :disabled="loadingMore"
          class="load-more-button"
          type="button"
        >
          <div v-if="loadingMore" class="loading-spinner small"></div>
          <span>{{ loadingMore ? 'Loading...' : 'Load More Reviews' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LawyerReviewsSection',
  props: {
    reviews: {
      type: Array,
      default: () => []
    },
    averageRating: {
      type: Number,
      default: 0
    },
    loading: {
      type: Boolean,
      default: false
    },
    loadingMore: {
      type: Boolean,
      default: false
    },
    error: {
      type: String,
      default: null
    },
    pagination: {
      type: Object,
      default: () => ({
        currentPage: 0,
        totalPages: 0,
        hasMore: false
      })
    }
  },
  emits: ['retry', 'load-page', 'load-more'],
  methods: {
    getClientInitial(clientName) {
      if (!clientName) return 'C'
      return clientName.charAt(0).toUpperCase()
    },

    formatDate(dateString) {
      if (!dateString) return ''
      
      try {
        const date = new Date(dateString)
        const now = new Date()
        const diffTime = Math.abs(now - date)
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
        
        if (diffDays === 1) {
          return 'Yesterday'
        } else if (diffDays < 7) {
          return `${diffDays} days ago`
        } else if (diffDays < 30) {
          const weeks = Math.floor(diffDays / 7)
          return weeks === 1 ? '1 week ago' : `${weeks} weeks ago`
        } else if (diffDays < 365) {
          const months = Math.floor(diffDays / 30)
          return months === 1 ? '1 month ago' : `${months} months ago`
        } else {
          return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
          })
        }
      } catch (error) {
        console.error('Error formatting date:', error)
        return dateString
      }
    }
  }
}
</script>

<style scoped>
.lawyer-reviews-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.section-header {
  margin-bottom: 24px;
  text-align: center;
}

.section-header h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 600;
}

.average-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.star-rating {
  display: flex;
  gap: 4px;
}

.star {
  width: 24px;
  height: 24px;
  color: #e9ecef;
  transition: color 0.2s ease;
}

.star.filled {
  color: #ffc107;
}

.star.filled svg path {
  fill: #ffc107;
}

.star svg {
  width: 100%;
  height: 100%;
}

.rating-text {
  color: #666;
  font-size: 0.95rem;
  font-weight: 500;
}

/* Loading State */
.loading-state {
  padding: 20px;
}

.reviews-skeleton {
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.skeleton-stars {
  width: 120px;
  height: 24px;
  background: #f3f4f6;
  border-radius: 4px;
}

.skeleton-rating-text {
  width: 80px;
  height: 16px;
  background: #f3f4f6;
  border-radius: 4px;
}

.skeleton-reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skeleton-review {
  background: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.skeleton-review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.skeleton-client-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f3f4f6;
  flex-shrink: 0;
}

.skeleton-client-info {
  flex: 1;
  margin-left: 12px;
}

.skeleton-line {
  height: 16px;
  background: #f3f4f6;
  border-radius: 4px;
  margin-bottom: 8px;
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
  background: #f3f4f6;
  border-radius: 4px;
}

.skeleton-review-content .skeleton-line {
  width: 100%;
  margin-bottom: 8px;
}

.skeleton-review-content .skeleton-line:last-child {
  width: 75%;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
  margin-bottom: 0;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.error-icon {
  width: 48px;
  height: 48px;
  color: #dc3545;
  margin-bottom: 16px;
}

.error-state h4 {
  margin: 0 0 8px 0;
  color: #dc3545;
  font-size: 1.125rem;
  font-weight: 600;
}

.error-state p {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 0.95rem;
}

.retry-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background: #0056b3;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: #6c757d;
  margin-bottom: 16px;
}

.empty-state h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 1.125rem;
  font-weight: 600;
}

.empty-state p {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
  font-style: italic;
}

/* Reviews Content */
.reviews-content {
  width: 100%;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.review-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
}

.review-card:hover {
  background: #f1f3f4;
  border-color: #007bff;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.client-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.client-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #007bff, #6610f2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  font-weight: 600;
  flex-shrink: 0;
}

.client-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.client-name {
  margin: 0;
  color: #333;
  font-size: 1rem;
  font-weight: 600;
}

.review-rating {
  display: flex;
  gap: 2px;
}

.review-rating .star {
  width: 16px;
  height: 16px;
}

.review-date {
  color: #666;
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
}

.review-content {
  margin-top: 12px;
}

.review-text {
  margin: 0;
  color: #333;
  line-height: 1.6;
  font-size: 0.95rem;
}

/* Pagination Controls */
.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #e9ecef;
  margin-top: 24px;
}

.pagination-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pagination-button:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
}

.pagination-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
}

.pagination-button svg {
  width: 16px;
  height: 16px;
}

.pagination-info {
  display: flex;
  align-items: center;
}

.page-numbers {
  color: #666;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Load More Section */
.load-more-section {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
  margin-top: 24px;
}

.load-more-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #28a745;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.load-more-button:hover:not(:disabled) {
  background: #218838;
  transform: translateY(-1px);
}

.load-more-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
}

/* Responsive Design */
@media (max-width: 768px) {
  .lawyer-reviews-section {
    padding: 16px;
  }
  
  .section-header {
    margin-bottom: 20px;
  }
  
  .section-header h3 {
    font-size: 1.25rem;
  }
  
  .average-rating {
    flex-direction: column;
    gap: 8px;
  }
  
  .reviews-list {
    gap: 16px;
    margin-bottom: 20px;
  }
  
  .review-card {
    padding: 16px;
  }
  
  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .client-info {
    width: 100%;
  }
  
  .review-date {
    align-self: flex-end;
  }
  
  .pagination-controls {
    flex-direction: column;
    gap: 16px;
  }
  
  .pagination-button {
    width: 100%;
    justify-content: center;
  }
  
  .loading-state,
  .error-state,
  .empty-state {
    padding: 40px 16px;
  }
}

@media (max-width: 480px) {
  .lawyer-reviews-section {
    padding: 12px;
  }
  
  .section-header h3 {
    font-size: 1.125rem;
  }
  
  .star {
    width: 20px;
    height: 20px;
  }
  
  .rating-text {
    font-size: 0.875rem;
  }
  
  .review-card {
    padding: 12px;
  }
  
  .client-avatar {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .client-name {
    font-size: 0.95rem;
  }
  
  .review-rating .star {
    width: 14px;
    height: 14px;
  }
  
  .review-date {
    font-size: 0.8125rem;
  }
  
  .review-text {
    font-size: 0.875rem;
  }
  
  .pagination-button,
  .load-more-button {
    font-size: 0.875rem;
    padding: 10px 16px;
  }

  /* Skeleton responsive adjustments */
  .skeleton-review {
    padding: 12px;
  }
  
  .skeleton-client-avatar {
    width: 40px;
    height: 40px;
  }
  
  .skeleton-review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .skeleton-client-name {
    width: 100px;
  }
  
  .skeleton-review-rating {
    width: 60px;
  }
  
  .skeleton-review-date {
    width: 60px;
  }
}
</style>