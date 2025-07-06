<script setup>
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
import { useThemeStore } from '@/stores/theme'
import { onMounted } from 'vue'

const themeStore = useThemeStore()
onMounted(() => {
  themeStore.initTheme()
  console.log('Theme initialized:', themeStore.isDark ? 'dark' : 'light')
})

// Transition hooks for smooth animations
const beforeEnter = (el) => {
  el.style.opacity = '0'
  el.style.transform = 'translateY(20px)'
}

const enter = (el, done) => {
  // Use requestAnimationFrame to ensure the initial state is applied
  requestAnimationFrame(() => {
    el.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
    el.style.opacity = '1'
    el.style.transform = 'translateY(0)'
    
    // Clean up transition after it completes
    setTimeout(() => {
      el.style.transition = ''
      done()
    }, 400)
  })
}

const leave = (el, done) => {
  el.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
  el.style.opacity = '0'
  el.style.transform = 'translateY(-20px)'
  
  setTimeout(done, 300)
}
</script>

<template>
  <div id="app">
    <AppHeader />
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition 
          name="page" 
          mode="out-in"
          @before-enter="beforeEnter"
          @enter="enter"
          @leave="leave"
        >
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
  </div>
</template>

<style>
/* Global app styles */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Global page transition styles */
.page-enter-active,
.page-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Ensure smooth scrolling for the entire app */
html {
  scroll-behavior: smooth;
}

/* Prevent layout shift during transitions */
.main-content {
  min-height: calc(100vh - 140px); /* Adjust based on header/footer height */
}

/* Smooth transitions for all interactive elements */
* {
  transition: color 0.2s ease, background-color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

/* Enhanced button transitions */
.btn {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.btn:hover {
  transform: translateY(-2px);
}

/* Smooth form input transitions */
input, textarea, select {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Card hover effects */
.card, .dashboard-card, .quick-action-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Loading states */
.loading {
  transition: opacity 0.3s ease;
}

/* Fade in animation for content */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Stagger animation for lists */
.stagger-item {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  animation-fill-mode: both;
}

.stagger-item:nth-child(1) { animation-delay: 0.1s; }
.stagger-item:nth-child(2) { animation-delay: 0.2s; }
.stagger-item:nth-child(3) { animation-delay: 0.3s; }
.stagger-item:nth-child(4) { animation-delay: 0.4s; }
.stagger-item:nth-child(5) { animation-delay: 0.5s; }
.stagger-item:nth-child(6) { animation-delay: 0.6s; }
</style>
