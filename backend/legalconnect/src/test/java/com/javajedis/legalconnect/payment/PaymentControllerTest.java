package com.javajedis.legalconnect.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.JWTFilter;
import com.javajedis.legalconnect.payment.dto.CreatePaymentDTO;
import com.javajedis.legalconnect.payment.dto.PaymentResponseDTO;
import com.javajedis.legalconnect.payment.dto.StripeSessionResponseDTO;

@WebMvcTest(controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {EmailVerificationFilter.class, JWTFilter.class}))
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(PaymentControllerTest.TestConfig.class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreatePaymentDTO createPaymentDTO;
    private PaymentResponseDTO paymentResponseDTO;
    private UUID testPaymentId;
    private UUID testPayerId;
    private UUID testPayeeId;
    private UUID testmeetingId;
    private String testSessionId;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PaymentService paymentService() {
            return mock(PaymentService.class);
        }
    }

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        testPayerId = UUID.randomUUID();
        testPayeeId = UUID.randomUUID();
        testmeetingId = UUID.randomUUID();

        createPaymentDTO = new CreatePaymentDTO();
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(new BigDecimal("100.00"));

        testSessionId = "cs_test_session_123";

        paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setId(testPaymentId);
        paymentResponseDTO.setPayerId(testPayerId);
        paymentResponseDTO.setPayeeId(testPayeeId);
        paymentResponseDTO.setMeetingId(testmeetingId);
        paymentResponseDTO.setAmount(new BigDecimal("100.00"));
        paymentResponseDTO.setStatus(PaymentStatus.PENDING);
        paymentResponseDTO.setCreatedAt(OffsetDateTime.now());
        paymentResponseDTO.setUpdatedAt(OffsetDateTime.now());
    }

    // Task 5.1: Create payment creation endpoint tests
    @Test
    @DisplayName("Should create payment successfully with valid data")
    void createPayment_ValidData_Success() throws Exception {
        when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenReturn(true);

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("Payment created successfully"));
    }

    @Test
    @DisplayName("Should return bad request when payer ID is null")
    void createPayment_NullPayerId_BadRequest() throws Exception {
        createPaymentDTO.setPayerId(null);

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when payee ID is null")
    void createPayment_NullPayeeId_BadRequest() throws Exception {
        createPaymentDTO.setPayeeId(null);

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when reference ID is null")
    void createPayment_NullmeetingId_BadRequest() throws Exception {
        createPaymentDTO.setMeetingId(null);

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when amount is null")
    void createPayment_NullAmount_BadRequest() throws Exception {
        createPaymentDTO.setAmount(null);

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when amount is zero")
    void createPayment_ZeroAmount_BadRequest() throws Exception {
        createPaymentDTO.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when amount is negative")
    void createPayment_NegativeAmount_BadRequest() throws Exception {
        createPaymentDTO.setAmount(new BigDecimal("-10.00"));

        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when request body is empty")
    void createPayment_EmptyRequestBody_BadRequest() throws Exception {
        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when content type is not JSON")
    void createPayment_InvalidContentType_InternalServerError() throws Exception {
        mockMvc.perform(post("/payments/")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(createPaymentDTO)))
                .andExpect(status().isInternalServerError());
    }

    // Task 5.2: Create payment completion endpoint tests
    @Test
    @DisplayName("Should complete payment successfully with valid session ID")
    void completePayment_ValidSessionId_Success() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> responseEntity = 
            ApiResponse.success(paymentResponseDTO, HttpStatus.CREATED, "Payment completed successfully");
        when(paymentService.completePayment(testSessionId)).thenReturn(responseEntity);

        mockMvc.perform(put("/payments/complete/{sessionId}", testSessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(testPaymentId.toString()))
                .andExpect(jsonPath("$.message").value("Payment completed successfully"));
    }

    @Test
    @DisplayName("Should return error when service returns error response")
    void completePayment_ServiceError_ErrorResponse() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> errorResponse = 
            ApiResponse.error("Payment not found", HttpStatus.NOT_FOUND);
        when(paymentService.completePayment(testSessionId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/complete/{sessionId}", testSessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Payment not found"));
    }

    // Task 5.3: Create payment retrieval endpoint tests
    @Test
    @DisplayName("Should retrieve payment successfully with valid ID")
    void getPayment_ValidId_Success() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> responseEntity = 
            ApiResponse.success(paymentResponseDTO, HttpStatus.OK, "Payment retrieved successfully");
        when(paymentService.getPayment(testPaymentId)).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/{paymentId}", testPaymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testPaymentId.toString()))
                .andExpect(jsonPath("$.message").value("Payment retrieved successfully"));
    }

    @Test
    @DisplayName("Should return not found when payment does not exist")
    void getPayment_NonExistentId_NotFound() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> errorResponse = 
            ApiResponse.error("Payment not found with this id", HttpStatus.NOT_FOUND);
        when(paymentService.getPayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(get("/payments/{paymentId}", testPaymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Payment not found with this id"));
    }

    @Test
    @DisplayName("Should return internal server error when payment ID is invalid UUID format")
    void getPayment_InvalidUuidFormat_InternalServerError() throws Exception {
        mockMvc.perform(get("/payments/{paymentId}", "invalid-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void getPayment_ServiceException_InternalServerError() throws Exception {
        when(paymentService.getPayment(testPaymentId)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/payments/{paymentId}", testPaymentId))
                .andExpect(status().isInternalServerError());
    }

    // Task 5.4: Create payment listing endpoint tests
    @Test
    @DisplayName("Should retrieve all payments with default pagination")
    void getAllPayments_DefaultPagination_Success() throws Exception {
        List<PaymentResponseDTO> payments = Arrays.asList(paymentResponseDTO);
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> responseEntity = 
            ApiResponse.success(payments, HttpStatus.OK, "All payments for user retrieved");
        when(paymentService.getAllPayments(0, 10, "DESC")).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testPaymentId.toString()))
                .andExpect(jsonPath("$.message").value("All payments for user retrieved"));
    }

    @Test
    @DisplayName("Should retrieve all payments with custom pagination parameters")
    void getAllPayments_CustomPagination_Success() throws Exception {
        List<PaymentResponseDTO> payments = Arrays.asList(paymentResponseDTO);
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> responseEntity = 
            ApiResponse.success(payments, HttpStatus.OK, "All payments for user retrieved");
        when(paymentService.getAllPayments(1, 5, "ASC")).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.message").value("All payments for user retrieved"));
    }

    @Test
    @DisplayName("Should handle invalid page parameter")
    void getAllPayments_InvalidPageParameter_InternalServerError() throws Exception {
        mockMvc.perform(get("/payments/")
                        .param("page", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle invalid size parameter")
    void getAllPayments_InvalidSizeParameter_InternalServerError() throws Exception {
        mockMvc.perform(get("/payments/")
                        .param("size", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle negative page parameter")
    void getAllPayments_NegativePageParameter_Success() throws Exception {
        List<PaymentResponseDTO> payments = Arrays.asList(paymentResponseDTO);
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> responseEntity = 
            ApiResponse.success(payments, HttpStatus.OK, "All payments for user retrieved");
        when(paymentService.getAllPayments(-1, 10, "DESC")).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/")
                        .param("page", "-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle zero size parameter")
    void getAllPayments_ZeroSizeParameter_Success() throws Exception {
        List<PaymentResponseDTO> payments = Arrays.asList();
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> responseEntity = 
            ApiResponse.success(payments, HttpStatus.OK, "All payments for user retrieved");
        when(paymentService.getAllPayments(0, 0, "DESC")).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/")
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should handle invalid sort direction parameter")
    void getAllPayments_InvalidSortDirection_Success() throws Exception {
        List<PaymentResponseDTO> payments = Arrays.asList(paymentResponseDTO);
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> responseEntity = 
            ApiResponse.success(payments, HttpStatus.OK, "All payments for user retrieved");
        when(paymentService.getAllPayments(0, 10, "INVALID")).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/")
                        .param("sortDirection", "INVALID"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when service returns unauthorized")
    void getAllPayments_Unauthorized_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(paymentService.getAllPayments(0, 10, "DESC")).thenReturn(errorResponse);

        mockMvc.perform(get("/payments/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return empty list when no payments found")
    void getAllPayments_NoPayments_EmptyList() throws Exception {
        List<PaymentResponseDTO> emptyPayments = Arrays.asList();
        ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> responseEntity = 
            ApiResponse.success(emptyPayments, HttpStatus.OK, "All payments for user retrieved");
        when(paymentService.getAllPayments(0, 10, "DESC")).thenReturn(responseEntity);

        mockMvc.perform(get("/payments/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // Task 5.5: Create payment release endpoint tests
    @Test
    @DisplayName("Should release payment successfully with valid ID")
    void releasePayment_ValidId_Success() throws Exception {
        paymentResponseDTO.setStatus(PaymentStatus.RELEASED);
        ResponseEntity<ApiResponse<PaymentResponseDTO>> responseEntity = 
            ApiResponse.success(paymentResponseDTO, HttpStatus.OK, "Payment released successfully");
        when(paymentService.releasePayment(testPaymentId)).thenReturn(responseEntity);

        mockMvc.perform(put("/payments/{paymentId}/release", testPaymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testPaymentId.toString()))
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.message").value("Payment released successfully"));
    }

    @Test
    @DisplayName("Should return not found when payment does not exist")
    void releasePayment_NonExistentId_NotFound() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> errorResponse = 
            ApiResponse.error("Payment with this id not found", HttpStatus.NOT_FOUND);
        when(paymentService.releasePayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/{paymentId}/release", testPaymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Payment with this id not found"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not authorized")
    void releasePayment_Unauthorized_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> errorResponse = 
            ApiResponse.error("You are not authorized to release payment", HttpStatus.FORBIDDEN);
        when(paymentService.releasePayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/{paymentId}/release", testPaymentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You are not authorized to release payment"));
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated")
    void releasePayment_NotAuthenticated_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<PaymentResponseDTO>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(paymentService.releasePayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/{paymentId}/release", testPaymentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return internal server error when payment ID is invalid UUID format")
    void releasePayment_InvalidUuidFormat_InternalServerError() throws Exception {
        mockMvc.perform(put("/payments/{paymentId}/release", "invalid-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void releasePayment_ServiceException_InternalServerError() throws Exception {
        when(paymentService.releasePayment(testPaymentId)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/payments/{paymentId}/release", testPaymentId))
                .andExpect(status().isInternalServerError());
    }

    // Task 5.6: Create payment cancellation endpoint tests
    @Test
    @DisplayName("Should cancel payment successfully with valid ID")
    void cancelPayment_ValidId_Success() throws Exception {
        ResponseEntity<ApiResponse<String>> responseEntity = 
            ApiResponse.success("Payment canceled successfully", HttpStatus.OK, "Payment cancelled");
        when(paymentService.cancelPayment(testPaymentId)).thenReturn(responseEntity);

        mockMvc.perform(put("/payments/{paymentId}/cancel", testPaymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Payment canceled successfully"))
                .andExpect(jsonPath("$.message").value("Payment cancelled"));
    }

    @Test
    @DisplayName("Should return not found when payment does not exist")
    void cancelPayment_NonExistentId_NotFound() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("Payment with this id not found", HttpStatus.NOT_FOUND);
        when(paymentService.cancelPayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/{paymentId}/cancel", testPaymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Payment with this id not found"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not authorized")
    void cancelPayment_Unauthorized_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("You are not authorized to cancel payment", HttpStatus.FORBIDDEN);
        when(paymentService.cancelPayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/{paymentId}/cancel", testPaymentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You are not authorized to cancel payment"));
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated")
    void cancelPayment_NotAuthenticated_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(paymentService.cancelPayment(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(put("/payments/{paymentId}/cancel", testPaymentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return internal server error when payment ID is invalid UUID format")
    void cancelPayment_InvalidUuidFormat_InternalServerError() throws Exception {
        mockMvc.perform(put("/payments/{paymentId}/cancel", "invalid-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void cancelPayment_ServiceException_InternalServerError() throws Exception {
        when(paymentService.cancelPayment(testPaymentId)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/payments/{paymentId}/cancel", testPaymentId))
                .andExpect(status().isInternalServerError());
    }

    // Task 5.7: Create Stripe session endpoint tests
    @Test
    @DisplayName("Should create Stripe session successfully with valid payment ID")
    void createStripeSession_ValidPaymentId_Success() throws Exception {
        StripeSessionResponseDTO stripeResponse = new StripeSessionResponseDTO();
        stripeResponse.setSessionId("cs_test_session_123");
        stripeResponse.setSessionUrl("https://checkout.stripe.com/pay/cs_test_session_123");
        
        ResponseEntity<ApiResponse<StripeSessionResponseDTO>> responseEntity = 
            ApiResponse.success(stripeResponse, HttpStatus.CREATED, "Stripe session created successfully");
        when(paymentService.createStripeSession(testPaymentId)).thenReturn(responseEntity);

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sessionId").value("cs_test_session_123"))
                .andExpect(jsonPath("$.data.sessionUrl").value("https://checkout.stripe.com/pay/cs_test_session_123"))
                .andExpect(jsonPath("$.message").value("Stripe session created successfully"));
    }

    @Test
    @DisplayName("Should return not found when payment does not exist for Stripe session")
    void createStripeSession_NonExistentPayment_NotFound() throws Exception {
        ResponseEntity<ApiResponse<StripeSessionResponseDTO>> errorResponse = 
            ApiResponse.error("Payment with this id not found", HttpStatus.NOT_FOUND);
        when(paymentService.createStripeSession(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Payment with this id not found"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not authorized for Stripe session")
    void createStripeSession_Unauthorized_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<StripeSessionResponseDTO>> errorResponse = 
            ApiResponse.error("You are not authorized to create Stripe session", HttpStatus.FORBIDDEN);
        when(paymentService.createStripeSession(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You are not authorized to create Stripe session"));
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated for Stripe session")
    void createStripeSession_NotAuthenticated_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<StripeSessionResponseDTO>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(paymentService.createStripeSession(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return bad request when payment is not in pending status")
    void createStripeSession_PaymentNotPending_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<StripeSessionResponseDTO>> errorResponse = 
            ApiResponse.error("Payment is not in pending status", HttpStatus.BAD_REQUEST);
        when(paymentService.createStripeSession(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Payment is not in pending status"));
    }

    @Test
    @DisplayName("Should return internal server error when payment ID is invalid UUID format for Stripe session")
    void createStripeSession_InvalidUuidFormat_InternalServerError() throws Exception {
        mockMvc.perform(post("/payments/{paymentId}/stripe-session", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully for Stripe session")
    void createStripeSession_ServiceException_InternalServerError() throws Exception {
        when(paymentService.createStripeSession(testPaymentId)).thenThrow(new RuntimeException("Stripe service error"));

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return internal server error when Stripe API fails")
    void createStripeSession_StripeApiError_InternalServerError() throws Exception {
        ResponseEntity<ApiResponse<StripeSessionResponseDTO>> errorResponse = 
            ApiResponse.error("Failed to create Stripe session: API error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(paymentService.createStripeSession(testPaymentId)).thenReturn(errorResponse);

        mockMvc.perform(post("/payments/{paymentId}/stripe-session", testPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.message").value("Failed to create Stripe session: API error"));
    }
}