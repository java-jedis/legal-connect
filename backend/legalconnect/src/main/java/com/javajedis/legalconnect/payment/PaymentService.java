package com.javajedis.legalconnect.payment;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.javajedis.legalconnect.payment.dto.CompletePaymentDTO;
import com.javajedis.legalconnect.payment.dto.CreatePaymentDTO;
import com.javajedis.legalconnect.payment.dto.PaymentResponseDTO;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling payment operations including creation, completion, release, and cancellation.
 * Provides comprehensive payment management functionality with proper authorization checks,
 * notification services, and email notifications.
 */
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
        payment.setRefId(paymentData.getRefId());
        payment.setAmount(paymentData.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepo.save(payment);
        log.info("Payment created successfully with id: {} for amount: {}",
                savedPayment.getId(), savedPayment.getAmount());

        return true;
    }

    /**
     * Completes a payment with transaction details and schedules payment release.
     */
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> completePayment(@Valid CompletePaymentDTO paymentData) {
        log.debug("Completing payment with id: {} and transaction id: {}",
                paymentData.getId(), paymentData.getTransactionId());

        Payment payment = paymentRepo.findById(paymentData.getId()).orElse(null);
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        Map<String, Object> checkAuth = checkAuthorization(currentUser, payment, "complete payment");

        if (Boolean.FALSE.equals(checkAuth.get(SUCCESS_STRING))) {
            HttpStatus status = HttpStatus.valueOf((int) checkAuth.get(HTTP_CODE_STRING));
            log.warn("Payment completion failed for payment id: {} - {}",
                    paymentData.getId(), checkAuth.get(MESSAGE_STRING));
            return ApiResponse.error(checkAuth.get(MESSAGE_STRING).toString(), status);
        }

        // At this point, payment cannot be null due to checkAuthorization logic
        if (payment == null) {
            log.error("Payment is null after authorization check for payment id: {}", paymentData.getId());
            return ApiResponse.error("Payment not found", HttpStatus.NOT_FOUND);
        }

        payment.setPaymentMethod(paymentData.getPaymentMethod());
        payment.setTransactionId(paymentData.getTransactionId());
        payment.setPaymentDate(paymentData.getPaymentDate());
        payment.setReleaseAt(paymentData.getReleaseAt());
        payment.setStatus(PaymentStatus.PAID);

        Payment updatedPayment = paymentRepo.save(payment);
        log.info("Payment completed successfully with id: {} and transaction id: {}",
                updatedPayment.getId(), updatedPayment.getTransactionId());

        jobSchedulerService.schedulePaymentRelease(payment.getId(), payment.getReleaseAt());
        log.debug("Payment release scheduled for payment id: {} at: {}",
                payment.getId(), payment.getReleaseAt());

        return ApiResponse.success(
                mapToPaymentResponseDTO(updatedPayment),
                HttpStatus.CREATED,
                "Payment completed successfully"
        );
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

        // At this point, payment cannot be null due to checkAuthorization logic
        if (payment == null) {
            log.error("Payment is null after authorization check for payment id: {}", paymentId);
            return ApiResponse.error("Payment not found", HttpStatus.NOT_FOUND);
        }

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
            response.put(HTTP_CODE_STRING, HttpStatus.OK.value()); // 200
        }

        return response;
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
        paymentResponseDTO.setRefId(paymentData.getRefId());
        paymentResponseDTO.setAmount(paymentData.getAmount());
        paymentResponseDTO.setStatus(paymentData.getStatus());
        paymentResponseDTO.setPaymentDate(paymentData.getPaymentDate());
        paymentResponseDTO.setPaymentMethod(paymentData.getPaymentMethod());
        paymentResponseDTO.setTransactionId(paymentData.getTransactionId());
        paymentResponseDTO.setCreatedAt(paymentData.getCreatedAt());
        paymentResponseDTO.setUpdatedAt(paymentData.getUpdatedAt());

        return paymentResponseDTO;
    }
}