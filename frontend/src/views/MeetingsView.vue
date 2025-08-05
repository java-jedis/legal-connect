<template>
  <div class="meetings-view">
    <!-- Header Section -->
    <section class="meetings-header">
      <div class="container">
        <div class="header-content">
          <div class="header-info">
            <h1>Meeting Management</h1>
            <p>Schedule and manage your video consultations with clients</p>
          </div>
          <div class="header-actions">
            <button @click="showScheduleModal = true" class="btn btn-primary">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
              >
                <line
                  x1="12"
                  y1="5"
                  x2="12"
                  y2="19"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <line
                  x1="5"
                  y1="12"
                  x2="19"
                  y2="12"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              Schedule Meeting
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Meetings Content -->
    <section class="meetings-content">
      <div class="container">
        <!-- Loading State -->
        <div v-if="isLoading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Loading meetings...</p>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="error-state">
          <div class="error-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
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
          <h3>Unable to load meetings</h3>
          <p>{{ error }}</p>
          <button @click="fetchMeetings" class="btn btn-primary">
            Try Again
          </button>
        </div>

        <!-- Meetings List -->
        <div v-else class="meetings-list">
          <!-- Empty State -->
          <div v-if="meetings.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect
                  x="2"
                  y="3"
                  width="20"
                  height="14"
                  rx="2"
                  ry="2"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <line
                  x1="8"
                  y1="21"
                  x2="16"
                  y2="21"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <line
                  x1="12"
                  y1="17"
                  x2="12"
                  y2="21"
                  stroke="currentColor"
                  stroke-width="2"
                />
              </svg>
            </div>
            <h3>No meetings scheduled</h3>
            <p>
              You haven't scheduled any meetings yet. Create your first meeting
              to get started.
            </p>
            <button @click="showScheduleModal = true" class="btn btn-primary">
              Schedule Your First Meeting
            </button>
          </div>

          <!-- Meetings Grid -->
          <div v-else class="meetings-grid">
            <div
              v-for="meeting in meetings"
              :key="meeting.id"
              class="meeting-card"
            >
              <div class="meeting-header">
                <div class="meeting-header-left">
                  <div
                    class="meeting-status"
                    :class="getMeetingStatusClass(meeting)"
                  >
                    {{ getMeetingStatus(meeting) }}
                  </div>
                  <div class="meeting-time-badge">
                    {{ formatTime(meeting.startTimestamp) }}
                  </div>
                </div>
                <div class="meeting-actions">
                  <button
                    @click="editMeeting(meeting)"
                    class="btn-icon"
                    title="Edit meeting"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                    >
                      <path
                        d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <path
                        d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                  </button>
                  <button
                    @click="deleteMeeting(meeting)"
                    class="btn-icon delete"
                    title="Delete meeting"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                    >
                      <polyline
                        points="3,6 5,6 21,6"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <path
                        d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2V6"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                  </button>
                </div>
              </div>

              <div class="meeting-info">
                <h3 class="meeting-title">
                  {{ meeting.roomName || "Video Consultation" }}
                </h3>

                <div class="meeting-client">
                  <div class="client-avatar">
                    <span>{{ getClientInitials(meeting.client) }}</span>
                  </div>
                  <div class="client-info">
                    <p class="client-name">
                      {{ getClientName(meeting.client) }}
                    </p>
                    <p class="client-email">{{ meeting.client?.email }}</p>
                  </div>
                </div>

                <div class="meeting-time">
                  <div class="time-info">
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
                      <polyline
                        points="12,6 12,12 16,14"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                    <span>{{ formatDateTime(meeting.startTimestamp) }}</span>
                  </div>
                  <div class="duration-info">
                    <span
                      >Duration:
                      {{
                        calculateDuration(
                          meeting.startTimestamp,
                          meeting.endTimestamp
                        )
                      }}</span
                    >
                  </div>
                </div>

                <div class="meeting-payment" v-if="meeting.payment">
                  <div class="payment-info">
                    <div class="payment-status-container">
                      <div
                        class="payment-icon"
                        :class="getPaymentStatusClass(meeting.payment.status)"
                      >
                        <!-- Success icon for PAID, COMPLETED, RELEASED -->
                        <svg
                          v-if="meeting.payment.status === 'PAID' || meeting.payment.status === 'COMPLETED' || meeting.payment.status === 'RELEASED'"
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
                        <!-- Clock icon for PENDING -->
                        <svg
                          v-else-if="meeting.payment.status === 'PENDING'"
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
                        <!-- Refresh icon for REFUNDED -->
                        <svg
                          v-else-if="meeting.payment.status === 'REFUNDED'"
                          viewBox="0 0 24 24"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                          width="16"
                          height="16"
                        >
                          <path
                            d="M23 4v6h-6"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                          />
                          <path
                            d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                          />
                        </svg>
                        <!-- X icon for CANCELED, FAILED, or other error states -->
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
                        :class="getPaymentStatusClass(meeting.payment.status)"
                      >
                        {{ getPaymentStatusText(meeting.payment.status) }}
                      </span>
                      <span class="payment-amount"
                        >৳{{ meeting.payment.amount }}</span
                      >
                    </div>
                  </div>
                </div>
              </div>

              <div class="meeting-footer">
                <button
                  v-if="canJoinMeeting(meeting)"
                  @click="joinMeeting(meeting)"
                  class="btn btn-primary btn-sm"
                >
                  Join Meeting
                </button>
                <button
                  v-else-if="getMeetingStatus(meeting) === 'Upcoming'"
                  class="btn btn-outline btn-sm"
                  disabled
                >
                  Meeting Not Started
                </button>
                <button
                  v-else-if="getMeetingStatus(meeting) === 'Ended'"
                  class="btn btn-outline btn-sm"
                  disabled
                >
                  Meeting Ended
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Schedule Meeting Modal -->
    <ScheduleMeetingModal
      v-if="showScheduleModal"
      @close="showScheduleModal = false"
      @meeting-scheduled="onMeetingScheduled"
    />

    <!-- Edit Meeting Modal -->
    <EditMeetingModal
      v-if="showEditModal && selectedMeeting"
      :meeting="selectedMeeting"
      @close="showEditModal = false"
      @meeting-updated="onMeetingUpdated"
    />

    <!-- Video Call Modal -->
    <VideoCallModal
      v-if="showVideoCall && selectedMeeting && videoCallRoomName"
      :meeting="selectedMeeting"
      :roomName="videoCallRoomName"
      @close="closeVideoCall"
    />

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content delete-modal" @click.stop>
        <div class="modal-header">
          <h3>Delete Meeting</h3>
          <button @click="closeDeleteModal" class="modal-close">
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

        <div class="delete-content">
          <div class="delete-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
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
          <h4>Are you sure?</h4>
          <p>
            This will permanently delete the meeting with
            <strong>{{ getClientName(meetingToDelete?.client) }}</strong>
            scheduled for
            <strong>{{
              formatDateTime(meetingToDelete?.startTimestamp)
            }}</strong
            >.
          </p>
          <p>This action cannot be undone.</p>

          <div class="delete-actions">
            <button @click="closeDeleteModal" class="btn btn-outline">
              Cancel
            </button>
            <button
              @click="confirmDelete"
              class="btn btn-danger"
              :disabled="isDeleting"
            >
              <span v-if="isDeleting" class="loading-spinner"></span>
              {{ isDeleting ? "Deleting..." : "Delete Meeting" }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { meetingAPI } from "../services/api";
import { useAuthStore } from "../stores/auth";
import ScheduleMeetingModal from "../components/ScheduleMeetingModal.vue";
import EditMeetingModal from "../components/EditMeetingModal.vue";
import VideoCallModal from "../components/VideoCallModal.vue";

// Initialize auth store
const authStore = useAuthStore();

// State
const meetings = ref([]);
const isLoading = ref(false);
const error = ref("");
const showScheduleModal = ref(false);
const showEditModal = ref(false);
const showDeleteModal = ref(false);
const showVideoCall = ref(false);
const selectedMeeting = ref(null);
const meetingToDelete = ref(null);
const isDeleting = ref(false);
const videoCallRoomName = ref("");

// Methods
const fetchMeetings = async () => {
  isLoading.value = true;
  error.value = "";

  try {
    const response = await meetingAPI.getMeetings();
    const fetchedMeetings = response.data || [];
    meetings.value = sortMeetings(fetchedMeetings);
  } catch (err) {
    console.error("Error fetching meetings:", err);
    error.value =
      err.response?.data?.error?.message || "Failed to load meetings";
  } finally {
    isLoading.value = false;
  }
};

const editMeeting = (meeting) => {
  selectedMeeting.value = meeting;
  showEditModal.value = true;
};

const deleteMeeting = (meeting) => {
  meetingToDelete.value = meeting;
  showDeleteModal.value = true;
};

const closeDeleteModal = () => {
  showDeleteModal.value = false;
  meetingToDelete.value = null;
};

const confirmDelete = async () => {
  if (!meetingToDelete.value) return;

  isDeleting.value = true;

  try {
    await meetingAPI.deleteMeeting(meetingToDelete.value.id);
    closeDeleteModal();
    await fetchMeetings();
  } catch (err) {
    console.error("Error deleting meeting:", err);

    if (err.response?.data?.error?.message) {
      alert(`Error: ${err.response.data.error.message}`);
    } else {
      alert("An error occurred while deleting the meeting. Please try again.");
    }
  } finally {
    isDeleting.value = false;
  }
};

const onMeetingScheduled = async () => {
  showScheduleModal.value = false;
  await fetchMeetings();
};

const onMeetingUpdated = async () => {
  showEditModal.value = false;
  selectedMeeting.value = null;
  await fetchMeetings();
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

const formatDateTime = (dateTime) => {
  if (!dateTime) return "";
  const date = new Date(dateTime);
  return date.toLocaleString("en-US", {
    weekday: "short",
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const formatTime = (dateTime) => {
  if (!dateTime) return "";
  const date = new Date(dateTime);
  return date.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
  });
};

const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return "";

  const start = new Date(startTime);
  const end = new Date(endTime);
  const diffMs = end - start;
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));

  if (diffHours === 0) {
    return `${diffMinutes} min`;
  } else if (diffMinutes === 0) {
    return `${diffHours} hr`;
  } else {
    return `${diffHours} hr ${diffMinutes} min`;
  }
};

const getMeetingStatus = (meeting) => {
  const now = new Date();
  const startTime = new Date(meeting.startTimestamp);
  const endTime = new Date(meeting.endTimestamp);

  if (now < startTime) {
    return "Upcoming";
  } else if (now >= startTime && now <= endTime) {
    return "In Progress";
  } else {
    return "Ended";
  }
};

const getMeetingStatusClass = (meeting) => {
  const status = getMeetingStatus(meeting);
  return status.toLowerCase().replace(" ", "-");
};

const canJoinMeeting = (meeting) => {
  const status = getMeetingStatus(meeting);

  // If meeting is not in progress, no one can join
  if (status !== "In Progress") {
    return false;
  }

  // Check user role
  const userRole = authStore.userInfo?.role;

  if (userRole === "LAWYER") {
    // Lawyers can join any meeting that's in progress
    return true;
  } else {
    // Clients/users need the meeting to be paid AND in progress
    const isPaid = meeting.isPaid || meeting.payment?.status === "COMPLETED";
    return isPaid;
  }
};

const getPaymentStatusClass = (status) => {
  switch (status) {
    case "PAID":
    case "COMPLETED":
      return "paid";
    case "PENDING":
      return "pending";
    case "FAILED":
      return "failed";
    case "RELEASED":
      return "released";
    case "REFUNDED":
      return "refunded";
    case "CANCELED":
      return "canceled";
    default:
      return "unknown";
  }
};

const getPaymentStatusText = (status) => {
  switch (status) {
    case "PAID":
    case "COMPLETED":
      return "Paid";
    case "PENDING":
      return "Pending";
    case "FAILED":
      return "Failed";
    case "RELEASED":
      return "Released";
    case "REFUNDED":
      return "Refunded";
    case "CANCELED":
      return "Canceled";
    default:
      return "Unknown";
  }
};

const joinMeeting = async (meeting) => {
  try {
    const response = await meetingAPI.generateMeetingToken(meeting.id);

    // Set the meeting data and room name for the video call
    selectedMeeting.value = meeting;
    videoCallRoomName.value = response.data.roomName;
    showVideoCall.value = true;
  } catch (err) {
    console.error("Error generating meeting token:", err);

    if (err.response?.data?.error?.message) {
      alert(`Error: ${err.response.data.error.message}`);
    } else {
      alert("Failed to join meeting. Please try again.");
    }
  }
};

const closeVideoCall = () => {
  showVideoCall.value = false;
  selectedMeeting.value = null;
  videoCallRoomName.value = "";
};

// Sort meetings by start time (latest first for upcoming meetings)
const sortMeetings = (meetings) => {
  return meetings.sort((a, b) => {
    const aTime = new Date(a.startTimestamp);
    const bTime = new Date(b.startTimestamp);
    const now = new Date();

    // Separate upcoming and past meetings
    const aIsUpcoming = aTime > now;
    const bIsUpcoming = bTime > now;

    if (aIsUpcoming && bIsUpcoming) {
      // For upcoming meetings, show earliest first
      return aTime - bTime;
    } else if (!aIsUpcoming && !bIsUpcoming) {
      // For past meetings, show latest first
      return bTime - aTime;
    } else {
      // Upcoming meetings come first
      return bIsUpcoming ? 1 : -1;
    }
  });
};

// Load meetings on mount
onMounted(() => {
  fetchMeetings();
});
</script>

<style scoped>
.meetings-view {
  min-height: 100vh;
  background: var(--color-background);
}

.meetings-header {
  background: linear-gradient(
    135deg,
    var(--color-primary),
    var(--color-secondary)
  );
  color: var(--color-background);
  padding: 2rem 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info h1 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.header-info p {
  font-size: 1.125rem;
  opacity: 0.9;
}

.header-actions .btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.meetings-content {
  padding: 2rem 0;
}

.loading-state,
.error-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-state .error-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-error);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--color-background-soft);
  border-radius: var(--border-radius-lg);
  margin: 2rem 0;
}

.empty-state .empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-text-muted);
}

.meetings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.meeting-card {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: all 0.2s ease;
}

.meeting-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
}

.meeting-header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.meeting-time-badge {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-primary);
}

.meeting-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.meeting-status.upcoming {
  background: #fef3c7;
  color: #92400e;
}

.meeting-status.in-progress {
  background: #dcfce7;
  color: #166534;
}

.meeting-status.ended {
  background: #f3f4f6;
  color: #6b7280;
}

.meeting-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  transition: all 0.2s ease;
}

.btn-icon:hover {
  background: var(--color-background);
  color: var(--color-text);
}

.btn-icon.delete:hover {
  background: var(--color-error);
  color: white;
}

.meeting-info {
  padding: 1.5rem;
}

.meeting-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.meeting-client {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
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

.meeting-time {
  margin-bottom: 1rem;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: var(--color-text);
}

.duration-info {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.meeting-payment {
  margin-bottom: 1rem;
}

.payment-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-amount {
  font-weight: 600;
  color: var(--color-heading);
}

.payment-status {
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
}

.payment-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.payment-status.completed {
  background: #dcfce7;
  color: #166534;
}

.payment-status.failed {
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
  width: 24px;
  height: 24px;
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

.payment-icon.paid {
  background: #dcfce7;
  color: #166534;
}

.payment-icon.released {
  background: #dcfce7;
  color: #166534;
}

.payment-icon.refunded {
  background: #e0f2fe;
  color: #0369a1;
}

.payment-icon.canceled {
  background: #fee2e2;
  color: #dc2626;
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

.payment-status-text.paid {
  color: #166534;
}

.payment-status-text.released {
  color: #166534;
}

.payment-status-text.refunded {
  color: #0369a1;
}

.payment-status-text.canceled {
  color: #dc2626;
}

.payment-amount {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 0.875rem;
  margin-left: auto;
}

.meeting-footer {
  padding: 1rem 1.5rem;
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

/* Modal Styles */
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
  max-width: 400px;
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

.delete-content {
  padding: 1.5rem;
  text-align: center;
}

.delete-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
  color: var(--color-error);
}

.delete-content h4 {
  margin: 0 0 1rem 0;
  color: var(--color-heading);
}

.delete-content p {
  margin: 0 0 1rem 0;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.delete-content p:last-of-type {
  margin-bottom: 2rem;
}

.delete-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-danger {
  background: var(--color-error);
  color: white;
  border: 1px solid var(--color-error);
}

.btn-danger:hover {
  background: #c53030;
  border-color: #c53030;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .meetings-grid {
    grid-template-columns: 1fr;
  }

  .delete-actions {
    flex-direction: column;
  }
}
</style>
