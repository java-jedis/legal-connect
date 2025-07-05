<template>
  <div class="how-it-works-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <h1 class="hero-title">How LegalConnect Works</h1>
        <p class="hero-subtitle">
          Your journey from legal question to expert solution in simple steps
        </p>
      </div>
    </section>

    <!-- Process Steps -->
    <section class="process-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Simple 4-Step Process</h2>
          <p class="section-subtitle">Get the legal help you need in minutes, not days</p>
        </div>

        <div class="process-steps">
          <div v-for="(step, index) in processSteps" :key="step.id" class="process-step">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <div class="step-icon">
                <component :is="step.icon" />
              </div>
              <h3 class="step-title">{{ step.title }}</h3>
              <p class="step-description">{{ step.description }}</p>
              <ul class="step-features">
                <li v-for="feature in step.features" :key="feature">{{ feature }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- AI vs Human -->
    <section class="comparison-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">AI + Human Expertise</h2>
          <p class="section-subtitle">The perfect combination for comprehensive legal support</p>
        </div>

        <div class="comparison-grid grid grid-2">
          <div class="comparison-card ai-card">
            <div class="comparison-header">
              <div class="comparison-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M12 2L2 7L12 12L22 7L12 2Z"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M2 17L12 22L22 17"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M2 12L12 17L22 12"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <h3>AI Legal Assistant</h3>
            </div>
            <ul class="comparison-features">
              <li>Instant 24/7 responses</li>
              <li>Basic legal information</li>
              <li>Document analysis</li>
              <li>Initial case assessment</li>
              <li>Free consultations</li>
            </ul>
          </div>

          <div class="comparison-card human-card">
            <div class="comparison-header">
              <div class="comparison-icon">
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
              <h3>Expert Lawyers</h3>
            </div>
            <ul class="comparison-features">
              <li>Complex legal strategy</li>
              <li>Court representation</li>
              <li>Document drafting</li>
              <li>Negotiation support</li>
              <li>Ongoing case management</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Deep Dive -->
    <section class="features-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Platform Features</h2>
          <p class="section-subtitle">Everything you need for your legal journey</p>
        </div>

        <div class="features-grid grid grid-3">
          <div v-for="feature in features" :key="feature.id" class="feature-card">
            <div class="feature-icon">
              <component :is="feature.icon" />
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- FAQ Section -->
    <section class="faq-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Frequently Asked Questions</h2>
          <p class="section-subtitle">Everything you need to know about using LegalConnect</p>
        </div>

        <div class="faq-list">
          <div v-for="faq in faqs" :key="faq.id" class="faq-item">
            <button class="faq-question" @click="toggleFaq(faq.id)">
              {{ faq.question }}
              <svg
                class="faq-icon"
                :class="{ rotated: faq.isOpen }"
                viewBox="0 0 24 24"
                fill="none"
              >
                <path
                  d="M6 9L12 15L18 9"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
            <div class="faq-answer" :class="{ open: faq.isOpen }">
              <p>{{ faq.answer }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">Ready to Get Started?</h2>
          <p class="cta-description">
            Join thousands of users who have already found the legal help they need
          </p>
          <div class="cta-buttons">
            <button class="btn btn-primary">Start Free Consultation</button>
            <button class="btn btn-secondary">Learn More</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { h, ref } from 'vue'

// Icon components
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

const MatchIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M20 7L10 17L5 12',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const ConnectIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M16 21V19A4 4 0 0 0 12 15H8A4 4 0 0 0 4 19V21',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('circle', {
        cx: '12',
        cy: '7',
        r: '4',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
    ],
  )

const ResolveIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M9 12L11 14L15 10',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
    ],
  )

const SecurityIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M12 22S20 18 20 12V5L12 2L4 5V12C4 18 12 22 12 22Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const DocumentIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8L14 2Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('polyline', {
        points: '14,2 14,8 20,8',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const CalendarIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('rect', {
        x: '3',
        y: '4',
        width: '18',
        height: '18',
        rx: '2',
        ry: '2',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
      h('line', {
        x1: '16',
        y1: '2',
        x2: '16',
        y2: '6',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
      h('line', {
        x1: '8',
        y1: '2',
        x2: '8',
        y2: '6',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
      h('line', {
        x1: '3',
        y1: '10',
        x2: '21',
        y2: '10',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
    ],
  )

const processSteps = [
  {
    id: 1,
    title: 'Ask Your Question',
    description: 'Start by describing your legal situation to our AI assistant.',
    icon: ChatIcon,
    features: [
      'Natural language input',
      '24/7 availability',
      'Instant responses',
      'Confidential conversations',
    ],
  },
  {
    id: 2,
    title: 'Get AI Analysis',
    description: 'Our AI analyzes your case and provides preliminary guidance.',
    icon: MatchIcon,
    features: ['Case assessment', 'Legal information', 'Risk evaluation', 'Next steps guidance'],
  },
  {
    id: 3,
    title: 'Connect with Lawyers',
    description: 'Get matched with verified lawyers who specialize in your case type.',
    icon: ConnectIcon,
    features: [
      'Expert matching',
      'Verified professionals',
      'Specialized expertise',
      'Client reviews',
    ],
  },
  {
    id: 4,
    title: 'Resolve Your Case',
    description: 'Work with your lawyer to achieve the best possible outcome.',
    icon: ResolveIcon,
    features: ['Ongoing support', 'Case management', 'Document preparation', 'Resolution tracking'],
  },
]

const features = [
  {
    id: 1,
    title: 'Secure Communication',
    description: 'End-to-end encrypted messaging and video calls with your lawyer.',
    icon: SecurityIcon,
  },
  {
    id: 2,
    title: 'Document Management',
    description: 'Upload, organize, and securely store all your legal documents.',
    icon: DocumentIcon,
  },
  {
    id: 3,
    title: 'Smart Scheduling',
    description: 'Book appointments and manage your legal calendar seamlessly.',
    icon: CalendarIcon,
  },
]

const faqs = ref([
  {
    id: 1,
    question: 'How much does LegalConnect cost?',
    answer:
      'Our AI assistant is free to use. Lawyer consultations start at $29 per session, with various pricing plans available for ongoing legal support.',
    isOpen: false,
  },
  {
    id: 2,
    question: 'Are the lawyers on LegalConnect verified?',
    answer:
      'Yes, all lawyers on our platform are thoroughly vetted, licensed, and verified. We check credentials, experience, and client reviews before allowing them to join.',
    isOpen: false,
  },
  {
    id: 3,
    question: 'Is my information secure?',
    answer:
      'Absolutely. We use enterprise-grade encryption and follow strict privacy protocols. Your conversations and documents are protected by the same security standards used by major financial institutions.',
    isOpen: false,
  },
  {
    id: 4,
    question: 'Can I use LegalConnect for any type of legal issue?',
    answer:
      "We cover most major practice areas including family law, business law, criminal defense, property law, and more. If we can't help directly, we'll guide you to the right resources.",
    isOpen: false,
  },
  {
    id: 5,
    question: 'How quickly can I get help?',
    answer:
      'Our AI assistant responds instantly, 24/7. For lawyer consultations, you can typically connect with a qualified attorney within 24 hours, often much sooner.',
    isOpen: false,
  },
])

const toggleFaq = (id) => {
  const faq = faqs.value.find((f) => f.id === id)
  if (faq) {
    faq.isOpen = !faq.isOpen
  }
}
</script>

<style scoped>
.how-it-works-page {
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

.process-section {
  background: var(--color-background);
}

.process-steps {
  max-width: 1000px;
  margin: 0 auto;
}

.process-step {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 2rem;
  margin-bottom: 4rem;
  align-items: start;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  font-size: 1.5rem;
  font-weight: 700;
  flex-shrink: 0;
}

.step-content {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
}

.step-icon {
  width: 48px;
  height: 48px;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  margin-bottom: 1rem;
}

.step-icon svg {
  width: 24px;
  height: 24px;
}

.step-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.step-description {
  color: var(--color-text-muted);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.step-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.step-features li {
  padding: 0.5rem 0;
  color: var(--color-text);
  position: relative;
  padding-left: 1.5rem;
}

.step-features li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

.comparison-section {
  background: var(--color-background-soft);
}

.comparison-card {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-normal);
}

.comparison-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.comparison-header {
  margin-bottom: 2rem;
}

.comparison-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
}

.comparison-icon svg {
  width: 32px;
  height: 32px;
}

.comparison-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

.comparison-features {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: left;
}

.comparison-features li {
  padding: 0.75rem 0;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
  position: relative;
  padding-left: 1.5rem;
}

.comparison-features li:last-child {
  border-bottom: none;
}

.comparison-features li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

.features-section {
  background: var(--color-background);
}

.feature-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-normal);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.feature-icon {
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

.feature-icon svg {
  width: 32px;
  height: 32px;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.feature-description {
  color: var(--color-text-muted);
  line-height: 1.6;
}

.faq-section {
  background: var(--color-background-soft);
}

.faq-list {
  max-width: 800px;
  margin: 0 auto;
}

.faq-item {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  margin-bottom: 1rem;
  overflow: hidden;
}

.faq-question {
  width: 100%;
  padding: 1.5rem;
  background: none;
  border: none;
  text-align: left;
  font-size: 1.125rem;
  font-weight: 500;
  color: var(--color-heading);
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color var(--transition-fast);
}

.faq-question:hover {
  background: var(--color-background-soft);
}

.faq-icon {
  width: 20px;
  height: 20px;
  transition: transform var(--transition-fast);
}

.faq-icon.rotated {
  transform: rotate(180deg);
}

.faq-answer {
  max-height: 0;
  overflow: hidden;
  transition: max-height var(--transition-normal);
}

.faq-answer.open {
  max-height: 200px;
}

.faq-answer p {
  padding: 0 1.5rem 1.5rem;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin: 0;
}

.cta-section {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  text-align: center;
}

.cta-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: var(--color-background);
}

.cta-description {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .process-step {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .step-number {
    margin: 0 auto 1rem;
  }

  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }

  .cta-title {
    font-size: 2rem;
  }
}
</style>
