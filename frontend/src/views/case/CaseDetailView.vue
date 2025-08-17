<template>
  <div class="case-detail-view">
    <div class="container">
      <CaseDetail />
    </div>
  </div>
</template>

<script setup>
import CaseDetail from '@/components/case/CaseDetail.vue';
</script>

<style scoped>
.case-detail-view {
  min-height: 100vh;
  background: var(--color-background-soft);
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }
}
</style> 