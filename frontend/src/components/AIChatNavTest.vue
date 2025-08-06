<template>
  <div class="ai-chat-nav">
    <h3>AI Chat Navigation Test</h3>
    
    <div class="nav-section">
      <h4>Basic Navigation:</h4>
      <button @click="navigateToNewChat" class="nav-btn">New AI Chat</button>
      <button @click="navigateToDocumentSearch" class="nav-btn">Document Search</button>
      <button @click="navigateToChatHistory" class="nav-btn">Chat History</button>
    </div>
    
    <div class="nav-section">
      <h4>Test Session URLs:</h4>
      <button @click="navigateToSampleSession" class="nav-btn">Sample Session (UUID)</button>
      <button @click="navigateToInvalidSession" class="nav-btn">Invalid Session (Test Error)</button>
    </div>
    
    <div class="nav-section">
      <h4>Current Route Info:</h4>
      <p><strong>Route Name:</strong> {{ route.name }}</p>
      <p><strong>Session ID:</strong> {{ route.params.sessionId || 'None' }}</p>
      <p><strong>Full Path:</strong> {{ route.fullPath }}</p>
    </div>
    
    <div class="nav-section">
      <h4>Generated Session ID:</h4>
      <p><strong>UUID Format:</strong> {{ sampleUUID }}</p>
      <button @click="generateNewUUID" class="nav-btn">Generate New UUID</button>
      <button @click="navigateToGeneratedSession" class="nav-btn">Navigate to Generated Session</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { aiChatService } from '../services/aiChatService'

const router = useRouter()
const route = useRoute()

const sampleUUID = ref(aiChatService.generateSessionId())

const navigateToNewChat = () => {
  router.push('/ai-chat')
}

const navigateToDocumentSearch = () => {
  router.push('/document-search')
}

const navigateToChatHistory = () => {
  router.push('/chat-history')
}

const navigateToSampleSession = () => {
  const sessionId = '4db59962-0a65-479f-ac8a-7ee5441d27ea'
  router.push(`/ai-chat/${sessionId}`)
}

const navigateToInvalidSession = () => {
  const invalidId = 'invalid-session-id'
  router.push(`/ai-chat/${invalidId}`)
}

const generateNewUUID = () => {
  sampleUUID.value = aiChatService.generateSessionId()
}

const navigateToGeneratedSession = () => {
  router.push(`/ai-chat/${sampleUUID.value}`)
}
</script>

<style scoped>
.ai-chat-nav {
  max-width: 600px;
  margin: 2rem auto;
  padding: 2rem;
  background: var(--color-background-soft);
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.nav-section {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.nav-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.nav-section h4 {
  margin-bottom: 1rem;
  color: var(--color-heading);
}

.nav-btn {
  display: inline-block;
  padding: 0.5rem 1rem;
  margin: 0.25rem;
  background: var(--vt-c-brand);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.9rem;
}

.nav-btn:hover {
  background: var(--vt-c-brand-dark);
}

.nav-section p {
  margin: 0.5rem 0;
  font-family: monospace;
  background: var(--color-background);
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid var(--color-border);
}
</style>
