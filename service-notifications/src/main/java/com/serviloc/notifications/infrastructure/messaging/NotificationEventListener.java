package com.serviloc.notifications.infrastructure.messaging;

import com.serviloc.notifications.application.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static com.serviloc.notifications.infrastructure.messaging.PayloadReader.getLong;
import static com.serviloc.notifications.infrastructure.messaging.PayloadReader.getOptionalString;
import static com.serviloc.notifications.infrastructure.messaging.PayloadReader.getString;

/**
 * Point d'entrée unique des événements consommés depuis {@code notifications.queue}.
 *
 * Une seule queue est bindée sur plusieurs routing keys (cf. {@code RabbitMqConfig}) ; ce listener
 * route donc chaque message reçu vers le handler correspondant à son routing key (= nom de
 * l'événement métier, ex: "user.registered").
 *
 * Ce service ne publie jamais d'événement — il est purement consommateur (cf. architecture §3.6).
 *
 * ⚠️ Plusieurs handlers lisent des champs qui ne sont PAS encore dans les payloads documentés en
 * architecture (ex: {@code phone} pour provider.validated/rejected/user.suspended). Ces champs sont
 * lus de façon défensive (warning + SMS sauté si absent) en attendant l'ajout côté service émetteur
 * — cf. récapitulatif communiqué à l'équipe.
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private static final NumberFormat XAF_FORMAT = NumberFormat.getNumberInstance(Locale.FRANCE);

    private final NotificationDispatchService dispatch;

    public NotificationEventListener(NotificationDispatchService dispatch) {
        this.dispatch = dispatch;
    }

    @RabbitListener(queues = "${serviloc.messaging.queue}")
    public void onEvent(@Payload Map<String, Object> rawEvent,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        // Les publishers envoient une enveloppe { eventId, eventType, occurredAt, payload }.
        // On extrait le sous-objet "payload" — avec fallback sur rawEvent si jamais un
        // event arrive déjà "à plat" (rétro-compatibilité).
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = rawEvent.containsKey("payload")
                ? (Map<String, Object>) rawEvent.get("payload")
                : rawEvent;

        switch (routingKey) {
            case "user.registered" -> onUserRegistered(payload);
            case "provider.validated" -> onProviderValidated(payload);
            case "provider.rejected" -> onProviderRejected(payload);
            case "provider.reviewed" -> onProviderReviewed(payload);
            case "agent.created" -> onAgentCreated(payload);
            case "user.suspended" -> onUserSuspended(payload);
            case "demand.published" -> onDemandPublished(payload);
            case "negotiation.conversation_opened" -> onNegotiationConversationOpened(payload);
            case "negotiation.quote_accepted" -> onNegotiationQuoteAccepted(payload);
            case "negotiation.message_sent" -> onNegotiationMessageSent(payload);
            case "negotiation.quote_refused" -> onNegotiationQuoteRefused(payload);
            case "payment.confirmed" -> onPaymentConfirmed(payload);
            case "payment.failed" -> onPaymentFailed(payload);
            case "payment.released" -> onPaymentReleased(payload);
            case "litige.opened" -> onLitigeOpened(payload);
            case "litige.assigned" -> onLitigeAssigned(payload);
            case "litige.resolved" -> onLitigeResolved(payload);
            default -> log.debug("[Notifications] Événement '{}' reçu mais non géré par ce service — ignoré.",
                    routingKey);
        }
    }

    // --- 1. user.registered { userId, role, phone, otpCode? } → SMS OTP ---
    private void onUserRegistered(Map<String, Object> payload) {
        String userId = getString(payload, "userId");
        String phone = getString(payload, "phone");
        String otpCode = getOptionalString(payload, "otpCode")
                .orElseGet(() -> {
                    log.warn("[user.registered] Champ 'otpCode' absent du payload pour userId={} — "
                            + "SMS de bienvenue envoyé sans code (champ à ajouter côté Service Utilisateurs)", userId);
                    return null;
                });

        String message = otpCode != null
                ? "ServiLoc : votre code de confirmation est " + otpCode + ". Il expire dans 5 minutes."
                : "Bienvenue sur ServiLoc ! Votre inscription a été prise en compte.";

        dispatch.sendSms(userId, "user.registered", phone, message);
    }

    // --- 2. provider.validated { providerId, decidedBy, phone? } → SMS + Push "Dossier validé" ---
    private void onProviderValidated(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String phone = getString(payload, "phone");

        dispatch.sendSms(providerId, "provider.validated", phone,
                "ServiLoc : votre dossier prestataire a été validé. Vous pouvez désormais recevoir des demandes.");
        dispatch.sendPush(providerId, "provider.validated", "Dossier validé",
                "Votre dossier prestataire a été validé.", Map.of());
    }

    // --- 3. provider.rejected { providerId, reason, decidedBy, phone? } → SMS + Push "Dossier refusé" ---
    private void onProviderRejected(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String phone = getString(payload, "phone");
        String reason = getOptionalString(payload, "reason").orElse("motif non précisé");

        dispatch.sendSms(providerId, "provider.rejected", phone,
                "ServiLoc : votre dossier prestataire a été refusé. Motif : " + reason);
        dispatch.sendPush(providerId, "provider.rejected", "Dossier refusé",
                "Dossier refusé : " + reason, Map.of());
    }

    // --- 4. provider.reviewed { providerId, agentId, verdict } → Push admin "Dossier instruit par agent" ---
    private void onProviderReviewed(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String agentId = getString(payload, "agentId");
        String verdict = getString(payload, "verdict");

        dispatch.sendPush(NotificationDispatchService.ADMIN_BROADCAST_USER_ID, "provider.reviewed",
                "Nouveau dossier instruit par agent",
                "Dossier prestataire " + providerId + " instruit par l'agent " + agentId + " — verdict : " + verdict,
                Map.of("providerId", String.valueOf(providerId), "agentId", String.valueOf(agentId)));
    }

    // --- 5. agent.created { agentId, email, provisionalPassword? } → Email mot de passe provisoire ---
    private void onAgentCreated(Map<String, Object> payload) {
        String agentId = getString(payload, "agentId");
        String email = getString(payload, "email");
        String provisionalPassword = getOptionalString(payload, "provisionalPassword")
                .orElseGet(() -> {
                    log.warn("[agent.created] Champ 'provisionalPassword' absent du payload pour agentId={} — "
                            + "email envoyé sans mot de passe (champ à ajouter côté Service Utilisateurs)", agentId);
                    return null;
                });

        String body = provisionalPassword != null
                ? "Bienvenue chez ServiLoc. Votre mot de passe provisoire : " + provisionalPassword
                        + ". Connectez-vous puis changez-le immédiatement."
                : "Bienvenue chez ServiLoc. Utilisez la fonction 'mot de passe oublié' pour définir votre mot de passe.";

        dispatch.sendEmail(agentId, "agent.created", email, "Bienvenue sur ServiLoc — vos identifiants agent", body);
    }

    // --- 6. user.suspended { userId, suspendedBy, suspendedByRole, litigeId?, phone? } → SMS "Compte suspendu" ---
    private void onUserSuspended(Map<String, Object> payload) {
        String userId = getString(payload, "userId");
        String phone = getString(payload, "phone");
        String suspendedByRole = getString(payload, "suspendedByRole");

        String duration = "agent".equalsIgnoreCase(suspendedByRole) ? " pour une durée de 7 jours" : "";
        dispatch.sendSms(userId, "user.suspended", phone,
                "ServiLoc : votre compte a été suspendu" + duration + ". Contactez le support pour plus d'informations.");
    }

    // --- 7. demand.published { demandId, location, categoryId, clientId } → Push prestataires zone ---
    private void onDemandPublished(Map<String, Object> payload) {
        // ⚠️ Non implémentable en l'état : ce service n'a pas accès à la liste des prestataires
        // d'une zone géographique (pas de duplication de données géo, pas de Feign vers Service
        // Utilisateurs prévu pour ce service). Nécessite soit une liste `targetProviderIds` déjà
        // résolue dans le payload (recommandé), soit un appel Feign GET /internal/providers.
        String demandId = getString(payload, "demandId");
        log.warn("[demand.published] Ciblage des prestataires de la zone non implémentable avec le payload "
                + "actuel (demandId={}) — nécessite l'ajout de 'targetProviderIds' au payload RabbitMQ.", demandId);
    }

    // --- 8. negotiation.conversation_opened { conversationId, clientId, providerId, demandId? } → Push destinataire ---
    private void onNegotiationConversationOpened(Map<String, Object> payload) {
        // ⚠️ Hypothèse : le destinataire de la notification est le prestataire (c'est généralement
        // le client qui initie le contact). À confirmer avec l'équipe Service Négociations — un champ
        // explicite `recipientId` (comme sur negotiation.message_sent) serait plus sûr.
        String providerId = getString(payload, "providerId");
        String conversationId = getString(payload, "conversationId");

        dispatch.sendPush(providerId, "negotiation.conversation_opened", "Nouvelle conversation",
                "Une nouvelle conversation a été ouverte avec un client.",
                Map.of("conversationId", String.valueOf(conversationId)));
    }

    // --- 9. negotiation.quote_accepted { quoteId, demandId, clientId, providerId, totalAmount,
    //         paymentMethod, phoneNumber } → Push prestataire "Devis accepté" ---
    private void onNegotiationQuoteAccepted(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String quoteId = getString(payload, "quoteId");

        dispatch.sendPush(providerId, "negotiation.quote_accepted", "Devis accepté",
                "Votre devis a été accepté, paiement en cours.", Map.of("quoteId", String.valueOf(quoteId)));
    }

    // --- 10. negotiation.message_sent { conversationId, recipientId, senderRole } → Push destinataire ---
    private void onNegotiationMessageSent(Map<String, Object> payload) {
        String recipientId = getString(payload, "recipientId");
        String senderRole = getOptionalString(payload, "senderRole").orElse("un correspondant");

        dispatch.sendPush(recipientId, "negotiation.message_sent", "Nouveau message",
                "Nouveau message de " + senderRole + ".",
                Map.of("conversationId", String.valueOf(getString(payload, "conversationId"))));
    }

    // --- 11. negotiation.quote_refused { quoteId, demandId, providerId } → Push prestataire "Devis refusé" ---
    private void onNegotiationQuoteRefused(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        String quoteId = getString(payload, "quoteId");

        dispatch.sendPush(providerId, "negotiation.quote_refused", "Devis refusé",
                "Votre devis a été refusé par le client.", Map.of("quoteId", String.valueOf(quoteId)));
    }

    // --- 12. payment.confirmed { transactionId, missionId, clientId, providerId, amount } →
    //         Push client + prestataire "Paiement confirmé" ---
    private void onPaymentConfirmed(Map<String, Object> payload) {
        String clientId = getString(payload, "clientId");
        String providerId = getString(payload, "providerId");
        String missionId = getString(payload, "missionId");

        String body = "Paiement confirmé, la mission peut démarrer.";
        dispatch.sendPush(clientId, "payment.confirmed", "Paiement confirmé", body,
                Map.of("missionId", String.valueOf(missionId)));
        dispatch.sendPush(providerId, "payment.confirmed", "Paiement confirmé", body,
                Map.of("missionId", String.valueOf(missionId)));
    }

    // --- 13. payment.failed { transactionId, demandId, clientId, reason } → Push client "Paiement échoué" ---
    private void onPaymentFailed(Map<String, Object> payload) {
        String clientId = getString(payload, "clientId");
        String reason = getOptionalString(payload, "reason").orElse("raison non précisée");

        dispatch.sendPush(clientId, "payment.failed", "Paiement échoué",
                "Le paiement a échoué : " + reason, Map.of());
    }

    // --- 14. payment.released { transactionId, missionId, providerId, netAmount } →
    //         Push prestataire "Paiement libéré : [montant] XAF" ---
    private void onPaymentReleased(Map<String, Object> payload) {
        String providerId = getString(payload, "providerId");
        long netAmount = getLong(payload, "netAmount");
        String formattedAmount = XAF_FORMAT.format(netAmount);

        dispatch.sendPush(providerId, "payment.released", "Paiement libéré",
                "Paiement libéré : " + formattedAmount + " XAF", Map.of());
    }

    // --- 15. litige.opened { litigeId, transactionId, clientId, providerId } → Push admin "Nouveau litige" ---
    private void onLitigeOpened(Map<String, Object> payload) {
        String litigeId = getString(payload, "litigeId");

        dispatch.sendPush(NotificationDispatchService.ADMIN_BROADCAST_USER_ID, "litige.opened",
                "Nouveau litige", "Nouveau litige #" + litigeId, Map.of("litigeId", String.valueOf(litigeId)));
    }

    // --- 16. litige.assigned { litigeId, agentId } → Push agent "Litige assigné à vous" ---
    private void onLitigeAssigned(Map<String, Object> payload) {
        String agentId = getString(payload, "agentId");
        String litigeId = getString(payload, "litigeId");

        dispatch.sendPush(agentId, "litige.assigned", "Litige assigné",
                "Le litige #" + litigeId + " vous a été assigné.", Map.of("litigeId", String.valueOf(litigeId)));
    }

    // --- 17. litige.resolved { litigeId, resolution, refundAmount } → Push client + prestataire "Litige résolu" ---
    // ⚠️ Le payload ne contient ni clientId ni providerId : il faut soit les ajouter au payload,
    // soit que ce handler appelle Service Litiges (pas prévu dans le périmètre actuel). En attendant,
    // log explicite plutôt qu'un envoi vers un mauvais destinataire.
    private void onLitigeResolved(Map<String, Object> payload) {
        String litigeId = getString(payload, "litigeId");
        String clientId = getString(payload, "clientId");
        String providerId = getString(payload, "providerId");

        if (clientId == null && providerId == null) {
            log.warn("[litige.resolved] Payload sans clientId/providerId pour litigeId={} — "
                    + "impossible de notifier les parties (champs à ajouter au payload RabbitMQ).", litigeId);
            return;
        }

        String body = "Le litige #" + litigeId + " a été résolu.";
        if (clientId != null) {
            dispatch.sendPush(clientId, "litige.resolved", "Litige résolu", body, Map.of("litigeId", String.valueOf(litigeId)));
        }
        if (providerId != null) {
            dispatch.sendPush(providerId, "litige.resolved", "Litige résolu", body, Map.of("litigeId", String.valueOf(litigeId)));
        }
    }
}
