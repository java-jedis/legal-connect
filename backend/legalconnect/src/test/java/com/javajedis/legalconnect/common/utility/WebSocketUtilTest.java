package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@DisplayName("WebSocketUtil Tests")
class WebSocketUtilTest {

    @Test
    @DisplayName("Should extract user ID from valid UUID principal name")
    void extractUserIdFromPrincipal_ValidUUID() {
        Principal principal = () -> UUID.randomUUID().toString();
        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        assertNotNull(userId);
        assertTrue(userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"));
    }

    @Test
    @DisplayName("Should extract user ID from UsernamePasswordAuthenticationToken with details")
    void extractUserIdFromPrincipal_AuthenticationTokenWithDetails() {
        String expectedUserId = UUID.randomUUID().toString();
        Map<String, Object> details = new HashMap<>();
        details.put("userId", expectedUserId);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("invalid-uuid-format", null);
        authToken.setDetails(details);
        String userId = WebSocketUtil.extractUserIdFromPrincipal(authToken);
        assertEquals(expectedUserId, userId);
    }

    @Test
    @DisplayName("Should throw exception when unable to extract user ID from principal")
    void extractUserIdFromPrincipal_InvalidPrincipal() {
        Principal invalidPrincipal = () -> "invalid-format";
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WebSocketUtil.extractUserIdFromPrincipal(invalidPrincipal)
        );
        assertTrue(exception.getMessage().contains("Cannot extract user ID from principal"));
    }
}
