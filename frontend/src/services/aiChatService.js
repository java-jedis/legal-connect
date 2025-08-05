import axios from 'axios'
import { API_CONFIG } from '../config/api'

// AI Chat service for communicating with the RAG backend
class AIChatService {
  constructor() {
    this.baseURL = API_CONFIG.AI_CHAT_BASE_URL
    this.apiClient = axios.create({
      baseURL: this.baseURL,
      timeout: 30000, // 30 seconds timeout for AI responses
      headers: {
        'Content-Type': 'application/json'
      }
    })

    // Add request interceptor for authentication if needed
    this.apiClient.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('auth_token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Add response interceptor for error handling
    this.apiClient.interceptors.response.use(
      (response) => response,
      (error) => {
        console.error('AI Chat API Error:', error)
        return Promise.reject(error)
      }
    )
  }

  /**
   * Send a chat message to the AI RAG system
   * @param {string} message - User's message
   * @param {string} sessionId - Optional session ID for conversation continuity
   * @param {string} userId - Optional user ID
   * @param {number} contextLimit - Number of context documents to retrieve
   * @returns {Promise<Object>} AI response with sources and metadata
   */
  async sendMessage(message, sessionId = null, userId = null, contextLimit = 5) {
    try {
      const response = await this.apiClient.post('/chat', {
        message,
        session_id: sessionId,
        user_id: userId,
        context_limit: contextLimit
      })
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  /**
   * Search legal documents without generating a chat response
   * @param {string} query - Search query
   * @param {number} topK - Number of results to return
   * @param {number} threshold - Similarity threshold
   * @returns {Promise<Object>} Search results
   */
  async searchDocuments(query, topK = 5, threshold = 0.7) {
    try {
      const response = await this.apiClient.post('/search', {
        query,
        top_k: topK,
        threshold
      })
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  /**
   * Get chat history for a session
   * @param {string} sessionId - Session ID
   * @returns {Promise<Object>} Chat history
   */
  async getChatHistory(sessionId) {
    try {
      const response = await this.apiClient.get(`/sessions/${sessionId}/history`)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  /**
   * Delete a chat session
   * @param {string} sessionId - Session ID to delete
   * @returns {Promise<Object>} Deletion confirmation
   */
  async deleteSession(sessionId) {
    try {
      const response = await this.apiClient.delete(`/sessions/${sessionId}`)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  /**
   * Check AI chat service health
   * @returns {Promise<Object>} Health status
   */
  async checkHealth() {
    try {
      const response = await this.apiClient.get('/health')
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  /**
   * Generate a unique session ID
   * @returns {string} Unique session ID
   */
  generateSessionId() {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * Handle API errors consistently
   * @param {Error} error - API error
   * @returns {Error} Formatted error
   */
  handleError(error) {
    if (error.response) {
      // Server responded with error status
      const message = error.response.data?.detail || error.response.data?.message || 'An error occurred'
      return new Error(`AI Chat Service Error: ${message}`)
    } else if (error.request) {
      // Request made but no response received
      return new Error('AI Chat Service is currently unavailable. Please try again later.')
    } else {
      // Something else happened
      return new Error(`Request Error: ${error.message}`)
    }
  }
}

// Create and export a singleton instance
export const aiChatService = new AIChatService()
export default aiChatService
