<template>
  <section class="features section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Why Choose LegalAI?</h2>
        <p class="section-subtitle">
          Experience the future of legal services with our comprehensive platform designed to make
          legal assistance accessible, efficient, and intelligent.
        </p>
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
</template>

<script setup>
import { h } from 'vue'

// Icon components
const AIIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M12 2L2 7L12 12L22 7L12 2Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M2 17L12 22L22 17',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('path', {
        d: 'M2 12L12 17L22 12',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const LawyerIcon = () =>
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

const features = [
  {
    id: 1,
    title: 'AI-Powered Legal Advice',
    description:
      'Get instant legal guidance from our advanced AI system that understands complex legal scenarios and provides accurate preliminary advice.',
    icon: AIIcon,
  },
  {
    id: 2,
    title: 'Expert Lawyer Network',
    description:
      'Connect with verified legal professionals across all practice areas, vetted for expertise and client satisfaction.',
    icon: LawyerIcon,
  },
  {
    id: 3,
    title: 'Real-Time Chat Support',
    description:
      'Communicate seamlessly with lawyers through our secure messaging system with instant notifications and file sharing.',
    icon: ChatIcon,
  },
  {
    id: 4,
    title: 'Secure & Confidential',
    description:
      'Your data is protected with enterprise-grade encryption and strict privacy controls ensuring complete confidentiality.',
    icon: SecurityIcon,
  },
  {
    id: 5,
    title: 'Smart Scheduling',
    description:
      'Book appointments with ease using our intelligent calendar system that syncs with your preferred calendar applications.',
    icon: CalendarIcon,
  },
  {
    id: 6,
    title: 'Document Management',
    description:
      'Upload, organize, and securely store all your legal documents with version control and easy access from anywhere.',
    icon: DocumentIcon,
  },
]
</script>

<style scoped>
.features {
  background: var(--color-background-soft);
  transition: background-color var(--transition-normal);
}

.section-header {
  text-align: center;
  margin-bottom: 4rem;
}

.feature-card {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition-normal);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  transform: scaleX(0);
  transition: transform var(--transition-normal);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.feature-card:hover::before {
  transform: scaleX(1);
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
  transition: all var(--transition-normal);
}

.feature-card:hover .feature-icon {
  transform: scale(1.1) rotate(5deg);
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
  transition: color var(--transition-normal);
}

.feature-description {
  color: var(--color-text-muted);
  line-height: 1.6;
  font-size: 0.875rem;
  transition: color var(--transition-normal);
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .feature-card {
    padding: 1.5rem;
  }

  .feature-icon {
    width: 56px;
    height: 56px;
    margin-bottom: 1rem;
  }

  .feature-icon svg {
    width: 28px;
    height: 28px;
  }
}

@media (max-width: 1024px) and (min-width: 769px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
  }
}
</style>
