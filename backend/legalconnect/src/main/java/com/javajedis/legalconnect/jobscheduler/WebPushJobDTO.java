package com.javajedis.legalconnect.jobscheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebPushJobDTO {
    private UUID taskId;
    private UUID recipientId;
    private String content;
    private OffsetDateTime dateTime;
}
