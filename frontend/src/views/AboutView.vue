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

        <!-- Team Section -->
    <section class="team-section section">
      <div class="container">
        <div class="section-header">
          <!-- <h2 class="section-title">Leadership Team</h2> -->
          <p class="section-subtitle">Meet the visionaries behind LegalConnect</p>
        </div>

        <div class="team-grid grid grid-2">
          <div v-for="member in team" :key="member.id" class="team-card">
            <div class="member-avatar">
              <img 
                v-if="member.photo" 
                :src="member.photo" 
                :alt="member.name" 
                class="avatar-image"
              />
              <div v-else class="avatar-placeholder">{{ member.initials }}</div>
            </div>
            <h3 class="member-name">{{ member.name }}</h3>
            <p class="member-title">{{ member.title }}</p>
            <p class="member-bio">{{ member.bio }}</p>
            
            <!-- Social Links -->
            <div class="member-social-links">
              <a 
                v-if="member.linkedin" 
                :href="member.linkedin" 
                target="_blank" 
                rel="noopener noreferrer"
                class="social-link linkedin"
                title="LinkedIn Profile"
              >
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                </svg>
              </a>
              
              <a 
                v-if="member.github" 
                :href="member.github" 
                target="_blank" 
                rel="noopener noreferrer"
                class="social-link github"
                title="GitHub Profile"
              >
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                </svg>
              </a>
            </div>
          </div>
        </div>
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
            <div class="timeline-marker">June</div>
            <div class="timeline-content">
              <h3>The Beginning</h3>
              <p>
                Our journey began when we united as a passionate team for the Therap Java Fest, 
                driven by a shared vision to revolutionize legal services through innovative 
                technology and make quality legal assistance accessible to everyone.
              </p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">July</div>
            <div class="timeline-content">
              <h3>Backend Development</h3>
              <p>
                We focused on comprehensive system design and robust backend development using 
                Java Spring Boot, creating a scalable foundation that ensures secure, efficient, 
                and reliable legal service delivery.
              </p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">August</div>
            <div class="timeline-content">
              <h3>AI Development</h3>
              <p>
                Dedicated months to implementing advanced RAG (Retrieval-Augmented Generation) 
                systems and completing major AI development milestones, creating intelligent 
                legal assistance capabilities that understand context and provide accurate guidance.
              </p>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">September</div>
            <div class="timeline-content">
              <h3>Platform Launch</h3>
              <p>
                Successfully completed full system integration and proudly launched our beta 
                version, bringing together all components to deliver a seamless legal assistance 
                platform ready to serve users worldwide.
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

        <div class="values-grid grid grid-4">
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
    name: 'Shakil Ahmed',
    title: 'CEO & Co-Founder',
    initials: 'SK',
    photo: '/shakil.png',
    bio: 'Full stack developer with expertise in Machine Learning, Python, legal technology systems and AI applications.',
    linkedin: 'https://www.linkedin.com/in/ahmedmshakil/',
    github: 'https://github.com/ahmedmshakil'
  },
  {
    id: 2,
    name: 'Majedul Islam',
    title: 'CTO & Co-Founder',
    initials: 'MJ',
    photo: '/majed.jpeg',
    bio: 'Full stack developer with expertise in Java, Spring Boot, legal technology systems and AI applications.',
    linkedin: 'https://www.linkedin.com/in/majedul-islam-041637220/',
    github: 'https://github.com/mr-majed7'
  },
  // {
  //   id: 3,
  //   name: 'Barrister XYZ',
  //   title: 'Head of Legal Operations',
  //   initials: 'SH',
  //   bio: 'Experienced legal professional specializing in legal process optimization and quality assurance.',
  // },
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
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary), var(--color-secondary));
  transform: translateX(-50%);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.timeline-item {
  display: flex;
  align-items: center;
  margin-bottom: 3rem;
  position: relative;
}

.timeline-item:nth-child(odd) {
  flex-direction: row;
}

.timeline-item:nth-child(even) {
  flex-direction: row-reverse;
}

.timeline-marker {
  width: 80px;
  height: 40px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
  font-weight: 600;
  font-size: 0.9rem;
  z-index: 2;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 3px solid var(--color-background);
}

.timeline-content {
  background: var(--color-background);
  padding: 1.5rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  margin: 0 2rem;
  flex: 1;
  max-width: 350px;
  min-height: 120px;
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

.team-grid {
  justify-content: center;
  max-width: 800px;
  margin: 0 auto;
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

.avatar-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto;
  display: block;
  border: 3px solid var(--color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all var(--transition-normal);
}

.avatar-image:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
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
  margin-bottom: 1rem;
}

.member-social-links {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.social-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: all var(--transition-fast);
  text-decoration: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.social-link svg {
  width: 20px;
  height: 20px;
}

.social-link.linkedin {
  background: #0077b5;
  color: white;
}

.social-link.linkedin:hover {
  background: #005582;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 119, 181, 0.3);
}

.social-link.github {
  background: #333;
  color: white;
}

.social-link.github:hover {
  background: #24292e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(51, 51, 51, 0.3);
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
