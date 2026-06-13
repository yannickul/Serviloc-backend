package com.serviloc.negociations.application.service;

import com.serviloc.negociations.application.dto.NegociationDtos.*;
import com.serviloc.negociations.domain.model.Conversation;
import com.serviloc.negociations.domain.model.Message;
import com.serviloc.negociations.domain.model.Quote;
import com.serviloc.negociations.domain.repository.ConversationRepository;
import com.serviloc.negociations.domain.repository.MessageRepository;
import com.serviloc.negociations.domain.repository.QuoteRepository;
import com.serviloc.negociations.infrastructure.messaging.NegociationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
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

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               QuoteRepository quoteRepository,
                               NegociationEventPublisher eventPublisher) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.quoteRepository = quoteRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── POST /client/conversations ───────────────────────────────

    public ConversationResponse createConversation(UUID clientId,
                                                   CreateConversationRequest request) {
        UUID providerId = UUID.fromString(request.providerId());
        UUID demandId   = UUID.fromString(request.demandId());

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
    public ConversationListResponse getClientConversations(UUID clientId,
                                                           int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<Conversation> result = conversationRepository
                .findByClientIdOrderByLastMessageAtDesc(clientId, pageable);

        return new ConversationListResponse(
                result.getContent().stream()
                        .map(c -> toConversationResponse(c, clientId))
                        .toList(),
                new PageMeta(page, limit, result.getTotalElements(), result.getTotalPages())
        );
    }

    // ─── GET /provider/conversations ──────────────────────────────

    @Transactional(readOnly = true)
    public ConversationListResponse getProviderConversations(UUID providerId,
                                                             int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<Conversation> result = conversationRepository
                .findByProviderIdOrderByLastMessageAtDesc(providerId, pageable);

        return new ConversationListResponse(
                result.getContent().stream()
                        .map(c -> toConversationResponse(c, providerId))
                        .toList(),
                new PageMeta(page, limit, result.getTotalElements(), result.getTotalPages())
        );
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

        return new MessageListResponse(
                result.getContent().stream().map(this::toMessageResponse).toList(),
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
        return toMessageResponse(saved);
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

        // Stub participants — sera enrichi avec Feign Utilisateurs en S3
        ParticipantSummary clientSummary = new ParticipantSummary(
                "usr_" + c.getClientId().toString().replace("-", "").substring(0, 8),
                "Client", "", "Client", "C"
        );
        ParticipantSummary providerSummary = new ParticipantSummary(
                "usr_" + c.getProviderId().toString().replace("-", "").substring(0, 8),
                "Prestataire", "", "Prestataire", "P"
        );

        return new ConversationResponse(
                c.getId().toString(),
                c.getDemandId().toString(),
                clientSummary,
                providerSummary,
                c.getStatus().name().toLowerCase(),
                unreadCount,
                null,   // lastMessage — stub S2
                c.getCreatedAt() != null ? c.getCreatedAt().format(FORMATTER) : null,
                c.getUpdatedAt() != null ? c.getUpdatedAt().format(FORMATTER) : null
        );
    }

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
                m.getId().toString(),
                m.getConversationId().toString(),
                m.getSenderId().toString(),
                m.getSenderRole(),
                m.getContent(),
                m.getImageId(),
                m.isRead(),
                m.getSentAt() != null ? m.getSentAt().format(FORMATTER) : null
        );
    }

    private QuoteResponse toQuoteResponse(Quote q) {
        return new QuoteResponse(
                q.getId().toString(),
                q.getDemandId().toString(),
                q.getProviderId().toString(),
                q.getAmount(),
                q.getDescription(),
                q.getStatus().name().toLowerCase(),
                q.getExpiresAt() != null ? q.getExpiresAt().format(FORMATTER) : null,
                q.getCreatedAt() != null ? q.getCreatedAt().format(FORMATTER) : null
        );
    }
}