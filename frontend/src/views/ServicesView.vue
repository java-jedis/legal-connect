<template>
  <div class="services-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <h1 class="hero-title">Our Legal Services</h1>
        <p class="hero-subtitle">
          Comprehensive legal solutions powered by AI technology and expert lawyers
        </p>
      </div>
    </section>

    <!-- Main Services -->
    <section class="services-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Legal Practice Areas</h2>
          <p class="section-subtitle">
            Expert legal assistance across all major practice areas with AI-powered guidance
          </p>
        </div>

        <div class="services-grid grid grid-3">
          <div v-for="service in services" :key="service.id" class="service-card">
            <div class="service-icon">
              <component :is="service.icon" />
            </div>
            <h3 class="service-title">{{ service.title }}</h3>
            <p class="service-description">{{ service.description }}</p>
            <ul class="service-features">
              <li v-for="feature in service.features" :key="feature">{{ feature }}</li>
            </ul>
            <div class="service-cta">
              <button class="btn btn-primary">Get Started</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- AI Services -->
    <section class="ai-services-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">AI-Powered Legal Solutions</h2>
          <p class="section-subtitle">
            Cutting-edge artificial intelligence to enhance your legal experience
          </p>
        </div>

        <div class="ai-services-grid grid grid-2">
          <div v-for="aiService in aiServices" :key="aiService.id" class="ai-service-card">
            <div class="ai-service-content">
              <h3 class="ai-service-title">{{ aiService.title }}</h3>
              <p class="ai-service-description">{{ aiService.description }}</p>
              <ul class="ai-service-benefits">
                <li v-for="benefit in aiService.benefits" :key="benefit">{{ benefit }}</li>
              </ul>
            </div>
            <div class="ai-service-visual">
              <div class="ai-visual-placeholder">
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
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Pricing -->
    <!-- <section class="pricing-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">Transparent Pricing</h2>
          <p class="section-subtitle">Choose the plan that best fits your legal needs</p>
        </div>

        <div class="pricing-grid grid grid-3">
          <div
            v-for="plan in pricingPlans"
            :key="plan.id"
            class="pricing-card"
            :class="{ featured: plan.featured }"
          >
            <div class="pricing-header">
              <h3 class="plan-name">{{ plan.name }}</h3>
              <div class="plan-price">
                <span class="currency">$</span>
                <span class="amount">{{ plan.price }}</span>
                <span class="period">/{{ plan.period }}</span>
              </div>
              <p class="plan-description">{{ plan.description }}</p>
            </div>
            <ul class="plan-features">
              <li v-for="feature in plan.features" :key="feature">{{ feature }}</li>
            </ul>
            <button class="btn" :class="plan.featured ? 'btn-primary' : 'btn-secondary'">
              {{ plan.featured ? 'Get Started' : 'Choose Plan' }}
            </button>
          </div>
        </div>
      </div>
    </section> -->
  </div>
</template>

<script setup>
import { h } from 'vue'

// Icon components
const ContractIcon = () =>
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

const FamilyIcon = () =>
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

const PropertyIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('path', {
        d: 'M3 9L12 2L21 9V20A2 2 0 0 1 19 22H5A2 2 0 0 1 3 20V9Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
      h('polyline', {
        points: '9,22 9,12 15,12 15,22',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const CriminalIcon = () =>
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

const BusinessIcon = () =>
  h(
    'svg',
    {
      viewBox: '0 0 24 24',
      fill: 'none',
      xmlns: 'http://www.w3.org/2000/svg',
    },
    [
      h('rect', {
        x: '2',
        y: '7',
        width: '20',
        height: '14',
        rx: '2',
        ry: '2',
        stroke: 'currentColor',
        'stroke-width': '2',
      }),
      h('path', {
        d: 'M16 21V5A2 2 0 0 0 14 3H10A2 2 0 0 0 8 5V21',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ],
  )

const EmploymentIcon = () =>
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

const services = [
  {
    id: 1,
    title: 'Contract Law',
    description: 'Comprehensive contract drafting, review, and negotiation services.',
    icon: ContractIcon,
    features: [
      'Contract drafting and review',
      'Negotiation support',
      'Breach of contract claims',
      'Contract dispute resolution',
    ],
  },
  {
    id: 2,
    title: 'Family Law',
    description: 'Sensitive family legal matters with compassionate expert guidance.',
    icon: FamilyIcon,
    features: [
      'Divorce and separation',
      'Child custody and support',
      'Property division',
      'Adoption and guardianship',
    ],
  },
  {
    id: 3,
    title: 'Property Law',
    description: 'Real estate transactions and property dispute resolution.',
    icon: PropertyIcon,
    features: [
      'Real estate transactions',
      'Property disputes',
      'Landlord-tenant issues',
      'Property development',
    ],
  },
  {
    id: 4,
    title: 'Criminal Defense',
    description: 'Expert criminal defense representation and legal protection.',
    icon: CriminalIcon,
    features: [
      'Criminal defense',
      'Traffic violations',
      'White-collar crimes',
      'Appeals and post-conviction',
    ],
  },
  {
    id: 5,
    title: 'Business Law',
    description: 'Comprehensive legal services for businesses of all sizes.',
    icon: BusinessIcon,
    features: [
      'Business formation',
      'Corporate governance',
      'Mergers and acquisitions',
      'Intellectual property',
    ],
  },
  {
    id: 6,
    title: 'Employment Law',
    description: 'Workplace legal issues and employment rights protection.',
    icon: EmploymentIcon,
    features: [
      'Employment contracts',
      'Workplace discrimination',
      'Wrongful termination',
      'Labor disputes',
    ],
  },
]

const aiServices = [
  {
    id: 1,
    title: 'AI Legal Assistant',
    description: 'Get instant answers to your legal questions with our advanced AI system.',
    benefits: [
      '24/7 availability',
      'Instant responses',
      'Accurate legal information',
      'Document analysis',
    ],
  },
  {
    id: 2,
    title: 'Smart Document Review',
    description: 'AI-powered document analysis and legal document generation.',
    benefits: [
      'Automated document review',
      'Legal document templates',
      'Risk assessment',
      'Compliance checking',
    ],
  },
]

const pricingPlans = [
  {
    id: 1,
    name: 'Basic',
    price: '29',
    period: 'month',
    description: 'Perfect for individuals with basic legal needs',
    featured: false,
    features: [
      'AI legal assistant access',
      'Basic document templates',
      'Email support',
      '1 consultation per month',
    ],
  },
  {
    id: 2,
    name: 'Professional',
    price: '99',
    period: 'month',
    description: 'Ideal for small businesses and families',
    featured: true,
    features: [
      'Everything in Basic',
      'Unlimited AI consultations',
      'Priority lawyer matching',
      'Document review services',
      'Phone support',
      '3 consultations per month',
    ],
  },
  {
    id: 3,
    name: 'Enterprise',
    price: '299',
    period: 'month',
    description: 'Comprehensive legal support for large organizations',
    featured: false,
    features: [
      'Everything in Professional',
      'Dedicated legal team',
      'Custom document creation',
      'Legal compliance monitoring',
      '24/7 priority support',
      'Unlimited consultations',
    ],
  },
]
</script>

<style scoped>
.services-page {
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

.services-section {
  background: var(--color-background);
}

.service-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition-normal);
  text-align: center;
}

.service-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.service-icon {
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

.service-icon svg {
  width: 32px;
  height: 32px;
}

.service-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.service-description {
  color: var(--color-text-muted);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.service-features {
  list-style: none;
  padding: 0;
  margin: 0 0 2rem 0;
  text-align: left;
}

.service-features li {
  padding: 0.5rem 0;
  color: var(--color-text);
  position: relative;
  padding-left: 1.5rem;
}

.service-features li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

.ai-services-section {
  background: var(--color-background-soft);
}

.ai-service-card {
  background: var(--color-background);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 2rem;
  align-items: center;
}

.ai-service-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.ai-service-description {
  color: var(--color-text-muted);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.ai-service-benefits {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ai-service-benefits li {
  padding: 0.5rem 0;
  color: var(--color-text);
  position: relative;
  padding-left: 1.5rem;
}

.ai-service-benefits li::before {
  content: '→';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

.ai-visual-placeholder {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-background);
}

.ai-visual-placeholder svg {
  width: 60px;
  height: 60px;
}

.pricing-section {
  background: var(--color-background);
}

.pricing-card {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-normal);
  position: relative;
}

.pricing-card.featured {
  border-color: var(--color-primary);
  transform: scale(1.05);
}

.pricing-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.pricing-card.featured:hover {
  transform: scale(1.05) translateY(-8px);
}

.plan-name {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.plan-price {
  margin-bottom: 1rem;
}

.currency {
  font-size: 1.5rem;
  color: var(--color-text-muted);
}

.amount {
  font-size: 3rem;
  font-weight: 700;
  color: var(--color-primary);
}

.period {
  font-size: 1rem;
  color: var(--color-text-muted);
}

.plan-description {
  color: var(--color-text-muted);
  margin-bottom: 2rem;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin: 0 0 2rem 0;
  text-align: left;
}

.plan-features li {
  padding: 0.5rem 0;
  color: var(--color-text);
  position: relative;
  padding-left: 1.5rem;
}

.plan-features li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: bold;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .ai-service-card {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .pricing-card.featured {
    transform: none;
  }

  .pricing-card.featured:hover {
    transform: translateY(-8px);
  }
}
</style>
