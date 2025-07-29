package com.javajedis.legalconnect.jobscheduler;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SuppressWarnings("javaarchitecture:S7091") // Cyclic dependency warning. Will Fix later
public class JobSchedulerService {
    private static final String WEBPUSH_JOB_PREFIX = "webpush_";
    private static final String EMAIL_JOB_PREFIX = "email_";
    private static final String PAYMENT_RELEASE_JOB_PREFIX = "payment release_";
    private static final String TRIGGER_PREFIX = "trigger_";
    private static final String TASK_ID_STRING = "taskId";


    private final Scheduler scheduler;

    public JobSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Schedule WebPush notification for a specific user
     */
    public void scheduleWebPushNotification(WebPushJobDTO webPushJobDTO) {
        if (webPushJobDTO.getTaskId() == null || webPushJobDTO.getRecipientId() == null || webPushJobDTO.getDateTime() == null) {
            log.warn("Cannot schedule WebPush notification: missing required fields (taskId, recipientId, or dateTime)");
            return;
        }

        try {
            String jobId = WEBPUSH_JOB_PREFIX + webPushJobDTO.getTaskId() + "_" + webPushJobDTO.getRecipientId();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(TASK_ID_STRING, webPushJobDTO.getTaskId().toString());
            jobDataMap.put("recipientId", webPushJobDTO.getRecipientId().toString());
            jobDataMap.put("content", webPushJobDTO.getContent());
            jobDataMap.put("dateTime", webPushJobDTO.getDateTime().toString());

            JobDetail job = JobBuilder.newJob(WebPushJob.class)
                    .withIdentity(jobId)
                    .setJobData(jobDataMap)
                    .storeDurably()
                    .requestRecovery()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_PREFIX + jobId)
                    .startAt(Date.from(webPushJobDTO.getDateTime().toInstant()))
                    .build();

            scheduler.scheduleJob(job, trigger);
            log.debug("Scheduled WebPush notification job: {} for time: {}", jobId, webPushJobDTO.getDateTime());

        } catch (SchedulerException e) {
            log.error("Failed to schedule WebPush notification for task: {} and recipient: {}",
                    webPushJobDTO.getTaskId(), webPushJobDTO.getRecipientId(), e);
        }
    }

    /**
     * Schedule Email notification for a specific user
     */
    public void scheduleEmailNotification(EmailJobDTO emailJobDTO) {
        if (emailJobDTO.getTaskId() == null || emailJobDTO.getReceiverEmailAddress() == null || emailJobDTO.getDateTime() == null) {
            log.warn("Cannot schedule Email notification: missing required fields (taskId, receiverEmailAddress, or dateTime)");
            return;
        }

        try {
            String jobId = EMAIL_JOB_PREFIX + emailJobDTO.getTaskId() + "_" + emailJobDTO.getReceiverEmailAddress();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(TASK_ID_STRING, emailJobDTO.getTaskId().toString());
            jobDataMap.put("emailTemplate", emailJobDTO.getEmailTemplate());
            jobDataMap.put("receiverEmailAddress", emailJobDTO.getReceiverEmailAddress());
            jobDataMap.put("subject", emailJobDTO.getSubject());
            jobDataMap.put("dateTime", emailJobDTO.getDateTime().toString());

            String templateVariablesJson = serializeTemplateVariables(emailJobDTO);
            if (templateVariablesJson == null) {
                return;
            }
            jobDataMap.put("templateVariables", templateVariablesJson);

            JobDetail job = JobBuilder.newJob(EmailJob.class)
                    .withIdentity(jobId)
                    .setJobData(jobDataMap)
                    .storeDurably()
                    .requestRecovery()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_PREFIX + jobId)
                    .startAt(Date.from(emailJobDTO.getDateTime().toInstant()))
                    .build();

            scheduler.scheduleJob(job, trigger);
            log.debug("Scheduled Email notification job: {} for time: {}", jobId, emailJobDTO.getDateTime());

        } catch (SchedulerException e) {
            log.error("Failed to schedule Email notification for task: {} and email: {}",
                    emailJobDTO.getTaskId(), emailJobDTO.getReceiverEmailAddress(), e);
        }
    }

    /**
     * Update WebPush notification job
     */
    public void updateWebPushNotification(WebPushJobDTO webPushJobDTO) {
        try {
            String jobId = WEBPUSH_JOB_PREFIX + webPushJobDTO.getTaskId() + "_" + webPushJobDTO.getRecipientId();
            scheduler.deleteJob(JobKey.jobKey(jobId));

            scheduleWebPushNotification(webPushJobDTO);
            log.debug("Updated WebPush notification job: {}", jobId);

        } catch (SchedulerException e) {
            log.error("Failed to update WebPush notification for task: {} and recipient: {}",
                    webPushJobDTO.getTaskId(), webPushJobDTO.getRecipientId(), e);
        }
    }

    /**
     * Update Email notification job
     */
    public void updateEmailNotification(EmailJobDTO emailJobDTO) {
        try {
            String jobId = EMAIL_JOB_PREFIX + emailJobDTO.getTaskId() + "_" + emailJobDTO.getReceiverEmailAddress();
            scheduler.deleteJob(JobKey.jobKey(jobId));

            scheduleEmailNotification(emailJobDTO);
            log.debug("Updated Email notification job: {}", jobId);

        } catch (SchedulerException e) {
            log.error("Failed to update Email notification for task: {} and email: {}",
                    emailJobDTO.getTaskId(), emailJobDTO.getReceiverEmailAddress(), e);
        }
    }

    /**
     * Delete WebPush notification job
     */
    public void deleteWebPushNotification(UUID taskId, UUID recipientId) {
        try {
            String jobId = WEBPUSH_JOB_PREFIX + taskId + "_" + recipientId;
            scheduler.deleteJob(JobKey.jobKey(jobId));
            log.debug("Deleted WebPush notification job: {}", jobId);
        } catch (SchedulerException e) {
            log.error("Failed to delete WebPush notification job for task: {} and recipient: {}",
                    taskId, recipientId, e);
        }
    }

    /**
     * Delete Email notification job
     */
    public void deleteEmailNotification(UUID taskId, String receiverEmail) {
        try {
            String jobId = EMAIL_JOB_PREFIX + taskId + "_" + receiverEmail;
            scheduler.deleteJob(JobKey.jobKey(jobId));
            log.debug("Deleted Email notification job: {}", jobId);
        } catch (SchedulerException e) {
            log.error("Failed to delete Email notification job for task: {} and email: {}",
                    taskId, receiverEmail, e);
        }
    }

    /**
     * Delete all jobs for a specific task
     */
    public void deleteAllJobsForTask(UUID taskId) {
        try {
            // Get all job keys and filter by task ID
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyGroup())) {
                String jobName = jobKey.getName();
                if (jobName.startsWith(WEBPUSH_JOB_PREFIX + taskId + "_") ||
                        jobName.startsWith(EMAIL_JOB_PREFIX + taskId + "_")) {
                    scheduler.deleteJob(jobKey);
                    log.debug("Deleted job: {}", jobName);
                }
            }
            log.info("Deleted all jobs for task: {}", taskId);
        } catch (SchedulerException e) {
            log.error("Failed to delete all jobs for task: {}", taskId, e);
        }
    }

    /**
     * Check if a WebPush notification job exists
     */
    public boolean webPushJobExists(UUID taskId, UUID recipientId) {
        try {
            String jobId = WEBPUSH_JOB_PREFIX + taskId + "_" + recipientId;
            return scheduler.checkExists(JobKey.jobKey(jobId));
        } catch (SchedulerException e) {
            log.error("Failed to check WebPush job existence for task: {} and recipient: {}",
                    taskId, recipientId, e);
            return false;
        }
    }

    /**
     * Check if an Email notification job exists
     */
    public boolean emailJobExists(UUID taskId, String receiverEmail) {
        try {
            String jobId = EMAIL_JOB_PREFIX + taskId + "_" + receiverEmail;
            return scheduler.checkExists(JobKey.jobKey(jobId));
        } catch (SchedulerException e) {
            log.error("Failed to check Email job existence for task: {} and email: {}",
                    taskId, receiverEmail, e);
            return false;
        }
    }

    /**
     * Schedule payment release job
     */
    public void schedulePaymentRelease(UUID paymentId, OffsetDateTime releaseAt) {
        try {
            String jobId = PAYMENT_RELEASE_JOB_PREFIX + paymentId.toString();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(TASK_ID_STRING, paymentId.toString());
            jobDataMap.put("paymentId", paymentId.toString());

            JobDetail job = JobBuilder.newJob(PaymentReleaseJob.class)
                    .withIdentity(jobId)
                    .setJobData(jobDataMap)
                    .storeDurably()
                    .requestRecovery()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_PREFIX + jobId)
                    .startAt(Date.from(releaseAt.toInstant()))
                    .build();

            scheduler.scheduleJob(job, trigger);
            log.debug("Scheduled Payment release job: {} for time: {}", jobId, releaseAt);

        } catch (SchedulerException e) {
            log.error("Failed to schedule Payment release for task: {} ",
                    paymentId, e);
        }
    }

    /**
     * Delete Payment release job
     */
    public void deletePaymentRelease(UUID taskId) {
        try {
            String jobId = PAYMENT_RELEASE_JOB_PREFIX + taskId;
            scheduler.deleteJob(JobKey.jobKey(jobId));
            log.debug("Deleted Payment release notification job: {}", jobId);
        } catch (SchedulerException e) {
            log.error("Failed to delete Payment Release job for task: {}",
                    taskId, e);
        }
    }

    /**
     * Check if a Payment release job exists
     */
    public boolean paymentReleaseJobExists(UUID taskId) {
        try {
            String jobId = PAYMENT_RELEASE_JOB_PREFIX + taskId;
            return scheduler.checkExists(JobKey.jobKey(jobId));
        } catch (SchedulerException e) {
            log.error("Failed to check Payment release existence for task: {}",
                    taskId, e);
            return false;
        }
    }

    /**
     * Get scheduler status information
     */
    public void logSchedulerStatus() {
        try {
            log.info("Scheduler Name: {}", scheduler.getSchedulerName());
            log.info("Scheduler Instance ID: {}", scheduler.getSchedulerInstanceId());
            log.info("Scheduler State: {}", scheduler.getMetaData().getRunningSince());
            log.info("Total Jobs: {}", scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
        } catch (SchedulerException e) {
            log.error("Failed to get scheduler status", e);
        }
    }

    /**
     * Serialize template variables to JSON string
     */
    private String serializeTemplateVariables(EmailJobDTO emailJobDTO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(emailJobDTO.getTemplateVariables());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize template variables for task: {}", emailJobDTO.getTaskId(), e);
            return null;
        }
    }
}
