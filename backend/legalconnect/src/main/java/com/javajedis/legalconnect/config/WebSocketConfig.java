package com.javajedis.legalconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
* WebSocket configuration for real-time notification delivery.
* Configures STOMP messaging with WebSocket transport and SockJS fallback.
*/
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

   private final WebSocketAuthInterceptor webSocketAuthInterceptor;
   @Value("${spring.cors.allowed-origins:http://localhost:5173}")
   private String allowedOrigins;

   public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
       this.webSocketAuthInterceptor = webSocketAuthInterceptor;
   }

   /**
    * Configure the message broker for handling WebSocket messages.
    * Sets up in-memory broker for topic-based messaging.
    */
   @Override
   public void configureMessageBroker(MessageBrokerRegistry config) {
       config.enableSimpleBroker("/topic", "/queue");
       config.setApplicationDestinationPrefixes("/app");
       config.setUserDestinationPrefix("/user");
   }

   /**
    * Register STOMP endpoints for WebSocket connections.
    * Enables SockJS fallback options for browsers that don't support WebSocket.
    */
   @Override
   public void registerStompEndpoints(StompEndpointRegistry registry) {
       registry.addEndpoint("/ws")
               .setAllowedOriginPatterns(allowedOrigins.split(","))
               .withSockJS()
               .setHeartbeatTime(25000)
               .setDisconnectDelay(5000)
               .setStreamBytesLimit(128 * 1024)
               .setHttpMessageCacheSize(1000)
               .setSessionCookieNeeded(false);
   }

   /**
    * Configure client inbound channel for authentication and authorization.
    * Registers the JWT authentication interceptor to validate WebSocket connections.
    */
   @Override
   public void configureClientInboundChannel(ChannelRegistration registration) {
       registration.interceptors(webSocketAuthInterceptor);
   }
}
