<template>
  <section class="hero">
    <div class="container">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            AI-Powered Legal Solutions
            <span class="hero-highlight">for Bangladeshi Citizens</span>
          </h1>
          <p class="hero-description">
            Connect with expert lawyers, get instant legal advice, and manage your cases with
            cutting-edge AI technology. Your legal journey starts here.
          </p>
          <div class="hero-actions">
            <router-link to="/register" class="btn btn-primary">Get Started</router-link>
            <router-link to="/services" class="btn btn-secondary">View Services</router-link>
          </div>
          <div class="hero-stats">
            <div class="stat">
              <span class="stat-number">500+</span>
              <span class="stat-label">Expert Lawyers</span>
            </div>
            <div class="stat">
              <span class="stat-number">10K+</span>
              <span class="stat-label">Cases Resolved</span>
            </div>
            <div class="stat">
              <span class="stat-number">98%</span>
              <span class="stat-label">Client Satisfaction</span>
            </div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="hero-card">
            <div class="card-header">
              <div class="avatar-group">
                <div class="avatar" v-for="i in 3" :key="i"></div>
              </div>
              <div class="status-indicator">
                <span class="status-dot"></span>
                Legal Connect Chatbot
              </div>
            </div>
            <div class="chat-messages">
              <div class="message ai-message">
                <div class="message-avatar">AI</div>
                <div class="message-content">
                  Hello! I can help you with legal questions. What would you like to know?
                </div>
              </div>
              <div class="message user-message">
                <div class="message-content">I need help with a contract dispute.</div>
                <div class="message-avatar">You</div>
              </div>
              <div class="message ai-message">
                <div class="message-avatar">AI</div>
                <div class="message-content">
                  I can help you understand your options. Let me connect you with a contract law
                  specialist.
                </div>
              </div>
            </div>
            <div class="chat-input">
              <input type="text" placeholder="Type your legal question..." />
              <button class="send-btn">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
// Component logic here
</script>

<style scoped>
.hero {
  padding: 6rem 0 4rem;
  background: linear-gradient(
    135deg,
    var(--color-background) 0%,
    var(--color-background-soft) 100%
  );
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 80%, var(--color-primary) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, var(--color-secondary) 0%, transparent 50%);
  opacity: 0.1;
  pointer-events: none;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.1;
  color: var(--color-heading);
  margin-bottom: 1.5rem;
}

.hero-highlight {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: 1.25rem;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 2rem;
  max-width: 500px;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 3rem;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  font-weight: 600;
  padding: 1rem 2rem;
  font-size: 1rem;
  border: none;
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-primary:hover::before {
  left: 100%;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  background: linear-gradient(135deg, var(--color-secondary), var(--color-primary));
}

.hero-stats {
  display: flex;
  gap: 2rem;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  text-align: center;
}

.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
}

.hero-card {
  background: var(--color-surface);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  box-shadow: var(--shadow-lg);
  max-width: 400px;
  width: 100%;
  border: 1px solid var(--color-border);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.avatar-group {
  display: flex;
  gap: -0.5rem;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border: 2px solid var(--color-background);
}

.avatar:nth-child(2) {
  margin-left: -0.5rem;
}

.avatar:nth-child(3) {
  margin-left: -0.5rem;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.chat-messages {
  margin-bottom: 1rem;
}

.message {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.message.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.message-content {
  background: var(--color-background-soft);
  padding: 0.75rem 1rem;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  line-height: 1.4;
  max-width: 80%;
}

.ai-message .message-content {
  background: var(--color-primary);
  color: var(--color-background);
}

.chat-input {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 0.5rem;
}

.chat-input input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text);
}

.chat-input input::placeholder {
  color: var(--color-text-muted);
}

.send-btn {
  width: 32px;
  height: 32px;
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

.send-btn:hover {
  transform: scale(1.1);
}

.send-btn svg {
  width: 16px;
  height: 16px;
}

@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 3rem;
    text-align: center;
  }

  .hero-title {
    font-size: 3rem;
  }

  .hero-description {
    margin: 0 auto 2rem;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-stats {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 4rem 0 2rem;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-description {
    font-size: 1.125rem;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-stats {
    gap: 1.5rem;
  }

  .stat-number {
    font-size: 1.75rem;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-stats {
    flex-direction: column;
    gap: 1rem;
  }

  .hero-card {
    padding: 1rem;
  }
}
</style>
