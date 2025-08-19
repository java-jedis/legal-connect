package com.javajedis.legalconnect.payment;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.exception.UserNotFoundException;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.jobscheduler.JobSchedulerService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.payment.dto.CreatePaymentDTO;
import com.javajedis.legalconnect.payment.dto.PaymentResponseDTO;
import com.javajedis.legalconnect.payment.dto.StripeSessionResponseDTO;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import com.javajedis.legalconnect.videocall.MeetingRepo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String NOT_AUTHENTICATED_STRING = "User is not authenticated";
    private static final String NOT_AUTHORIZED_STRING = "You are not authorized to ";
    private static final String PAYMENT_NOT_FOUND = "Payment with this id not found";
    private static final String HTTP_CODE_STRING = "httpCode";
    private static final String SUCCESS_STRING = "success";
    private static final String MESSAGE_STRING = "message";

    private final PaymentRepo paymentRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final JobSchedulerService jobSchedulerService;
    private final MeetingRepo meetingRepo;
    
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;
    @Value("${frontend.url}")
    private String frontendUrl;

    @PostConstruct
    @SuppressWarnings("java:S2696") // This method is from Stripe dependency can not enclose it in static
    public void initializeStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Parses payment ID from Stripe session metadata.
     *
     * @param paymentIdStr the payment ID string from metadata
     * @return UUID if parsing is successful, null otherwise
     */
    private UUID parsePaymentIdFromMetadata(String paymentIdStr) {
        try {
            return UUID.fromString(paymentIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment ID format in session metadata: {}", paymentIdStr);
            return null;
        }
    }


    /**
     * Creates a new payment with the provided data.
     */
    public Boolean createPayment(@Valid CreatePaymentDTO paymentData) {
        log.debug("Creating payment with amount: {} for payer: {} and payee: {}",
                paymentData.getAmount(), paymentData.getPayerId(), paymentData.getPayeeId());

        User payee = userRepo.findById(paymentData.getPayeeId()).orElseThrow(
                () -> {
                    log.error("Payee not found with id: {}", paymentData.getPayeeId());
                    return new UserNotFoundException("User with this payee id not found");
                }
        );

        User payer = userRepo.findById(paymentData.getPayerId()).orElseThrow(
                () -> {
                    log.error("Payer not found with id: {}", paymentData.getPayerId());
                    return new UserNotFoundException("User with this payer id not found");
                }
        );

        Payment payment = new Payment();
        payment.setPayee(payee);
        payment.setPayer(payer);
        payment.setMeetingId(paymentData.getMeetingId());
        payment.setAmount(paymentData.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepo.save(payment);
        log.info("Payment created successfully with id: {} for amount: {}",
                savedPayment.getId(), savedPayment.getAmount());

        return true;
    }

    /**
     * Validates payment authorization and status for operations requiring PENDING status.
     */
    private ResponseEntity<ApiResponse<Object>> validatePaymentForOperation(Payment payment, User currentUser, String operation) {
        Map<String, Object> checkAuth = checkAuthorization(currentUser, payment, operation);

        if (Boolean.FALSE.equals(checkAuth.get(SUCCESS_STRING))) {
            HttpStatus status = HttpStatus.valueOf((int) checkAuth.get(HTTP_CODE_STRING));
            log.warn("{} failed for payment id: {} - {}",
                    operation, payment != null ? payment.getId() : "null", checkAuth.get(MESSAGE_STRING));
            return ApiResponse.error(checkAuth.get(MESSAGE_STRING).toString(), status);
        }

        assert payment != null; // At this point, payment cannot be null due to checkAuthorization logic

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} is not in PENDING status. Current status: {}", payment.getId(), payment.getStatus());
            return ApiResponse.error("Payment is not in pending status", HttpStatus.BAD_REQUEST);
        }

        return null; // No error, validation passed
    }

    /**
     * Completes a payment using session ID verification and schedules payment release.
     */
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> completePayment(String sessionId) {
        log.debug("Completing payment with session id: {}", sessionId);

        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Session ID is required for payment completion");
            return ApiResponse.error("Session ID is required", HttpStatus.BAD_REQUEST);
        }

        try {
            Session session = Session.retrieve(sessionId);
            
            if (!"complete".equals(session.getStatus()) || !"paid".equals(session.getPaymentStatus())) {
                log.warn("Stripe session not completed or not paid. Session ID: {}", sessionId);
                return ApiResponse.error("Payment not completed on Stripe", HttpStatus.BAD_REQUEST);
            }

            String paymentIdStr = session.getMetadata().get("payment_id");
            if (paymentIdStr == null) {
                log.error("Payment ID not found in session metadata");
                return ApiResponse.error("Payment ID not found in session metadata", HttpStatus.BAD_REQUEST);
            }

            UUID paymentId = parsePaymentIdFromMetadata(paymentIdStr);
            if (paymentId == null) {
                return ApiResponse.error("Invalid payment ID format", HttpStatus.BAD_REQUEST);
            }

            Payment payment = paymentRepo.findById(paymentId).orElse(null);
            User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

            ResponseEntity<ApiResponse<Object>> validationResult = validatePaymentForOperation(payment, currentUser, "complete payment");
            if (validationResult != null) {
                return (ResponseEntity<ApiResponse<PaymentResponseDTO>>) (ResponseEntity<?>) validationResult;
            }

            com.javajedis.legalconnect.videocall.Meeting meeting = meetingRepo.findById(payment.getMeetingId()).orElse(null);
            if (meeting == null) {
                log.warn("Meeting not found with id: {}", payment.getMeetingId());
                return ApiResponse.error("Meeting not found with this id", HttpStatus.NOT_FOUND);
            }
            
            
            payment.setPaymentMethod(PaymentMethod.CARD);
            payment.setTransactionId(session.getPaymentIntent());
            payment.setPaymentDate(OffsetDateTime.now());
            payment.setReleaseAt(meeting.getEndTimestamp().plusHours(6));
            payment.setStatus(PaymentStatus.PAID);

            Payment updatedPayment = paymentRepo.save(payment);
            log.info("Payment completed successfully with id: {} and session id: {}",
                    updatedPayment.getId(), sessionId);

            jobSchedulerService.schedulePaymentRelease(payment.getId(), payment.getReleaseAt());
            log.debug("Payment release scheduled for payment id: {} at: {}",
                    payment.getId(), payment.getReleaseAt());

            return ApiResponse.success(
                    mapToPaymentResponseDTO(updatedPayment),
                    HttpStatus.CREATED,
                    "Payment completed successfully"
            );

        } catch (StripeException e) {
            log.error("Error verifying Stripe session: {}", sessionId, e);
            return ApiResponse.error("Failed to verify payment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a payment by its ID.
     */
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPayment(UUID paymentId) {
        log.debug("Retrieving payment with id: {}", paymentId);

        Payment payment = paymentRepo.findById(paymentId).orElse(null);

        if (payment == null) {
            log.warn("Payment not found with id: {}", paymentId);
            return ApiResponse.error("Payment not found with this id", HttpStatus.NOT_FOUND);
        }

        log.debug("Payment retrieved successfully with id: {}", paymentId);
        return ApiResponse.success((mapToPaymentResponseDTO(payment)),
                HttpStatus.OK, "Payment retrieved successfully");
    }

    /**
     * Retrieves all payments for the authenticated user with pagination and sorting.
     */
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getAllPayments(
            int page, int size, String sortDirection) {

        log.debug("Retrieving payments for user with pagination - page: {}, size: {}, sort: {}",
                page, size, sortDirection);

        User user = GetUserUtil.getAuthenticatedUser(userRepo);

        if (user == null) {
            log.warn("Unauthenticated user attempted to retrieve payments");
            return ApiResponse.error(NOT_AUTHENTICATED_STRING, HttpStatus.UNAUTHORIZED);
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Payment> paymentPage = paymentRepo.findByUserAsPayerOrPayee(user, pageable);

        List<PaymentResponseDTO> payments = paymentPage.getContent().stream().
                map(this::mapToPaymentResponseDTO).toList();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", paymentPage.getTotalElements());
        metadata.put("pageNumber", paymentPage.getNumber());
        metadata.put("pageSize", paymentPage.getSize());
        metadata.put("totalPages", paymentPage.getTotalPages());
        metadata.put("hasNext", paymentPage.hasNext());
        metadata.put("hasPrevious", paymentPage.hasPrevious());
        metadata.put("isFirst", paymentPage.isFirst());
        metadata.put("isLast", paymentPage.isLast());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", CREATED_AT_FIELD);

        log.info("Retrieved {} payments for user: {} (page {} of {})",
                payments.size(), user.getEmail(), page + 1, paymentPage.getTotalPages());

        return ApiResponse.success(payments, HttpStatus.OK, "All payments for user retrieved", metadata);
    }

    /**
     * Releases a payment to the payee and sends notifications.
     */
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> releasePayment(UUID paymentId) {
        log.debug("Releasing payment with id: {}", paymentId);

        Payment payment = paymentRepo.findById(paymentId).orElse(null);
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        Map<String, Object> checkAuth = checkAuthorization(currentUser, payment, "release payment");

        if (Boolean.FALSE.equals(checkAuth.get(SUCCESS_STRING))) {
            HttpStatus status = HttpStatus.valueOf((int) checkAuth.get(HTTP_CODE_STRING));
            log.warn("Payment release failed for payment id: {} - {}",
                    paymentId, checkAuth.get(MESSAGE_STRING));
            return ApiResponse.error(checkAuth.get(MESSAGE_STRING).toString(), status);
        }

        assert payment != null; // At this point payment can not be null
        return executePaymentRelease(payment);
    }

    /**
     * Executes payment release without authentication checks (for scheduled jobs).
     * This method is intended for internal use by scheduled jobs and should not be called directly from controllers.
     */
    @Transactional
    public void executeScheduledPaymentRelease(UUID paymentId) {
        log.debug("Executing scheduled payment release for payment id: {}", paymentId);

        Payment payment = paymentRepo.findById(paymentId).orElse(null);

        if (payment == null) {
            log.warn("Payment not found for scheduled release: {}", paymentId);
            return;
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            log.warn("Payment {} is not in PAID status for scheduled release. Current status: {}",
                    paymentId, payment.getStatus());
            return;
        }

        executePaymentRelease(payment);
    }

    /**
     * Internal method to execute payment release logic.
     */
    private ResponseEntity<ApiResponse<PaymentResponseDTO>> executePaymentRelease(Payment payment) {
        payment.getPayer().getFirstName();
        payment.getPayee().getEmail();

        if (payment.getPayer() == null || payment.getPayee() == null) {
            throw new IllegalStateException("Payer or Payee cannot be null for payment release");
        }

        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleaseAt(OffsetDateTime.now());
        Payment updatedPayment = paymentRepo.save(payment);

        log.info("Payment released successfully with id: {} for amount: {}",
                updatedPayment.getId(), updatedPayment.getAmount());

        String subject = "Payment Received";
        String content = String.format("You have received a payment of BDT '%s' from '%s' '%s'",
                updatedPayment.getAmount(),
                updatedPayment.getPayer().getFirstName(),
                updatedPayment.getPayer().getLastName());

        UUID recipientId = updatedPayment.getPayee().getId();

        log.debug("Sending notification to payee: {} for payment release", recipientId);
        notificationService.sendNotification(recipientId, content);

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("notificationType", "Payment Received");
        templateVariables.put("content", content);

        log.debug("Sending email notification to payee: {} for payment release", updatedPayment.getPayee().getEmail());
        emailService.sendTemplateEmail(
                updatedPayment.getPayee().getEmail(),
                subject,
                "notification-email",
                templateVariables
        );

        jobSchedulerService.deletePaymentRelease(updatedPayment.getId());
        log.debug("Payment release job deleted for payment id: {}", updatedPayment.getId());

        return ApiResponse.success((mapToPaymentResponseDTO(updatedPayment)),
                HttpStatus.OK, "Payment released successfully");
    }

    /**
     * Cancels a payment and removes scheduled release job.
     */
    public ResponseEntity<ApiResponse<String>> cancelPayment(UUID paymentId) {
        log.debug("Canceling payment with id: {}", paymentId);

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        Payment payment = paymentRepo.findById(paymentId).orElse(null);

        Map<String, Object> checkAuth = checkAuthorization(currentUser, payment, "cancel payment");

        if (Boolean.FALSE.equals(checkAuth.get(SUCCESS_STRING))) {
            HttpStatus status = HttpStatus.valueOf((int) checkAuth.get(HTTP_CODE_STRING));
            log.warn("Payment cancellation failed for payment id: {} - {}",
                    paymentId, checkAuth.get(MESSAGE_STRING));
            return ApiResponse.error(checkAuth.get(MESSAGE_STRING).toString(), status);
        }

        assert payment != null; // At this point, payment cannot be null due to checkAuthorization logic
        payment.setStatus(PaymentStatus.CANCELED);
        Payment updatedPayment = paymentRepo.save(payment);
        log.info("Payment canceled successfully with id: {}", updatedPayment.getId());

        jobSchedulerService.deletePaymentRelease(paymentId);
        log.debug("Payment release job deleted for canceled payment id: {}", paymentId);

        return ApiResponse.success("Payment canceled successfully", HttpStatus.OK, "Payment cancelled");
    }

    /**
     * Checks if the current user is authorized to perform the specified operation on the payment.
     */
    public Map<String, Object> checkAuthorization(User currentUser, Payment payment, String operation) {
        Map<String, Object> response = new HashMap<>();

        if (currentUser == null) {
            log.debug("Authorization check failed: user not authenticated for operation: {}", operation);
            response.put(SUCCESS_STRING, false);
            response.put(MESSAGE_STRING, NOT_AUTHENTICATED_STRING);
            response.put(HTTP_CODE_STRING, HttpStatus.UNAUTHORIZED.value());
        } else if (payment == null) {
            log.debug("Authorization check failed: payment not found for operation: {}", operation);
            response.put(SUCCESS_STRING, false);
            response.put(MESSAGE_STRING, PAYMENT_NOT_FOUND);
            response.put(HTTP_CODE_STRING, HttpStatus.NOT_FOUND.value());
        } else if (!currentUser.equals(payment.getPayer())) {
            log.warn("Authorization check failed: user {} not authorized to {} payment {}",
                    currentUser.getEmail(), operation, payment.getId());
            response.put(SUCCESS_STRING, false);
            response.put(MESSAGE_STRING, NOT_AUTHORIZED_STRING + operation);
            response.put(HTTP_CODE_STRING, HttpStatus.FORBIDDEN.value());
        } else {
            log.debug("Authorization check passed: user {} authorized to {} payment {}",
                    currentUser.getEmail(), operation, payment.getId());
            response.put(SUCCESS_STRING, true);
            response.put(MESSAGE_STRING, "OK");
            response.put(HTTP_CODE_STRING, HttpStatus.OK.value()); 
        }

        return response;
    }


    /**
     * Creates a Stripe checkout session for a payment.
     */
    public ResponseEntity<ApiResponse<StripeSessionResponseDTO>> createStripeSession(UUID paymentId) {
        log.debug("Creating Stripe session for payment id: {}", paymentId);

        Payment payment = paymentRepo.findById(paymentId).orElse(null);
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        ResponseEntity<ApiResponse<Object>> validationResult = validatePaymentForOperation(payment, currentUser, "create Stripe session");
        if (validationResult != null) {
            return (ResponseEntity<ApiResponse<StripeSessionResponseDTO>>) (ResponseEntity<?>) validationResult;
        }

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/payment/cancel?session_id={CHECKOUT_SESSION_ID}")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("bdt")
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("LegalConnect Payment")
                                            .setDescription("Payment for " + payment.getMeetingId())
                                            .build())
                                    .setUnitAmount(payment.getAmount().longValue() * 100)
                                    .build())
                            .setQuantity(1L)
                            .build())
                    .putMetadata("payment_id", paymentId.toString())
                    .putMetadata("payer_id", payment.getPayer().getId().toString())
                    .putMetadata("payee_id", payment.getPayee().getId().toString())
                    .putMetadata("meeting_id", payment.getMeetingId().toString())
                    .build();

            Session session = Session.create(params);

            StripeSessionResponseDTO response = new StripeSessionResponseDTO();
            response.setSessionId(session.getId());
            response.setSessionUrl(session.getUrl());

            log.info("Stripe session created successfully for payment id: {} with session id: {}", 
                    paymentId, session.getId());

            return ApiResponse.success(response, HttpStatus.CREATED, "Stripe session created successfully");

        } catch (StripeException e) {
            log.error("Error creating Stripe session for payment id: {}", paymentId, e);
            return ApiResponse.error("Failed to create Stripe session: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Maps a Payment entity to PaymentResponseDTO.
     *
     * @param paymentData the payment entity to map
     * @return PaymentResponseDTO containing the payment data
     */
    public PaymentResponseDTO mapToPaymentResponseDTO(Payment paymentData) {
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setId(paymentData.getId());
        paymentResponseDTO.setPayerId(paymentData.getPayer().getId());
        paymentResponseDTO.setPayeeId(paymentData.getPayee().getId());
        paymentResponseDTO.setMeetingId(paymentData.getMeetingId());
        paymentResponseDTO.setAmount(paymentData.getAmount());
        paymentResponseDTO.setStatus(paymentData.getStatus());
        paymentResponseDTO.setPaymentDate(paymentData.getPaymentDate());
        paymentResponseDTO.setPaymentMethod(paymentData.getPaymentMethod());
        paymentResponseDTO.setTransactionId(paymentData.getTransactionId());
        paymentResponseDTO.setCreatedAt(paymentData.getCreatedAt());
        paymentResponseDTO.setUpdatedAt(paymentData.getUpdatedAt());
        paymentResponseDTO.setPayerFirstName(paymentData.getPayer().getFirstName());
        paymentResponseDTO.setPayerLastName(paymentData.getPayer().getLastName());
        paymentResponseDTO.setPayerEmail(paymentData.getPayer().getEmail());
        paymentResponseDTO.setPayeeFirstName(paymentData.getPayee().getFirstName());
        paymentResponseDTO.setPayeeLastName(paymentData.getPayee().getLastName());
        paymentResponseDTO.setPayeeEmail(paymentData.getPayee().getEmail());

        return paymentResponseDTO;
    }

    /**
     * Updates payment amount for a meeting when meeting duration changes.
     * Only updates if payment is still in PENDING status.
     *
     * @param meetingId the meeting ID
     * @param newAmount the new amount to set
     * @return true if payment was updated successfully, false otherwise
     */
    @Transactional
    public boolean updatePaymentAmount(UUID meetingId, java.math.BigDecimal newAmount) {
        log.debug("Updating payment amount for meeting id: {} to amount: {}", meetingId, newAmount);

        Payment payment = paymentRepo.findBymeetingId(meetingId);
        
        if (payment == null) {
            log.warn("No payment found for meeting id: {}", meetingId);
            return false;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Cannot update payment amount for meeting id: {} - payment status is not PENDING. Current status: {}", 
                    meetingId, payment.getStatus());
            return false;
        }

        payment.setAmount(newAmount);
        paymentRepo.save(payment);
        
        log.info("Payment amount updated successfully for meeting id: {} to amount: {}", meetingId, newAmount);
        return true;
    }

    /**
     * Deletes payment for a meeting when meeting is deleted.
     * Only deletes if payment is still in PENDING status.
     *
     * @param meetingId the meeting ID
     * @return true if payment was deleted successfully, false otherwise
     */
    @Transactional
    public boolean deletePaymentByMeetingId(UUID meetingId) {
        log.debug("Deleting payment for meeting id: {}", meetingId);

        Payment payment = paymentRepo.findBymeetingId(meetingId);
        
        if (payment == null) {
            log.warn("No payment found for meeting id: {}", meetingId);
            return false;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Cannot delete payment for meeting id: {} - payment status is not PENDING. Current status: {}", 
                    meetingId, payment.getStatus());
            return false;
        }

        paymentRepo.delete(payment);
        
        log.info("Payment deleted successfully for meeting id: {}", meetingId);
        return true;
    }
}