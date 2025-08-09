<template>
  <div class="payment-success-view">
    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <h2>Processing your payment...</h2>
        <p>Please wait while we confirm your payment with Stripe.</p>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <div class="error-content">
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
        <h2>Payment Processing Failed</h2>
        <p>{{ error }}</p>
        <div class="error-actions">
          <button @click="goToMeetings" class="btn btn-primary">
            Back to Meetings
          </button>
          <button @click="retryPayment" class="btn btn-outline">
            Try Again
          </button>
        </div>
      </div>
    </div>

    <!-- Success State -->
    <div v-else-if="paymentData" class="success-container">
      <div class="success-content">
        <!-- Success Icon -->
        <div class="success-icon">
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
            <path
              d="M8 12l2 2 4-4"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>

        <!-- Success Message -->
        <h1>Payment Successful!</h1>
        <p class="success-message">
          Your payment has been processed successfully. You can now join your
          scheduled meeting.
        </p>

        <!-- Payment Details Card -->
        <div class="payment-details-card">
          <h3>Payment Details</h3>

          <div class="detail-row">
            <span class="detail-label">Amount Paid:</span>
            <span class="detail-value amount">৳{{ paymentData.amount }}</span>
          </div>

          <div class="detail-row">
            <span class="detail-label">Payment Method:</span>
            <span class="detail-value">{{
              formatPaymentMethod(paymentData.paymentMethod)
            }}</span>
          </div>

          <div class="detail-row">
            <span class="detail-label">Transaction ID:</span>
            <span class="detail-value transaction-id">{{
              paymentData.transactionId
            }}</span>
          </div>

          <div class="detail-row">
            <span class="detail-label">Payment Date:</span>
            <span class="detail-value">{{
              formatPaymentDate(paymentData.paymentDate)
            }}</span>
          </div>

          <div class="detail-row">
            <span class="detail-label">Status:</span>
            <span class="detail-value status-badge completed">
              <svg
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
              Completed
            </span>
          </div>

          <!-- Lawyer Information -->
          <div class="lawyer-info-section">
            <h4>Payment To:</h4>
            <div class="lawyer-info">
              <div class="lawyer-avatar">
                <span>{{ getLawyerInitials() }}</span>
              </div>
              <div class="lawyer-details">
                <p class="lawyer-name">{{ getLawyerName() }}</p>
                <p class="lawyer-email">{{ paymentData.payeeEmail }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Payment Release Information -->
        <div class="release-info-card">
          <div class="release-icon">
            <svg
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
          <div class="release-content">
            <h4>Payment Release Information</h4>
            <p>
              Your payment is held securely and will be automatically released
              to the lawyer
              <strong>6 hours after your meeting ends</strong>. You can also
              manually release or cancel the payment from your meetings page if
              needed.
            </p>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="success-actions">
          <button @click="goToMeetings" class="btn btn-primary btn-lg">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
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
              <circle
                cx="8"
                cy="9"
                r="2"
                stroke="currentColor"
                stroke-width="2"
              />
              <path
                d="M16 7v2a2 2 0 0 1-2 2H10"
                stroke="currentColor"
                stroke-width="2"
              />
            </svg>
            View My Meetings
          </button>

          <button @click="downloadInvoice" class="btn btn-outline">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
            >
              <path
                d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <polyline
                points="7,10 12,15 17,10"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <line
                x1="12"
                y1="15"
                x2="12"
                y2="3"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            Download Invoice
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { paymentAPI } from "@/services/api";
import { useAuthStore } from "@/stores/auth";
import jsPDF from "jspdf";
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

// State
const isLoading = ref(true);
const error = ref("");
const paymentData = ref(null);

// Methods
const completePayment = async () => {
  const sessionId = route.query.session_id;

  if (!sessionId) {
    error.value = "Invalid payment session. Please try again.";
    isLoading.value = false;
    return;
  }

  try {
    const response = await paymentAPI.completePayment(sessionId);
    // Backend returns ApiResponse<T>; extract data safely
    paymentData.value = response?.data?.data ?? response?.data ?? null;
  } catch (err) {
    console.error("Error completing payment:", err);
    error.value =
      err.response?.data?.error?.message ||
      "Failed to process payment. Please contact support.";
  } finally {
    isLoading.value = false;
  }
};

const formatPaymentMethod = (method) => {
  switch (method) {
    case "STRIPE":
      return "Credit/Debit Card (Stripe)";
    case "BKASH":
      return "bKash";
    case "NAGAD":
      return "Nagad";
    case "ROCKET":
      return "Rocket";
    default:
      return method || "Unknown";
  }
};

const formatPaymentDate = (dateTime) => {
  if (!dateTime) return "";
  const date = new Date(dateTime);
  return date.toLocaleString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const getLawyerName = () => {
  if (!paymentData.value) return "Unknown Lawyer";
  const firstName = paymentData.value.payeeFirstName || "";
  const lastName = paymentData.value.payeeLastName || "";
  return (
    `${firstName} ${lastName}`.trim() ||
    paymentData.value.payeeEmail ||
    "Unknown Lawyer"
  );
};

const getLawyerInitials = () => {
  if (!paymentData.value) return "?";
  const firstName = paymentData.value.payeeFirstName || "";
  const lastName = paymentData.value.payeeLastName || "";
  if (firstName && lastName) {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`;
  }
  if (firstName) return firstName.charAt(0);
  if (paymentData.value.payeeEmail)
    return paymentData.value.payeeEmail.charAt(0).toUpperCase();
  return "?";
};

const goToMeetings = () => {
  // Add a query parameter to trigger refresh
  router.push({ path: "/my-meetings", query: { refresh: "true" } });
};

const retryPayment = () => {
  router.push("/my-meetings");
};

// Helpers to read CSS brand colors for consistent PDF styling
const getCssVar = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim();
const parseRgbTriplet = (value) => {
  // Accepts formats like "34, 139, 34" or "rgb(34, 139, 34)" or hex like "#228B22"
  if (!value) return null;
  const rgbMatch = value.match(/(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})/);
  if (rgbMatch) return [parseInt(rgbMatch[1]), parseInt(rgbMatch[2]), parseInt(rgbMatch[3])];
  const hexMatch = value.match(/^#([0-9a-fA-F]{6})$/);
  if (hexMatch) {
    const hex = hexMatch[1];
    return [
      parseInt(hex.substring(0, 2), 16),
      parseInt(hex.substring(2, 4), 16),
      parseInt(hex.substring(4, 6), 16),
    ];
  }
  return null;
};
const getFirstAvailableCssRgb = (vars, fallback) => {
  for (const v of vars) {
    const raw = getCssVar(v);
    const parsed = parseRgbTriplet(raw);
    if (parsed) return parsed;
  }
  return fallback;
};

const downloadInvoice = () => {
  const authStore = useAuthStore();
  const doc = new jsPDF();

  // Brand-aligned colors pulled from CSS variables with sensible fallbacks
  const primaryColor = getFirstAvailableCssRgb([
    "--color-brand-rgb",
    "--color-primary-rgb",
  ], [34, 139, 34]);
  const secondaryColor = getFirstAvailableCssRgb([
    "--color-secondary-rgb",
    "--color-accent-rgb",
    "--color-primary-rgb",
  ], [70, 130, 180]);
  const textColor = getFirstAvailableCssRgb([
    "--color-text-rgb",
  ], [51, 51, 51]);
  const lightGray = [160, 160, 160];

  // Header Section
  doc.setFillColor(...primaryColor);
  doc.rect(0, 0, 210, 40, "F");

  // Company Logo/Name
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(24);
  doc.setFont("helvetica", "bold");
  doc.text("LegalConnect", 20, 25);

  // Invoice Title
  doc.setFontSize(16);
  doc.setFont("helvetica", "normal");
  doc.text("Payment Invoice", 150, 25);

  // Invoice Details (Top Right)
  doc.setTextColor(...textColor);
  doc.setFontSize(10);
  doc.setFont("helvetica", "normal");
  const invoiceDate = new Date().toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
  doc.text(`Invoice Date: ${invoiceDate}`, 150, 50);
  doc.text(`Transaction ID: ${paymentData.value.transactionId}`, 150, 57);

  // Payer Information Section
  doc.setFontSize(14);
  doc.setFont("helvetica", "bold");
  doc.setTextColor(...secondaryColor);
  doc.text("BILL TO:", 20, 70);

  doc.setFontSize(11);
  doc.setFont("helvetica", "normal");
  doc.setTextColor(...textColor);

  const payerName =
    `${authStore.userInfo?.firstName || ""} ${authStore.userInfo?.lastName || ""}`.trim() ||
    "N/A";
  const payerEmail = authStore.userInfo?.email || "N/A";

  doc.text(`Name: ${payerName}`, 20, 80);
  doc.text(`Email: ${payerEmail}`, 20, 87);

  // Payee Information Section
  doc.setFontSize(14);
  doc.setFont("helvetica", "bold");
  doc.setTextColor(...secondaryColor);
  doc.text("PAYMENT TO:", 20, 115);

  doc.setFontSize(11);
  doc.setFont("helvetica", "normal");
  doc.setTextColor(...textColor);

  doc.text(`Name: ${getLawyerName()}`, 20, 125);
  doc.text(`Email: ${paymentData.value.payeeEmail}`, 20, 132);

  // Payment Details Table
  const tableStartY = 160;

  // Table Header
  // Use subtle brand tint for header background
  const headerTint = [
    Math.min(primaryColor[0] + 200, 255),
    Math.min(primaryColor[1] + 200, 255),
    Math.min(primaryColor[2] + 200, 255),
  ];
  doc.setFillColor(...headerTint);
  doc.rect(20, tableStartY, 170, 10, "F");

  doc.setFontSize(12);
  doc.setFont("helvetica", "bold");
  doc.setTextColor(...textColor);
  doc.text("PAYMENT DETAILS", 25, tableStartY + 7);

  // Table Content
  doc.setFontSize(10);
  doc.setFont("helvetica", "normal");

  const details = [
    ["Description", "Legal Consultation Payment"],
    ["Amount", `${paymentData.value.amount} BDT`],
    ["Payment Method", formatPaymentMethod(paymentData.value.paymentMethod)],
    ["Payment Date", formatPaymentDate(paymentData.value.paymentDate)],
    ["Status", "Completed"],
  ];

  let currentY = tableStartY + 20;
  details.forEach(([label, value]) => {
    doc.setTextColor(...lightGray);
    doc.text(label + ":", 25, currentY);
    doc.setTextColor(...textColor);
    doc.text(value, 80, currentY);
    currentY += 8;
  });

  // Total Amount Section
  const totalY = currentY + 10;
  doc.setFillColor(...primaryColor);
  doc.rect(20, totalY, 170, 15, "F");

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(14);
  doc.setFont("helvetica", "bold");
  doc.text("TOTAL AMOUNT PAID:", 25, totalY + 10);
  doc.text(`${paymentData.value.amount} BDT`, 150, totalY + 10);

  // Payment Release Information
  doc.setTextColor(...textColor);
  doc.setFontSize(10);
  doc.setFont("helvetica", "normal");
  const releaseY = totalY + 30;

  doc.text("Payment Release Information:", 20, releaseY);
  doc.text("• Payment is held securely in escrow", 20, releaseY + 8);
  doc.text(
    "• Funds will be automatically released 6 hours after meeting completion",
    20,
    releaseY + 16
  );
  doc.text(
    "• You can manually release or cancel payment from your meetings page",
    20,
    releaseY + 24
  );

  // Footer
  const footerY = 270;
  doc.setDrawColor(...lightGray);
  doc.line(20, footerY, 190, footerY);

  doc.setTextColor(...lightGray);
  doc.setFontSize(8);
  doc.text(
    "This is a computer-generated invoice. No signature required.",
    20,
    footerY + 8
  );
  doc.text("For support, contact: support@legalconnect.com", 20, footerY + 15);
  doc.text(`Generated on: ${new Date().toLocaleString()}`, 20, footerY + 22);

  // Thank you message
  doc.setTextColor(...primaryColor);
  doc.setFontSize(12);
  doc.setFont("helvetica", "bold");
  doc.text("Thank you for using LegalConnect!", 105, footerY + 35, {
    align: "center",
  });

  // Save the PDF
  const fileName = `LegalConnect-Invoice-${paymentData.value.transactionId}.pdf`;
  doc.save(fileName);
};

// Initialize on mount
onMounted(() => {
  completePayment();
});
</script>

<style scoped>
.payment-success-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.loading-container,
.error-container,
.success-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.loading-content,
.error-content,
.success-content {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  padding: 3rem;
  text-align: center;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

/* Loading State */
.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--color-border);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 2rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-content h2 {
  color: var(--color-heading);
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.loading-content p {
  color: var(--color-text-muted);
  font-size: 1rem;
}

/* Error State */
.error-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
  color: var(--color-error);
}

.error-content h2 {
  color: var(--color-error);
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.error-content p {
  color: var(--color-text);
  margin-bottom: 2rem;
  font-size: 1rem;
}

.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* Success State */
.success-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 2rem;
  color: #10b981;
  background: #dcfce7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-content h1 {
  color: var(--color-heading);
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.success-message {
  color: var(--color-text);
  font-size: 1.125rem;
  margin-bottom: 2rem;
  line-height: 1.6;
}

/* Payment Details Card */
.payment-details-card {
  background: var(--color-background-soft);
  border-radius: var(--border-radius);
  padding: 2rem;
  margin-bottom: 2rem;
  text-align: left;
  border: 1px solid var(--color-border);
}

.payment-details-card h3 {
  color: var(--color-heading);
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  text-align: center;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--color-border);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  color: var(--color-text-muted);
  font-weight: 500;
}

.detail-value {
  color: var(--color-heading);
  font-weight: 600;
}

.detail-value.amount {
  color: #10b981;
  font-size: 1.125rem;
}

.transaction-id {
  font-family: monospace;
  font-size: 0.875rem;
  background: var(--color-background);
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
}

.status-badge.completed {
  background: #dcfce7;
  color: #166534;
}

/* Lawyer Information */
.lawyer-info-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border);
}

.lawyer-info-section h4 {
  color: var(--color-heading);
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.lawyer-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.lawyer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--color-secondary);
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.125rem;
}

.lawyer-name {
  color: var(--color-heading);
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.lawyer-email {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

/* Release Info Card */
.release-info-card {
  background: rgba(var(--color-primary-rgb), 0.1);
  border-radius: var(--border-radius);
  padding: 1.5rem;
  margin-bottom: 2rem;
  display: flex;
  gap: 1rem;
  text-align: left;
  border: 1px solid rgba(var(--color-primary-rgb), 0.2);
}

.release-icon {
  color: var(--color-primary);
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.release-content h4 {
  color: var(--color-heading);
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.release-content p {
  color: var(--color-text);
  font-size: 0.875rem;
  line-height: 1.5;
}

/* Action Buttons */
.success-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-lg {
  padding: 0.875rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .payment-success-view {
    padding: 1rem;
  }

  .loading-content,
  .error-content,
  .success-content {
    padding: 2rem;
  }

  .success-content h1 {
    font-size: 1.5rem;
  }

  .success-message {
    font-size: 1rem;
  }

  .payment-details-card {
    padding: 1.5rem;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .success-actions {
    flex-direction: column;
  }

  .btn-lg {
    width: 100%;
    justify-content: center;
  }

  .release-info-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
