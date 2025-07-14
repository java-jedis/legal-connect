<template>
  <div class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ note.title }}</h3>
        <button class="modal-close" @click="closeModal">&times;</button>
      </div>
      <div class="modal-body">
        <p class="note-full-content">{{ note.content }}</p>
      </div>
      <div class="modal-actions">
        <button type="button" class="btn btn-outline" @click="closeModal">Close</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick } from 'vue'

const props = defineProps({
  note: { type: Object, required: true }
})
const emit = defineEmits(['close'])

const closeModal = () => {
  emit('close')
}

// Scroll to modal on open
nextTick(() => {
  const modal = document.querySelector('.modal-overlay');
  if (modal) {
    modal.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}
.modal-content {
  background: var(--color-background);
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 600px; /* Slightly wider for content */
  max-height: 90vh;
  overflow-y: auto;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}
.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}
.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--color-text-muted);
  cursor: pointer;
}
.modal-body {
  padding: 1.5rem;
}
.note-full-content {
  white-space: pre-wrap; /* Preserve whitespace and line breaks */
  word-wrap: break-word;
  color: var(--color-text);
  line-height: 1.6;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
}
.btn {
  padding: 0.6rem 1.25rem;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}
.btn-outline {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
</style>
