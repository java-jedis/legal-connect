package com.javajedis.legalconnect.common.utility;

import java.security.Principal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketUtil {
    private WebSocketUtil() {}

    /**
     * Extracts the user ID from the Principal's authentication details.
     */
    public static String extractUserIdFromPrincipal(Principal principal) {
        String principalName = principal.getName();
        if (principalName != null && principalName.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")) {
            return principalName;
        }

        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            Object details = auth.getDetails();
            if (details instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> detailsMap = (java.util.Map<String, Object>) details;
                Object userId = detailsMap.get("userId");
                if (userId != null) {
                    return userId.toString();
                }
            }
        }
        log.error("Failed to extract user ID from principal {}", principalName);
        throw new IllegalArgumentException("Cannot extract user ID from principal: " + principalName);
    }
}
