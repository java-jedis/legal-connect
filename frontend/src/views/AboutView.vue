<template>
  <div class="about-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <h1 class="hero-title">About LegalConnect</h1>
        <p class="hero-subtitle">
          Revolutionizing legal services through AI innovation and human expertise
        </p>
      </div>
    </section>

    <!-- Mission Section -->
    <section class="mission-section section">
      <div class="container">
        <div class="mission-content">
          <div class="mission-text">
            <h2 class="section-title">Our Mission</h2>
            <p class="mission-description">
              At LegalConnect, we believe that access to quality legal assistance should be available to
              everyone, regardless of their background or financial situation. Our mission is to
              democratize legal services by combining cutting-edge artificial intelligence with the
              expertise of verified legal professionals.
            </p>
            <p class="mission-description">
              We're building a future where legal guidance is instant, affordable, and accessible
              24/7, while maintaining the human touch and expertise that only qualified lawyers can
              provide.
            </p>
          </div>
          <div class="mission-visual">
            <div class="mission-card">
              <div class="mission-icon">
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
              <h3>AI + Human Expertise</h3>
              <p>Combining the best of both worlds</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Story Section -->
    <section class="story-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Our Story</h2>
          <p class="section-subtitle">From a simple idea to a revolutionary legal platform</p>
        </div>

        <div class="story-timeline">
          <div class="timeline-item">
            <div class="timeline-marker">2020</div>
            <div class="timeline-content">
              <h3>The Beginning</h3>
              <p>
                Founded by a team of legal professionals and AI researchers who recognized the need
                for more accessible legal services.
              </p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">2021</div>
            <div class="timeline-content">
              <h3>AI Development</h3>
              <p>
                Developed our proprietary AI legal assistant, trained on millions of legal documents
                and cases.
              </p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">2022</div>
            <div class="timeline-content">
              <h3>Platform Launch</h3>
              <p>
                Launched our beta platform with 100+ verified lawyers and thousands of satisfied
                users.
              </p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">2023</div>
            <div class="timeline-content">
              <h3>Expansion</h3>
              <p>
                Expanded to cover all major practice areas with 500+ expert lawyers and advanced AI
                capabilities.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Values Section -->
    <section class="values-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Our Values</h2>
          <p class="section-subtitle">The principles that guide everything we do</p>
        </div>

        <div class="values-grid grid grid-3">
          <div v-for="value in values" :key="value.id" class="value-card">
            <div class="value-icon">
              <component :is="value.icon" />
            </div>
            <h3 class="value-title">{{ value.title }}</h3>
            <p class="value-description">{{ value.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Team Section -->
    <section class="team-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Leadership Team</h2>
          <p class="section-subtitle">Meet the visionaries behind LegalConnect</p>
        </div>

        <div class="team-grid grid grid-3">
          <div v-for="member in team" :key="member.id" class="team-card">
            <div class="member-avatar">
              <div class="avatar-placeholder">{{ member.initials }}</div>
            </div>
            <h3 class="member-name">{{ member.name }}</h3>
            <p class="member-title">{{ member.title }}</p>
            <p class="member-bio">{{ member.bio }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section section">
      <div class="container">
        <div class="stats-grid grid grid-4">
          <div v-for="stat in stats" :key="stat.id" class="stat-card">
            <div class="stat-number">{{ stat.number }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { h } from 'vue'

// Icon components
const InnovationIcon = () =>
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

const TrustIcon = () =>
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

const AccessIcon = () =>
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

const ExcellenceIcon = () =>
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

const values = [
  {
    id: 1,
    title: 'Innovation',
    description: "We continuously push the boundaries of what's possible in legal technology.",
    icon: InnovationIcon,
  },
  {
    id: 2,
    title: 'Trust & Security',
    description: 'Your privacy and data security are our top priorities.',
    icon: TrustIcon,
  },
  {
    id: 3,
    title: 'Accessibility',
    description: 'Making quality legal services available to everyone, everywhere.',
    icon: AccessIcon,
  },
  {
    id: 4,
    title: 'Excellence',
    description: 'We maintain the highest standards in both AI and human legal services.',
    icon: ExcellenceIcon,
  },
]

const team = [
  {
    id: 1,
    name: 'Dr. Sarah Chen',
    title: 'CEO & Co-Founder',
    initials: 'SC',
    bio: 'Former partner at a top-tier law firm with 15+ years of experience in corporate law and AI applications.',
  },
  {
    id: 2,
    name: 'Michael Rodriguez',
    title: 'CTO & Co-Founder',
    initials: 'MR',
    bio: 'AI researcher with expertise in natural language processing and legal technology systems.',
  },
  {
    id: 3,
    name: 'Jennifer Park',
    title: 'Head of Legal Operations',
    initials: 'JP',
    bio: 'Experienced legal professional specializing in legal process optimization and quality assurance.',
  },
]

const stats = [
  {
    id: 1,
    number: '500+',
    label: 'Expert Lawyers',
  },
  {
    id: 2,
    number: '10K+',
    label: 'Cases Resolved',
  },
  {
    id: 3,
    number: '98%',
    label: 'Client Satisfaction',
  },
  {
    id: 4,
    number: '24/7',
    label: 'AI Support',
  },
]
</script>

<style scoped>
.about-page {
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

.mission-section {
  background: var(--color-background);
}

.mission-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}

.mission-description {
  font-size: 1.125rem;
  color: var(--color-text);
  line-height: 1.7;
  margin-bottom: 1.5rem;
}

.mission-visual {
  display: flex;
  justify-content: center;
}

.mission-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  max-width: 300px;
}

.mission-icon {
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

.mission-icon svg {
  width: 32px;
  height: 32px;
}

.story-section {
  background: var(--color-background-soft);
}

.story-timeline {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.story-timeline::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--color-border);
  transform: translateX(-50%);
}

.timeline-item {
  display: flex;
  align-items: center;
  margin-bottom: 3rem;
  position: relative;
}

.timeline-item:nth-child(even) {
  flex-direction: row-reverse;
}

.timeline-marker {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  font-weight: 600;
  font-size: 1.125rem;
  z-index: 1;
  flex-shrink: 0;
}

.timeline-content {
  background: var(--color-background);
  padding: 1.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  margin: 0 2rem;
  flex: 1;
  max-width: 300px;
}

.timeline-content h3 {
  color: var(--color-heading);
  margin-bottom: 0.5rem;
  font-size: 1.125rem;
}

.timeline-content p {
  color: var(--color-text-muted);
  line-height: 1.6;
}

.values-section {
  background: var(--color-background);
}

.value-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-normal);
}

.value-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.value-icon {
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

.value-icon svg {
  width: 32px;
  height: 32px;
}

.value-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.value-description {
  color: var(--color-text-muted);
  line-height: 1.6;
}

.team-section {
  background: var(--color-background-soft);
}

.team-card {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-normal);
}

.team-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.member-avatar {
  margin-bottom: 1.5rem;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 auto;
}

.member-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.member-title {
  color: var(--color-primary);
  font-weight: 500;
  margin-bottom: 1rem;
}

.member-bio {
  color: var(--color-text-muted);
  line-height: 1.6;
  font-size: 0.875rem;
}

.stats-section {
  background: var(--color-background);
}

.stat-card {
  text-align: center;
  padding: 2rem;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.stat-label {
  color: var(--color-text-muted);
  font-size: 1.125rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .mission-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .story-timeline::before {
    left: 30px;
  }

  .timeline-item {
    flex-direction: row !important;
  }

  .timeline-content {
    margin-left: 2rem;
    margin-right: 0;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
