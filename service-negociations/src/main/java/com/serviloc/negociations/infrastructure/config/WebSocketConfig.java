package com.serviloc.negociations.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Chat temps réel — WebSocket STOMP avec relais externe RabbitMQ (plugin rabbitmq_stomp).
 *
 * Auth : déléguée au Gateway (JwtAuthFilter), qui injecte X-User-Id / X-User-Role
 * lors du handshake HTTP initial — voir {@link WsHandshakeInterceptor}. Ce service
 * ne revalide pas le JWT, il fait confiance aux headers du Gateway, comme pour le
 * reste de l'API REST.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.stomp-relay.host}")
    private String relayHost;

    @Value("${websocket.stomp-relay.port}")
    private int relayPort;

    @Value("${websocket.stomp-relay.client-login}")
    private String relayLogin;

    @Value("${websocket.stomp-relay.client-passcode}")
    private String relayPasscode;

    private final WsHandshakeInterceptor handshakeInterceptor;
    private final WsChannelInterceptor channelInterceptor;

    public WebSocketConfig(WsHandshakeInterceptor handshakeInterceptor,
                           WsChannelInterceptor channelInterceptor) {
        this.handshakeInterceptor = handshakeInterceptor;
        this.channelInterceptor = channelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Le Gateway route déjà /ws/** vers ce service (StripPrefix=0) — endpoint local "/ws"
        registry.addEndpoint("/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*"); // le Gateway est le seul point d'entrée public
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(relayLogin)
                .setClientPasscode(relayPasscode)
                .setSystemLogin(relayLogin)
                .setSystemPasscode(relayPasscode);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
