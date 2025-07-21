package com.javajedis.legalconnect.config;

import com.javajedis.legalconnect.common.utility.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JWTUtil jwtUtil;

    public WebSocketAuthInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Intercepts messages before they are sent to the channel.
     * Performs JWT authentication for CONNECT commands with comprehensive error handling.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleWebSocketAuthentication(accessor, message);
        }

        return message;
    }

    /**
     * Handles WebSocket authentication for CONNECT commands.
     * Separated for better error handling and testing.
     */
    private Message<?> handleWebSocketAuthentication(StompHeaderAccessor accessor, Message<?> message) {
        String token = extractTokenFromHeaders(accessor);

        if (token == null || token.trim().isEmpty()) {
            log.warn("No JWT token provided for WebSocket connection from session: {}", accessor.getSessionId());
            return null;
        }

        boolean isValid = jwtUtil.validateToken(token);
        if (!isValid) {
            log.warn("Invalid JWT token provided for WebSocket connection from session: {}", accessor.getSessionId());
            return null;
        }

        String userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractUserRole(token);

        if (userId == null || userId.trim().isEmpty()) {
            log.error("JWT token missing userId for WebSocket connection from session: {}", accessor.getSessionId());
            return null;
        }

        if (email == null || email.trim().isEmpty()) {
            log.error("JWT token missing email for WebSocket connection from session: {}", accessor.getSessionId());
            return null;
        }

        Authentication authentication = createAuthentication(userId, email, role);
        accessor.setUser(authentication);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("userId", userId);
            sessionAttributes.put("email", email);
            sessionAttributes.put("role", role);
        } else {
            log.warn("Session attributes are null for WebSocket connection from session: {}", accessor.getSessionId());
        }

        log.debug("WebSocket connection authenticated for user: {} ({})", email, userId);
        return message;
    }

    /**
     * Extracts JWT token from WebSocket connection headers.
     * Looks for token in Authorization header or as a query parameter.
     */
    private String extractTokenFromHeaders(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            return JWTUtil.extractJwtFromHeader(authHeader);
        }

        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }
        return null;
    }

    /**
     * Creates an Authentication object from user information extracted from JWT token.
     * Uses userId as the principal name for proper WebSocket user destination routing.
     */
    private Authentication createAuthentication(String userId, String email, String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
        
        authentication.setDetails(Map.of(
                "userId", userId,
                "email", email,
                "role", role
        ));

        return authentication;
    }

    /**
     * Called after a message is sent to the channel.
     * Used for logging and cleanup if needed.
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand()) && sent) {
                log.debug("WebSocket connection established successfully");
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                log.debug("WebSocket connection disconnected");
            }
        }
    }
}