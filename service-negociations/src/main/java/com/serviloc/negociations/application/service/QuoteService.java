package com.serviloc.negociations.application.service;

import com.serviloc.negociations.application.dto.NegociationDtos.*;
import com.serviloc.negociations.domain.model.Material;
import com.serviloc.negociations.domain.model.Quote;
import com.serviloc.negociations.domain.model.QuoteStatus;
import com.serviloc.negociations.domain.repository.ConversationRepository;
import com.serviloc.negociations.domain.repository.QuoteRepository;
import com.serviloc.negociations.infrastructure.messaging.NegociationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'");

    private final QuoteRepository quoteRepository;
    private final ConversationRepository conversationRepository;
    private final NegociationEventPublisher eventPublisher;

    public QuoteService(QuoteRepository quoteRepository,
                        ConversationRepository conversationRepository,
                        NegociationEventPublisher eventPublisher) {
        this.quoteRepository = quoteRepository;
        this.conversationRepository = conversationRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── POST /internal/quotes ────────────────────────────────────

    public QuoteResponse createQuote(UUID conversationId, CreateQuoteRequest request) {
        UUID demandId   = UUID.fromString(request.demandId());
        UUID providerId = UUID.fromString(request.providerId());

        List<Material> materials = request.materials() != null
                ? request.materials().stream()
                  .map(m -> new Material(m.name(), m.quantity(), m.unitPrice()))
                  .toList()
                : List.of();

        Quote quote = Quote.create(
                conversationId, demandId, providerId,
                request.amount(), request.description(),
                materials, request.estimatedDurationHours()
        );

        Quote saved = quoteRepository.save(quote);
        log.info("[QUOTE] Devis créé : id={} demandId={} amount={}",
                saved.getId(), demandId, saved.getAmount());

        return toQuoteResponse(saved);
    }

    // ─── PUT /internal/quotes/:id ─────────────────────────────────

    public QuoteResponse updateQuote(UUID quoteId, UpdateQuoteRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Devis introuvable : " + quoteId));

        if (quote.getStatus() != QuoteStatus.EN_ATTENTE) {
            throw new IllegalStateException("Seul un devis en attente peut être modifié");
        }

        List<Material> materials = request.materials() != null
                ? request.materials().stream()
                  .map(m -> new Material(m.name(), m.quantity(), m.unitPrice()))
                  .toList()
                : quote.getMaterials();

        // Recréer le devis avec les nouvelles valeurs
        Quote updated = Quote.create(
                quote.getConversationId(), quote.getDemandId(), quote.getProviderId(),
                request.amount() > 0 ? request.amount() : quote.getAmount(),
                request.description() != null ? request.description() : quote.getDescription(),
                materials,
                request.estimatedDurationHours() > 0
                        ? request.estimatedDurationHours()
                        : quote.getEstimatedDurationHours()
        );

        Quote saved = quoteRepository.save(updated);
        log.info("[QUOTE] Devis mis à jour : id={}", quoteId);
        return toQuoteResponse(saved);
    }

    // ─── PATCH /internal/quotes/:id/status ───────────────────────

    public QuoteResponse updateQuoteStatus(UUID quoteId, UpdateQuoteStatusRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Devis introuvable : " + quoteId));

        switch (request.status().toLowerCase()) {
            case "accepte" -> {
                quote.accept();
                quoteRepository.save(quote);

                // Déclenche la Saga 1 — Paiement reçoit cet event
                eventPublisher.publishQuoteAccepted(
                        quote.getId(), quote.getDemandId(),
                        getClientIdFromConversation(quote.getConversationId()),
                        quote.getProviderId(), quote.getAmount(),
                        request.paymentMethod() != null ? request.paymentMethod() : "orange_money",
                        request.phoneNumber() != null ? request.phoneNumber() : ""
                );
                log.info("[QUOTE] Devis accepté → Saga 1 déclenchée : quoteId={}", quoteId);
            }
            case "refuse" -> {
                quote.refuse();
                quoteRepository.save(quote);
                eventPublisher.publishQuoteRefused(
                        quote.getId(), quote.getDemandId(), quote.getProviderId());
                log.info("[QUOTE] Devis refusé : quoteId={}", quoteId);
            }
            default -> throw new IllegalArgumentException(
                    "Statut invalide : " + request.status() + " (accepte|refuse)");
        }

        return toQuoteResponse(quote);
    }

    // ─── Consumer payment.failed → reset devis en EN_ATTENTE ─────

    public void resetQuoteOnPaymentFailed(UUID demandId) {
        quoteRepository.findByDemandId(demandId).ifPresent(quote -> {
            if (quote.getStatus() == QuoteStatus.ACCEPTE) {
                quote.resetToWaiting();
                quoteRepository.save(quote);
                log.info("[QUOTE] Devis remis en attente suite payment.failed : demandId={}",
                        demandId);
            }
        });
    }

    // ─── @Scheduled — expiration des devis ───────────────────────

    @Scheduled(fixedDelay = 3600000) // toutes les heures
    @Transactional
    public void expireQuotes() {
        List<Quote> expired = quoteRepository.findExpiredPending();
        expired.forEach(quote -> {
            quote.expire();
            quoteRepository.save(quote);
            log.info("[QUOTE] Devis expiré : id={} demandId={}",
                    quote.getId(), quote.getDemandId());
        });
        if (!expired.isEmpty()) {
            log.info("[QUOTE] {} devis expirés", expired.size());
        }
    }

    // ─── Helper ───────────────────────────────────────────────────

    private UUID getClientIdFromConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conv -> conv.getClientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation introuvable : " + conversationId));
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