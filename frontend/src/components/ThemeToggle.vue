<template>
  <button
    @click="themeStore.toggleTheme"
    class="theme-toggle"
    :aria-label="themeStore.isDark ? 'Switch to light mode' : 'Switch to dark mode'"
  >
    <svg
      v-if="themeStore.isDark"
      class="theme-icon"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
      />
    </svg>
    <svg
      v-else
      class="theme-icon"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
      />
    </svg>
  </button>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: var(--color-background-soft);
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;
}

.theme-toggle:hover {
  background: var(--color-primary);
  color: var(--color-background);
  transform: scale(1.05);
}

.theme-toggle:active {
  transform: scale(0.95);
}

.theme-icon {
  width: 20px;
  height: 20px;
  transition: transform var(--transition-fast);
}

.theme-toggle:hover .theme-icon {
  transform: rotate(15deg);
}
</style>
