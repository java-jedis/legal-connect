<template>
  <div class="document-search-container">
    <div class="search-header">
      <div class="header-content">
        <div class="search-info">
          <div class="search-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="search-details">
            <h1 class="search-title">Legal Document Search</h1>
            <p class="search-description">Search through Bangladesh legal documents and laws</p>
          </div>
        </div>
      </div>
    </div>

    <div class="search-content">
      <!-- Search Input Section -->
      <div class="search-input-section">
        <div class="search-input-wrapper">
          <div class="input-container">
            <svg class="search-input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
            </svg>
            <input
              ref="searchInput"
              v-model="searchQuery"
              @keydown="handleKeydown"
              placeholder="Search legal documents, acts, regulations..."
              class="search-input"
              :disabled="isLoading"
            />
            <button 
              @click="performSearch" 
              class="search-button"
              :disabled="!searchQuery.trim() || isLoading"
            >
              <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
              </svg>
              <svg v-else class="loading-spinner" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-dasharray="31.416"
                  stroke-dashoffset="31.416"
                />
              </svg>
            </button>
          </div>
          
          <!-- Search Options -->
          <div class="search-options">
            <div class="option-group">
              <label for="topK">Results:</label>
              <select id="topK" v-model="searchOptions.topK" class="option-select">
                <option value="5">5 results</option>
                <option value="10">10 results</option>
                <option value="15">15 results</option>
                <option value="20">20 results</option>
              </select>
            </div>
            
            <div class="option-group">
              <label for="threshold">Relevance:</label>
              <select id="threshold" v-model="searchOptions.threshold" class="option-select">
                <option value="0.5">Low (50%)</option>
                <option value="0.6">Medium (60%)</option>
                <option value="0.7">High (70%)</option>
                <option value="0.8">Very High (80%)</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- Search Results Section -->
      <div class="search-results-section">
        <!-- Loading State -->
        <div v-if="isLoading" class="loading-state">
          <div class="loading-content">
            <div class="loading-spinner-large"></div>
            <p>Searching legal documents...</p>
          </div>
        </div>

        <!-- No Results State -->
        <div v-else-if="hasSearched && searchResults.length === 0" class="no-results-state">
          <div class="no-results-content">
            <svg class="no-results-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>No documents found</h3>
            <p>Try different keywords or adjust your search criteria</p>
          </div>
        </div>

        <!-- Search Results -->
        <div v-else-if="searchResults.length > 0" class="results-container">
          <div class="results-header">
            <h2>Search Results</h2>
            <span class="results-count">{{ searchResults.length }} documents found</span>
          </div>
          
          <div class="results-list">
            <div 
              v-for="(result, index) in searchResults" 
              :key="index" 
              class="result-item"
            >
              <div class="result-header">
                <div class="result-title-section">
                  <svg class="document-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z"
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
                  <h3 class="result-title">{{ result.metadata?.title || 'Legal Document' }}</h3>
                </div>
                
                <div class="result-meta">
                  <span v-if="result.score" class="relevance-score">
                    {{ Math.round(result.score * 100) }}% match
                  </span>
                  <span v-if="result.metadata?.volume" class="document-volume">
                    Vol. {{ result.metadata.volume }}
                  </span>
                </div>
              </div>
              
              <div class="result-content">
                <p class="result-text">{{ result.content || result.text }}</p>
                
                <div v-if="result.metadata" class="result-metadata">
                  <div v-if="result.metadata.section" class="metadata-item">
                    <span class="metadata-label">Section:</span>
                    <span class="metadata-value">{{ result.metadata.section }}</span>
                  </div>
                  <div v-if="result.metadata.chapter" class="metadata-item">
                    <span class="metadata-label">Chapter:</span>
                    <span class="metadata-value">{{ result.metadata.chapter }}</span>
                  </div>
                  <div v-if="result.metadata.act_name" class="metadata-item">
                    <span class="metadata-label">Act:</span>
                    <span class="metadata-value">{{ result.metadata.act_name }}</span>
                  </div>
                </div>
              </div>
              
              <!-- Document Information Section -->
              <div class="document-information">
                <div class="document-info-header">
                  <h4>Document Information</h4>
                  <button 
                    @click="toggleDocumentInfo(index)"
                    class="toggle-info-btn"
                    :class="{ 'expanded': expandedDocuments.has(index) }"
                  >
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
                
                <div v-show="expandedDocuments.has(index)" class="document-info-content">
                  <div class="info-grid">
                    <div v-if="result.metadata?.chunk_index !== undefined" class="info-item">
                      <span class="info-label">Chunk Index:</span>
                      <span class="info-value">{{ result.metadata.chunk_index }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.length" class="info-item">
                      <span class="info-label">Length:</span>
                      <span class="info-value">{{ result.metadata.length }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.name" class="info-item">
                      <span class="info-label">Name:</span>
                      <span class="info-value">{{ result.metadata.name }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.filename" class="info-item">
                      <span class="info-label">Filename:</span>
                      <span class="info-value">{{ result.metadata.filename }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.url" class="info-item">
                      <span class="info-label">URL:</span>
                      <span class="info-value">
                        <a :href="result.metadata.url" target="_blank" class="info-link">
                          {{ truncateUrl(result.metadata.url) }}
                        </a>
                      </span>
                    </div>
                    
                    <div v-if="result.metadata?.pdf_path" class="info-item">
                      <span class="info-label">PDF Path:</span>
                      <span class="info-value">{{ result.metadata.pdf_path }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.extraction_date" class="info-item">
                      <span class="info-label">Extraction Date:</span>
                      <span class="info-value">{{ formatExtractionDate(result.metadata.extraction_date) }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.language" class="info-item">
                      <span class="info-label">Language:</span>
                      <span class="info-value">{{ result.metadata.language }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.ocr_language" class="info-item">
                      <span class="info-label">OCR Language:</span>
                      <span class="info-value">{{ result.metadata.ocr_language }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.total_pages" class="info-item">
                      <span class="info-label">Total Pages:</span>
                      <span class="info-value">{{ result.metadata.total_pages }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.extraction_method" class="info-item">
                      <span class="info-label">Extraction Method:</span>
                      <span class="info-value">{{ result.metadata.extraction_method }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.file_size" class="info-item">
                      <span class="info-label">File Size:</span>
                      <span class="info-value">{{ formatFileSize(result.metadata.file_size) }}</span>
                    </div>
                    
                    <div v-if="result.metadata?.page_number" class="info-item">
                      <span class="info-label">Page Number:</span>
                      <span class="info-value">{{ result.metadata.page_number }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="result-actions">
                <button 
                  @click="copyToClipboard(result.content || result.text)"
                  class="action-button"
                  title="Copy content"
                >
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
                
                <button 
                  @click="openDocumentDetail(result)"
                  class="action-button primary"
                  title="View details"
                >
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Default State -->
        <div v-else class="default-state">
          <div class="default-content">
            <svg class="default-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z"
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
              <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2"/>
              <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2"/>
              <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>Search Legal Documents</h3>
            <p>Enter keywords to search through Bangladesh legal documents, acts, and regulations</p>
            <div class="search-tips">
              <h4>Search Tips:</h4>
              <ul>
                <li>Use specific legal terms for better results</li>
                <li>Try different keywords if you don't find what you're looking for</li>
                <li>Adjust relevance settings for broader or narrower results</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error message -->
    <div v-if="errorMessage" class="error-message">
      <div class="error-content">
        <svg class="error-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
          <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
        </svg>
        <span>{{ errorMessage }}</span>
        <button @click="errorMessage = ''" class="error-close">×</button>
      </div>
    </div>

    <!-- Document Detail Modal -->
    <div v-if="selectedDocument" class="modal-overlay" @click="closeDocumentDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ selectedDocument.metadata?.title || 'Legal Document' }}</h2>
          <button @click="closeDocumentDetail" class="modal-close">×</button>
        </div>
        
        <div class="modal-body">
          <div v-if="selectedDocument.metadata" class="document-metadata">
            <div v-if="selectedDocument.metadata.act_name" class="metadata-row">
              <strong>Act:</strong> {{ selectedDocument.metadata.act_name }}
            </div>
            <div v-if="selectedDocument.metadata.volume" class="metadata-row">
              <strong>Volume:</strong> {{ selectedDocument.metadata.volume }}
            </div>
            <div v-if="selectedDocument.metadata.chapter" class="metadata-row">
              <strong>Chapter:</strong> {{ selectedDocument.metadata.chapter }}
            </div>
            <div v-if="selectedDocument.metadata.section" class="metadata-row">
              <strong>Section:</strong> {{ selectedDocument.metadata.section }}
            </div>
            <div v-if="selectedDocument.score" class="metadata-row">
              <strong>Relevance:</strong> {{ Math.round(selectedDocument.score * 100) }}%
            </div>
          </div>
          
          <div class="document-content">
            <h3>Content:</h3>
            <p>{{ selectedDocument.content || selectedDocument.text }}</p>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="copyToClipboard(selectedDocument.content || selectedDocument.text)" class="btn btn-secondary">
            Copy Content
          </button>
          <button @click="closeDocumentDetail" class="btn">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { aiChatService } from '@/services/aiChatService'
import { onMounted, ref } from 'vue'

// Reactive data
const searchQuery = ref('')
const searchResults = ref([])
const isLoading = ref(false)
const hasSearched = ref(false)
const errorMessage = ref('')
const selectedDocument = ref(null)
const searchInput = ref(null)
const expandedDocuments = ref(new Set())

// Search options
const searchOptions = ref({
  topK: 10,
  threshold: 0.7
})

// Methods
const performSearch = async () => {
  if (!searchQuery.value.trim() || isLoading.value) return
  
  isLoading.value = true
  errorMessage.value = ''
  hasSearched.value = true
  
  try {
    const response = await aiChatService.searchDocuments(
      searchQuery.value.trim(),
      searchOptions.value.topK,
      searchOptions.value.threshold
    )
    
    searchResults.value = response.results || []
  } catch (error) {
    console.error('Error searching documents:', error)
    errorMessage.value = error.message || 'Failed to search documents. Please try again.'
    searchResults.value = []
  } finally {
    isLoading.value = false
  }
}

const handleKeydown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    performSearch()
  }
}

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    // You could add a toast notification here
  } catch (error) {
    console.error('Failed to copy to clipboard:', error)
  }
}

const openDocumentDetail = (document) => {
  selectedDocument.value = document
}

const closeDocumentDetail = () => {
  selectedDocument.value = null
}

const toggleDocumentInfo = (index) => {
  if (expandedDocuments.value.has(index)) {
    expandedDocuments.value.delete(index)
  } else {
    expandedDocuments.value.add(index)
  }
}

const truncateUrl = (url) => {
  if (!url) return ''
  if (url.length <= 50) return url
  return url.substring(0, 30) + '...' + url.substring(url.length - 20)
}

const formatExtractionDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleString()
  } catch (error) {
    return dateString
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return ''
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  if (bytes === 0) return '0 Bytes'
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

// Initialize component
onMounted(() => {
  searchInput.value?.focus()
})
</script>

<style scoped>
.document-search-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--color-background);
}

.search-header {
  background: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  padding: 1.5rem;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.search-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-icon svg {
  width: 24px;
  height: 24px;
}

.search-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

.search-description {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.search-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  width: 100%;
}

.search-input-section {
  margin-bottom: 2rem;
}

.search-input-wrapper {
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.search-input-icon {
  position: absolute;
  left: 1rem;
  width: 20px;
  height: 20px;
  color: var(--color-text-muted);
  pointer-events: none;
}

.search-input {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 1.5rem;
  padding: 1rem 1rem 1rem 3rem;
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  transition: all var(--transition-fast);
}

.search-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.search-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.search-button {
  margin-left: 0.75rem;
  width: 50px;
  height: 50px;
  border: none;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-button:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
}

.search-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.search-button svg {
  width: 20px;
  height: 20px;
}

.search-options {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.option-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.option-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
}

.option-select {
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: 0.5rem;
  background: var(--color-background);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.875rem;
  outline: none;
  transition: border-color var(--transition-fast);
}

.option-select:focus {
  border-color: var(--color-primary);
}

.search-results-section {
  min-height: 400px;
}

.loading-state,
.no-results-state,
.default-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-content,
.no-results-content,
.default-content {
  text-align: center;
  max-width: 500px;
}

.loading-spinner-large {
  width: 60px;
  height: 60px;
  border: 4px solid var(--color-border);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.no-results-icon,
.default-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  color: var(--color-text-muted);
}

.loading-content h3,
.no-results-content h3,
.default-content h3 {
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
  color: var(--color-heading);
}

.loading-content p,
.no-results-content p,
.default-content p {
  margin: 0 0 1rem 0;
  color: var(--color-text);
  line-height: 1.6;
}

.search-tips {
  text-align: left;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1rem;
  margin-top: 1rem;
}

.search-tips h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  color: var(--color-heading);
}

.search-tips ul {
  margin: 0;
  padding-left: 1.25rem;
}

.search-tips li {
  margin-bottom: 0.25rem;
  color: var(--color-text);
  font-size: 0.875rem;
}

.results-container {
  animation: fadeIn 0.3s ease-out;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.results-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--color-heading);
}

.results-count {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.result-item {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  transition: all var(--transition-fast);
}

.result-item:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.result-title-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.document-icon {
  width: 20px;
  height: 20px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.result-title {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  line-height: 1.4;
}

.result-meta {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-shrink: 0;
}

.relevance-score {
  font-size: 0.75rem;
  color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 1rem;
  font-weight: 500;
}

.document-volume {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  background: var(--color-background);
  padding: 0.25rem 0.5rem;
  border-radius: 1rem;
  border: 1px solid var(--color-border);
}

.result-content {
  margin-bottom: 1rem;
}

.result-text {
  margin: 0 0 1rem 0;
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
}

.result-metadata {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.metadata-item {
  display: flex;
  gap: 0.25rem;
  font-size: 0.875rem;
}

.metadata-label {
  color: var(--color-text-muted);
  font-weight: 500;
}

.metadata-value {
  color: var(--color-text);
}

.result-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.action-button {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  background: var(--color-background);
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-muted);
}

.action-button:hover {
  background: var(--color-background-soft);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.action-button.primary {
  background: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
}

.action-button.primary:hover {
  background: var(--color-primary-dark);
}

.action-button svg {
  width: 16px;
  height: 16px;
}

/* Document Information Styles */
.document-information {
  margin-top: 1rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  overflow: hidden;
}

.document-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.document-info-header:hover {
  background: rgba(var(--color-primary-rgb), 0.05);
}

.document-info-header h4 {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-heading);
}

.toggle-info-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
  color: var(--color-text-muted);
}

.toggle-info-btn:hover {
  background: var(--color-background);
  color: var(--color-primary);
}

.toggle-info-btn svg {
  width: 16px;
  height: 16px;
  transition: transform var(--transition-fast);
}

.toggle-info-btn.expanded svg {
  transform: rotate(180deg);
}

.document-info-content {
  padding: 1rem;
  animation: slideDown 0.2s ease-out;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 0.75rem;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  font-size: 0.875rem;
}

.info-label {
  font-weight: 600;
  color: var(--color-text-muted);
  min-width: 120px;
  flex-shrink: 0;
}

.info-value {
  color: var(--color-text);
  word-break: break-word;
  flex: 1;
}

.info-link {
  color: var(--color-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.info-link:hover {
  color: var(--color-primary-dark);
  text-decoration: underline;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    max-height: 500px;
    transform: translateY(0);
  }
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.error-message {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  max-width: 90%;
  width: 400px;
}

.error-content {
  background: var(--color-danger);
  color: var(--color-background);
  padding: 1rem;
  border-radius: var(--border-radius-lg);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  box-shadow: var(--shadow-lg);
}

.error-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.error-close {
  background: none;
  border: none;
  color: var(--color-background);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0;
  margin-left: auto;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background var(--transition-fast);
}

.error-close:hover {
  background: rgba(255, 255, 255, 0.2);
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
  max-width: 800px;
  max-height: 90vh;
  width: 100%;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
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
  font-size: 1.25rem;
  color: var(--color-heading);
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-text-muted);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-body {
  padding: 1.5rem;
  max-height: 60vh;
  overflow-y: auto;
}

.document-metadata {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.metadata-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.metadata-row:last-child {
  margin-bottom: 0;
}

.metadata-row strong {
  color: var(--color-text-muted);
  min-width: 80px;
}

.document-content h3 {
  margin: 0 0 1rem 0;
  font-size: 1.125rem;
  color: var(--color-heading);
}

.document-content p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.6;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
}

/* Responsive design */
@media (max-width: 768px) {
  .search-content {
    padding: 1rem;
  }
  
  .search-options {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .result-header {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }
  
  .result-meta {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
  
  .result-metadata {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .info-item {
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .info-label {
    min-width: auto;
  }
  
  .modal-content {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }
}

@media (max-width: 480px) {
  .search-header {
    padding: 1rem;
  }
  
  .search-info {
    gap: 0.75rem;
  }
  
  .search-icon {
    width: 40px;
    height: 40px;
  }
  
  .search-title {
    font-size: 1.25rem;
  }
  
  .search-input-wrapper {
    padding: 1rem;
  }
  
  .input-container {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .search-input {
    padding-left: 1rem;
  }
  
  .search-input-icon {
    display: none;
  }
  
  .result-item {
    padding: 1rem;
  }
}
</style>
