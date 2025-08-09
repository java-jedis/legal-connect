<template>
  <div class="modal-overlay" @click.self="cancel">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Confirm Deletion</h3>
        <button class="modal-close" @click="cancel">&times;</button>
      </div>
      <div class="modal-body">
        <p>Are you sure you want to delete the document "<strong>{{ documentTitle }}</strong>"?</p>
        <p>This action cannot be undone.</p>
      </div>
      <div class="modal-actions">
        <button class="btn btn-danger" @click="confirm">Delete</button>
        <button class="btn btn-outline" @click="cancel">Cancel</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  documentTitle: { type: String, default: '' }
})
const emit = defineEmits(['confirm', 'cancel'])

function confirm() {
  emit('confirm')
}

function cancel() {
  emit('cancel')
}
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
  z-index: 1010; /* Higher than other modals */
  padding: 1rem;
}
.modal-content {
  background: var(--color-background);
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 400px;
  padding: 1.5rem;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
}
.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--color-text-muted);
  cursor: pointer;
}
.modal-body p {
  margin: 0 0 1rem 0;
  line-height: 1.6;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}
.btn {
  padding: 0.5rem 1.25rem;
  border-radius: 6px;
  border: none;
  font-weight: 500;
  cursor: pointer;
}
.btn-danger {
  background: #dc2626;
  color: #fff;
}
.btn-outline {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}
</style>
