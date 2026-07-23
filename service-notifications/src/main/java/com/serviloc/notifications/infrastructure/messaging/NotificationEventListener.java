package com.serviloc.notifications.infrastructure.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.notifications.application.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static com.serviloc.notifications.infrastructure.messaging.PayloadReader.getLong;
import static com.serviloc.notifications.infrastructure.messaging.PayloadReader.getOptionalString;
import static com.serviloc.notifications.infrastructure.messaging.PayloadReader.getString;

/**
 * Point d'entrée unique des événements consommés depuis {@code notifications.queue}.
 *
 * Reçoit le body brut en {@code byte[]} via {@link Message} et parse le JSON manuellement
 * avec Jackson — indépendant du header {@code content_type} (absent quand publié depuis
 * RabbitMQ Management UI ou certains services qui ne le positionnent pas).
 *
 * Les publishers envoient une enveloppe { eventId, eventType, occurredAt, payload }.
 * On extrait le sous-objet "payload" avec fallback sur rawEvent (rétro-compatibilité).
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private static final NumberFormat XAF_FORMAT = NumberFormat.getNumberInstance(Locale.FRANCE);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final NotificationDispatchService dispatch;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(NotificationDispatchService dispatch, ObjectMapper objectMapper) {
        this.dispatch = dispatch;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${serviloc.messaging.queue}")
    public void onEvent(Message message,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        Map<String, Object> rawEvent;
        try {
            rawEvent = objectMapper.readValue(body, MAP_TYPE);
        } catch (Exception e) {
            log.error("[Notifications] Impossible de parser le message (routingKey={}) : {} — body={}",
                    routingKey, e.getMessage(), body);
            // On lève pour déclencher le retry → DLQ si le body n'est pas du JSON valide.
            throw new IllegalArgumentException("Message non JSON reçu sur " + routingKey, e);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = rawEvent.containsKey("payload")
                ? (Map<String, Object>) rawEvent.get("payload")
                : rawEvent;

        switch (routingKey) {
            case "user.registered"                -> onUserRegistered(payload);
            case "provider.validated"             -> onProviderValidated(payload);
            case "provider.rejected"              -> onProviderRejected(payload);
            case "provider.reviewed"              -> onProviderReviewed(payload);
            case "agent.created"                  -> onAgentCreated(payload);
            case "user.suspended"                 -> onUserSuspended(payload);
            case "demand.published"               -> onDemandPublished(payload);
            case "negotiation.conversation_opened"-> onNegotiationConversationOpened(payload);
            case "negotiation.quote.accepted"     -> onNegotiationQuoteAccepted(payload);
            case "negotiation.message_sent"       -> onNegotiationMessageSent(payload);
            case "negotiation.quote_refused"      -> onNegotiationQuoteRefused(payload);
            case "payment.confirmed"              -> onPaymentConfirmed(payload);
            case "payment.failed"                 -> onPaymentFailed(payload);
            case "payment.released"               -> onPaymentReleased(payload);
            case "payment.refunded"               -> onPaymentRefunded(payload);
            case "litige.opened"                  -> onLitigeOpened(payload);
            case "litige.assigned"                -> onLitigeAssigned(payload);
            case "litige.resolved"                -> onLitigeResolved(payload);
            default -> log.debug("[Notifications] Événement '{}' non géré — ignoré.", routingKey);
        }
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    // 1. user.registered → Email OTP inscription
    private void onUserRegistered(Map<String, Object> payload) {
        String userId  = getString(payload, "userId");
        String email   = getString(payload, "email");
        String otpCode = getOptionalString(payload, "otpCode").orElse(null);

        if (otpCode != null) {
            dispatch.sendEmail(userId, "user.registered", email,
                    "Votre code de confirmation ServiLoc",
                    "Bienvenue sur ServiLoc !\n\nVotre code de confirmation est :\n\n"
                            + "    <strong style='font-size:32px;letter-spacing:8px'>" + otpCode + "</strong>\n\n"
                            + "Ce code expire dans 5 minutes. Ne le communiquez à personne.");
        } else {
            log.warn("[user.registered] Champ 'otpCode' absent du payload userId={} — "
                    + "email de bienvenue envoyé sans code", userId);
            dispatch.sendEmail(userId, "user.registered", email,
                    "Bienvenue sur ServiLoc",
                    "Votre inscription a bien été prise en compte.\n"
                            + "Connectez-vous dès maintenant sur ServiLoc.");
        }
    }

    // 2. provider.validated → Email "Dossier validé" + Push
    private void onProviderValidated(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String email      = getString(payload, "email");

        dispatch.sendEmail(providerId, "provider.validated", email,
                "Votre dossier prestataire a été validé ✓",
                "Félicitations !\n\nVotre dossier prestataire ServiLoc a été validé. "
                        + "Vous pouvez désormais recevoir des demandes de clients.");
        dispatch.sendPush(providerId, "provider.validated", "Dossier validé ✓",
                "Votre dossier prestataire a été validé.", Map.of());
    }

    // 3. provider.rejected → Email "Dossier refusé" + Push
    private void onProviderRejected(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String email      = getString(payload, "email");
        String reason     = getOptionalString(payload, "reason").orElse("motif non précisé");

        dispatch.sendEmail(providerId, "provider.rejected", email,
                "Votre dossier prestataire a été refusé",
                "Votre dossier prestataire ServiLoc n'a pas été retenu.\n\n"
                        + "Motif : " + reason + "\n\n"
                        + "Vous pouvez soumettre un nouveau dossier après correction.");
        dispatch.sendPush(providerId, "provider.rejected", "Dossier refusé",
                "Motif : " + reason, Map.of());
    }

    // 4. provider.reviewed → Push admin "Dossier instruit par agent"
    private void onProviderReviewed(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String agentId    = getString(payload, "agentId");
        String verdict    = getString(payload, "verdict");

        dispatch.sendPush(NotificationDispatchService.ADMIN_BROADCAST_USER_ID,
                "provider.reviewed",
                "Dossier instruit par agent",
                "Dossier #" + providerId + " — verdict : " + verdict + " (agent #" + agentId + ")",
                Map.of("providerId", String.valueOf(providerId), "agentId", String.valueOf(agentId)));
    }

    // 5. agent.created → Email mot de passe provisoire
    private void onAgentCreated(Map<String, Object> payload) {
        String agentId             = getString(payload, "agentId");
        String email               = getString(payload, "email");
        String provisionalPassword = getOptionalString(payload, "provisionalPassword").orElse(null);

        if (provisionalPassword != null) {
            dispatch.sendEmail(agentId, "agent.created", email,
                    "Vos identifiants agent ServiLoc",
                    "Bienvenue dans l'équipe ServiLoc !\n\n"
                            + "Votre mot de passe provisoire : <strong>" + provisionalPassword + "</strong>\n\n"
                            + "Connectez-vous et changez-le immédiatement.");
        } else {
            log.warn("[agent.created] Champ 'provisionalPassword' absent du payload agentId={}", agentId);
            dispatch.sendEmail(agentId, "agent.created", email,
                    "Votre compte agent ServiLoc a été créé",
                    "Bienvenue dans l'équipe ServiLoc !\n\n"
                            + "Utilisez la fonction 'mot de passe oublié' pour définir votre mot de passe.");
        }
    }

    // 6. user.suspended → Email "Compte suspendu"
    private void onUserSuspended(Map<String, Object> payload) {
        String userId          = getString(payload, "userId");
        String email           = getString(payload, "email");
        String suspendedByRole = getOptionalString(payload, "suspendedByRole").orElse("");
        String duration        = "agent".equalsIgnoreCase(suspendedByRole) ? " pour une durée de 7 jours" : "";

        dispatch.sendEmail(userId, "user.suspended", email,
                "Votre compte ServiLoc a été suspendu",
                "Votre compte ServiLoc a été suspendu" + duration + ".\n\n"
                        + "Pour toute contestation, contactez notre support.");
    }

    // 7. demand.published → Push prestataires zone
    private void onDemandPublished(Map<String, Object> payload) {
        String demandId = getString(payload, "demandId");
        String targetProviderIds = getString(payload, "targetProviderIds");

        if (targetProviderIds == null) {
            log.warn("[demand.published] Champ 'targetProviderIds' absent (demandId={}) — "
                    + "push non envoyé (à ajouter dans le payload)", demandId);
            return;
        }
        for (String providerId : targetProviderIds.split(",")) {
            dispatch.sendPush(providerId.trim(), "demand.published",
                    "Nouvelle demande disponible",
                    "Une nouvelle demande correspond à votre profil.",
                    Map.of("demandId", String.valueOf(demandId)));
        }
    }

    // 8. negotiation.conversation_opened → Push prestataire
    private void onNegotiationConversationOpened(Map<String, Object> payload) {
        String providerId     = getString(payload, "providerId");
        String conversationId = getString(payload, "conversationId");

        dispatch.sendPush(providerId, "negotiation.conversation_opened",
                "Nouvelle conversation",
                "Un client souhaite vous contacter.",
                Map.of("conversationId", String.valueOf(conversationId)));
    }

    // 9. negotiation.quote_accepted → Push prestataire
    private void onNegotiationQuoteAccepted(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String quoteId    = getString(payload, "quoteId");

        dispatch.sendPush(providerId, "negotiation.quote_accepted",
                "Devis accepté ✓",
                "Votre devis a été accepté, le paiement est en cours.",
                Map.of("quoteId", String.valueOf(quoteId)));
    }

    // 10. negotiation.message_sent → Push destinataire
    private void onNegotiationMessageSent(Map<String, Object> payload) {
        String recipientId    = getString(payload, "recipientId");
        String senderRole     = getOptionalString(payload, "senderRole").orElse("votre correspondant");
        String conversationId = getString(payload, "conversationId");

        dispatch.sendPush(recipientId, "negotiation.message_sent",
                "Nouveau message",
                "Nouveau message de " + senderRole + ".",
                Map.of("conversationId", String.valueOf(conversationId)));
    }

    // 11. negotiation.quote_refused → Push prestataire
    private void onNegotiationQuoteRefused(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String quoteId    = getString(payload, "quoteId");

        dispatch.sendPush(providerId, "negotiation.quote_refused",
                "Devis refusé",
                "Votre devis a été refusé par le client.",
                Map.of("quoteId", String.valueOf(quoteId)));
    }

    // 12. payment.confirmed → Push client + prestataire
    private void onPaymentConfirmed(Map<String, Object> payload) {
        String clientId   = getString(payload, "clientId");
        String providerId = getString(payload, "providerId");
        String missionId  = getString(payload, "missionId");

        dispatch.sendPush(clientId, "payment.confirmed",
                "Paiement confirmé ✓", "Paiement confirmé, la mission peut démarrer.",
                Map.of("missionId", String.valueOf(missionId)));
        dispatch.sendPush(providerId, "payment.confirmed",
                "Paiement confirmé ✓", "Paiement confirmé, la mission peut démarrer.",
                Map.of("missionId", String.valueOf(missionId)));
    }

    // 13. payment.failed → Push client
    private void onPaymentFailed(Map<String, Object> payload) {
        String clientId = getString(payload, "clientId");
        String reason   = getOptionalString(payload, "reason").orElse("raison non précisée");

        dispatch.sendPush(clientId, "payment.failed",
                "Paiement échoué", "Le paiement a échoué : " + reason, Map.of());
    }

    // 14. payment.released → Push prestataire
    private void onPaymentReleased(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        long netAmount    = getLong(payload, "netAmount");
        String formatted  = XAF_FORMAT.format(netAmount);

        dispatch.sendPush(providerId, "payment.released",
                "Paiement libéré", "Paiement libéré : " + formatted + " XAF", Map.of());
    }

    // 14b. payment.refunded → Push client "Remboursement effectué"
    private void onPaymentRefunded(Map<String, Object> payload) {
        String clientId       = getString(payload, "clientId");
        String transactionId  = getString(payload, "transactionId");
        long amount           = getLong(payload, "amount");
        String formatted      = XAF_FORMAT.format(amount);

        dispatch.sendPush(clientId, "payment.refunded",
                "Remboursement effectué",
                "Vous avez été remboursé de " + formatted + " XAF.",
                Map.of("transactionId", String.valueOf(transactionId)));
    }

    // 15. litige.opened → Push admin
    private void onLitigeOpened(Map<String, Object> payload) {
        String litigeId = getString(payload, "litigeId");

        dispatch.sendPush(NotificationDispatchService.ADMIN_BROADCAST_USER_ID,
                "litige.opened", "Nouveau litige",
                "Nouveau litige #" + litigeId,
                Map.of("litigeId", String.valueOf(litigeId)));
    }

    // 16. litige.assigned → Push agent
    private void onLitigeAssigned(Map<String, Object> payload) {
        String agentId  = getString(payload, "agentId");
        String litigeId = getString(payload, "litigeId");

        dispatch.sendPush(agentId, "litige.assigned",
                "Litige assigné", "Le litige #" + litigeId + " vous a été assigné.",
                Map.of("litigeId", String.valueOf(litigeId)));
    }

    // 17. litige.resolved → Push client + prestataire
    private void onLitigeResolved(Map<String, Object> payload) {
        String litigeId   = getString(payload, "litigeId");
        String clientId   = getString(payload, "clientId");
        String providerId = getString(payload, "providerId");

        if (clientId == null && providerId == null) {
            log.warn("[litige.resolved] Payload sans clientId/providerId (litigeId={}) — "
                    + "push non envoyé (champs à ajouter)", litigeId);
            return;
        }
        String body = "Le litige #" + litigeId + " a été résolu.";
        if (clientId != null) {
            dispatch.sendPush(clientId, "litige.resolved", "Litige résolu", body,
                    Map.of("litigeId", String.valueOf(litigeId)));
        }
        if (providerId != null) {
            dispatch.sendPush(providerId, "litige.resolved", "Litige résolu", body,
                    Map.of("litigeId", String.valueOf(litigeId)));
        }
    }
}
