<template>
  <div class="subs-list" v-if="authors?.length">
    <div class="sub-item" v-for="a in authors" :key="a.email">
      <div class="avatar">{{ initials(a.firstName, a.lastName) }}</div>
      <div class="info">
        <div class="name">{{ a.firstName }} {{ a.lastName }}</div>
        <div class="email">{{ a.email }}</div>
      </div>
      <div class="spacer"/>
      <button class="btn btn-outline btn-sm" @click="$emit('unsubscribe', a)">Unsubscribe</button>
    </div>
  </div>
  <div v-else class="empty-state">You haven't subscribed to any author yet.</div>
</template>

<script setup>
defineProps({
  authors: { type: Array, default: () => [] }
})

function initials(fn, ln) {
  return `${(fn||'')[0]||''}${(ln||'')[0]||''}`.toUpperCase()
}
</script>

<style scoped>
.subs-list { display:flex; flex-direction:column; gap:.5rem; }
.sub-item { display:flex; gap:.75rem; align-items:center; border:1px solid var(--color-border); background: var(--color-background-soft); border-radius: var(--border-radius); padding:.75rem; }
.avatar { width:36px; height:36px; border-radius:999px; background: var(--color-primary); color: var(--color-background); display:flex; align-items:center; justify-content:center; font-weight:700; }
.info .name { font-weight:600; }
.info .email { font-size:.85rem; color: var(--color-text-muted); }
.spacer { flex: 1; }
.empty-state { color: var(--color-text-muted); }
</style> 