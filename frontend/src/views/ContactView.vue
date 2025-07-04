<template>
  <div class="contact-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <h1 class="hero-title">Contact Us</h1>
        <p class="hero-subtitle">
          Get in touch with our team for support, partnerships, or general inquiries
        </p>
      </div>
    </section>

    <!-- Contact Form Section -->
    <section class="contact-form-section section">
      <div class="container">
        <div class="contact-content">
          <div class="contact-info">
            <h2 class="section-title">Get in Touch</h2>
            <p class="contact-description">
              Have a question or need assistance? Our team is here to help you with any legal
              inquiries, technical support, or partnership opportunities.
            </p>

            <div class="contact-methods">
              <div class="contact-method">
                <div class="method-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <polyline
                      points="22,6 12,13 2,6"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
                <div class="method-content">
                  <h3>Email Us</h3>
                  <p>support@legalai.com</p>
                  <p>partnerships@legalai.com</p>
                </div>
              </div>

              <div class="contact-method">
                <div class="method-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M22 16.92V19.92C22.0011 20.1985 21.9441 20.4742 21.8325 20.7294C21.7209 20.9845 21.5573 21.2136 21.3521 21.4019C21.1469 21.5901 20.9046 21.7335 20.6407 21.8227C20.3769 21.9119 20.0973 21.9452 19.82 21.92C16.7428 21.5856 13.787 20.5341 11.19 18.85C8.77382 17.3146 6.72533 15.2661 5.18999 12.85C3.49997 10.2412 2.44824 7.27099 2.11999 4.18C2.09494 3.90347 2.12787 3.62476 2.21649 3.36162C2.30512 3.09849 2.44756 2.85669 2.63476 2.65178C2.82196 2.44687 3.0498 2.28321 3.30379 2.17128C3.55778 2.05935 3.83233 2.00169 4.10999 2H7.10999C7.59522 1.99522 8.06574 2.16708 8.43376 2.48353C8.80178 2.79999 9.042 3.23945 9.10999 3.72C9.23662 4.68007 9.47144 5.62273 9.80999 6.53C9.94454 6.88792 9.97348 7.27675 9.89381 7.64999C9.81415 8.02323 9.62976 8.36825 9.35999 8.65L8.08999 9.92C9.51355 12.4135 11.5865 14.4865 14.08 15.91L15.35 14.64C15.6318 14.3702 15.9768 14.1858 16.35 14.1062C16.7232 14.0265 17.1121 14.0555 17.47 14.19C18.3773 14.5286 19.3199 14.7634 20.28 14.89C20.7658 14.9585 21.2094 15.2032 21.5265 15.5775C21.8437 15.9518 22.0122 16.4296 22 16.92Z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
                <div class="method-content">
                  <h3>Call Us</h3>
                  <p>+1 (555) 123-4567</p>
                  <p>Mon-Fri: 9AM-6PM EST</p>
                </div>
              </div>

              <div class="contact-method">
                <div class="method-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M21 10C21 17 12 23 12 23S3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2" />
                  </svg>
                </div>
                <div class="method-content">
                  <h3>Visit Us</h3>
                  <p>123 Legal Street</p>
                  <p>New York, NY 10001</p>
                </div>
              </div>
            </div>
          </div>

          <div class="contact-form-container">
            <form class="contact-form" @submit.prevent="submitForm">
              <h3 class="form-title">Send us a Message</h3>

              <div class="form-group">
                <label for="name">Full Name *</label>
                <input
                  type="text"
                  id="name"
                  v-model="form.name"
                  required
                  placeholder="Enter your full name"
                />
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
                <label for="subject">Subject *</label>
                <select id="subject" v-model="form.subject" required>
                  <option value="">Select a subject</option>
                  <option value="general">General Inquiry</option>
                  <option value="support">Technical Support</option>
                  <option value="partnership">Partnership</option>
                  <option value="legal">Legal Question</option>
                  <option value="feedback">Feedback</option>
                </select>
              </div>

              <div class="form-group">
                <label for="message">Message *</label>
                <textarea
                  id="message"
                  v-model="form.message"
                  required
                  rows="5"
                  placeholder="Tell us how we can help you..."
                ></textarea>
              </div>

              <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
                {{ isSubmitting ? 'Sending...' : 'Send Message' }}
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>

    <!-- Office Locations -->
    <section class="offices-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Our Offices</h2>
          <p class="section-subtitle">Visit us at any of our locations across the country</p>
        </div>

        <div class="offices-grid grid grid-3">
          <div v-for="office in offices" :key="office.id" class="office-card">
            <div class="office-header">
              <h3 class="office-name">{{ office.name }}</h3>
              <p class="office-type">{{ office.type }}</p>
            </div>
            <div class="office-details">
              <p class="office-address">{{ office.address }}</p>
              <p class="office-phone">{{ office.phone }}</p>
              <p class="office-hours">{{ office.hours }}</p>
            </div>
            <div class="office-actions">
              <button class="btn btn-secondary">Get Directions</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Support Section -->
    <section class="support-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Need Help?</h2>
          <p class="section-subtitle">Quick access to support resources and documentation</p>
        </div>

        <div class="support-grid grid grid-3">
          <div v-for="support in supportOptions" :key="support.id" class="support-card">
            <div class="support-icon">
              <component :is="support.icon" />
            </div>
            <h3 class="support-title">{{ support.title }}</h3>
            <p class="support-description">{{ support.description }}</p>
            <button class="btn btn-outline">{{ support.action }}</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { h } from 'vue'

// Icon components
const HelpIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('circle', {
        cx: '12',
        cy: '12',
        r: '10',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
      h('path', {
        d: 'M9.09 9A3 3 0 0 1 12 6A3 3 0 0 1 12 12',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M12 18V12',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const BookIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M4 19.5A2.5 2.5 0 0 1 6.5 17H20',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M6.5 2H20V22H6.5A2.5 2.5 0 0 1 4 19.5V2.5A2.5 2.5 0 0 1 6.5 2Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const ChatIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M21 15A2 2 0 0 1 19 17H7L3 21V5A2 2 0 0 1 5 3H19A2 2 0 0 1 21 5Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const form = ref({
  name: '',
  email: '',
  subject: '',
  message: '',
})

const isSubmitting = ref(false)

const offices = [
  {
    id: 1,
    name: 'New York',
    type: 'Headquarters',
    address: '123 Legal Street, New York, NY 10001',
    phone: '+1 (555) 123-4567',
    hours: 'Mon-Fri: 9AM-6PM EST',
  },
  {
    id: 2,
    name: 'San Francisco',
    type: 'West Coast Office',
    address: '456 Tech Avenue, San Francisco, CA 94105',
    phone: '+1 (555) 234-5678',
    hours: 'Mon-Fri: 9AM-6PM PST',
  },
  {
    id: 3,
    name: 'Chicago',
    type: 'Midwest Office',
    address: '789 Business Blvd, Chicago, IL 60601',
    phone: '+1 (555) 345-6789',
    hours: 'Mon-Fri: 9AM-6PM CST',
  },
]

const supportOptions = [
  {
    id: 1,
    title: 'Help Center',
    description: 'Browse our comprehensive knowledge base and find answers to common questions.',
    action: 'Browse Articles',
    icon: HelpIcon,
  },
  {
    id: 2,
    title: 'Documentation',
    description: 'Access detailed guides, tutorials, and API documentation for developers.',
    action: 'View Docs',
    icon: BookIcon,
  },
  {
    id: 3,
    title: 'Live Chat',
    description: 'Connect with our support team in real-time for immediate assistance.',
    action: 'Start Chat',
    icon: ChatIcon,
  },
]

const submitForm = async () => {
  isSubmitting.value = true

  // Simulate form submission
  await new Promise((resolve) => setTimeout(resolve, 2000))

  // Reset form
  form.value = {
    name: '',
    email: '',
    subject: '',
    message: '',
  }

  isSubmitting.value = false

  // Show success message (in a real app, you'd use a toast notification)
  alert("Thank you for your message! We'll get back to you soon.")
}
</script>

<style scoped>
.contact-page {
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(
    135deg,
    var(--color-background) 0%,
    var(--color-background-soft) 100%
  );
  padding: 6rem 0 4rem;
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

.contact-form-section {
  background: var(--color-background);
}

.contact-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: start;
}

.contact-description {
  font-size: 1.125rem;
  color: var(--color-text);
  line-height: 1.7;
  margin-bottom: 2rem;
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.contact-method {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
}

.method-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  flex-shrink: 0;
}

.method-icon svg {
  width: 24px;
  height: 24px;
}

.method-content h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.method-content p {
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}

.contact-form-container {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
}

.form-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 2rem;
  text-align: center;
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
  background: var(--color-background);
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
  min-height: 120px;
}

.offices-section {
  background: var(--color-background-soft);
}

.office-card {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition-normal);
}

.office-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.office-header {
  margin-bottom: 1.5rem;
}

.office-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.office-type {
  color: var(--color-primary);
  font-weight: 500;
  font-size: 0.875rem;
}

.office-details {
  margin-bottom: 1.5rem;
}

.office-details p {
  color: var(--color-text-muted);
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.support-section {
  background: var(--color-background);
}

.support-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-normal);
}

.support-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.support-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
}

.support-icon svg {
  width: 32px;
  height: 32px;
}

.support-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.support-description {
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--color-primary);
  color: var(--color-primary);
}

.btn-outline:hover {
  background: var(--color-primary);
  color: var(--color-background);
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .contact-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .contact-method {
    flex-direction: column;
    text-align: center;
  }

  .method-icon {
    margin: 0 auto 1rem;
  }
}
</style>
