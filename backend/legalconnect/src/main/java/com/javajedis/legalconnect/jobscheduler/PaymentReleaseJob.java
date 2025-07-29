package com.javajedis.legalconnect.jobscheduler;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.javajedis.legalconnect.payment.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReleaseJob implements Job {
    private final PaymentService paymentService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            var dataMap = context.getJobDetail().getJobDataMap();
            paymentService.executeScheduledPaymentRelease(UUID.fromString(dataMap.get("paymentId").toString()));
            log.info("Payment release job completed successfully for {}", dataMap.get("taskId"));
        } catch (Exception e) {
            log.error("Error executing payment release job: {}", e.getMessage(), e);
            throw new JobExecutionException("Failed to execute payment release job", e, false);
        }
    }
}
