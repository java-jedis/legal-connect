<template>
  <div class="topbar-container">
    <nav class="topbar-nav">
      <a href="#profile" class="nav-link" :class="{ active: activeSection === 'profile' }">Profile</a>
      <a v-if="isLawyer" href="#lawyer-profile" class="nav-link" :class="{ active: activeSection === 'lawyer-profile' }">Lawyer</a>
      <a href="#security" class="nav-link" :class="{ active: activeSection === 'security' }">Security</a>
      <a href="#notifications" class="nav-link" :class="{ active: activeSection === 'notifications' }">Notifications</a>
    </nav>
  </div>
</template>

<script setup>
import { defineProps, onMounted, onUnmounted, ref } from 'vue';

defineProps({
  isLawyer: {
    type: Boolean,
    default: false
  }
});

const activeSection = ref('profile');

const handleScroll = () => {
  const sections = ['profile', 'lawyer-profile', 'security', 'notifications'];
  const scrollPosition = window.scrollY + 100;

  for (const sectionId of sections) {
    const section = document.getElementById(sectionId);
    if (section && scrollPosition >= section.offsetTop && scrollPosition < section.offsetTop + section.offsetHeight) {
      activeSection.value = sectionId;
      break;
    }
  }
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
.topbar-container {
  position: sticky;
  top: 75px;
  width: 100%;
  background-color: var(--color-background);
  border-bottom: 1px solid var(--color-border);
  z-index: 1000;
  padding: 0.75rem 2rem;
  box-shadow: var(--shadow-sm);
}

.topbar-nav {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.nav-link {
  color: var(--color-text-muted);
  text-decoration: none;
  font-weight: 600;
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius);
  transition: all 0.3s ease;
}

.nav-link.active,
.nav-link:hover {
  color: var(--color-primary);
  background-color: var(--color-background-soft);
}
</style>
