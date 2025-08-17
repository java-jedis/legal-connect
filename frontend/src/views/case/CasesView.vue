<template>
  <div class="cases-view">
    <div class="container">
      <CaseManagement />
    </div>
  </div>
</template>

<script setup>
import CaseManagement from '@/components/case/CaseManagement.vue';
</script>

<style scoped>
.cases-view {
  min-height: 100vh;
  background: var(--color-background-soft);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }
}
</style> 