<template>
  <div class="client-meetings-view">
    <!-- Header Section -->
    <section class="meetings-header">
      <div class="container">
        <div class="header-content">
          <div class="header-info">
            <h1>My Meetings</h1>
            <p>View and manage your scheduled consultations with lawyers</p>
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
              You don't have any meetings scheduled yet. Your lawyer will
              schedule meetings with you.
            </p>
          </div>

          <!-- Meetings Grid -->
          <div v-else class="meetings-grid">
            <div
              v-for="meeting in meetings"
              :key="meeting.id"
              class="meeting-card"
            >
              <div class="meeting-header">
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

              <div class="meeting-info">
                <h3 class="meeting-title">
                  {{ meeting.roomName || "Legal Consultation" }}
                </h3>

                <div class="meeting-lawyer">
                  <div class="lawyer-avatar">
                    <span>{{ getLawyerInitials(meeting.lawyer) }}</span>
                  </div>
                  <div class="lawyer-info">
                    <p class="lawyer-name">
                      {{ getLawyerName(meeting.lawyer) }}
                    </p>
                    <p class="lawyer-title">Legal Consultant</p>
                  </div>
                </div>

                <div class="meeting-details">
                  <div class="detail-item">
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
                  <div class="detail-item">
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
                      <path
                        d="M12 6v6l4 2"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                      />
                    </svg>
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

                <!-- Payment Information -->
                <div class="payment-section" v-if="meeting.payment">
                  <div class="payment-status-display">
                    <div
                      class="payment-icon"
                      :class="getPaymentStatusClass(meeting.payment.status)"
                    >
                      <!-- Success icon for PAID, COMPLETED, RELEASED -->
                      <svg
                        v-if="
                          meeting.payment.status === 'PAID' ||
                          meeting.payment.status === 'COMPLETED' ||
                          meeting.payment.status === 'RELEASED'
                        "
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
                    <div class="payment-details">
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

                  <!-- Payment Release Information -->
                  <div
                    v-if="meeting.payment.status === 'PAID'"
                    class="payment-release-info"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      width="14"
                      height="14"
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
                    <span
                      >Payment will be released at
                      {{ formatReleaseTime(meeting.endTimestamp) }}</span
                    >
                  </div>
                </div>
              </div>

              <!-- Meeting Actions -->
              <div class="meeting-footer">
                <!-- Payment Required State -->
                <div
                  v-if="needsPayment(meeting)"
                  class="payment-required-actions"
                >
                  <button
                    @click="payForMeeting(meeting)"
                    class="btn btn-primary btn-sm payment-btn"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                    >
                      <rect
                        x="1"
                        y="4"
                        width="22"
                        height="16"
                        rx="2"
                        ry="2"
                        stroke="currentColor"
                        stroke-width="2"
                      />
                      <line
                        x1="1"
                        y1="10"
                        x2="23"
                        y2="10"
                        stroke="currentColor"
                        stroke-width="2"
                      />
                    </svg>
                    <span>Pay ৳{{ meeting.payment?.amount }}</span>
                  </button>
                </div>

                <!-- Payment Completed State - PAID (shows join + management buttons) -->
                <div
                  v-else-if="meeting.payment?.status === 'PAID'"
                  class="payment-completed-actions"
                >
                  <!-- Join Meeting Button -->
                  <button
                    v-if="canJoinMeeting(meeting)"
                    @click="joinMeeting(meeting)"
                    class="btn btn-primary btn-sm"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                    >
                      <polygon
                        points="5,3 19,12 5,21"
                        stroke="currentColor"
                        stroke-width="2"
                        fill="currentColor"
                      />
                    </svg>
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

                  <!-- Payment Management Buttons -->
                  <div class="payment-management-buttons">
                    <button
                      @click="cancelPayment(meeting)"
                      class="btn btn-outline btn-sm btn-warning"
                    >
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                        width="14"
                        height="14"
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
                      Cancel
                    </button>
                    <button
                      @click="releasePayment(meeting)"
                      class="btn btn-outline btn-sm btn-success"
                    >
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                        width="14"
                        height="14"
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
                      Release
                    </button>
                  </div>
                </div>

                <!-- Payment Released State - RELEASED (shows join only) -->
                <div
                  v-else-if="meeting.payment?.status === 'RELEASED'"
                  class="payment-released-actions"
                >
                  <!-- Join Meeting Button -->
                  <button
                    v-if="canJoinMeeting(meeting)"
                    @click="joinMeeting(meeting)"
                    class="btn btn-primary btn-sm"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                    >
                      <polygon
                        points="5,3 19,12 5,21"
                        stroke="currentColor"
                        stroke-width="2"
                        fill="currentColor"
                      />
                    </svg>
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

                <!-- Other Payment States -->
                <div v-else class="other-payment-states">
                  <span class="payment-status-message">
                    {{ getPaymentActionMessage(meeting) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Notification Banner -->
    <div
      v-if="notification.show"
      class="notification-banner"
      :class="notification.type"
    >
      <div class="notification-content">
        <div class="notification-icon">
          <svg
            v-if="notification.type === 'success'"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="20"
            height="20"
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
            v-else-if="notification.type === 'error'"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="20"
            height="20"
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
          <svg
            v-else
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="20"
            height="20"
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
        </div>
        <span class="notification-message">{{ notification.message }}</span>
        <button @click="hideNotification" class="notification-close">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
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
    </div>

    <!-- Release Payment Confirmation Modal -->
    <div
      v-if="showReleaseConfirmation"
      class="modal-overlay"
      @click="cancelReleasePayment"
    >
      <div class="confirmation-modal" @click.stop>
        <div class="modal-header">
          <h3>Release Payment</h3>
          <button @click="cancelReleasePayment" class="modal-close">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
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

        <div class="modal-body">
          <div class="warning-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              width="48"
              height="48"
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
          </div>

          <h4>Are you sure you want to release this payment to the lawyer?</h4>
          <p>
            This action cannot be undone. The payment of
            <strong>৳{{ selectedMeetingForRelease?.payment?.amount }}</strong>
            will be immediately transferred to the lawyer.
          </p>

          <div class="lawyer-info-preview" v-if="selectedMeetingForRelease">
            <div class="lawyer-avatar-small">
              <span>{{
                getLawyerInitials(selectedMeetingForRelease.lawyer)
              }}</span>
            </div>
            <div class="lawyer-details-small">
              <p class="lawyer-name-small">
                {{ getLawyerName(selectedMeetingForRelease.lawyer) }}
              </p>
              <p class="meeting-title-small">
                {{ selectedMeetingForRelease.roomName || "Legal Consultation" }}
              </p>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="cancelReleasePayment" class="btn btn-outline">
            Cancel
          </button>
          <button
            @click="confirmReleasePayment"
            class="btn btn-success"
            :disabled="isProcessingRelease"
          >
            <span v-if="isProcessingRelease">
              <svg
                class="spinner"
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
              </svg>
              Processing...
            </span>
            <span v-else> Release Payment </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Cancel Payment Confirmation Modal -->
    <div
      v-if="showCancelConfirmation"
      class="modal-overlay"
      @click="cancelCancelPayment"
    >
      <div class="confirmation-modal" @click.stop>
        <div class="modal-header">
          <h3>Cancel Payment</h3>
          <button @click="cancelCancelPayment" class="modal-close">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
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

        <div class="modal-body">
          <div class="warning-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              width="48"
              height="48"
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
          </div>

          <h4>Are you sure you want to cancel this payment?</h4>
          <p>
            This action cannot be undone. The payment of
            <strong>৳{{ selectedMeetingForCancel?.payment?.amount }}</strong>
            will be cancelled and refunded to your account.
          </p>

          <div class="lawyer-info-preview" v-if="selectedMeetingForCancel">
            <div class="lawyer-avatar-small">
              <span>{{
                getLawyerInitials(selectedMeetingForCancel.lawyer)
              }}</span>
            </div>
            <div class="lawyer-details-small">
              <p class="lawyer-name-small">
                {{ getLawyerName(selectedMeetingForCancel.lawyer) }}
              </p>
              <p class="meeting-title-small">
                {{ selectedMeetingForCancel.roomName || "Legal Consultation" }}
              </p>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="cancelCancelPayment" class="btn btn-outline">
            Keep Payment
          </button>
          <button
            @click="confirmCancelPayment"
            class="btn btn-warning"
            :disabled="isProcessingCancel"
          >
            <span v-if="isProcessingCancel">
              <svg
                class="spinner"
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
              </svg>
              Processing...
            </span>
            <span v-else> Cancel Payment </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Video Call Modal -->
    <VideoCallModal
      v-if="showVideoCall && selectedMeeting && videoCallRoomName"
      :meeting="selectedMeeting"
      :roomName="videoCallRoomName"
      @close="closeVideoCall"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { meetingAPI, paymentAPI } from "../services/api";

import VideoCallModal from "../components/VideoCallModal.vue";

// Initialize auth store

// State
const meetings = ref([]);
const isLoading = ref(false);
const error = ref("");
const showVideoCall = ref(false);
const selectedMeeting = ref(null);
const videoCallRoomName = ref("");

// Release payment confirmation state
const showReleaseConfirmation = ref(false);
const selectedMeetingForRelease = ref(null);
const isProcessingRelease = ref(false);

// Cancel payment confirmation state
const showCancelConfirmation = ref(false);
const selectedMeetingForCancel = ref(null);
const isProcessingCancel = ref(false);

// Notification state
const notification = ref({
  show: false,
  message: "",
  type: "success", // 'success', 'error', 'warning'
});

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

const getLawyerName = (lawyer) => {
  if (!lawyer) return "Unknown Lawyer";
  return (
    `${lawyer.firstName || ""} ${lawyer.lastName || ""}`.trim() ||
    lawyer.email ||
    "Unknown Lawyer"
  );
};

const getLawyerInitials = (lawyer) => {
  if (!lawyer) return "?";
  const firstName = lawyer.firstName || "";
  const lastName = lawyer.lastName || "";
  if (firstName && lastName) {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`;
  }
  if (firstName) return firstName.charAt(0);
  if (lawyer.email) return lawyer.email.charAt(0).toUpperCase();
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

const formatReleaseTime = (endTime) => {
  if (!endTime) return "";
  const releaseTime = new Date(endTime);
  releaseTime.setHours(releaseTime.getHours() + 6); // Add 6 hours
  return releaseTime.toLocaleString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
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
      return "Unpaid";
  }
};

const needsPayment = (meeting) => {
  // Only show pay button for PENDING status
  return meeting.payment && meeting.payment.status === "PENDING";
};

const canJoinMeeting = (meeting) => {
  const status = getMeetingStatus(meeting);
  const isPaid =
    meeting.payment?.status === "PAID" ||
    meeting.payment?.status === "COMPLETED" ||
    meeting.payment?.status === "RELEASED";
  return status === "In Progress" && isPaid;
};

const getPaymentActionMessage = (meeting) => {
  if (!meeting.payment) {
    return "Payment information not available";
  }
  switch (meeting.payment.status) {
    case "PENDING":
      return "Payment is being processed...";
    case "FAILED":
      return "Payment failed. Please try again.";
    default:
      return "Payment required to join meeting";
  }
};

// Payment Actions
const payForMeeting = async (meeting) => {
  if (!meeting.payment?.id) {
    alert("Payment information not available");
    return;
  }

  try {
    // Create Stripe checkout session
    const response = await paymentAPI.createStripeSession(meeting.payment.id);

    if (response.data?.sessionUrl) {
      // Redirect to Stripe checkout
      window.location.href = response.data.sessionUrl;
    } else {
      alert("Failed to create payment session. Please try again.");
    }
  } catch (err) {
    console.error("Error creating payment session:", err);
    const errorMessage =
      err.response?.data?.error?.message ||
      "Failed to initiate payment. Please try again.";
    alert(errorMessage);
  }
};

const cancelPayment = async (meeting) => {
  if (!meeting.payment?.id) {
    showNotification("Payment information not available", "error");
    return;
  }

  // Show confirmation banner
  showCancelConfirmation.value = true;
  selectedMeetingForCancel.value = meeting;
};

const confirmCancelPayment = async () => {
  if (!selectedMeetingForCancel.value?.payment?.id) {
    return;
  }

  isProcessingCancel.value = true;

  try {
    await paymentAPI.cancelPayment(selectedMeetingForCancel.value.payment.id);
    showNotification("Payment cancelled successfully", "success");
    // Refresh meetings to show updated status
    await fetchMeetings();
  } catch (err) {
    console.error("Error cancelling payment:", err);
    const errorMessage =
      err.response?.data?.error?.message ||
      "Failed to cancel payment. Please try again.";
    showNotification(errorMessage, "error");
  } finally {
    isProcessingCancel.value = false;
    showCancelConfirmation.value = false;
    selectedMeetingForCancel.value = null;
  }
};

const cancelCancelPayment = () => {
  showCancelConfirmation.value = false;
  selectedMeetingForCancel.value = null;
};

const releasePayment = async (meeting) => {
  if (!meeting.payment?.id) {
    showNotification("Payment information not available", "error");
    return;
  }

  // Show confirmation banner
  showReleaseConfirmation.value = true;
  selectedMeetingForRelease.value = meeting;
};

const confirmReleasePayment = async () => {
  if (!selectedMeetingForRelease.value?.payment?.id) {
    return;
  }

  isProcessingRelease.value = true;

  try {
    await paymentAPI.releasePayment(selectedMeetingForRelease.value.payment.id);
    showNotification("Payment released successfully to the lawyer", "success");
    // Refresh meetings to show updated status
    await fetchMeetings();
  } catch (err) {
    console.error("Error releasing payment:", err);
    const errorMessage =
      err.response?.data?.error?.message ||
      "Failed to release payment. Please try again.";
    showNotification(errorMessage, "error");
  } finally {
    isProcessingRelease.value = false;
    showReleaseConfirmation.value = false;
    selectedMeetingForRelease.value = null;
  }
};

const cancelReleasePayment = () => {
  showReleaseConfirmation.value = false;
  selectedMeetingForRelease.value = null;
};

// Notification system
const showNotification = (message, type = "success") => {
  notification.value = {
    show: true,
    message,
    type,
  };

  // Auto hide after 5 seconds
  setTimeout(() => {
    notification.value.show = false;
  }, 5000);
};

const hideNotification = () => {
  notification.value.show = false;
};

// Video Call Functions
const joinMeeting = async (meeting) => {
  try {
    const response = await meetingAPI.generateMeetingToken(meeting.id);
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

// Sort meetings by start time (upcoming first, then by time)
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
.client-meetings-view {
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

.meeting-time-badge {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-primary);
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

.meeting-lawyer {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.lawyer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-secondary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.lawyer-name {
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.lawyer-title {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.meeting-details {
  margin-bottom: 1rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: var(--color-text);
  font-size: 0.875rem;
}

.payment-section {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 1rem;
  margin-bottom: 1rem;
}

.payment-status-display {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.payment-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  padding: 0.5rem;
}

.payment-icon.completed,
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

.payment-icon.pending {
  background: #fef3c7;
  color: #92400e;
}

.payment-icon.failed {
  background: #fee2e2;
  color: #dc2626;
}

.payment-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.payment-status-text {
  font-weight: 500;
  font-size: 0.875rem;
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
  font-weight: 700;
  font-size: 1rem;
  color: var(--color-heading);
}

.payment-release-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  background: rgba(var(--color-primary-rgb), 0.1);
  padding: 0.5rem;
  border-radius: var(--border-radius);
}

.meeting-footer {
  padding: 1rem 1.5rem;
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
}

.payment-required-actions {
  display: flex;
  justify-content: center;
}

.payment-completed-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.payment-management-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.other-payment-states {
  text-align: center;
}

.payment-status-message {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  font-style: italic;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

.btn-success {
  background: #10b981;
  color: white;
  border: 1px solid #10b981;
}

.btn-success:hover {
  background: #059669;
  border-color: #059669;
}

.btn-warning {
  color: #92400e;
  border-color: #fbbf24;
}

.btn-warning:hover {
  background: #fbbf24;
  color: white;
}

.payment-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Notification Banner */
.notification-banner {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  max-width: 400px;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  animation: slideInRight 0.3s ease-out;
}

.notification-banner.success {
  background: #dcfce7;
  border: 1px solid #10b981;
  color: #166534;
}

.notification-banner.error {
  background: #fee2e2;
  border: 1px solid #dc2626;
  color: #991b1b;
}

.notification-banner.warning {
  background: #fef3c7;
  border: 1px solid #f59e0b;
  color: #92400e;
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
}

.notification-icon {
  flex-shrink: 0;
}

.notification-message {
  flex: 1;
  font-weight: 500;
  font-size: 0.875rem;
}

.notification-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--border-radius);
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.notification-close:hover {
  opacity: 1;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
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
  padding: 2rem;
}

.confirmation-modal {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
  max-width: 500px;
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
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--border-radius);
  color: var(--color-text-muted);
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modal-body {
  padding: 2rem;
  text-align: center;
}

.warning-icon {
  color: #f59e0b;
  margin-bottom: 1.5rem;
}

.modal-body h4 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.modal-body p {
  color: var(--color-text);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.lawyer-info-preview {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: var(--color-background-soft);
  padding: 1rem;
  border-radius: var(--border-radius);
  margin-top: 1rem;
  text-align: left;
}

.lawyer-avatar-small {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-secondary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  flex-shrink: 0;
}

.lawyer-name-small {
  font-weight: 500;
  color: var(--color-heading);
  margin-bottom: 0.25rem;
}

.meeting-title-small {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
}

.btn-danger {
  background: #dc2626;
  color: white;
  border: 1px solid #dc2626;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
  border-color: #b91c1c;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-warning {
  background: #f59e0b;
  color: white;
  border: 1px solid #f59e0b;
}

.btn-warning:hover:not(:disabled) {
  background: #d97706;
  border-color: #d97706;
}

.btn-warning:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
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

@media (max-width: 768px) {
  .meetings-grid {
    grid-template-columns: 1fr;
  }

  .payment-management-buttons {
    flex-direction: column;
  }

  .notification-banner {
    top: 10px;
    right: 10px;
    left: 10px;
    max-width: none;
  }

  .modal-overlay {
    padding: 1rem;
  }

  .modal-footer {
    flex-direction: column;
  }

  .modal-footer .btn {
    width: 100%;
  }
}
</style>
