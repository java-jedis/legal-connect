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
</script>

<template>
  <div id="app">
    <AppHeader />
    <router-view />
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
</style>
