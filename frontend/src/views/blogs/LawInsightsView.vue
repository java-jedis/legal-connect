<template>
  <div class="insights-container">
    <div class="header">
      <div class="header-left">
        <h1>Law Insights</h1>
        <p class="subtitle">Explore legal blogs from lawyers. Subscribe to authors you like.</p>
      </div>
    </div>

    <div class="tabs">
      <button @click="activeTab='subscribed'" :class="['tab', {active: activeTab==='subscribed'}]">Subscribed Blogs</button>
      <button @click="activeTab='search'" :class="['tab', {active: activeTab==='search'}]">Search</button>
      <button @click="activeTab='authors'" :class="['tab', {active: activeTab==='authors'}]">My Subscriptions</button>
    </div>

    <div class="content">
      <div v-if="isLoading" class="loading">Loading...</div>

      <template v-else>
        <!-- Subscribed Blogs -->
        <div v-if="activeTab==='subscribed'">
          <div v-if="!hasSubscribedBlogs" class="empty-state">
            <p>No subscribed blogs yet. Use Search to find authors and subscribe.</p>
          </div>
          <div v-else class="blog-list">
            <BlogCard
              v-for="b in subscribedBlogs"
              :key="b.blogId"
              :blog="b"
              @view="openBlog"
            />
          </div>
          <div class="pagination" v-if="subscribedBlogsPage.totalPages > 1">
            <button class="btn btn-outline" :disabled="subscribedBlogsPage.page === 0" @click="loadSubscribed(subscribedBlogsPage.page-1)">Prev</button>
            <span>Page {{ subscribedBlogsPage.page + 1 }} / {{ subscribedBlogsPage.totalPages }}</span>
            <button class="btn btn-outline" :disabled="subscribedBlogsPage.page >= subscribedBlogsPage.totalPages - 1" @click="loadSubscribed(subscribedBlogsPage.page+1)">Next</button>
          </div>
        </div>

        <!-- Search -->
        <div v-if="activeTab==='search'">
          <div class="search-controls">
            <BlogSearchBar @search="onSearch" />
          </div>
          <div v-if="searchResults.length === 0" class="empty-state">
            <p>Search to discover published blogs from lawyers.</p>
          </div>
          <div v-else class="blog-list">
            <BlogCard
              v-for="r in searchResults"
              :key="r.blog.blogId"
              :blog="r.blog"
              @view="openBlog"
            >
              <template #default>
                <div class="highlight" v-if="r.highlightedTitle" v-html="r.highlightedTitle"></div>
                <div class="highlight" v-if="r.highlightedContent" v-html="r.highlightedContent"></div>
              </template>
            </BlogCard>
          </div>
          <div class="pagination" v-if="searchPage.totalPages > 1">
            <button class="btn btn-outline" :disabled="searchPage.page === 0" @click="doSearch(searchQuery, searchPage.page-1)">Prev</button>
            <span>Page {{ searchPage.page + 1 }} / {{ searchPage.totalPages }}</span>
            <button class="btn btn-outline" :disabled="searchPage.page >= searchPage.totalPages - 1" @click="doSearch(searchQuery, searchPage.page+1)">Next</button>
          </div>
        </div>

        <!-- My Subscriptions (authors) -->
        <div v-if="activeTab==='authors'">
          <SubscriptionsList :authors="subscriptions" @unsubscribe="onUnsubscribe" />
          <div class="pagination" v-if="subscriptionsPage.totalPages > 1">
            <button class="btn btn-outline" :disabled="subscriptionsPage.page === 0" @click="subscriptionsPage.page = subscriptionsPage.page - 1">Prev</button>
            <span>Page {{ subscriptionsPage.page + 1 }} / {{ subscriptionsPage.totalPages }}</span>
            <button class="btn btn-outline" :disabled="subscriptionsPage.page >= subscriptionsPage.totalPages - 1" @click="subscriptionsPage.page = subscriptionsPage.page + 1">Next</button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import BlogCard from '@/components/blogs/BlogCard.vue'
import BlogSearchBar from '@/components/blogs/BlogSearchBar.vue'
import SubscriptionsList from '@/components/blogs/SubscriptionsList.vue'
import { useBlogStore } from '@/stores/blog'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'

const blogStore = useBlogStore()
const {
  isLoading,
  subscribedBlogs,
  subscribedBlogsPage,
  hasSubscribedBlogs,
  searchResults,
  searchPage,
} = storeToRefs(blogStore)

const { loadSubscribedBlogs, searchPublishedBlogs, unsubscribeFromAuthor } = blogStore

const activeTab = ref('subscribed')
const searchQuery = ref('')

// Derive authors list from subscribed blogs
const subscriptions = computed(() => {
  const map = new Map()
  for (const b of subscribedBlogs.value || []) {
    const a = b.author
    if (a && !map.has(a.id)) {
      map.set(a.id, { id: a.id, firstName: a.firstName, lastName: a.lastName, email: a.email })
    }
  }
  return Array.from(map.values())
})
const subscriptionsPage = ref({ page: 0, size: 10, total: 0, totalPages: 1 })

onMounted(() => {
  loadSubscribed(0)
})

function loadSubscribed(page) {
  loadSubscribedBlogs(page, subscribedBlogsPage.value.size, subscribedBlogsPage.value.sortDirection)
}

function onSearch(q) {
  searchQuery.value = q
  doSearch(q, 0)
}

function doSearch(q, page) {
  if (!q) return
  searchPublishedBlogs(q, page, searchPage.value.size)
}

function openBlog(blogId) {
  const url = `/blog/${blogId}`
  window.open(url, '_blank')
}

async function onUnsubscribe(author) {
  try {
    await unsubscribeFromAuthor(author.id || author.userId)
    loadSubscribed(0)
    if (searchQuery.value) doSearch(searchQuery.value, searchPage.value.page)
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.insights-container { padding: 1.5rem; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem; gap: 1rem; }
.subtitle { color: var(--color-text-muted); margin-top: .25rem; }
.header-actions { display:flex; gap:.5rem; }
.tabs { display:flex; gap:.5rem; margin-bottom: 1rem; }
.tab { padding: 0.5rem 1rem; border: none; background: none; border-radius: 8px; cursor: pointer; color: var(--color-text-muted); font-weight: 500; transition: all 0.2s ease; }
.tab.active { background: #e0f2fe; color: #0369a1 !important; font-weight: 600; }
.loading { padding: 2rem; text-align:center; }
.empty-state { padding:2rem; text-align:center; color: var(--color-text-muted); }
.blog-list { display:flex; flex-direction:column; gap:1rem; }
.highlight :deep(em) { background: yellow; font-style: normal; }
.pagination { display:flex; justify-content:center; align-items:center; gap:.75rem; margin-top:1rem; }
.search-controls { margin-bottom: .75rem; }
</style> 