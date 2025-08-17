package com.javajedis.legalconnect.jobscheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.javajedis.legalconnect.notifications.NotificationService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("WebPushJob Tests")
class WebPushJobTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @InjectMocks
    private WebPushJob webPushJob;

    private JobDataMap jobDataMap;
    private UUID testTaskId;
    private UUID testRecipientId;
    private String testContent;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testRecipientId = UUID.randomUUID();
        testContent = "Test notification content";

        jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", testTaskId.toString());
        jobDataMap.put("recipientId", testRecipientId.toString());
        jobDataMap.put("content", testContent);

        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    }

    @Test
    @DisplayName("Should execute web push job successfully")
    void execute_Success() {
        // When
        assertDoesNotThrow(() -> webPushJob.execute(context));

        // Then
        verify(notificationService, times(1)).sendNotification(
                testRecipientId,
                testContent
        );
    }

    @Test
    @DisplayName("Should handle notification service exception")
    void execute_NotificationServiceException_ThrowsJobExecutionException() {
        // Given
        doThrow(new RuntimeException("Notification service error")).when(notificationService)
                .sendNotification(any(UUID.class), anyString());

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, times(1)).sendNotification(
                testRecipientId,
                testContent
        );
    }

    @Test
    @DisplayName("Should handle missing task ID in job data")
    void execute_MissingTaskId_Success() {
        // Given
        jobDataMap.remove("taskId");

        // When
        assertDoesNotThrow(() -> webPushJob.execute(context));

        // Then
        verify(notificationService, times(1)).sendNotification(
                testRecipientId,
                testContent
        );
    }

    @Test
    @DisplayName("Should handle missing recipient ID in job data")
    void execute_MissingRecipientId_ThrowsJobExecutionException() {
        // Given
        jobDataMap.remove("recipientId");

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should handle missing content in job data")
    void execute_MissingContent_ThrowsJobExecutionException() {
        // Given
        jobDataMap.remove("content");

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should handle invalid recipient ID format")
    void execute_InvalidRecipientIdFormat_ThrowsJobExecutionException() {
        // Given
        jobDataMap.put("recipientId", "invalid-uuid");

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"recipientId", "content"})
    @DisplayName("Should handle null values in job data")
    void execute_NullValues_ThrowsJobExecutionException(String fieldName) {
        // Given
        jobDataMap.put(fieldName, null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should handle empty content")
    void execute_EmptyContent_Success() {
        // Given
        jobDataMap.put("content", "");

        // When
        assertDoesNotThrow(() -> webPushJob.execute(context));

        // Then
        verify(notificationService, times(1)).sendNotification(
                testRecipientId,
                ""
        );
    }

    @Test
    @DisplayName("Should handle null job data map")
    void execute_NullJobDataMap_ThrowsJobExecutionException() {
        // Given
        when(jobDetail.getJobDataMap()).thenReturn(null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should handle null job detail")
    void execute_NullJobDetail_ThrowsJobExecutionException() {
        // Given
        when(context.getJobDetail()).thenReturn(null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            webPushJob.execute(context);
        });

        assertEquals("Failed to execute WebPush notification job", exception.getMessage());
        verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "This is a very long notification content that might be used in real scenarios. This is a very long notification content that might be used in real scenarios. This is a very long notification content that might be used in real scenarios. ",
        "Content with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?",
        "Unicode content: 你好世界 🌍 العالم",
        "   \t\n   "
    })
    @DisplayName("Should execute with various content types")
    void execute_VariousContentTypes_Success(String content) {
        // Given
        jobDataMap.put("content", content);

        // When
        assertDoesNotThrow(() -> webPushJob.execute(context));

        // Then
        verify(notificationService, times(1)).sendNotification(
                testRecipientId,
                content
        );
    }
}