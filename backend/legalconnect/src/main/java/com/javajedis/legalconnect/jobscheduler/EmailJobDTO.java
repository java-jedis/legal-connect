package com.javajedis.legalconnect.jobscheduler;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailJobDTO {
    private UUID taskId;
    private String emailTemplate;
    private String receiverEmailAddress;
    private String subject;
    private Map<String, Object> templateVariables;
    private OffsetDateTime dateTime;
}
