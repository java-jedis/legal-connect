package com.javajedis.legalconnect.jobscheduler;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailJob implements Job {
    private final EmailService emailService;

    public EmailJob(EmailService emailService){
        this.emailService = emailService;
    }
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            var dataMap = context.getJobDetail().getJobDataMap();
            
            // Extract data from JobDataMap
            String taskId = dataMap.getString("taskId");
            String emailTemplate = dataMap.getString("emailTemplate");
            String receiverEmailAddress = dataMap.getString("receiverEmailAddress");
            String subject = dataMap.getString("subject");

            processAndSendEmail(taskId, emailTemplate, receiverEmailAddress, subject, dataMap);

            log.info("Email notification job completed successfully for task: {}", taskId);
            
        } catch (Exception e) {
            log.error("Error executing Email notification job: {}", e.getMessage(), e);
            throw new JobExecutionException("Failed to execute Email notification job", e, false);
        }
    }

    private void processAndSendEmail(String taskId, String emailTemplate, String receiverEmailAddress, 
                                   String subject, org.quartz.JobDataMap dataMap) throws JobExecutionException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String templateVariablesJson = dataMap.getString("templateVariables");
            Map<String, Object> templateVariables = objectMapper.readValue(templateVariablesJson, new TypeReference<Map<String, Object>>() {});
            emailService.sendTemplateEmail(receiverEmailAddress, subject, emailTemplate, templateVariables);
        } catch (Exception e) {
            log.error("Failed to deserialize template variables or send email for task: {}", taskId, e);
            throw new JobExecutionException("Failed to process email job", e, false);
        }
    }
}
