<template>
  <div class="lawyer-blogs-container">
    <div class="header">
      <div class="header-left">
        <h1>My Blogs</h1>
        <p class="subtitle">Write, publish, and manage your blogs. View your subscribers.</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openCreateModal">
          <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
            <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
          </svg>
          New Blog
        </button>
        <button class="btn btn-outline" @click="openSubscribersModal">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" />
          </svg>
          Subscribers ({{ subscriberPage.total || 0 }})
        </button>
      </div>
    </div>

    <!-- Filters Section (similar to CaseManagement.vue) -->
    <div class="blog-filters">
      <div class="filter-tabs">
        <button @click="filterByStatus(null)" :class="['filter-tab', { active: currentFilter === null }]">
          All Blogs ({{ totalBlogsCount }})
        </button>
        <button @click="filterByStatus('PUBLISHED')" :class="['filter-tab', { active: currentFilter === 'PUBLISHED' }]">
          Published ({{ publishedCount }})
        </button>
        <button @click="filterByStatus('DRAFT')" :class="['filter-tab', { active: currentFilter === 'DRAFT' }]">
          Drafts ({{ draftCount }})
        </button>
        <button @click="filterByStatus('ARCHIVED')" :class="['filter-tab', { active: currentFilter === 'ARCHIVED' }]">
          Archived ({{ archivedCount }})
        </button>
      </div>
    </div>

    <div class="content">
      <div v-if="isLoading" class="loading">Loading...</div>
      <div v-else>
        <div v-if="!hasBlogs" class="empty-state">
          <p>No blogs yet. Click "New Blog" to create your first post.</p>
        </div>
        <div v-else class="blog-list">
          <div v-for="blog in filteredBlogs" :key="blog.blogId" class="blog-card">
            <div class="blog-main">
              <h3 class="title">{{ blog.title }}</h3>
              <div class="meta">
                <span class="status" :class="blog.status.toLowerCase()">{{ blog.status }}</span>
                <span class="timestamp">Updated: {{ formatDate(blog.updatedAt) }}</span>
              </div>
              <p class="excerpt">{{ truncate(blog.content, 220) }}</p>
            </div>
            <div class="actions">
              <button class="btn btn-outline btn-sm" @click="openEditModal(blog)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Edit
              </button>
              <button class="btn btn-outline btn-sm" v-if="blog.status !== 'ARCHIVED'" @click="archive(blog)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <path d="M3 7h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M5 7v11a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <rect x="3" y="3" width="18" height="4" rx="1" ry="1" stroke="currentColor" stroke-width="2"/>
                </svg>
                Archive
              </button>
              <button class="btn btn-sm btn-danger" @click="confirmDelete(blog)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Delete
              </button>
              <div class="divider"></div>
              <button class="btn btn-primary btn-sm" v-if="blog.status !== 'PUBLISHED'" @click="publish(blog)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <path d="M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M13 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Publish
              </button>
            </div>
          </div>
        </div>

        <div class="pagination" v-if="pagination.totalPages > 1 && currentFilter === null">
          <button class="btn btn-outline" :disabled="pagination.page === 0" @click="goToPage(pagination.page-1)">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
              <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Prev
          </button>
          <span>Page {{ pagination.page + 1 }} / {{ pagination.totalPages }}</span>
          <button class="btn btn-outline" :disabled="pagination.page >= pagination.totalPages - 1" @click="goToPage(pagination.page+1)">
            Next
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
              <path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showEditor" class="modal-overlay" @click.self="closeEditor">
      <div class="modal-content editor-modal">
        <div class="modal-body">
          <div class="form-group">
            <label>Title</label>
            <input v-model="form.title" class="title-input" placeholder="Enter a compelling blog title" />
          </div>
          <div class="form-group">
            <label>Content</label>
            <div ref="editor" class="rich-editor" contenteditable="true" @input="onEditorInput"></div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="closeEditor">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
              <path d="M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            Cancel
          </button>
          <button class="btn btn-outline" @click="saveDraft">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
              <path d="M19 21H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h9l5 5v9a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M17 21v-8H7v8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 3v5h8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Save as Draft
          </button>
          <button class="btn btn-primary" @click="savePublish">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
              <path d="M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M13 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Publish
          </button>
        </div>
      </div>
    </div>

    <!-- Subscribers Modal -->
    <div v-if="showSubscribers" class="modal-overlay" @click.self="closeSubscribersModal">
      <div class="modal-content subscribers-modal">
        <div class="modal-header soft">
          <button class="modal-close" @click="closeSubscribersModal">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="subscribers.length === 0" class="empty-state">
            <p>No subscribers yet.</p>
          </div>
          <div v-else class="subs-list">
            <div v-for="s in subscribers" :key="s.email" class="sub-item">
              <div class="avatar">{{ initials(s.firstName, s.lastName) }}</div>
              <div class="info">
                <div class="name">{{ s.firstName }} {{ s.lastName }}</div>
                <div class="email">{{ s.email }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" :disabled="subscriberPage.page === 0" @click="loadSubs(subscriberPage.page-1)">Prev</button>
          <span>Page {{ subscriberPage.page + 1 }} / {{ subscriberPage.totalPages || 1 }}</span>
          <button class="btn btn-outline" :disabled="subscriberPage.page >= (subscriberPage.totalPages || 1) - 1" @click="loadSubs(subscriberPage.page+1)">Next</button>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <DeleteConfirmationModal v-if="toDelete" :eventTitle="toDelete.title" @confirm="doDelete" @cancel="toDelete=null" />
  </div>
</template>

<script setup>
import DeleteConfirmationModal from '@/components/DeleteConfirmationModal.vue'
import { useBlogStore } from '@/stores/blog'
import { storeToRefs } from 'pinia'
import { computed, nextTick, onMounted, ref } from 'vue'

const blogStore = useBlogStore()
const { blogs, pagination, isLoading, subscribers, subscriberPage } = storeToRefs(blogStore)
const { loadMyBlogs, createBlog, updateBlog, deleteBlog, changeStatus, loadSubscribers } = blogStore

const currentFilter = ref(null) // null | 'PUBLISHED' | 'DRAFT' | 'ARCHIVED'

const showEditor = ref(false)
const showSubscribers = ref(false)
const editingBlog = ref(null)
const toDelete = ref(null)

const form = ref({ title: '', content: '' })
const hasBlogs = computed(() => (blogs.value?.length || 0) > 0)

const editor = ref(null)

const filteredBlogs = computed(() => {
  if (!blogs.value) return []
  if (!currentFilter.value) return blogs.value
  return blogs.value.filter(b => b.status === currentFilter.value)
})

const totalBlogsCount = computed(() => blogs.value?.length || 0)
const publishedCount = computed(() => blogs.value?.filter(b => b.status === 'PUBLISHED').length || 0)
const draftCount = computed(() => blogs.value?.filter(b => b.status === 'DRAFT').length || 0)
const archivedCount = computed(() => blogs.value?.filter(b => b.status === 'ARCHIVED').length || 0)

onMounted(() => {
  loadPage(0)
  loadSubs(0)
})

function loadPage(page) {
  loadMyBlogs(page, pagination.value.size, pagination.value.sortDirection)
}

function goToPage(page) {
  loadPage(page)
}

function filterByStatus(status) {
  currentFilter.value = status
}

function loadSubs(page = 0) {
  loadSubscribers(page, subscriberPage.value.size || 10)
}

function openCreateModal() {
  editingBlog.value = null
  form.value = { title: '', content: '' }
  showEditor.value = true
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay')
    modal?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    if (editor.value) {
      editor.value.innerHTML = ''
      editor.value.focus()
    }
  })
}

function openEditModal(blog) {
  editingBlog.value = blog
  form.value = { title: blog.title, content: blog.content }
  showEditor.value = true
  nextTick(() => {
    const modal = document.querySelector('.modal-overlay')
    modal?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    if (editor.value) {
      editor.value.innerHTML = sanitizeHTML(blog.content || '')
      editor.value.focus()
    }
  })
}

function closeEditor() {
  showEditor.value = false
}

function onEditorInput() {
  if (!editor.value) return
  form.value.content = sanitizeHTML(editor.value.innerHTML)
}


function sanitizeHTML(html) {
  // Basic sanitizer allowing a limited set of tags/attributes
  const temp = document.createElement('div')
  temp.innerHTML = html || ''
  const allowedTags = new Set(['B','STRONG','I','EM','U','P','H1','H2','UL','OL','LI','A','BR'])
  const traverse = (node) => {
    const children = Array.from(node.childNodes)
    for (const child of children) {
      if (child.nodeType === 1) {
        if (!allowedTags.has(child.tagName)) {
          // unwrap disallowed element but keep its children
          while (child.firstChild) node.insertBefore(child.firstChild, child)
          node.removeChild(child)
          continue
        }
        // Clean attributes
        for (const attr of Array.from(child.attributes)) {
          if (child.tagName === 'A' && (attr.name === 'href' || attr.name === 'target' || attr.name === 'rel')) continue
          child.removeAttribute(attr.name)
        }
        if (child.tagName === 'A') {
          child.setAttribute('target','_blank')
          child.setAttribute('rel','noopener noreferrer')
        }
        traverse(child)
      } else if (child.nodeType === 8) {
        // remove comments
        node.removeChild(child)
      }
    }
  }
  traverse(temp)
  return temp.innerHTML
}

function openSubscribersModal() {
  showSubscribers.value = true
  loadSubs(0)
}

function closeSubscribersModal() {
  showSubscribers.value = false
}

async function saveDraft() {
  // Ensure content reflects editor
  onEditorInput()
  if (editingBlog.value) {
    await updateBlog(editingBlog.value.blogId, { title: form.value.title, content: form.value.content, status: 'DRAFT' })
  } else {
    await createBlog({ title: form.value.title, content: form.value.content, status: 'DRAFT' })
  }
  showEditor.value = false
}

async function savePublish() {
  // Ensure content reflects editor
  onEditorInput()
  if (editingBlog.value) {
    await updateBlog(editingBlog.value.blogId, { title: form.value.title, content: form.value.content, status: 'PUBLISHED' })
  } else {
    await createBlog({ title: form.value.title, content: form.value.content, status: 'PUBLISHED' })
  }
  showEditor.value = false
}

async function publish(blog) {
  await changeStatus(blog.blogId, 'PUBLISHED')
}

async function archive(blog) {
  await changeStatus(blog.blogId, 'ARCHIVED')
}

function confirmDelete(blog) {
  toDelete.value = blog
}

async function doDelete() {
  if (!toDelete.value) return
  await deleteBlog(toDelete.value.blogId)
  toDelete.value = null
}

function formatDate(dt) {
  try { return new Date(dt).toLocaleString() } catch { return dt }
}

function truncate(html, len) {
  const div = document.createElement('div')
  div.innerHTML = html || ''
  const text = div.textContent || div.innerText || ''
  if (text.length <= len) return text
  return text.slice(0, len) + '…'
}

function initials(fn, ln) {
  return `${(fn||'')[0]||''}${(ln||'')[0]||''}`.toUpperCase()
}
</script>

<style scoped>
.lawyer-blogs-container { padding: 1.5rem; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem; }
.subtitle { color: var(--color-text-muted); margin-top: .25rem; }
.header-actions { display:flex; gap:.5rem; }
.header-actions .btn { padding: .6rem 1rem; }

/* Filters (borrowed from CaseManagement.vue) */
.blog-filters { margin-bottom: 1rem; }
.filter-tabs { display:flex; gap: .5rem; }
.filter-tab { padding: 0.5rem 1rem; border: none; background: none; border-radius: 8px; cursor: pointer; color: var(--color-text-muted); font-weight: 500; transition: all 0.2s ease; }
.filter-tab:hover { background: var(--color-background-mute); color: var(--color-text); }
.filter-tab.active { background: #e0f2fe; color: #0369a1 !important; font-weight: 600; }

.loading { padding: 2rem; text-align:center; }
.empty-state { padding:2rem; text-align:center; color: var(--color-text-muted); }

.blog-list { display:flex; flex-direction:column; gap:1rem; }
.blog-card { display:flex; justify-content:space-between; gap:1rem; border:1px solid var(--color-border); background: var(--color-background-soft); border-radius: var(--border-radius-lg); padding:1rem; }
.blog-main { max-width: 70%; }
.title { margin:0 0 .25rem 0; }
.meta { display:flex; gap:.75rem; align-items:center; margin-bottom:.5rem; font-size:.85rem; color: var(--color-text-muted); }
.status { padding:.15rem .5rem; border-radius: 999px; border:1px solid var(--color-border); font-weight:500; }
.status.draft { background: #f8fafc; }
.status.published { background: rgba(var(--color-primary-rgb), 0.1); color: var(--color-primary); border-color: var(--color-primary); }
.status.archived { background:#f3f4f6; color:#6b7280; }
.excerpt { margin:.25rem 0 0; color: var(--color-text); }
.actions { display:flex; align-items:center; gap:.5rem; }
.actions .divider { width:1px; height:20px; background: var(--color-border); margin:0 .25rem; }

.pagination { display:flex; justify-content:center; align-items:center; gap:.75rem; margin-top:1rem; }

.subs-list { display:flex; flex-direction:column; gap:.5rem; }
.sub-item { display:flex; gap:.75rem; align-items:center; border:1px solid var(--color-border); background: var(--color-background-soft); border-radius: var(--border-radius); padding:.75rem; }
.avatar { width:36px; height:36px; border-radius:999px; background: var(--color-primary); color: var(--color-background); display:flex; align-items:center; justify-content:center; font-weight:700; }
.info .name { font-weight:600; }
.info .email { font-size:.85rem; color: var(--color-text-muted); }

/* Enlarged editor modal */
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.5); display:flex; align-items:center; justify-content:center; z-index:1000; padding:1rem; }
.modal-content { background: var(--color-background); border-radius:12px; width:100%; max-width:720px; overflow:hidden; display:flex; flex-direction:column; max-height:80vh; }
.editor-modal { max-width: 1100px; }
.modal-body { padding:1rem 1.25rem; flex:1; overflow-y: auto; }
.modal-actions { display:flex; justify-content:flex-end; align-items:center; gap:.5rem; padding:1rem 1.25rem; border-top:1px solid var(--color-border); }
.form-group { display:flex; flex-direction:column; gap:.375rem; margin-bottom:1rem; }
.form-group label { font-weight:600; color: var(--color-heading); }
.title-input { font-size:1.05rem; padding:.7rem .8rem; }
.content-textarea { width:100%; min-height: 480px; resize: vertical; line-height: 1.7; padding:.9rem 1rem; font-family: inherit; }
input, textarea { width:100%; border:1px solid var(--color-border); border-radius:8px; background: var(--color-background); color: var(--color-text); font-family: inherit; }
input:focus, textarea:focus { outline:none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1); }
.modal-close { background:none; border:none; font-size:1.5rem; cursor:pointer; color: var(--color-text-muted); }

/* Match delete button style with NoteList.vue */
.btn-danger {
  background-color: #fee2e2;
  color: #ef4444;
  border: 1px solid #fee2e2;
}
.btn-danger:hover {
  background-color: #fecaca;
}
.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 0.5rem;
}
.editor-toolbar .toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--color-border);
  margin: 0 0.25rem;
}
.rich-editor {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.75rem 0.9rem;
  min-height: 320px;
  background: var(--color-background);
  color: var(--color-text);
  line-height: 1.7;
}
.rich-editor:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}
</style> 