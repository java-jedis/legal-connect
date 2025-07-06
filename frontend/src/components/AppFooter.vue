<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section">
          <div class="footer-brand">
            <svg
              class="brand-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
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
            <span class="brand-text">Legal Connect</span>
          </div>
          <p class="footer-description">
            Revolutionizing legal services with AI-powered solutions that make legal assistance
            accessible, efficient, and intelligent for everyone.
          </p>
          <div class="social-links">
            <a href="#" class="social-link" aria-label="Facebook">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
                />
              </svg>
            </a>
            <a href="#" class="social-link" aria-label="X">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
              </svg>
            </a>
            <a href="#" class="social-link" aria-label="LinkedIn">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"
                />
              </svg>
            </a>
          </div>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">Platform</h3>
          <ul class="footer-links">
            <li><router-link to="/services" class="footer-link">Legal Services</router-link></li>
            <li><router-link to="/lawyers" class="footer-link">Find Lawyers</router-link></li>
            <li><router-link to="/ai-chat" class="footer-link">AI Legal Assistant</router-link></li>
            <li>
              <router-link to="/document-management" class="footer-link"
                >Document Management</router-link
              >
            </li>
            <li>
              <router-link to="/scheduling" class="footer-link">Appointment Booking</router-link>
            </li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">Support</h3>
          <ul class="footer-links">
            <li><router-link to="/help" class="footer-link">Help Center</router-link></li>
            <li><router-link to="/contact" class="footer-link">Contact Us</router-link></li>
            <li><router-link to="/faq" class="footer-link">FAQ</router-link></li>
            <li><router-link to="/tutorials" class="footer-link">Tutorials</router-link></li>
            <li><router-link to="/status" class="footer-link">System Status</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">Company</h3>
          <ul class="footer-links">
            <li><router-link to="/about" class="footer-link">About Us</router-link></li>
            <li><router-link to="/careers" class="footer-link">Careers</router-link></li>
            <li><router-link to="/press" class="footer-link">Press</router-link></li>
            <li><router-link to="/partners" class="footer-link">Partners</router-link></li>
            <li><router-link to="/blog" class="footer-link">Blog</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">Legal</h3>
          <ul class="footer-links">
            <li><router-link to="/privacy" class="footer-link">Privacy Policy</router-link></li>
            <li><router-link to="/terms" class="footer-link">Terms of Service</router-link></li>
            <li><router-link to="/cookies" class="footer-link">Cookie Policy</router-link></li>
            <li><router-link to="/security" class="footer-link">Security</router-link></li>
            <li><router-link to="/compliance" class="footer-link">Compliance</router-link></li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="footer-bottom-content">
          <p class="copyright">© {{ currentYear }} LegalConnect. All rights reserved.</p>
          <div class="footer-bottom-links">
            <router-link to="/privacy" class="footer-bottom-link">Privacy</router-link>
            <router-link to="/terms" class="footer-bottom-link">Terms</router-link>
            <router-link to="/cookies" class="footer-bottom-link">Cookies</router-link>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
  padding: 4rem 0 2rem;
  margin-top: auto;
  transition:
    background-color var(--transition-normal),
    border-color var(--transition-normal);
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 3rem;
  margin-bottom: 3rem;
}

.footer-brand {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.brand-icon {
  width: 32px;
  height: 32px;
  margin-right: 0.5rem;
  color: var(--color-primary);
  transition: color var(--transition-normal);
}

.brand-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-description {
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 1.5rem;
  max-width: 300px;
  transition: color var(--transition-normal);
}

.social-links {
  display: flex;
  gap: 1rem;
}

.social-link {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-background);
  color: var(--color-text);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  text-decoration: none;
  border: 1px solid var(--color-border);
}

.social-link:hover {
  background: var(--color-primary);
  color: var(--color-background);
  transform: translateY(-2px);
}

.social-link svg {
  width: 20px;
  height: 20px;
}

.footer-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1.5rem;
  transition: color var(--transition-normal);
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 0.75rem;
}

.footer-link {
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);
  font-size: 0.875rem;
}

.footer-link:hover {
  color: var(--color-primary);
}

.footer-bottom {
  border-top: 1px solid var(--color-border);
  padding-top: 2rem;
  transition: border-color var(--transition-normal);
}

.footer-bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  transition: color var(--transition-normal);
}

.footer-bottom-links {
  display: flex;
  gap: 2rem;
}

.footer-bottom-link {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.875rem;
  transition: color var(--transition-fast);
}

.footer-bottom-link:hover {
  color: var(--color-primary);
}

@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
  }

  .footer-section:first-child {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .footer {
    padding: 3rem 0 1.5rem;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .footer-description {
    max-width: none;
  }

  .footer-bottom-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .footer-bottom-links {
    gap: 1rem;
  }
}

@media (max-width: 480px) {
  .footer {
    padding: 2rem 0 1rem;
  }

  .social-links {
    justify-content: center;
  }
}
</style>
