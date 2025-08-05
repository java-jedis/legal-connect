<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>Edit Meeting</h3>
        <button @click="closeModal" class="modal-close">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <line
              x1="18"
              y1="6"
              x2="6"
              y2="18"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <line
              x1="6"
              y1="6"
              x2="18"
              y2="18"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>

      <form @submit.prevent="submitMeeting" class="meeting-form">
        <!-- Client Info (Read-only) -->
        <div class="form-group">
          <label>Client</label>
          <div class="client-info">
            <div class="client-avatar">
              <span>{{ getClientInitials(meeting.client) }}</span>
            </div>
            <div class="client-details">
              <p class="client-name">{{ getClientName(meeting.client) }}</p>
              <p class="client-email">{{ meeting.client?.email }}</p>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startDate">Start Date *</label>
            <input
              id="startDate"
              v-model="startDate"
              type="date"
              class="form-input"
              :class="{ error: formErrors.startDateTime }"
              :min="minDate"
              required
            />
          </div>

          <div class="form-group">
            <label for="startTime">Start Time *</label>
            <input
              id="startTime"
              v-model="startTime"
              type="time"
              class="form-input"
              :class="{ error: formErrors.startDateTime }"
              required
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="endDate">End Date *</label>
            <input
              id="endDate"
              v-model="endDate"
              type="date"
              class="form-input"
              :class="{ error: formErrors.endDateTime }"
              :min="minDate"
              required
            />
          </div>

          <div class="form-group">
            <label for="endTime">End Time *</label>
            <input
              id="endTime"
              v-model="endTime"
              type="time"
              class="form-input"
              :class="{ error: formErrors.endDateTime }"
              required
            />
          </div>
        </div>

        <div v-if="formErrors.startDateTime" class="error-message">
          {{ formErrors.startDateTime }}
        </div>
        <div v-if="formErrors.endDateTime" class="error-message">
          {{ formErrors.endDateTime }}
        </div>

        <div v-if="timeConflict" class="time-conflict-warning">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
          >
            <circle
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="2"
            />
            <line
              x1="12"
              y1="8"
              x2="12"
              y2="12"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <line
              x1="12"
              y1="16"
              x2="12.01"
              y2="16"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <span>End time must be after start time</span>
        </div>

        <!-- Meeting Status Info -->
        <div class="meeting-status-info">
          <div class="status-item">
            <span class="status-label">Payment Status:</span>
            <div class="payment-status-container">
              <div
                class="payment-icon"
                :class="getPaymentStatusClass(meeting.payment?.status)"
              >
                <svg
                  v-if="meeting.payment?.status === 'COMPLETED'"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                >
                  <path
                    d="M22 11.08V12A10 10 0 1 1 5.68 3.57"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M22 4L12 14.01L9 11.01"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                <svg
                  v-else-if="meeting.payment?.status === 'PENDING'"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                >
                  <circle
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                  <polyline
                    points="12,6 12,12 16,14"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                <svg
                  v-else
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                >
                  <circle
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                  <line
                    x1="15"
                    y1="9"
                    x2="9"
                    y2="15"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                  <line
                    x1="9"
                    y1="9"
                    x2="15"
                    y2="15"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                </svg>
              </div>
              <span
                class="payment-status-text"
                :class="getPaymentStatusClass(meeting.payment?.status)"
              >
                {{ getPaymentStatusText(meeting.payment?.status) }}
              </span>
              <span v-if="meeting.payment?.amount" class="payment-amount"
                >৳{{ meeting.payment.amount }}</span
              >
            </div>
          </div>
          <div class="status-item">
            <span class="status-label">Meeting Status:</span>
            <span class="status-value">{{ getMeetingStatus() }}</span>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="closeModal" class="btn btn-outline">
            Cancel
          </button>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="isSubmitting || timeConflict || !canEdit"
          >
            <span v-if="isSubmitting" class="loading-spinner"></span>
            {{ isSubmitting ? "Updating..." : "Update Meeting" }}
          </button>
        </div>

        <div v-if="!canEdit" class="edit-restriction-notice">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
          >
            <circle
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="2"
            />
            <line
              x1="12"
              y1="8"
              x2="12"
              y2="12"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <line
              x1="12"
              y1="16"
              x2="12.01"
              y2="16"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <span>{{ getEditRestrictionMessage() }}</span>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { meetingAPI } from "../services/api";

const props = defineProps({
  meeting: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["close", "meeting-updated"]);

// State
const isSubmitting = ref(false);
const formErrors = reactive({});

// Form data
const meetingForm = reactive({
  meetingId: "",
  startDateTime: "",
  endDateTime: "",
});

// Separate date and time inputs for better UX
const startDate = ref("");
const startTime = ref("");
const endDate = ref("");
const endTime = ref("");

// Computed properties
const minDate = computed(() => {
  const today = new Date();
  return today.toISOString().split("T")[0];
});

const timeConflict = computed(() => {
  if (!meetingForm.startDateTime || !meetingForm.endDateTime) return false;
  return (
    new Date(meetingForm.startDateTime) >= new Date(meetingForm.endDateTime)
  );
});

const canEdit = computed(() => {
  const now = new Date();
  const startTime = new Date(props.meeting.startTimestamp);

  // Can't edit if meeting has already started or if it's paid
  return now < startTime && !props.meeting.isPaid;
});

// Watchers to combine date and time inputs
watch([startDate, startTime], ([date, time]) => {
  if (date && time) {
    meetingForm.startDateTime = `${date}T${time}:00`;
  }
});

watch([endDate, endTime], ([date, time]) => {
  if (date && time) {
    meetingForm.endDateTime = `${date}T${time}:00`;
  }
});

// Methods
const closeModal = () => {
  emit("close");
};

const getClientName = (client) => {
  if (!client) return "Unknown Client";
  return (
    `${client.firstName || ""} ${client.lastName || ""}`.trim() ||
    client.email ||
    "Unknown Client"
  );
};

const getClientInitials = (client) => {
  if (!client) return "?";
  const firstName = client.firstName || "";
  const lastName = client.lastName || "";
  if (firstName && lastName) {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`;
  }
  if (firstName) return firstName.charAt(0);
  if (client.email) return client.email.charAt(0).toUpperCase();
  return "?";
};

const getMeetingStatus = () => {
  const now = new Date();
  const startTime = new Date(props.meeting.startTimestamp);
  const endTime = new Date(props.meeting.endTimestamp);

  if (now < startTime) {
    return "Upcoming";
  } else if (now >= startTime && now <= endTime) {
    return "In Progress";
  } else {
    return "Ended";
  }
};

const getPaymentStatusClass = (status) => {
  return status?.toLowerCase() || "unknown";
};

const getPaymentStatusText = (status) => {
  switch (status) {
    case "COMPLETED":
      return "Paid";
    case "PENDING":
      return "Pending";
    case "FAILED":
      return "Failed";
    default:
      return "No Payment";
  }
};

const getEditRestrictionMessage = () => {
  if (props.meeting.isPaid) {
    return "Cannot edit paid meetings";
  }
  if (getMeetingStatus() !== "Upcoming") {
    return "Cannot edit meetings that have already started";
  }
  return "Meeting cannot be edited";
};

const validateForm = () => {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);

  if (!meetingForm.startDateTime) {
    formErrors.startDateTime = "Start date and time are required";
  }

  if (!meetingForm.endDateTime) {
    formErrors.endDateTime = "End date and time are required";
  }

  if (timeConflict.value) {
    formErrors.endDateTime = "End time must be after start time";
  }

  // Check if meeting is in the past
  if (
    meetingForm.startDateTime &&
    new Date(meetingForm.startDateTime) <= new Date()
  ) {
    formErrors.startDateTime = "Meeting cannot be scheduled in the past";
  }

  return Object.keys(formErrors).length === 0;
};

const submitMeeting = async () => {
  if (!validateForm() || !canEdit.value) {
    return;
  }

  isSubmitting.value = true;

  try {
    // Convert to ISO format for API
    const meetingData = {
      meetingId: meetingForm.meetingId,
      startDateTime: new Date(meetingForm.startDateTime).toISOString(),
      endDateTime: new Date(meetingForm.endDateTime).toISOString(),
    };

    await meetingAPI.updateMeeting(meetingData);
    emit("meeting-updated");
  } catch (err) {
    console.error("Error updating meeting:", err);

    if (err.response?.data?.error?.message) {
      // Handle specific API errors
      const errorMessage = err.response.data.error.message;
      if (errorMessage.includes("time")) {
        formErrors.startDateTime = errorMessage;
      } else {
        alert(`Error: ${errorMessage}`);
      }
    } else {
      alert("An error occurred while updating the meeting. Please try again.");
    }
  } finally {
    isSubmitting.value = false;
  }
};

// Initialize form with meeting data
const initializeForm = () => {
  meetingForm.meetingId = props.meeting.id;

  // Parse existing meeting times
  const startDateTime = new Date(props.meeting.startTimestamp);
  const endDateTime = new Date(props.meeting.endTimestamp);

  startDate.value = startDateTime.toISOString().split("T")[0];
  startTime.value = startDateTime.toTimeString().slice(0, 5);
  endDate.value = endDateTime.toISOString().split("T")[0];
  endTime.value = endDateTime.toTimeString().slice(0, 5);
};

// Initialize form when component mounts
onMounted(() => {
  initializeForm();
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  max-width: 600px;
  width: 100%;
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
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.meeting-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

label {
  display: block;
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
}

.form-input.error {
  border-color: var(--color-error);
}

.form-input:disabled {
  background: var(--color-background-soft);
  color: var(--color-text-muted);
  cursor: not-allowed;
}

.client-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
}

.client-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.client-name {
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.client-email {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.meeting-status-info {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1rem;
  margin-bottom: 1rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-label {
  font-weight: 500;
  color: var(--color-text);
}

.status-value {
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  text-transform: uppercase;
}

.status-value.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-value.completed {
  background: #dcfce7;
  color: #166534;
}

.status-value.failed {
  background: #fee2e2;
  color: #dc2626;
}

.payment-status-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.payment-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  padding: 0.25rem;
}

.payment-icon.completed {
  background: #dcfce7;
  color: #166534;
}

.payment-icon.pending {
  background: #fef3c7;
  color: #92400e;
}

.payment-icon.failed {
  background: #fee2e2;
  color: #dc2626;
}

.payment-icon.unknown {
  background: #f3f4f6;
  color: #6b7280;
}

.payment-status-text {
  font-size: 0.875rem;
  font-weight: 500;
}

.payment-status-text.completed {
  color: #166534;
}

.payment-status-text.pending {
  color: #92400e;
}

.payment-status-text.failed {
  color: #dc2626;
}

.payment-status-text.unknown {
  color: #6b7280;
}

.payment-amount {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 0.875rem;
  margin-left: auto;
}

.error-message {
  color: var(--color-error);
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.time-conflict-warning {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(var(--color-error-rgb), 0.1);
  border: 1px solid var(--color-error);
  border-radius: var(--border-radius);
  color: var(--color-error);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.edit-restriction-notice {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(var(--color-warning-rgb), 0.1);
  border: 1px solid var(--color-warning);
  border-radius: var(--border-radius);
  color: var(--color-warning);
  font-size: 0.875rem;
  margin-top: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>
