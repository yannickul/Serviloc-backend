// application/service/AgentLitigeService.java
package com.serviloc.litiges.application.service;

import com.serviloc.litiges.application.dto.request.AgentSuspendUserRequest;
import com.serviloc.litiges.application.dto.request.ProposeResolutionRequest;
import com.serviloc.litiges.application.dto.request.SendMessageRequest;
import com.serviloc.litiges.application.dto.response.*;
import com.serviloc.litiges.application.port.in.AgentLitigeUseCase;
import com.serviloc.litiges.application.port.out.ConversationPort;
import com.serviloc.litiges.application.port.out.PaymentPort;
import com.serviloc.litiges.application.port.out.UserProfilePort;
import com.serviloc.litiges.domain.event.LitigeResolutionProposedEvent;
import com.serviloc.litiges.domain.event.LitigeResolvedEvent;
import com.serviloc.litiges.domain.exception.LitigeNotFoundException;
import com.serviloc.litiges.domain.exception.PaymentServiceUnavailableException;
import com.serviloc.litiges.domain.exception.UnauthorizedLitigeAccessException;
import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeMessage;
import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.domain.model.Resolution;
import com.serviloc.litiges.domain.model.ResolutionType;
import com.serviloc.litiges.domain.repository.LitigeMessageRepository;
import com.serviloc.litiges.domain.repository.LitigeRepository;
import com.serviloc.litiges.domain.repository.ResolutionRepository;
import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import com.serviloc.litiges.infrastructure.messaging.LitigeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentLitigeService implements AgentLitigeUseCase {

    private final LitigeRepository litigeRepository;
    private final ResolutionRepository resolutionRepository;
    private final LitigeMessageRepository litigeMessageRepository;
    private final ConversationPort conversationPort;
    private final PaymentPort paymentPort;
    private final UserProfilePort userProfilePort;
    private final LitigeEventPublisher eventPublisher;
    private final LitigeService litigeService;

    @Override
    public LitigeListResponse getMyLitiges(String agentId, int page, int limit, LitigeStatus status) {
        List<Litige> litiges = litigeRepository.findAll(page, limit, status, agentId);
        List<LitigeResponse> data = litiges.stream().map(litigeService::toResponse).toList();
        // Les metrics de la liste agent portent sur son propre portefeuille, pas sur toute la plateforme
        long open = data.stream().filter(l -> "ouvert".equals(l.status())).count();
        long inProgress = data.stream().filter(l -> "assigne".equals(l.status())).count();
        long resolvedThisMonth = data.stream().filter(l -> "resolu".equals(l.status()) || "cloture".equals(l.status())).count();
        BigDecimal totalBlocked = litiges.stream()
                .filter(l -> l.getStatus() == LitigeStatus.OUVERT || l.getStatus() == LitigeStatus.EN_COURS)
                .map(Litige::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LitigeListResponse(new LitigeMetrics(open, inProgress, resolvedThisMonth, totalBlocked), data);
    }

    @Override
    public LitigeDetailResponse getLitigeById(String agentId, String litigeId) {
        Litige litige = getOwnedLitige(agentId, litigeId);
        ConversationDto conversation = conversationPort.getConversation(litige.getDemandId());
        var client = userProfilePort.getProfile(litige.getClientId());
        var provider = userProfilePort.getProfile(litige.getProviderId());
        var agent = userProfilePort.getProfile(litige.getAgentId());
        return new LitigeDetailResponse(litigeService.toResponse(litige), conversation, client, provider, agent);
    }

    @Override
    public LitigeHistoryResponse getHistory(String agentId, String litigeId) {
        Litige litige = getOwnedLitige(agentId, litigeId);
        ConversationDto conversation = conversationPort.getConversation(litige.getDemandId());
        List<LitigeMessageResponse> messages = toMessageResponses(litigeMessageRepository.findByLitigeId(litigeId));
        return new LitigeHistoryResponse(conversation, messages);
    }

    @Override
    public List<LitigeMessageResponse> getMessages(String agentId, String litigeId) {
        getOwnedLitige(agentId, litigeId);
        return toMessageResponses(litigeMessageRepository.findByLitigeId(litigeId));
    }

    @Override
    @Transactional
    public LitigeMessageResponse sendMessage(String agentId, String litigeId, SendMessageRequest request) {
        getOwnedLitige(agentId, litigeId);

        LitigeMessage message = new LitigeMessage();
        message.setId(UUID.randomUUID().toString());
        message.setLitigeId(litigeId);
        message.setSenderId(agentId);
        message.setSenderRole("AGENT");
        message.setContent(request.content());
        message.setSentAt(Instant.now());

        LitigeMessage saved = litigeMessageRepository.save(message);
        log.info("[LITIGE] Message agent envoyé — litigeId={} agentId={}", litigeId, agentId);
        return toMessageResponse(saved);
    }

    @Override
    @Transactional
    public void proposeResolution(String agentId, String litigeId, ProposeResolutionRequest request) {
        Litige litige = getOwnedLitige(agentId, litigeId);

        if (litige.getStatus() != LitigeStatus.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de proposer une résolution pour le litige " + litigeId +
                            " — statut actuel : " + litige.getStatus());
        }
        validateResolutionAmount(litige, request);

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

        litige.setResolution(resolution);
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);

        log.info("[LITIGE] Résolution proposée — litigeId={} type={} agentId={}", litigeId, request.type(), agentId);
        eventPublisher.publishLitigeResolutionProposed(new LitigeResolutionProposedEvent(
                litigeId, agentId, request.type().name(), request.refundAmount()));
    }

    @Override
    @Transactional
    public void updateResolution(String agentId, String litigeId, ProposeResolutionRequest request) {
        Litige litige = getOwnedLitige(agentId, litigeId);

        if (litige.getStatus() != LitigeStatus.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de modifier la résolution du litige " + litigeId +
                            " — statut actuel : " + litige.getStatus());
        }
        Resolution existing = resolutionRepository.findByLitigeId(litigeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune résolution à modifier pour le litige " + litigeId + " — utilisez POST d'abord"));
        validateResolutionAmount(litige, request);

        existing.setType(request.type());
        existing.setRefundAmount(request.refundAmount());
        existing.setNote(request.note());
        // Toute modification remet les deux parties en attente de validation
        existing.setClientAccepted(null);
        existing.setProviderAccepted(null);
        resolutionRepository.save(existing);

        litige.setResolution(existing);
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);

        log.info("[LITIGE] Résolution modifiée — litigeId={} type={} agentId={}", litigeId, request.type(), agentId);
        eventPublisher.publishLitigeResolutionProposed(new LitigeResolutionProposedEvent(
                litigeId, agentId, request.type().name(), request.refundAmount()));
    }

    @Override
    @Transactional
    public void closeLitige(String agentId, String litigeId) {
        Litige litige = getOwnedLitige(agentId, litigeId);

        if (litige.getStatus() != LitigeStatus.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de clôturer le litige " + litigeId +
                            " — statut actuel : " + litige.getStatus());
        }
        Resolution resolution = resolutionRepository.findByLitigeId(litigeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune résolution proposée pour le litige " + litigeId));

        boolean requiresBothAcceptances = resolution.getType() != ResolutionType.REJET;
        if (requiresBothAcceptances &&
                !(Boolean.TRUE.equals(resolution.getClientAccepted())
                        && Boolean.TRUE.equals(resolution.getProviderAccepted()))) {
            throw new IllegalStateException(
                    "Le client et le prestataire doivent tous deux accepter la résolution avant clôture");
        }

        if (resolution.getType() == ResolutionType.REMBOURSEMENT_TOTAL
                || resolution.getType() == ResolutionType.REMBOURSEMENT_PARTIEL) {
            try {
                paymentPort.refund(litige.getTransactionId(), resolution.getRefundAmount());
            } catch (Exception e) {
                log.error("[LITIGE] Remboursement échoué pour transactionId={}", litige.getTransactionId(), e);
                throw new PaymentServiceUnavailableException(
                        "Remboursement indisponible — service-paiement non joignable");
            }
        }

        litige.setStatus(LitigeStatus.RESOLU);
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);

        log.info("[LITIGE] Clôturé — litigeId={} type={} agentId={}", litigeId, resolution.getType(), agentId);
        eventPublisher.publishLitigeResolved(new LitigeResolvedEvent(
                litige.getId(),
                LitigeEventPublisher.toResolutionLabel(resolution.getType()),
                resolution.getRefundAmount(),
                litige.getClientId(),
                litige.getProviderId()
        ));
    }

    @Override
    public void suspendUser(String agentId, String litigeId, AgentSuspendUserRequest request) {
        Litige litige = getOwnedLitige(agentId, litigeId);

        if (!request.userId().equals(litige.getClientId()) && !request.userId().equals(litige.getProviderId())) {
            throw new IllegalArgumentException(
                    "userId doit être le client ou le prestataire du litige " + litigeId);
        }
        userProfilePort.suspend(request.userId(), request.reason());
        log.info("[LITIGE] Suspension demandée — litigeId={} userId={} agentId={}", litigeId, request.userId(), agentId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Litige getOwnedLitige(String agentId, String litigeId) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new LitigeNotFoundException(litigeId));
        if (litige.getAgentId() == null || !litige.getAgentId().equals(agentId)) {
            throw new UnauthorizedLitigeAccessException(
                    "Le litige " + litigeId + " n'est pas assigné à l'agent " + agentId);
        }
        return litige;
    }

    private void validateResolutionAmount(Litige litige, ProposeResolutionRequest request) {
        if (request.refundAmount() != null &&
                request.refundAmount().compareTo(litige.getAmount()) > 0) {
            throw new IllegalStateException(
                    "Le montant du remboursement (" + request.refundAmount() +
                            ") dépasse le montant du litige (" + litige.getAmount() + ")");
        }
        if (request.type() == ResolutionType.REMBOURSEMENT_PARTIEL) {
            if (request.refundAmount() == null ||
                    request.refundAmount().compareTo(BigDecimal.ZERO) <= 0 ||
                    request.refundAmount().compareTo(litige.getAmount()) >= 0) {
                throw new IllegalStateException(
                        "REMBOURSEMENT_PARTIEL requiert un montant > 0 et < " + litige.getAmount());
            }
        }
    }

    private List<LitigeMessageResponse> toMessageResponses(List<LitigeMessage> messages) {
        return messages.stream().map(this::toMessageResponse).toList();
    }

    private LitigeMessageResponse toMessageResponse(LitigeMessage m) {
        return new LitigeMessageResponse(m.getId(), m.getLitigeId(), m.getSenderId(), m.getSenderRole(), m.getContent(), m.getSentAt());
    }
}
