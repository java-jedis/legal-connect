package com.javajedis.legalconnect.jobscheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobSchedulerService Tests")
class JobSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private SchedulerMetaData schedulerMetaData;

    @InjectMocks
    private JobSchedulerService jobSchedulerService;

    private WebPushJobDTO webPushJobDTO;
    private EmailJobDTO emailJobDTO;
    private UUID testTaskId;
    private UUID testRecipientId;
    private String testReceiverEmail;
    private UUID testPaymentId;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testRecipientId = UUID.randomUUID();
        testReceiverEmail = "test@example.com";
        testPaymentId = UUID.randomUUID();

        webPushJobDTO = new WebPushJobDTO();
        webPushJobDTO.setTaskId(testTaskId);
        webPushJobDTO.setRecipientId(testRecipientId);
        webPushJobDTO.setContent("Test web push content");
        webPushJobDTO.setDateTime(OffsetDateTime.now().plusHours(1));

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", "John Doe");
        templateVariables.put("otp", "123456");

        emailJobDTO = new EmailJobDTO();
        emailJobDTO.setTaskId(testTaskId);
        emailJobDTO.setEmailTemplate("test-template");
        emailJobDTO.setReceiverEmailAddress(testReceiverEmail);
        emailJobDTO.setSubject("Test Subject");
        emailJobDTO.setTemplateVariables(templateVariables);
        emailJobDTO.setDateTime(OffsetDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should schedule web push notification successfully")
    void scheduleWebPushNotification_Success() throws SchedulerException {
        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when scheduling web push notification")
    void scheduleWebPushNotification_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should schedule email notification successfully")
    void scheduleEmailNotification_Success() throws SchedulerException {
        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when scheduling email notification")
    void scheduleEmailNotification_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle null template variables in email notification")
    void scheduleEmailNotification_NullTemplateVariables_Success() throws SchedulerException {
        // Given
        emailJobDTO.setTemplateVariables(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle empty template variables in email notification")
    void scheduleEmailNotification_EmptyTemplateVariables_Success() throws SchedulerException {
        // Given
        emailJobDTO.setTemplateVariables(new HashMap<>());

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should update web push notification successfully")
    void updateWebPushNotification_Success() throws SchedulerException {
        // Given
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.updateWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when updating web push notification")
    void updateWebPushNotification_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .deleteJob(any(JobKey.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.updateWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should update email notification successfully")
    void updateEmailNotification_Success() throws SchedulerException {
        // Given
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.updateEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when updating email notification")
    void updateEmailNotification_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .deleteJob(any(JobKey.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.updateEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should delete web push notification successfully")
    void deleteWebPushNotification_Success() throws SchedulerException {
        // Given
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deleteWebPushNotification(testTaskId, testRecipientId));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when deleting web push notification")
    void deleteWebPushNotification_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .deleteJob(any(JobKey.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deleteWebPushNotification(testTaskId, testRecipientId));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("Should delete email notification successfully")
    void deleteEmailNotification_Success() throws SchedulerException {
        // Given
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deleteEmailNotification(testTaskId, testReceiverEmail));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when deleting email notification")
    void deleteEmailNotification_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .deleteJob(any(JobKey.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deleteEmailNotification(testTaskId, testReceiverEmail));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("Should delete all jobs for task successfully")
    void deleteAllJobsForTask_Success() throws SchedulerException {
        // Given
        Set<JobKey> jobKeys = new HashSet<>();
        jobKeys.add(JobKey.jobKey("webpush_" + testTaskId + "_" + testRecipientId));
        jobKeys.add(JobKey.jobKey("email_" + testTaskId + "_" + testReceiverEmail));
        jobKeys.add(JobKey.jobKey("other_job_key"));

        when(scheduler.getJobKeys(any(GroupMatcher.class))).thenReturn(jobKeys);
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deleteAllJobsForTask(testTaskId));

        // Then
        verify(scheduler, times(1)).getJobKeys(any(GroupMatcher.class));
        verify(scheduler, times(2)).deleteJob(any(JobKey.class)); // Only 2 jobs match the task ID
    }

    @Test
    @DisplayName("Should handle scheduler exception when deleting all jobs for task")
    void deleteAllJobsForTask_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .getJobKeys(any(GroupMatcher.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deleteAllJobsForTask(testTaskId));

        // Then
        verify(scheduler, times(1)).getJobKeys(any(GroupMatcher.class));
        verify(scheduler, never()).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("Should check web push job exists successfully")
    void webPushJobExists_Success() throws SchedulerException {
        // Given
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        // When
        boolean exists = jobSchedulerService.webPushJobExists(testTaskId, testRecipientId);

        // Then
        assertTrue(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should return false when web push job does not exist")
    void webPushJobExists_NotExists() throws SchedulerException {
        // Given
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // When
        boolean exists = jobSchedulerService.webPushJobExists(testTaskId, testRecipientId);

        // Then
        assertFalse(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when checking web push job exists")
    void webPushJobExists_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .checkExists(any(JobKey.class));

        // When
        boolean exists = jobSchedulerService.webPushJobExists(testTaskId, testRecipientId);

        // Then
        assertFalse(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should check email job exists successfully")
    void emailJobExists_Success() throws SchedulerException {
        // Given
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        // When
        boolean exists = jobSchedulerService.emailJobExists(testTaskId, testReceiverEmail);

        // Then
        assertTrue(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should return false when email job does not exist")
    void emailJobExists_NotExists() throws SchedulerException {
        // Given
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // When
        boolean exists = jobSchedulerService.emailJobExists(testTaskId, testReceiverEmail);

        // Then
        assertFalse(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when checking email job exists")
    void emailJobExists_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .checkExists(any(JobKey.class));

        // When
        boolean exists = jobSchedulerService.emailJobExists(testTaskId, testReceiverEmail);

        // Then
        assertFalse(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should log scheduler status successfully")
    void logSchedulerStatus_Success() throws SchedulerException {
        // Given
        when(scheduler.getSchedulerName()).thenReturn("TestScheduler");
        when(scheduler.getSchedulerInstanceId()).thenReturn("TestInstance");
        when(scheduler.getMetaData()).thenReturn(schedulerMetaData);
        when(schedulerMetaData.getRunningSince()).thenReturn(new java.util.Date());
        
        Set<JobKey> jobKeys = new HashSet<>();
        jobKeys.add(JobKey.jobKey("job1"));
        jobKeys.add(JobKey.jobKey("job2"));
        when(scheduler.getJobKeys(any(GroupMatcher.class))).thenReturn(jobKeys);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.logSchedulerStatus());

        // Then
        verify(scheduler, times(1)).getSchedulerName();
        verify(scheduler, times(1)).getSchedulerInstanceId();
        verify(scheduler, times(1)).getMetaData();
        verify(scheduler, times(1)).getJobKeys(any(GroupMatcher.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when logging scheduler status")
    void logSchedulerStatus_SchedulerException() throws SchedulerException {
        // Given
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .getSchedulerName();

        // When
        assertDoesNotThrow(() -> jobSchedulerService.logSchedulerStatus());

        // Then
        verify(scheduler, times(1)).getSchedulerName();
    }

    @Test
    @DisplayName("Should handle null task ID in web push notification")
    void scheduleWebPushNotification_NullTaskId() throws SchedulerException {
        // Given
        webPushJobDTO.setTaskId(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle null recipient ID in web push notification")
    void scheduleWebPushNotification_NullRecipientId() throws SchedulerException {
        // Given
        webPushJobDTO.setRecipientId(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle null date time in web push notification")
    void scheduleWebPushNotification_NullDateTime() throws SchedulerException {
        // Given
        webPushJobDTO.setDateTime(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle null task ID in email notification")
    void scheduleEmailNotification_NullTaskId() throws SchedulerException {
        // Given
        emailJobDTO.setTaskId(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle null receiver email in email notification")
    void scheduleEmailNotification_NullReceiverEmail() throws SchedulerException {
        // Given
        emailJobDTO.setReceiverEmailAddress(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle null date time in email notification")
    void scheduleEmailNotification_NullDateTime() throws SchedulerException {
        // Given
        emailJobDTO.setDateTime(null);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should schedule web push notification with past date time")
    void scheduleWebPushNotification_PastDateTime_Success() throws SchedulerException {
        // Given
        webPushJobDTO.setDateTime(OffsetDateTime.now().minusHours(1));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleWebPushNotification(webPushJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should schedule email notification with past date time")
    void scheduleEmailNotification_PastDateTime_Success() throws SchedulerException {
        // Given
        emailJobDTO.setDateTime(OffsetDateTime.now().minusHours(1));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.scheduleEmailNotification(emailJobDTO));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    // Payment Release Scheduling Tests

    @ParameterizedTest
    @ValueSource(ints = {1, 24, 48, 12})
    @DisplayName("Should schedule payment release successfully with different time offsets")
    void schedulePaymentRelease_Success(int hoursOffset) throws SchedulerException {
        // Given
        OffsetDateTime releaseAt = OffsetDateTime.now().plusHours(hoursOffset);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.schedulePaymentRelease(testPaymentId, releaseAt));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle scheduler exception when scheduling payment release")
    void schedulePaymentRelease_SchedulerException() throws SchedulerException {
        // Given
        OffsetDateTime releaseAt = OffsetDateTime.now().plusDays(1);
        doThrow(new SchedulerException("Scheduler error")).when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.schedulePaymentRelease(testPaymentId, releaseAt));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should handle graceful error handling for scheduling failures")
    void schedulePaymentRelease_SchedulingFailure_GracefulHandling() throws SchedulerException {
        // Given
        OffsetDateTime releaseAt = OffsetDateTime.now().plusHours(6);
        doThrow(new SchedulerException("Job scheduling failed")).when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.schedulePaymentRelease(testPaymentId, releaseAt));

        // Then
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("Should validate graceful error handling without propagation")
    void schedulePaymentRelease_ErrorHandling_NoPropagation() throws SchedulerException {
        // Given
        OffsetDateTime releaseAt = OffsetDateTime.now().plusDays(3);
        doThrow(new SchedulerException("Critical scheduler error")).when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> jobSchedulerService.schedulePaymentRelease(testPaymentId, releaseAt));
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    // Payment Release Deletion Tests

    @Test
    @DisplayName("Should delete payment release successfully")
    void deletePaymentRelease_Success() throws SchedulerException {
        // Given
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deletePaymentRelease(testPaymentId));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Scheduler error", "Job deletion failed", "Critical deletion error"})
    @DisplayName("Should handle scheduler exceptions when deleting payment release")
    void deletePaymentRelease_SchedulerException(String errorMessage) throws SchedulerException {
        // Given
        doThrow(new SchedulerException(errorMessage)).when(scheduler)
                .deleteJob(any(JobKey.class));

        // When
        assertDoesNotThrow(() -> jobSchedulerService.deletePaymentRelease(testPaymentId));

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    // Payment Release Existence Check Tests

    @Test
    @DisplayName("Should return true when payment release job exists")
    void paymentReleaseJobExists_JobExists_ReturnsTrue() throws SchedulerException {
        // Given
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        // When
        boolean exists = jobSchedulerService.paymentReleaseJobExists(testPaymentId);

        // Then
        assertTrue(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @Test
    @DisplayName("Should return false when payment release job does not exist")
    void paymentReleaseJobExists_JobNotExists_ReturnsFalse() throws SchedulerException {
        // Given
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // When
        boolean exists = jobSchedulerService.paymentReleaseJobExists(testPaymentId);

        // Then
        assertFalse(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Scheduler error", "Existence check failed", "Critical existence check error"})
    @DisplayName("Should handle scheduler exceptions when checking payment release job exists")
    void paymentReleaseJobExists_SchedulerException(String errorMessage) throws SchedulerException {
        // Given
        doThrow(new SchedulerException(errorMessage)).when(scheduler)
                .checkExists(any(JobKey.class));

        // When
        boolean exists = jobSchedulerService.paymentReleaseJobExists(testPaymentId);

        // Then
        assertFalse(exists);
        verify(scheduler, times(1)).checkExists(any(JobKey.class));
    }
}