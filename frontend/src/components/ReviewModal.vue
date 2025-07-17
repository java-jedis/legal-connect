<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div v-if="showDeleteConfirmation" class="delete-confirmation">
        <h4>Are you sure?</h4>
        <p>This action cannot be undone. The review will be permanently deleted.</p>
        <div class="confirmation-actions">
          <button @click="confirmDelete" class="btn btn-danger" :disabled="loading">Yes, delete</button>
          <button @click="showDeleteConfirmation = false" class="btn btn-secondary">Cancel</button>
        </div>
      </div>
      <div v-else>
        <h2 v-if="mode === 'add'">Add Review</h2>
        <h2 v-else-if="mode === 'edit'">Edit Review</h2>
        <h2 v-else>Client Review</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="rating">Rating</label>
            <div class="star-rating" :class="{ 'disabled': mode === 'view' }" @mouseleave="mode !== 'view' && (hoverRating = 0)">
              <span
                v-for="star in 5"
                :key="star"
                class="star"
                :class="{ 'filled': star <= (hoverRating || form.rating) }"
                @mouseover="mode !== 'view' && (hoverRating = star)"
                @click="mode !== 'view' && (form.rating = star)"
              >
                <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
                </svg>
              </span>
            </div>
          </div>
          <div class="form-group">
            <label for="comment">Comment</label>
            <textarea v-model="form.comment" id="comment" rows="4" required :readonly="mode === 'view'" placeholder="No comment provided."></textarea>
          </div>
          <div class="modal-actions">
            <template v-if="mode !== 'view'">
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <svg v-if="mode === 'edit'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                {{ mode === 'add' ? 'Submit' : 'Update' }}
              </button>
              <button type="button" class="btn btn-secondary" @click="close">Cancel</button>
              <button v-if="mode === 'edit'" type="button" class="btn btn-danger" @click="handleDelete" :disabled="loading">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
                  <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Delete
              </button>
            </template>
            <template v-else>
              <button type="button" class="btn btn-secondary" @click="close">Close</button>
            </template>
          </div>
          <div v-if="error" class="error-message">{{ error }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useReviewStore } from '../stores/review'

const props = defineProps({
  caseId: { type: String, required: true },
  review: { type: Object, default: null },
  mode: { type: String, default: 'add' } // 'add', 'edit', or 'view'
})
const emit = defineEmits(['close', 'review-added', 'review-updated', 'review-deleted'])

const reviewStore = useReviewStore()
const form = ref({
  rating: 0,
  comment: ''
})
const hoverRating = ref(0)
const showDeleteConfirmation = ref(false)
const loading = computed(() => reviewStore.loading[props.caseId])
const error = computed(() => reviewStore.error[props.caseId])

watch(() => props.review, (val) => {
  if (val) {
    form.value.rating = val.rating
    form.value.comment = val.review // Use 'review' field for comment
  } else {
    form.value.rating = 0
    form.value.comment = ''
  }
}, { immediate: true })

function close() {
  emit('close')
}

async function handleSubmit() {
  if (!form.value.rating || !form.value.comment) {
    // Maybe show a more specific error to the user
    return
  }
  const payload = {
    rating: form.value.rating,
    review: form.value.comment
  }
  try {
    if (props.mode === 'add') {
      await reviewStore.addReview(props.caseId, payload)
      emit('review-added')
    } else {
      await reviewStore.updateReview(props.caseId, props.review.id, payload)
      emit('review-updated')
    }
    close()
  } catch {
    // error handled by store
  }
}

function handleDelete() {
  showDeleteConfirmation.value = true
}

async function confirmDelete() {
  if (!props.review) return
  try {
    await reviewStore.deleteReview(props.caseId, props.review.id)
    emit('review-deleted')
    close()
  } catch {
    // error handled by store
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}
.modal-content {
  background: #fff;
  border-radius: 16px;
  padding: 2rem 2.5rem;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  border: 1px solid #e5e7eb;
  text-align: center;
}
h2 {
    font-size: 1.75rem;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 1.5rem;
}
.form-group {
  margin-bottom: 1.5rem;
  text-align: left;
}
label {
  font-weight: 600;
  display: block;
  margin-bottom: 0.75rem;
  color: #374151;
  font-size: 0.95rem;
}
.star-rating {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
.star {
  width: 36px;
  height: 36px;
  color: #d1d5db; /* Tailwind gray-300 */
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}
.star:hover {
  transform: scale(1.15);
}
.star.filled {
  color: #f59e0b; /* Tailwind amber-500 */
}
.star-rating.disabled .star {
  cursor: default;
}
.star-rating.disabled .star:hover {
  transform: none;
}
.star svg {
  width: 100%;
  height: 100%;
  fill: currentColor;
}

textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-size: 1rem;
  resize: vertical;
  min-height: 120px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
textarea:focus {
    outline: none;
    border-color: #2563eb;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2);
}
.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  justify-content: center; 
}
.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-primary {
  background: #4f46e5; /* Indigo-600 */
  color: #fff;
}
.btn-primary:hover {
    background: #4338ca; /* Indigo-700 */
}
.btn-secondary {
  background: #e5e7eb;
  color: #1f2937;
  border: 1px solid #d1d5db;
}
.btn-secondary:hover {
    background: #d1d5db;
}
.btn-danger {
  background-color: #fee2e2;
  color: #ef4444;
  border: 1px solid #fee2e2;
}
.btn-danger:hover {
    background: #fecaca;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error-message {
  color: #b91c1c;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  font-weight: 500;
}

.delete-confirmation {
  padding: 1rem;
}

.delete-confirmation h4 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.delete-confirmation p {
  color: #6b7280;
  margin-bottom: 1.5rem;
}

.confirmation-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style>