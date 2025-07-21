package com.javajedis.legalconnect.jobscheduler;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.javajedis.legalconnect.notifications.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebPushJob implements Job {
    private final NotificationService notificationService;

    public WebPushJob(NotificationService notificationService){
        this.notificationService = notificationService;
    }
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            var dataMap = context.getJobDetail().getJobDataMap();
            
            notificationService.sendNotification(UUID.fromString(dataMap.get("recipientId").toString()), dataMap.get("content").toString());
            log.info("WebPush notification job completed successfully for schedule: {}", dataMap.get("taskId"));
            
        } catch (Exception e) {
            log.error("Error executing WebPush notification job: {}", e.getMessage(), e);
            throw new JobExecutionException("Failed to execute WebPush notification job", e, false);
        }
    }
}
