package com.jameselner.convo.config;

import com.jameselner.convo.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(@NonNull final MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // Prefix for messages FROM server TO client
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull final StompEndpointRegistry registry) {
        // WebSocket endpoint that clients will connect to
        registry.addEndpoint("/ws-chat")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*") // TODO: Restrict origins in production
                .withSockJS(); // Fallback for browsers without WebSocket support
    }

    @Override
    public void configureClientInboundChannel(@NonNull final ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

}