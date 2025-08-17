<template>
  <div class="notification-preferences">
    <!-- Loading State -->
    <div
      v-if="isLoading"
      class="preferences-loading"
      role="status"
      aria-live="polite"
    >
      <svg class="loading-spinner" viewBox="0 0 24 24" aria-hidden="true">
        <circle
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="2"
          fill="none"
          opacity="0.3"
        />
        <path
          d="M12 2a10 10 0 0 1 10 10"
          stroke="currentColor"
          stroke-width="2"
          fill="none"
        />
      </svg>
      <span>Loading notification preferences...</span>
    </div>

    <!-- Error State -->
    <div
      v-else-if="error"
      class="preferences-error"
      role="alert"
      aria-live="assertive"
    >
      <svg
        class="error-icon"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <path
          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <p>Failed to load notification preferences</p>
      <button
        @click="retryFetch"
        class="retry-btn"
        aria-label="Try loading notification preferences again"
      >
        Try again
      </button>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="preferences.length === 0"
      class="preferences-empty"
      role="status"
    >
      <svg
        class="empty-icon"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <path
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <p>No notification preferences available</p>
      <span>Notification preferences will be available soon</span>
    </div>

    <!-- Preferences List -->
    <div v-else class="preferences-list">
      <p class="preferences-description">
        Configure how you want to receive notifications. Toggle the switches
        below to enable or disable different notification methods.
      </p>

      <!-- Feedback Messages -->
      <div
        v-if="updateSuccess"
        class="update-success"
        role="status"
        aria-live="polite"
      >
        <svg
          class="success-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <path
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span>{{ updateSuccess }}</span>
      </div>

      <div
        v-if="updateError"
        class="update-error"
        role="alert"
        aria-live="assertive"
      >
        <svg
          class="error-icon"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <path
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span>{{ updateError }}</span>
      </div>

      <!-- Preference Items -->
      <div
        v-for="preference in preferences"
        :key="preference.type"
        class="preference-item"
        :class="{
          'preference-item--updating': updatingPreferences.includes(
            preference.type
          ),
        }"
      >
        <div class="preference-info">
          <h3 class="preference-type" :id="`preference-${preference.type}`">
            {{ formatPreferenceType(preference.type) }}
          </h3>
          <p class="preference-description">
            {{ getPreferenceDescription(preference.type) }}
          </p>
        </div>
        <div class="preference-toggles">
          <div class="toggle-group">
            <label class="toggle-label" :for="`email-${preference.type}`">
              Email
            </label>
            <div class="toggle-switch">
              <input
                :id="`email-${preference.type}`"
                type="checkbox"
                v-model="preference.emailEnabled"
                @change="
                  updatePreference(preference.type, {
                    emailEnabled: preference.emailEnabled,
                    webPushEnabled: preference.webPushEnabled,
                  })
                "
                :disabled="updatingPreferences.includes(preference.type)"
                :aria-labelledby="`preference-${preference.type}`"
                aria-describedby="email-description"
              />
              <span class="toggle-slider"></span>
            </div>
          </div>
          <div class="toggle-group">
            <label class="toggle-label" :for="`webpush-${preference.type}`">
              Browser
            </label>
            <div class="toggle-switch">
              <input
                :id="`webpush-${preference.type}`"
                type="checkbox"
                v-model="preference.webPushEnabled"
                @change="
                  updatePreference(preference.type, {
                    emailEnabled: preference.emailEnabled,
                    webPushEnabled: preference.webPushEnabled,
                  })
                "
                :disabled="updatingPreferences.includes(preference.type)"
                :aria-labelledby="`preference-${preference.type}`"
                aria-describedby="browser-description"
              />
              <span class="toggle-slider"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Hidden descriptions for screen readers -->
      <div class="sr-only" id="email-description">
        Toggle to enable or disable email notifications for this type of event
      </div>
      <div class="sr-only" id="browser-description">
        Toggle to enable or disable browser notifications for this type of event
      </div>
    </div>
  </div>
</template>

<script setup>
import { useNotificationStore } from "@/stores/notification";
import { computed, onMounted, ref } from "vue";

// Store
const notificationStore = useNotificationStore();

// Refs
const updatingPreferences = ref([]);
const updateSuccess = ref("");
const updateError = ref("");
const successTimeout = ref(null);
const errorTimeout = ref(null);

// Computed properties
const preferences = computed(() => notificationStore.preferences);
const isLoading = computed(() => notificationStore.isLoading);
const error = computed(() => notificationStore.error);

// Methods
const retryFetch = async () => {
  notificationStore.clearError();
  await fetchPreferences();
};

const fetchPreferences = async () => {
  await notificationStore.fetchPreferences();
};

const updatePreference = async (type, preferenceData) => {
  // Clear previous messages
  clearMessages();

  // Add to updating list
  updatingPreferences.value.push(type);

  try {
    const result = await notificationStore.updatePreference(
      type,
      preferenceData
    );

    if (result && result.success) {
      updateSuccess.value = "Notification preference updated successfully";
      successTimeout.value = setTimeout(() => {
        updateSuccess.value = "";
      }, 3000);
    } else {
      // Show error message
      updateError.value =
        result?.message || "Failed to update notification preference";
      errorTimeout.value = setTimeout(() => {
        updateError.value = "";
      }, 5000);
    }
  } catch (err) {
    console.error("Error updating preference:", err);
    updateError.value =
      err.message || "An error occurred while updating preference";
    errorTimeout.value = setTimeout(() => {
      updateError.value = "";
    }, 5000);
  } finally {
    // Remove from updating list
    const index = updatingPreferences.value.indexOf(type);
    if (index !== -1) {
      updatingPreferences.value.splice(index, 1);
    }
  }
};

const clearMessages = () => {
  if (successTimeout.value) {
    clearTimeout(successTimeout.value);
    successTimeout.value = null;
  }

  if (errorTimeout.value) {
    clearTimeout(errorTimeout.value);
    errorTimeout.value = null;
  }

  updateSuccess.value = "";
  updateError.value = "";
};

const formatPreferenceType = (type) => {
  // Convert SNAKE_CASE to Title Case with spaces
  return type
    .split("_")
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(" ");
};

const getPreferenceDescription = (type) => {
  // Return a user-friendly description based on notification type
  const descriptions = {
    CASE_CREATE: "Notifications when a new case is created",
    EVENT_ADD: "Notifications when events are added",
    SCHEDULE_REMINDER: "Reminders for upcoming appointments",
    DOC_UPLOAD: "Notifications when documents are uploaded",
    NOTE_CREATE: "Notifications when notes are created",
  };

  return descriptions[type] || "Notifications for this event type";
};

// Lifecycle
onMounted(async () => {

  // If preferences are not already loaded, fetch them
  if (preferences.value.length === 0) {
    await fetchPreferences();
  }
});
</script>

<style scoped>
.notification-preferences {
  padding: 1rem 0;
}

.preferences-loading,
.preferences-empty,
.preferences-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  text-align: center;
  color: var(--color-text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  margin-bottom: 1rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-icon,
.error-icon,
.success-icon {
  width: 40px;
  height: 40px;
  margin-bottom: 1rem;
  opacity: 0.7;
}

.preferences-empty p,
.preferences-error p {
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.preferences-description {
  margin-bottom: 1.5rem;
  color: var(--color-text-muted);
  font-size: 0.95rem;
  line-height: 1.5;
}

.retry-btn {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: var(--color-background);
  border: none;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.retry-btn:hover {
  background: var(--color-secondary);
  transform: translateY(-1px);
}

.update-success,
.update-error {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  margin-bottom: 1.5rem;
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  animation: fadeIn 0.3s ease;
}

.update-success {
  background: rgba(var(--color-success-rgb, 34, 197, 94), 0.1);
  border: 1px solid rgba(var(--color-success-rgb, 34, 197, 94), 0.2);
  color: var(--color-success, #22c55e);
}

.update-error {
  background: rgba(var(--color-error-rgb, 239, 68, 68), 0.1);
  border: 1px solid rgba(var(--color-error-rgb, 239, 68, 68), 0.2);
  color: var(--color-error, #ef4444);
}

.update-success .success-icon,
.update-error .error-icon {
  width: 20px;
  height: 20px;
  margin-right: 0.75rem;
  margin-bottom: 0;
}

.preferences-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preference-item {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

@media (min-width: 640px) {
  .preference-item {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.preference-item:hover {
  background: var(--color-background-mute);
}

.preference-item--updating {
  opacity: 0.7;
  pointer-events: none;
}

.preference-info {
  flex: 1;
}

.preference-type {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.25rem 0;
}

.preference-description {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
  line-height: 1.4;
}

.preference-toggles {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.toggle-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.toggle-label {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  margin: 0;
  cursor: pointer;
  z-index: 1;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--color-background-mute);
  transition: 0.3s;
  border-radius: 24px;
  border: 1px solid var(--color-border);
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 2px;
  bottom: 2px;
  background-color: var(--color-background);
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: var(--shadow-sm);
}

input:checked + .toggle-slider {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

input:focus + .toggle-slider {
  box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb, 59, 130, 246), 0.3);
}

input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

input:disabled + .toggle-slider {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .preference-toggles {
    width: 100%;
    justify-content: space-around;
    margin-top: 0.5rem;
  }

  .preference-item {
    padding: 1rem;
  }

  .preference-type {
    font-size: 0.9375rem;
  }

  .preference-description {
    font-size: 0.8125rem;
  }

  .toggle-switch {
    width: 48px; /* Slightly larger for better touch targets */
    height: 26px;
  }

  .toggle-slider:before {
    height: 20px;
    width: 20px;
  }

  input:checked + .toggle-slider:before {
    transform: translateX(22px);
  }

  .toggle-group {
    padding: 0.25rem;
  }

  .retry-btn {
    padding: 0.75rem 1.25rem; /* Larger touch target */
  }
}

/* Accessibility improvements */
@media (prefers-reduced-motion: reduce) {
  .toggle-slider,
  .toggle-slider:before,
  .preference-item,
  .retry-btn {
    transition: none;
  }

  .loading-spinner {
    animation: none;
  }

  .update-success,
  .update-error {
    animation: none;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .toggle-slider {
    border-width: 2px;
  }

  .preference-item {
    border-width: 2px;
  }

  input:checked + .toggle-slider {
    background-color: var(--color-primary);
  }
}

/* Screen reader only class */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>
