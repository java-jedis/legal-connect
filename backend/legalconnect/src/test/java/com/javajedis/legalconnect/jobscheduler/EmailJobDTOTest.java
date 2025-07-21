package com.javajedis.legalconnect.jobscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmailJobDTO Tests")
class EmailJobDTOTest {

    private EmailJobDTO emailJobDTO;
    private UUID testTaskId;
    private String testEmailTemplate;
    private String testReceiverEmailAddress;
    private String testSubject;
    private Map<String, Object> testTemplateVariables;
    private OffsetDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testEmailTemplate = "test-template";
        testReceiverEmailAddress = "test@example.com";
        testSubject = "Test Subject";
        testTemplateVariables = new HashMap<>();
        testTemplateVariables.put("name", "John Doe");
        testTemplateVariables.put("otp", "123456");
        testDateTime = OffsetDateTime.now();

        emailJobDTO = new EmailJobDTO();
    }

    @Test
    @DisplayName("Should create EmailJobDTO with no-args constructor")
    void noArgsConstructor_Success() {
        // Given & When
        EmailJobDTO dto = new EmailJobDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getTaskId());
        assertNull(dto.getEmailTemplate());
        assertNull(dto.getReceiverEmailAddress());
        assertNull(dto.getSubject());
        assertNull(dto.getTemplateVariables());
        assertNull(dto.getDateTime());
    }

    @Test
    @DisplayName("Should create EmailJobDTO with all-args constructor")
    void allArgsConstructor_Success() {
        // Given & When
        EmailJobDTO dto = new EmailJobDTO(
                testTaskId,
                testEmailTemplate,
                testReceiverEmailAddress,
                testSubject,
                testTemplateVariables,
                testDateTime
        );

        // Then
        assertNotNull(dto);
        assertEquals(testTaskId, dto.getTaskId());
        assertEquals(testEmailTemplate, dto.getEmailTemplate());
        assertEquals(testReceiverEmailAddress, dto.getReceiverEmailAddress());
        assertEquals(testSubject, dto.getSubject());
        assertEquals(testTemplateVariables, dto.getTemplateVariables());
        assertEquals(testDateTime, dto.getDateTime());
    }

    @Test
    @DisplayName("Should set and get task ID")
    void setAndGetTaskId_Success() {
        // When
        emailJobDTO.setTaskId(testTaskId);

        // Then
        assertEquals(testTaskId, emailJobDTO.getTaskId());
    }

    @Test
    @DisplayName("Should set and get email template")
    void setAndGetEmailTemplate_Success() {
        // When
        emailJobDTO.setEmailTemplate(testEmailTemplate);

        // Then
        assertEquals(testEmailTemplate, emailJobDTO.getEmailTemplate());
    }

    @Test
    @DisplayName("Should set and get receiver email address")
    void setAndGetReceiverEmailAddress_Success() {
        // When
        emailJobDTO.setReceiverEmailAddress(testReceiverEmailAddress);

        // Then
        assertEquals(testReceiverEmailAddress, emailJobDTO.getReceiverEmailAddress());
    }

    @Test
    @DisplayName("Should set and get subject")
    void setAndGetSubject_Success() {
        // When
        emailJobDTO.setSubject(testSubject);

        // Then
        assertEquals(testSubject, emailJobDTO.getSubject());
    }

    @Test
    @DisplayName("Should set and get template variables")
    void setAndGetTemplateVariables_Success() {
        // When
        emailJobDTO.setTemplateVariables(testTemplateVariables);

        // Then
        assertEquals(testTemplateVariables, emailJobDTO.getTemplateVariables());
    }

    @Test
    @DisplayName("Should set and get date time")
    void setAndGetDateTime_Success() {
        // When
        emailJobDTO.setDateTime(testDateTime);

        // Then
        assertEquals(testDateTime, emailJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle null task ID")
    void setTaskId_Null_Success() {
        // When
        emailJobDTO.setTaskId(null);

        // Then
        assertNull(emailJobDTO.getTaskId());
    }

    @Test
    @DisplayName("Should handle null email template")
    void setEmailTemplate_Null_Success() {
        // When
        emailJobDTO.setEmailTemplate(null);

        // Then
        assertNull(emailJobDTO.getEmailTemplate());
    }

    @Test
    @DisplayName("Should handle null receiver email address")
    void setReceiverEmailAddress_Null_Success() {
        // When
        emailJobDTO.setReceiverEmailAddress(null);

        // Then
        assertNull(emailJobDTO.getReceiverEmailAddress());
    }

    @Test
    @DisplayName("Should handle null subject")
    void setSubject_Null_Success() {
        // When
        emailJobDTO.setSubject(null);

        // Then
        assertNull(emailJobDTO.getSubject());
    }

    @Test
    @DisplayName("Should handle null template variables")
    void setTemplateVariables_Null_Success() {
        // When
        emailJobDTO.setTemplateVariables(null);

        // Then
        assertNull(emailJobDTO.getTemplateVariables());
    }

    @Test
    @DisplayName("Should handle null date time")
    void setDateTime_Null_Success() {
        // When
        emailJobDTO.setDateTime(null);

        // Then
        assertNull(emailJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle empty email template")
    void setEmailTemplate_Empty_Success() {
        // When
        emailJobDTO.setEmailTemplate("");

        // Then
        assertEquals("", emailJobDTO.getEmailTemplate());
    }

    @Test
    @DisplayName("Should handle empty receiver email address")
    void setReceiverEmailAddress_Empty_Success() {
        // When
        emailJobDTO.setReceiverEmailAddress("");

        // Then
        assertEquals("", emailJobDTO.getReceiverEmailAddress());
    }

    @Test
    @DisplayName("Should handle empty subject")
    void setSubject_Empty_Success() {
        // When
        emailJobDTO.setSubject("");

        // Then
        assertEquals("", emailJobDTO.getSubject());
    }

    @Test
    @DisplayName("Should handle empty template variables map")
    void setTemplateVariables_Empty_Success() {
        // Given
        Map<String, Object> emptyMap = new HashMap<>();

        // When
        emailJobDTO.setTemplateVariables(emptyMap);

        // Then
        assertEquals(emptyMap, emailJobDTO.getTemplateVariables());
        assertEquals(0, emailJobDTO.getTemplateVariables().size());
    }

    @Test
    @DisplayName("Should handle complex template variables")
    void setTemplateVariables_Complex_Success() {
        // Given
        Map<String, Object> complexVariables = new HashMap<>();
        complexVariables.put("user", Map.of("name", "John Doe", "id", 123));
        complexVariables.put("items", java.util.Arrays.asList("item1", "item2", "item3"));
        complexVariables.put("isActive", true);
        complexVariables.put("count", 42);
        complexVariables.put("nullValue", null);

        // When
        emailJobDTO.setTemplateVariables(complexVariables);

        // Then
        assertEquals(complexVariables, emailJobDTO.getTemplateVariables());
        assertEquals(5, emailJobDTO.getTemplateVariables().size());
    }

    @Test
    @DisplayName("Should handle special characters in string fields")
    void setStringFields_SpecialCharacters_Success() {
        // Given
        String specialTemplate = "template-with-special-chars!@#$%^&*()";
        String specialEmail = "test+special@example-domain.com";
        String specialSubject = "Subject with special chars: 你好世界 🌍";

        // When
        emailJobDTO.setEmailTemplate(specialTemplate);
        emailJobDTO.setReceiverEmailAddress(specialEmail);
        emailJobDTO.setSubject(specialSubject);

        // Then
        assertEquals(specialTemplate, emailJobDTO.getEmailTemplate());
        assertEquals(specialEmail, emailJobDTO.getReceiverEmailAddress());
        assertEquals(specialSubject, emailJobDTO.getSubject());
    }

    @Test
    @DisplayName("Should handle whitespace in string fields")
    void setStringFields_Whitespace_Success() {
        // Given
        String whitespaceTemplate = "   template-with-spaces   ";
        String whitespaceEmail = "  test@example.com  ";
        String whitespaceSubject = "  Subject with spaces  ";

        // When
        emailJobDTO.setEmailTemplate(whitespaceTemplate);
        emailJobDTO.setReceiverEmailAddress(whitespaceEmail);
        emailJobDTO.setSubject(whitespaceSubject);

        // Then
        assertEquals(whitespaceTemplate, emailJobDTO.getEmailTemplate());
        assertEquals(whitespaceEmail, emailJobDTO.getReceiverEmailAddress());
        assertEquals(whitespaceSubject, emailJobDTO.getSubject());
    }

    @Test
    @DisplayName("Should handle past date time")
    void setDateTime_Past_Success() {
        // Given
        OffsetDateTime pastDateTime = OffsetDateTime.now().minusDays(1);

        // When
        emailJobDTO.setDateTime(pastDateTime);

        // Then
        assertEquals(pastDateTime, emailJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle future date time")
    void setDateTime_Future_Success() {
        // Given
        OffsetDateTime futureDateTime = OffsetDateTime.now().plusDays(1);

        // When
        emailJobDTO.setDateTime(futureDateTime);

        // Then
        assertEquals(futureDateTime, emailJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should maintain template variables reference")
    void templateVariables_Reference_Success() {
        // Given
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("key1", "value1");

        // When
        emailJobDTO.setTemplateVariables(originalMap);
        originalMap.put("key2", "value2");

        // Then
        assertEquals(2, emailJobDTO.getTemplateVariables().size());
        assertEquals("value1", emailJobDTO.getTemplateVariables().get("key1"));
        assertEquals("value2", emailJobDTO.getTemplateVariables().get("key2"));
    }
}