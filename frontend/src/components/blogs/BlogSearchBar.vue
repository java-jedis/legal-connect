<template>
  <div class="search-bar">
    <input
      v-model="query"
      type="text"
      class="search-input"
      placeholder="Search law insights (title, content)"
      @keyup.enter="emitSearch"
    />
    <button class="btn btn-outline" @click="emitSearch">Search</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['search'])
const query = ref('')

function emitSearch() {
  emit('search', query.value.trim())
}
</script>

<style scoped>
.search-bar { display:flex; gap:.5rem; align-items:center; }
.search-input { flex:1; padding:.6rem .8rem; border:1px solid var(--color-border); border-radius:8px; }
</style> 