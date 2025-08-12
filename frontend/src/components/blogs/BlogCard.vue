<template>
  <div class="blog-card">
    <div class="blog-main">
      <h3 class="title">{{ blog.title }}</h3>
      <div class="meta">
        <span class="author">By {{ blog.author.firstName }} {{ blog.author.lastName }}</span>
        <span class="timestamp">Updated: {{ formatDate(blog.updatedAt) }}</span>
      </div>
      <p class="excerpt">{{ excerpt }}</p>
      <slot></slot>
    </div>
    <div class="actions">
      <slot name="actions">
        <button class="btn btn-outline btn-sm" @click="$emit('view', blog.blogId)">
          View
        </button>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  blog: { type: Object, required: true },
  subscribed: { type: Boolean, default: false },
  excerptLength: { type: Number, default: 220 },
})

const excerpt = computed(() => {
  const div = document.createElement('div')
  div.innerHTML = props.blog.content || ''
  const text = div.textContent || div.innerText || ''
  return text.length <= props.excerptLength ? text : text.slice(0, props.excerptLength) + '…'
})

function formatDate(dt) {
  try { return new Date(dt).toLocaleString() } catch { return dt }
}
</script>

<style scoped>
.blog-card { display:flex; justify-content:space-between; gap:1rem; border:1px solid var(--color-border); background: var(--color-background-soft); border-radius: var(--border-radius-lg); padding:1rem; }
.blog-main { max-width: 70%; }
.title { margin:0 0 .25rem 0; }
.meta { display:flex; gap:.75rem; align-items:center; margin-bottom:.5rem; font-size:.85rem; color: var(--color-text-muted); }
.excerpt { margin:.25rem 0 0; color: var(--color-text); }
.actions { display:flex; align-items:center; gap:.5rem; }
</style> 