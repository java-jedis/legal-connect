<template>
  <div class="blog-detail-container" v-if="blog">
    <div class="header">
      <h1 class="title">{{ blog.title }}</h1>
      <div class="meta">
        <span>{{ formatDate(blog.updatedAt) }}</span>
        <span>By {{ blog.author.firstName }} {{ blog.author.lastName }}</span>
        <button
          v-if="blog && blog.subscribed === false"
          class="subscribe-btn"
          @click="subscribe"
        >
          Subscribe
        </button>
      </div>
    </div>
    <div class="content" v-html="blog.content"></div>
  </div>
  <div v-else class="loading">Loading...</div>
</template>

<script setup>
import { useBlogStore } from '@/stores/blog'
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const blogStore = useBlogStore()
const blog = ref(null)

onMounted(async () => {
  const id = route.params.id
  blog.value = await blogStore.getBlogById(id)
})

function formatDate(dt) {
  try { return new Date(dt).toLocaleString() } catch { return dt }
}

async function subscribe() {
  try {
    await blogStore.subscribeToAuthor(blog.value.author.id)
    blog.value.subscribed = true
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.blog-detail-container { padding: 1.5rem; max-width: 960px; margin: 0 auto; }
.header { margin-bottom: 1rem; }
.title { margin: 0 0 .5rem 0; }
.meta { display:flex; align-items: center; gap: .75rem; color: var(--color-text-muted); margin-bottom: .5rem; }
.content :deep(p) { line-height: 1.8; margin: .5rem 0; }
.content :deep(h1), .content :deep(h2), .content :deep(h3) { margin-top: 1rem; }
.content :deep(a) { color: var(--color-primary); }

.subscribe-btn {
  padding: .25rem .6rem;
  font-size: .85rem;
  line-height: 1.2;
  border: 1px solid var(--color-primary);
  color: var(--color-primary);
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
}
.subscribe-btn:hover {
  background: rgba(0, 0, 0, 0.03);
}
</style> 