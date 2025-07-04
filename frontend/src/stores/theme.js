import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  // Initialize theme from localStorage or system preference
  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      isDark.value = savedTheme === 'dark'
      console.log('Theme loaded from localStorage:', savedTheme)
    } else {
      // Check system preference
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
      console.log('Theme set from system preference:', isDark.value ? 'dark' : 'light')
    }
    applyTheme()
  }

  // Toggle theme
  const toggleTheme = () => {
    console.log('Theme toggle clicked. Current theme:', isDark.value ? 'dark' : 'light')
    isDark.value = !isDark.value
    console.log('New theme:', isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  // Apply theme to document
  const applyTheme = () => {
    const html = document.documentElement
    const body = document.body

    if (isDark.value) {
      html.classList.add('dark')
      body.classList.add('dark')
      console.log('Dark class added to document and body')

      // Force immediate style update for all variables
      html.style.setProperty('--color-background', '#0f0f0f')
      html.style.setProperty('--color-background-soft', '#1a1a1a')
      html.style.setProperty('--color-background-mute', '#2a2a2a')
      html.style.setProperty('--color-surface', '#27374D')
      html.style.setProperty('--color-text', '#e0e0e0')
      html.style.setProperty('--color-text-muted', '#b0b0b0')
      html.style.setProperty('--color-heading', '#ffffff')
      html.style.setProperty('--color-primary', '#27374D')
      html.style.setProperty('--color-secondary', '#526D82')
      html.style.setProperty('--color-accent', '#9DB2BF')
      html.style.setProperty('--color-border', '#2a2a2a')
      html.style.setProperty('--color-border-hover', '#3a3a3a')
    } else {
      html.classList.remove('dark')
      body.classList.remove('dark')
      console.log('Dark class removed from document and body')

      // Force immediate style update for all variables - Clio Light Theme
      html.style.setProperty('--color-background', '#ffffff')
      html.style.setProperty('--color-background-soft', '#f5f7f8')
      html.style.setProperty('--color-background-mute', '#ebf9ff')
      html.style.setProperty('--color-surface', '#0693e3')
      html.style.setProperty('--color-text', '#000000')
      html.style.setProperty('--color-text-muted', '#6b7280')
      html.style.setProperty('--color-heading', '#32373c')
      html.style.setProperty('--color-primary', '#0693e3')
      html.style.setProperty('--color-secondary', '#8ed1fc')
      html.style.setProperty('--color-accent', '#7bdcb5')
      html.style.setProperty('--color-border', '#d6eef9')
      html.style.setProperty('--color-border-hover', '#0693e3')
    }

    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')

    // Debug: Check if classes are actually applied
    console.log('HTML has dark class:', html.classList.contains('dark'))
    console.log('Body has dark class:', body.classList.contains('dark'))
  }

  // Watch for system theme changes
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', (e) => {
    if (!localStorage.getItem('theme')) {
      isDark.value = e.matches
      applyTheme()
    }
  })

  return {
    isDark,
    toggleTheme,
    initTheme,
  }
})
