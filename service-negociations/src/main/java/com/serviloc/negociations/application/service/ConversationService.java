package com.serviloc.negociations.application.service;

import com.serviloc.negociations.application.dto.NegociationDtos.*;
import com.serviloc.negociations.domain.model.Conversation;
import com.serviloc.negociations.domain.model.Message;
import com.serviloc.negociations.domain.model.Quote;
import com.serviloc.negociations.domain.repository.ConversationRepository;
import com.serviloc.negociations.domain.repository.MessageRepository;
import com.serviloc.negociations.domain.repository.QuoteRepository;
import com.serviloc.negociations.infrastructure.external.FichiersClient;
import com.serviloc.negociations.infrastructure.external.UtilisateursClient;
import com.serviloc.negociations.infrastructure.messaging.NegociationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'");

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final QuoteRepository quoteRepository;
    private final NegociationEventPublisher eventPublisher;
    private final FichiersClient fichiersClient;
    private final UtilisateursClient utilisateursClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${internal.token}")
    private String internalToken;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               QuoteRepository quoteRepository,
                               NegociationEventPublisher eventPublisher,
                               FichiersClient fichiersClient,
                               UtilisateursClient utilisateursClient,
                               SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.quoteRepository = quoteRepository;
        this.eventPublisher = eventPublisher;
        this.fichiersClient = fichiersClient;
        this.utilisateursClient = utilisateursClient;
        this.messagingTemplate = messagingTemplate;
    }

    // ─── POST /client/conversations ───────────────────────────────

    public ConversationResponse createConversation(UUID clientId,
                                                   CreateConversationRequest request) {
        UUID providerId = UUID.fromString(request.providerId());
        UUID demandId   = request.demandId() != null
                ? UUID.fromString(request.demandId())
                : null;

        // Idempotence — retourne la conversation existante si elle existe
        return conversationRepository
                .findByClientIdAndProviderIdAndDemandId(clientId, providerId, demandId)
                .map(existing -> {
                    log.info("[NEGO] Conversation existante retournée : id={}", existing.getId());
                    return toConversationResponse(existing, clientId);
                })
                .orElseGet(() -> {
                    Conversation conv = Conversation.create(clientId, providerId, demandId);
                    Conversation saved = conversationRepository.save(conv);
                    eventPublisher.publishConversationOpened(
                            saved.getId(), clientId, providerId, demandId);
                    log.info("[NEGO] Conversation créée : id={}", saved.getId());
                    return toConversationResponse(saved, clientId);
                });
    }

    // ─── GET /client/conversations ────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConversationResponse> getClientConversations(UUID clientId) {
        List<Conversation> conversations = conversationRepository
                .findByClientIdOrderByLastMessageAtDesc(clientId,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        return conversations.stream()
                .map(c -> toConversationResponse(c, clientId))
                .toList();
    }

    // ─── GET /provider/conversations ──────────────────────────────

    @Transactional(readOnly = true)
    public List<ConversationResponse> getProviderConversations(UUID providerId) {
        List<Conversation> conversations = conversationRepository
                .findByProviderIdOrderByLastMessageAtDesc(providerId,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        return conversations.stream()
                .map(c -> toConversationResponse(c, providerId))
                .toList();
    }

    // ─── GET /client|provider/conversations/:id/messages ──────────

    @Transactional(readOnly = true)
    public MessageListResponse getMessages(UUID conversationId, UUID requesterId,
                                           int page, int limit) {
        // Vérifie que le demandeur est participant
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));

        if (!conv.getClientId().equals(requesterId) &&
                !conv.getProviderId().equals(requesterId)) {
            throw new IllegalStateException("Accès non autorisé à cette conversation");
        }

        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<Message> result = messageRepository
                .findByConversationIdOrderBySentAtDesc(conversationId, pageable);

        // Résolution des imageUrl en un seul appel batch (évite le N+1 vers service-fichiers)
        List<String> imageIds = result.getContent().stream()
                .map(Message::getImageId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        Map<String, String> urlsById = imageIds.isEmpty()
                ? Map.of()
                : fichiersClient.batchUrls(
                        new FichiersClient.BatchUrlsRequest(imageIds), internalToken)
                    .data().urls();

        return new MessageListResponse(
                result.getContent().stream()
                        .map(m -> toMessageResponse(m, urlsById.get(m.getImageId())))
                        .toList(),
                new PageMeta(page, limit, result.getTotalElements(), result.getTotalPages())
        );
    }

    // ─── POST /client|provider/conversations/:id/messages ─────────

    public MessageResponse sendMessage(UUID conversationId, UUID senderId,
                                       String senderRole, SendMessageRequest request) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));

        // Vérifie que l'expéditeur est participant
        if (!conv.getClientId().equals(senderId) &&
                !conv.getProviderId().equals(senderId)) {
            throw new IllegalStateException("Accès non autorisé à cette conversation");
        }

        Message message = Message.create(
                conversationId, senderId, senderRole,
                request.content(), request.imageId()
        );
        Message saved = messageRepository.save(message);

        // Met à jour la conversation
        conv.onNewMessage(senderRole);
        conversationRepository.save(conv);

        // Event RabbitMQ
        eventPublisher.publishMessageSent(
                conversationId, senderId, senderRole,
                conv.getClientId(), conv.getProviderId()
        );

        log.info("[NEGO] Message envoyé : convId={} senderRole={}", conversationId, senderRole);

        String imageUrl = null;
        if (saved.getImageId() != null && !saved.getImageId().isBlank()) {
            imageUrl = fichiersClient.getUrl(saved.getImageId(), internalToken).data().url();
        }
        MessageResponse response = toMessageResponse(saved, imageUrl);

        // Diffusion temps réel — /topic/conversation.{id} (relais RabbitMQ STOMP)
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId, response);

        return response;
    }

    // ─── DELETE /client|provider/conversations/:id/messages/:messageId ───

    public DeleteMessageResponse deleteMessage(UUID conversationId, UUID messageId,
                                               UUID requesterId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));

        if (!conv.getClientId().equals(requesterId) &&
                !conv.getProviderId().equals(requesterId)) {
            throw new IllegalStateException("Accès non autorisé à cette conversation");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable"));

        if (!message.getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException(
                    "Ce message n'appartient pas à cette conversation");
        }

        // Seul l'auteur du message peut le supprimer
        if (!message.getSenderId().equals(requesterId)) {
            throw new IllegalStateException("Seul l'auteur du message peut le supprimer");
        }

        message.softDelete();
        messageRepository.save(message);
        log.info("[NEGO] Message supprimé (soft-delete) : id={} convId={}",
                messageId, conversationId);

        // Diffusion temps réel — l'autre participant voit le message masqué en direct
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                toMessageResponse(message, null));

        return new DeleteMessageResponse(messageId.toString(), true);
    }

    // ─── GET /internal/quotes/:quoteId ────────────────────────────

    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(UUID quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Devis introuvable : " + quoteId));
        return toQuoteResponse(quote);
    }

    // ─── GET /internal/conversations/:demandId ────────────────────

    @Transactional(readOnly = true)
    public ConversationInternalResponse getConversationByDemandId(UUID demandId) {
        Conversation conv = conversationRepository.findByDemandId(demandId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation introuvable pour demandId : " + demandId));
        return new ConversationInternalResponse(
                conv.getId().toString(),
                conv.getDemandId().toString(),
                conv.getClientId().toString(),
                conv.getProviderId().toString(),
                conv.getStatus().name().toLowerCase(),
                conv.getCreatedAt() != null ? conv.getCreatedAt().format(FORMATTER) : null
        );
    }

    // ─── Mappers ──────────────────────────────────────────────────

    private ConversationResponse toConversationResponse(Conversation c, UUID requesterId) {
        int unreadCount = c.getClientId().equals(requesterId)
                ? c.getUnreadCountClient()
                : c.getUnreadCountProvider();

        var clientUser = utilisateursClient.getUserById(
                c.getClientId().toString(), internalToken);
        var providerUser = utilisateursClient.getUserById(
                c.getProviderId().toString(), internalToken);

        ClientSummary clientSummary = new ClientSummary(
                c.getClientId().toString(),
                clientUser.fullName(),
                clientUser.avatarInitial(),
                false   // isOnline — en attendant la validation du WebSocket (voir backlog #1)
        );
        ProviderSummary providerSummary = new ProviderSummary(
                c.getProviderId().toString(),
                providerUser.fullName(),
                providerUser.avatarInitial(),
                providerUser.rating() != null ? providerUser.rating() : 0.0,
                providerUser.specialty(),
                false   // isOnline — en attendant la validation du WebSocket (voir backlog #1)
        );

        LastMessageSummary lastMessage = messageRepository.findLastMessage(c.getId())
                .filter(m -> !m.isDeleted())
                .map(m -> new LastMessageSummary(
                        m.getContent(), m.getSentAt().format(FORMATTER), m.getSenderRole()))
                .orElse(null);

        return new ConversationResponse(
                c.getId().toString(),
                c.getDemandId() != null ? c.getDemandId().toString() : null,
                clientSummary,
                providerSummary,
                c.getStatus().name().toLowerCase(),
                unreadCount,
                lastMessage,
                c.getCreatedAt() != null ? c.getCreatedAt().format(FORMATTER) : null,
                c.getUpdatedAt() != null ? c.getUpdatedAt().format(FORMATTER) : null
        );
    }

    private MessageResponse toMessageResponse(Message m, String imageUrl) {
        boolean deleted = m.isDeleted();
        return new MessageResponse(
                m.getId().toString(),
                m.getConversationId().toString(),
                m.getSenderId().toString(),
                m.getSenderRole(),
                deleted ? "Message supprimé" : m.getContent(),
                deleted ? null : m.getImageId(),
                deleted ? null : imageUrl,
                m.isRead(),
                deleted,
                m.getSentAt() != null ? m.getSentAt().format(FORMATTER) : null
        );
    }

    private QuoteResponse toQuoteResponse(Quote q) {
        List<MaterialResponse> materials = q.getMaterials() != null
                ? q.getMaterials().stream()
                    .map(m -> new MaterialResponse(
                            m.id().toString(), m.name(), m.quantity(),
                            m.unitPrice(), m.subtotal()))
                    .toList()
                : List.of();
        double materialsTotal = materials.stream().mapToDouble(MaterialResponse::subtotal).sum();

        UUID clientId = conversationRepository.findById(q.getConversationId())
                .map(Conversation::getClientId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation introuvable : " + q.getConversationId()));

        return new QuoteResponse(
                q.getId().toString(),
                q.getDemandId().toString(),
                q.getProviderId().toString(),
                clientId.toString(),
                q.getDescription(),
                q.getAmount(),
                materials,
                materialsTotal,
                q.getAmount() + materialsTotal,
                q.getEstimatedDurationHours(),
                q.getValidityDays(),
                q.getStatus().name().toLowerCase(),
                q.getCreatedAt() != null ? q.getCreatedAt().format(FORMATTER) : null,
                q.getExpiresAt() != null ? q.getExpiresAt().format(FORMATTER) : null
        );
    }
}