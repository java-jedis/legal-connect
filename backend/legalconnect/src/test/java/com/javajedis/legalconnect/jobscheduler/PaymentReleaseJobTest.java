package com.javajedis.legalconnect.jobscheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.javajedis.legalconnect.payment.PaymentService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("PaymentReleaseJob Tests")
class PaymentReleaseJobTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @InjectMocks
    private PaymentReleaseJob paymentReleaseJob;

    private JobDataMap jobDataMap;
    private UUID testTaskId;
    private UUID testPaymentId;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testPaymentId = UUID.randomUUID();

        jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", testTaskId.toString());
        jobDataMap.put("paymentId", testPaymentId.toString());

        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    }

    @Test
    @DisplayName("Should execute payment release job successfully")
    void execute_Success() {
        // When
        assertDoesNotThrow(() -> paymentReleaseJob.execute(context));

        // Then
        verify(paymentService, times(1)).executeScheduledPaymentRelease(testPaymentId);
    }

    @Test
    @DisplayName("Should execute payment release job successfully without taskId")
    void execute_WithoutTaskId_Success() {
        // Given
        jobDataMap.remove("taskId");

        // When
        assertDoesNotThrow(() -> paymentReleaseJob.execute(context));

        // Then
        verify(paymentService, times(1)).executeScheduledPaymentRelease(testPaymentId);
    }

    @Test
    @DisplayName("Should handle payment service exception")
    void execute_PaymentServiceException_ThrowsJobExecutionException() {
        // Given
        doThrow(new RuntimeException("Payment service error")).when(paymentService)
                .executeScheduledPaymentRelease(any(UUID.class));

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, times(1)).executeScheduledPaymentRelease(testPaymentId);
    }

    @Test
    @DisplayName("Should handle missing payment ID in job data")
    void execute_MissingPaymentId_ThrowsJobExecutionException() {
        // Given
        jobDataMap.remove("paymentId");

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, never()).executeScheduledPaymentRelease(any(UUID.class));
    }

    @Test
    @DisplayName("Should handle null payment ID in job data")
    void execute_NullPaymentId_ThrowsJobExecutionException() {
        // Given
        jobDataMap.put("paymentId", null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, never()).executeScheduledPaymentRelease(any(UUID.class));
    }

    @Test
    @DisplayName("Should handle invalid payment ID format")
    void execute_InvalidPaymentIdFormat_ThrowsJobExecutionException() {
        // Given
        jobDataMap.put("paymentId", "invalid-uuid");

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, never()).executeScheduledPaymentRelease(any(UUID.class));
    }

    @Test
    @DisplayName("Should handle null job data map")
    void execute_NullJobDataMap_ThrowsJobExecutionException() {
        // Given
        when(jobDetail.getJobDataMap()).thenReturn(null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, never()).executeScheduledPaymentRelease(any(UUID.class));
    }

    @Test
    @DisplayName("Should handle null job detail")
    void execute_NullJobDetail_ThrowsJobExecutionException() {
        // Given
        when(context.getJobDetail()).thenReturn(null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, never()).executeScheduledPaymentRelease(any(UUID.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "invalid-uuid", "12345678-1234-1234-1234-12345678901z", "not-a-uuid-at-all"})
    @DisplayName("Should handle invalid payment ID formats")
    void execute_InvalidPaymentIdFormats_ThrowsJobExecutionException(String invalidPaymentId) {
        // Given
        jobDataMap.put("paymentId", invalidPaymentId);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, never()).executeScheduledPaymentRelease(any(UUID.class));
    }

    @Test
    @DisplayName("Should execute with valid UUID and null taskId")
    void execute_ValidPaymentIdNullTaskId_Success() {
        // Given
        jobDataMap.put("taskId", null);

        // When
        assertDoesNotThrow(() -> paymentReleaseJob.execute(context));

        // Then
        verify(paymentService, times(1)).executeScheduledPaymentRelease(testPaymentId);
    }

    @Test
    @DisplayName("Should handle various payment service exceptions")
    void execute_VariousPaymentServiceExceptions_ThrowsJobExecutionException() {
        // Given
        doThrow(new IllegalArgumentException("Invalid payment ID")).when(paymentService)
                .executeScheduledPaymentRelease(any(UUID.class));

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            paymentReleaseJob.execute(context);
        });

        assertEquals("Failed to execute payment release job", exception.getMessage());
        verify(paymentService, times(1)).executeScheduledPaymentRelease(testPaymentId);
    }
}