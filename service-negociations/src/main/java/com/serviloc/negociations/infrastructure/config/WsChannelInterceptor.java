package com.serviloc.negociations.infrastructure.config;

import com.serviloc.negociations.domain.model.Conversation;
import com.serviloc.negociations.domain.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Un utilisateur ne doit pouvoir s'abonner qu'aux topics des conversations
 * dont il est client ou prestataire (sinon n'importe quel utilisateur connecté
 * pourrait écouter n'importe quelle conversation en devinant son id).
 */
@Component
public class WsChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WsChannelInterceptor.class);
    private static final String CONV_TOPIC_PREFIX = "/topic/conversation.";

    private final ConversationRepository conversationRepository;

    public WsChannelInterceptor(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(CONV_TOPIC_PREFIX)) {
            // /topic/litige.* et autres : pas encore géré ici (service-litiges)
            return message;
        }

        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        String userId = sessionAttrs != null ? (String) sessionAttrs.get("userId") : null;

        if (userId == null) {
            log.warn("[WS] Abonnement refusé — session sans userId : dest={}", destination);
            throw new org.springframework.messaging.MessagingException(
                    "Session non authentifiée");
        }

        UUID conversationId;
        try {
            conversationId = UUID.fromString(
                    destination.substring(CONV_TOPIC_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new org.springframework.messaging.MessagingException(
                    "Identifiant de conversation invalide");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new org.springframework.messaging.MessagingException(
                        "Conversation introuvable"));

        UUID requester = UUID.fromString(userId);
        boolean autorise = requester.equals(conversation.getClientId())
                || requester.equals(conversation.getProviderId());

        if (!autorise) {
            log.warn("[WS] Abonnement refusé — userId={} n'est pas partie prenante de conv={}",
                    userId, conversationId);
            throw new org.springframework.messaging.MessagingException(
                    "Accès refusé à cette conversation");
        }

        return message;
    }
}
