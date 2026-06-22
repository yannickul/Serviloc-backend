// application/service/AdminLitigeService.java
package com.serviloc.litiges.application.service;

import com.serviloc.litiges.application.dto.request.AssignLitigeRequest;
import com.serviloc.litiges.application.dto.request.ResolveRequest;
import com.serviloc.litiges.application.dto.response.LitigeDetailResponse;
import com.serviloc.litiges.application.dto.response.LitigeListResponse;
import com.serviloc.litiges.application.dto.response.LitigeResponse;
import com.serviloc.litiges.application.port.in.AdminLitigeUseCase;
import com.serviloc.litiges.application.port.out.ConversationPort;
import com.serviloc.litiges.application.port.out.PaymentPort;
import com.serviloc.litiges.domain.event.LitigeResolvedEvent;
import com.serviloc.litiges.domain.exception.LitigeNotFoundException;
import com.serviloc.litiges.domain.exception.PaymentServiceUnavailableException;
import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.domain.model.Resolution;
import com.serviloc.litiges.domain.model.ResolutionType;
import com.serviloc.litiges.domain.repository.LitigeRepository;
import com.serviloc.litiges.domain.repository.ResolutionRepository;
import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import com.serviloc.litiges.infrastructure.messaging.LitigeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLitigeService implements AdminLitigeUseCase {

    private final LitigeRepository litigeRepository;
    private final ResolutionRepository resolutionRepository;
    private final ConversationPort conversationPort;
    private final PaymentPort paymentPort;
    private final LitigeEventPublisher eventPublisher;
    private final LitigeService litigeService;

    @Override
    public LitigeListResponse getLitiges(int page, int limit, LitigeStatus status, String agentId) {
        List<Litige> litiges = litigeRepository.findAll(page, limit, status, agentId);
        long total = litigeRepository.count(status, agentId);
        int totalPages = (int) Math.ceil((double) total / limit);

        List<LitigeResponse> data = litiges.stream()
                .map(litigeService::toResponse)
                .toList();

        return new LitigeListResponse(data, page, limit, total, totalPages);
    }

    @Override
    public LitigeDetailResponse getLitigeById(String id) {
        Litige litige = litigeRepository.findById(id)
                .orElseThrow(() -> new LitigeNotFoundException(id));

        ConversationDto conversation = conversationPort.getConversation(litige.getDemandId());
        return new LitigeDetailResponse(litigeService.toResponse(litige), conversation);
    }

    @Override
    @Transactional
    public void assignLitige(String litigeId, AssignLitigeRequest request) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new LitigeNotFoundException(litigeId));

        // Règle 1 — assignation impossible si pas OUVERT
        if (litige.getStatus() != LitigeStatus.OUVERT) {
            throw new IllegalStateException(
                    "Impossible d'assigner le litige " + litigeId +
                            " — statut actuel : " + litige.getStatus());
        }

        litige.setAgentId(request.agentId());
        litige.setStatus(LitigeStatus.EN_COURS);
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);

        log.info("[LITIGE] Assigné — litigeId={} agentId={}", litigeId, request.agentId());
    }

    @Override
    @Transactional
    public void resolveLitige(String litigeId, ResolveRequest request, String agentId) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new LitigeNotFoundException(litigeId));

        // Règle 2 — résolution impossible si pas EN_COURS
        if (litige.getStatus() != LitigeStatus.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de résoudre le litige " + litigeId +
                            " — statut actuel : " + litige.getStatus());
        }

        // Règle 3 — le montant du remboursement ne peut pas dépasser le montant du litige
        if (request.refundAmount() != null &&
                request.refundAmount().compareTo(litige.getAmount()) > 0) {
            throw new IllegalStateException(
                    "Le montant du remboursement (" + request.refundAmount() +
                            ") dépasse le montant du litige (" + litige.getAmount() + ")");
        }

        // Règle 4 — REMBOURSEMENT_PARTIEL : montant > 0 et < amount
        if (request.type() == ResolutionType.REMBOURSEMENT_PARTIEL) {
            if (request.refundAmount() == null ||
                    request.refundAmount().compareTo(java.math.BigDecimal.ZERO) <= 0 ||
                    request.refundAmount().compareTo(litige.getAmount()) >= 0) {
                throw new IllegalStateException(
                        "REMBOURSEMENT_PARTIEL requiert un montant > 0 et < " + litige.getAmount());
            }
        }

        // Persiste la résolution
        Resolution resolution = new Resolution();
        resolution.setId(UUID.randomUUID().toString());
        resolution.setLitigeId(litigeId);
        resolution.setAgentId(agentId);
        resolution.setType(request.type());
        resolution.setRefundAmount(request.refundAmount());
        resolution.setNote(request.note());
        resolution.setClientAccepted(null);
        resolution.setProviderAccepted(null);
        resolution.setCreatedAt(Instant.now());
        resolutionRepository.save(resolution);

        // Déclenche le remboursement si applicable
        if (request.type() != ResolutionType.REJET) {
            if (request.type() != ResolutionType.REJET) {
                try {
                    paymentPort.refund(litige.getTransactionId(), request.refundAmount(), request.note());
                } catch (Exception e) {
                    log.error("[LITIGE] Remboursement échoué pour transactionId={} — litige quand même RESOLU",
                            litige.getTransactionId(), e);
                    throw new PaymentServiceUnavailableException(
                            "Remboursement indisponible — service-paiement non joignable");
                }
            }
            log.warn("[LITIGE] transactionId non disponible dans Litige — remboursement à déclencher manuellement. litigeId={}", litigeId);
            // paymentPort.refund(litige.getTransactionId(), request.refundAmount(), request.note());
        }

        litige.setStatus(LitigeStatus.RESOLU);
        litige.setResolution(resolution);
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);

        log.info("[LITIGE] Résolu — litigeId={} type={} agentId={}", litigeId, request.type(), agentId);

        // Publication de l'event
        eventPublisher.publishLitigeResolved(new LitigeResolvedEvent(
                litige.getId(),
                litige.getReference(),
                request.type().name(),
                request.refundAmount(),
                request.note(),
                Instant.now()
        ));
    }

    @Transactional
    public void closeByTransactionId(String transactionId) {
        Optional<Litige> litigeOpt = litigeRepository.findByTransactionIdAndStatus(
                transactionId, LitigeStatus.RESOLU);

        if (litigeOpt.isEmpty()) {
            log.info("[CONSUMER] payment.released — aucun litige RESOLU pour transactionId={}", transactionId);
            return;
        }

        Litige litige = litigeOpt.get();
        litige.setStatus(LitigeStatus.FERME);
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);
        log.info("[CONSUMER] Litige fermé — litigeId={} transactionId={}", litige.getId(), transactionId);
    }
}