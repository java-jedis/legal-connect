package com.javajedis.legalconnect.jobscheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.service.EmailService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("EmailJob Tests")
class EmailJobTest {

    @Mock
    private EmailService emailService;

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @InjectMocks
    private EmailJob emailJob;

    private JobDataMap jobDataMap;
    private UUID testTaskId;
    private String testEmailTemplate;
    private String testReceiverEmail;
    private String testSubject;
    private Map<String, Object> testTemplateVariables;
    private String testTemplateVariablesJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        testTaskId = UUID.randomUUID();
        testEmailTemplate = "test-template";
        testReceiverEmail = "test@example.com";
        testSubject = "Test Subject";
        
        testTemplateVariables = new HashMap<>();
        testTemplateVariables.put("name", "John Doe");
        testTemplateVariables.put("otp", "123456");
        
        ObjectMapper objectMapper = new ObjectMapper();
        testTemplateVariablesJson = objectMapper.writeValueAsString(testTemplateVariables);

        jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", testTaskId.toString());
        jobDataMap.put("emailTemplate", testEmailTemplate);
        jobDataMap.put("receiverEmailAddress", testReceiverEmail);
        jobDataMap.put("subject", testSubject);
        jobDataMap.put("templateVariables", testTemplateVariablesJson);

        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    }

    @Test
    @DisplayName("Should execute email job successfully")
    void execute_Success() {
        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(testReceiverEmail),
                eq(testSubject),
                eq(testEmailTemplate),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should execute email job with empty template variables")
    void execute_EmptyTemplateVariables_Success() throws JsonProcessingException {
        // Given
        Map<String, Object> emptyVariables = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String emptyVariablesJson = objectMapper.writeValueAsString(emptyVariables);
        jobDataMap.put("templateVariables", emptyVariablesJson);

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                testReceiverEmail,
                testSubject,
                testEmailTemplate,
                emptyVariables
        );
    }

    @Test
    @DisplayName("Should handle invalid JSON in template variables")
    void execute_InvalidTemplateVariablesJson_ThrowsJobExecutionException() {
        // Given
        jobDataMap.put("templateVariables", "invalid-json");

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            emailJob.execute(context);
        });

        assertEquals("Failed to execute Email notification job", exception.getMessage());
        verify(emailService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should handle null template variables JSON")
    void execute_NullTemplateVariablesJson_ThrowsJobExecutionException() {
        // Given
        jobDataMap.put("templateVariables", null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            emailJob.execute(context);
        });

        assertEquals("Failed to execute Email notification job", exception.getMessage());
        verify(emailService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should handle email service exception")
    void execute_EmailServiceException_ThrowsJobExecutionException() {
        // Given
        doThrow(new RuntimeException("Email service error")).when(emailService)
                .sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            emailJob.execute(context);
        });

        assertEquals("Failed to execute Email notification job", exception.getMessage());
        verify(emailService, times(1)).sendTemplateEmail(
                eq(testReceiverEmail),
                eq(testSubject),
                eq(testEmailTemplate),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should handle missing task ID in job data")
    void execute_MissingTaskId_Success() {
        // Given
        jobDataMap.remove("taskId");

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(testReceiverEmail),
                eq(testSubject),
                eq(testEmailTemplate),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should handle missing email template in job data")
    void execute_MissingEmailTemplate_Success() {
        // Given
        jobDataMap.remove("emailTemplate");

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(testReceiverEmail),
                eq(testSubject),
                eq(null),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should handle missing receiver email in job data")
    void execute_MissingReceiverEmail_Success() {
        // Given
        jobDataMap.remove("receiverEmailAddress");

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(null),
                eq(testSubject),
                eq(testEmailTemplate),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should handle missing subject in job data")
    void execute_MissingSubject_Success() {
        // Given
        jobDataMap.remove("subject");

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(testReceiverEmail),
                eq(null),
                eq(testEmailTemplate),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should handle null job data map")
    void execute_NullJobDataMap_ThrowsJobExecutionException() {
        // Given
        when(jobDetail.getJobDataMap()).thenReturn(null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            emailJob.execute(context);
        });

        assertEquals("Failed to execute Email notification job", exception.getMessage());
        verify(emailService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should handle null job detail")
    void execute_NullJobDetail_ThrowsJobExecutionException() {
        // Given - Create a fresh context mock to avoid unnecessary stubbing
        JobExecutionContext nullJobDetailContext = org.mockito.Mockito.mock(JobExecutionContext.class);
        when(nullJobDetailContext.getJobDetail()).thenReturn(null);

        // When & Then
        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> {
            emailJob.execute(nullJobDetailContext);
        });

        assertEquals("Failed to execute Email notification job", exception.getMessage());
        verify(emailService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should execute with complex template variables")
    void execute_ComplexTemplateVariables_Success() throws JsonProcessingException {
        // Given
        Map<String, Object> complexVariables = new HashMap<>();
        complexVariables.put("user", Map.of("name", "John Doe", "id", 123));
        complexVariables.put("items", java.util.Arrays.asList("item1", "item2", "item3"));
        complexVariables.put("isActive", true);
        complexVariables.put("count", 42);
        
        ObjectMapper objectMapper = new ObjectMapper();
        String complexVariablesJson = objectMapper.writeValueAsString(complexVariables);
        jobDataMap.put("templateVariables", complexVariablesJson);

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(testReceiverEmail),
                eq(testSubject),
                eq(testEmailTemplate),
                any(Map.class)
        );
    }

    @Test
    @DisplayName("Should handle empty string values in job data")
    void execute_EmptyStringValues_Success() {
        // Given
        jobDataMap.put("emailTemplate", "");
        jobDataMap.put("receiverEmailAddress", "");
        jobDataMap.put("subject", "");

        // When
        assertDoesNotThrow(() -> emailJob.execute(context));

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq(""),
                eq(""),
                eq(""),
                anyMap()
        );
    }
}