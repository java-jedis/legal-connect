package com.javajedis.legalconnect.jobscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebPushJobDTO Tests")
class WebPushJobDTOTest {

    private WebPushJobDTO webPushJobDTO;
    private UUID testTaskId;
    private UUID testRecipientId;
    private String testContent;
    private OffsetDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testRecipientId = UUID.randomUUID();
        testContent = "Test notification content";
        testDateTime = OffsetDateTime.now();

        webPushJobDTO = new WebPushJobDTO();
    }

    @Test
    @DisplayName("Should create WebPushJobDTO with no-args constructor")
    void noArgsConstructor_Success() {
        // Given & When
        WebPushJobDTO dto = new WebPushJobDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getTaskId());
        assertNull(dto.getRecipientId());
        assertNull(dto.getContent());
        assertNull(dto.getDateTime());
    }

    @Test
    @DisplayName("Should create WebPushJobDTO with all-args constructor")
    void allArgsConstructor_Success() {
        // Given & When
        WebPushJobDTO dto = new WebPushJobDTO(
                testTaskId,
                testRecipientId,
                testContent,
                testDateTime
        );

        // Then
        assertNotNull(dto);
        assertEquals(testTaskId, dto.getTaskId());
        assertEquals(testRecipientId, dto.getRecipientId());
        assertEquals(testContent, dto.getContent());
        assertEquals(testDateTime, dto.getDateTime());
    }

    @Test
    @DisplayName("Should set and get task ID")
    void setAndGetTaskId_Success() {
        // When
        webPushJobDTO.setTaskId(testTaskId);

        // Then
        assertEquals(testTaskId, webPushJobDTO.getTaskId());
    }

    @Test
    @DisplayName("Should set and get recipient ID")
    void setAndGetRecipientId_Success() {
        // When
        webPushJobDTO.setRecipientId(testRecipientId);

        // Then
        assertEquals(testRecipientId, webPushJobDTO.getRecipientId());
    }

    @Test
    @DisplayName("Should set and get content")
    void setAndGetContent_Success() {
        // When
        webPushJobDTO.setContent(testContent);

        // Then
        assertEquals(testContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should set and get date time")
    void setAndGetDateTime_Success() {
        // When
        webPushJobDTO.setDateTime(testDateTime);

        // Then
        assertEquals(testDateTime, webPushJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle null task ID")
    void setTaskId_Null_Success() {
        // When
        webPushJobDTO.setTaskId(null);

        // Then
        assertNull(webPushJobDTO.getTaskId());
    }

    @Test
    @DisplayName("Should handle null recipient ID")
    void setRecipientId_Null_Success() {
        // When
        webPushJobDTO.setRecipientId(null);

        // Then
        assertNull(webPushJobDTO.getRecipientId());
    }

    @Test
    @DisplayName("Should handle null content")
    void setContent_Null_Success() {
        // When
        webPushJobDTO.setContent(null);

        // Then
        assertNull(webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle null date time")
    void setDateTime_Null_Success() {
        // When
        webPushJobDTO.setDateTime(null);

        // Then
        assertNull(webPushJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle empty content")
    void setContent_Empty_Success() {
        // When
        webPushJobDTO.setContent("");

        // Then
        assertEquals("", webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle whitespace-only content")
    void setContent_WhitespaceOnly_Success() {
        // Given
        String whitespaceContent = "   \t\n   ";

        // When
        webPushJobDTO.setContent(whitespaceContent);

        // Then
        assertEquals(whitespaceContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle long content")
    void setContent_Long_Success() {
        // Given
        String longContent = "This is a very long notification content that might be used in real scenarios. ".repeat(10);

        // When
        webPushJobDTO.setContent(longContent);

        // Then
        assertEquals(longContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle special characters in content")
    void setContent_SpecialCharacters_Success() {
        // Given
        String specialContent = "Content with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        webPushJobDTO.setContent(specialContent);

        // Then
        assertEquals(specialContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle unicode content")
    void setContent_Unicode_Success() {
        // Given
        String unicodeContent = "Unicode content: 你好世界 🌍 العالم";

        // When
        webPushJobDTO.setContent(unicodeContent);

        // Then
        assertEquals(unicodeContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle newlines and tabs in content")
    void setContent_NewlinesAndTabs_Success() {
        // Given
        String contentWithFormatting = "Line 1\nLine 2\tTabbed content\r\nWindows line ending";

        // When
        webPushJobDTO.setContent(contentWithFormatting);

        // Then
        assertEquals(contentWithFormatting, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle past date time")
    void setDateTime_Past_Success() {
        // Given
        OffsetDateTime pastDateTime = OffsetDateTime.now().minusDays(1);

        // When
        webPushJobDTO.setDateTime(pastDateTime);

        // Then
        assertEquals(pastDateTime, webPushJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle future date time")
    void setDateTime_Future_Success() {
        // Given
        OffsetDateTime futureDateTime = OffsetDateTime.now().plusDays(1);

        // When
        webPushJobDTO.setDateTime(futureDateTime);

        // Then
        assertEquals(futureDateTime, webPushJobDTO.getDateTime());
    }

    @Test
    @DisplayName("Should handle same task ID and recipient ID")
    void setSameTaskIdAndRecipientId_Success() {
        // Given
        UUID sameId = UUID.randomUUID();

        // When
        webPushJobDTO.setTaskId(sameId);
        webPushJobDTO.setRecipientId(sameId);

        // Then
        assertEquals(sameId, webPushJobDTO.getTaskId());
        assertEquals(sameId, webPushJobDTO.getRecipientId());
        assertEquals(webPushJobDTO.getTaskId(), webPushJobDTO.getRecipientId());
    }

    @Test
    @DisplayName("Should handle different UUID formats")
    void setUUIDs_DifferentFormats_Success() {
        // Given
        UUID randomUUID = UUID.randomUUID();
        UUID nameBasedUUID = UUID.nameUUIDFromBytes("test".getBytes());

        // When
        webPushJobDTO.setTaskId(randomUUID);
        webPushJobDTO.setRecipientId(nameBasedUUID);

        // Then
        assertEquals(randomUUID, webPushJobDTO.getTaskId());
        assertEquals(nameBasedUUID, webPushJobDTO.getRecipientId());
    }

    @Test
    @DisplayName("Should handle content with quotes")
    void setContent_WithQuotes_Success() {
        // Given
        String contentWithQuotes = "Content with \"double quotes\" and 'single quotes'";

        // When
        webPushJobDTO.setContent(contentWithQuotes);

        // Then
        assertEquals(contentWithQuotes, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle content with JSON-like structure")
    void setContent_JsonLike_Success() {
        // Given
        String jsonLikeContent = "{\"message\": \"Hello World\", \"priority\": \"high\"}";

        // When
        webPushJobDTO.setContent(jsonLikeContent);

        // Then
        assertEquals(jsonLikeContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle content with HTML-like structure")
    void setContent_HtmlLike_Success() {
        // Given
        String htmlLikeContent = "<div>Hello <strong>World</strong>!</div>";

        // When
        webPushJobDTO.setContent(htmlLikeContent);

        // Then
        assertEquals(htmlLikeContent, webPushJobDTO.getContent());
    }

    @Test
    @DisplayName("Should handle very precise date time")
    void setDateTime_Precise_Success() {
        // Given
        OffsetDateTime preciseDateTime = OffsetDateTime.parse("2024-12-25T15:30:45.123456789+05:30");

        // When
        webPushJobDTO.setDateTime(preciseDateTime);

        // Then
        assertEquals(preciseDateTime, webPushJobDTO.getDateTime());
    }
}