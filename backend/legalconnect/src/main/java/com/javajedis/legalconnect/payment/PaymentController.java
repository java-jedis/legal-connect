package com.javajedis.legalconnect.payment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.payment.dto.CompletePaymentDTO;
import com.javajedis.legalconnect.payment.dto.CreatePaymentDTO;
import com.javajedis.legalconnect.payment.dto.PaymentResponseDTO;
import com.javajedis.legalconnect.payment.dto.StripeSessionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling payment operations including creation, completion, release, and cancellation.
 * Provides REST endpoints for payment management with proper authorization and validation.
 */
@Slf4j
@Tag(name = "9. Payments", description = "Payment management endpoints")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates a new payment with the provided data.
     * This is for internal testing only
     */
    @Operation(summary = "Create payment", description = "Creates a new payment with the provided data. For internal usage")
    @RequireUserOrVerifiedLawyer
    @PostMapping("/")
    public ResponseEntity<ApiResponse<Boolean>> createPayment(@Valid @RequestBody CreatePaymentDTO paymentData) {
        log.info("POST /payments/ called for payer: {} and payee: {} with amount: {}", 
                paymentData.getPayerId(), paymentData.getPayeeId(), paymentData.getAmount());
        Boolean result = paymentService.createPayment(paymentData);
        return ApiResponse.success(result, org.springframework.http.HttpStatus.CREATED, "Payment created successfully");
    }

    /**
     * Completes a payment with transaction details and schedules payment release.
     */
    @Operation(summary = "Complete payment", description = "Completes a payment with transaction details and schedules payment release.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/complete")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> completePayment(@Valid @RequestBody CompletePaymentDTO paymentData) {
        log.info("PUT /payments/complete called for payment id: {} with transaction id: {}", 
                paymentData.getId(), paymentData.getTransactionId());
        return paymentService.completePayment(paymentData);
    }

    /**
     * Retrieves a payment by its ID.
     */
    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPayment(@PathVariable UUID paymentId) {
        log.info("GET /payments/{} called", paymentId);
        return paymentService.getPayment(paymentId);
    }

    /**
     * Retrieves all payments for the authenticated user with pagination and sorting.
     */
    @Operation(summary = "Get all user payments", description = "Retrieves all payments for the authenticated user with pagination and sorting.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /payments/ called with page={}, size={}, sortDirection={}", page, size, sortDirection);
        return paymentService.getAllPayments(page, size, sortDirection);
    }

    /**
     * Releases a payment to the payee and sends notifications.
     */
    @Operation(summary = "Release payment", description = "Releases a payment to the payee and sends notifications.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/{paymentId}/release")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> releasePayment(@PathVariable UUID paymentId) {
        log.info("PUT /payments/{}/release called", paymentId);
        return paymentService.releasePayment(paymentId);
    }

    /**
     * Creates a Stripe checkout session for a payment.
     */
    @Operation(summary = "Create Stripe session", description = "Creates a Stripe checkout session for a payment.")
    @RequireUserOrVerifiedLawyer
    @PostMapping("/{paymentId}/stripe-session")
    public ResponseEntity<ApiResponse<StripeSessionResponseDTO>> createStripeSession(@PathVariable UUID paymentId) {
        log.info("POST /payments/{}/stripe-session called", paymentId);
        return paymentService.createStripeSession(paymentId);
    }

    /**
     * Cancels a payment and removes scheduled release job.
     */
    @Operation(summary = "Cancel payment", description = "Cancels a payment and removes scheduled release job.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelPayment(@PathVariable UUID paymentId) {
        log.info("PUT /payments/{}/cancel called", paymentId);
        return paymentService.cancelPayment(paymentId);
    }
} 