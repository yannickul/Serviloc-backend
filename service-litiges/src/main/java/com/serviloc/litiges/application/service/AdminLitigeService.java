// application/service/AdminLitigeService.java
package com.serviloc.litiges.application.service;

import com.serviloc.litiges.application.dto.request.AssignLitigeRequest;
import com.serviloc.litiges.application.dto.request.ReassignLitigeRequest;
import com.serviloc.litiges.application.dto.response.*;
import com.serviloc.litiges.application.port.in.AdminLitigeUseCase;
import com.serviloc.litiges.application.port.out.ConversationPort;
import com.serviloc.litiges.application.port.out.UserProfilePort;
import com.serviloc.litiges.domain.event.LitigeAssignedEvent;
import com.serviloc.litiges.domain.exception.LitigeNotFoundException;
import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.domain.repository.LitigeRepository;
import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import com.serviloc.litiges.infrastructure.messaging.LitigeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLitigeService implements AdminLitigeUseCase {

    private final LitigeRepository litigeRepository;
    private final ConversationPort conversationPort;
    private final UserProfilePort userProfilePort;
    private final LitigeEventPublisher eventPublisher;
    private final LitigeService litigeService;

    @Override
    public LitigeListResponse getLitiges(int page, int limit, LitigeStatus status, String agentId) {
        List<Litige> litiges = litigeRepository.findAll(page, limit, status, agentId);

        List<LitigeResponse> data = litiges.stream()
                .map(litigeService::toResponse)
                .toList();

        LitigeMetrics metrics = buildMetrics();
        return new LitigeListResponse(metrics, data);
    }

    /** Pagination exposée via le `meta` de l'ApiResponse (voir AdminLitigeController). */
    public PageMeta getPageMeta(int page, int limit, LitigeStatus status, String agentId) {
        long total = litigeRepository.count(status, agentId);
        int totalPages = limit > 0 ? (int) Math.ceil((double) total / limit) : 0;
        return new PageMeta(page, limit, total, totalPages);
    }

    @Override
    public LitigeDetailResponse getLitigeById(String id) {
        Litige litige = litigeRepository.findById(id)
                .orElseThrow(() -> new LitigeNotFoundException(id));

        ConversationDto conversation = conversationPort.getConversation(litige.getDemandId());
        var client = userProfilePort.getProfile(litige.getClientId());
        var provider = userProfilePort.getProfile(litige.getProviderId());
        var agent = litige.getAgentId() != null ? userProfilePort.getProfile(litige.getAgentId()) : null;

        return new LitigeDetailResponse(litigeService.toResponse(litige), conversation, client, provider, agent);
    }

    @Override
    @Transactional
    public com.serviloc.litiges.application.dto.response.AssignResponse assignLitige(String litigeId, AssignLitigeRequest request) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new LitigeNotFoundException(litigeId));

        // Règle 1 — première assignation impossible si pas OUVERT (réassignation : voir reassignLitige)
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
        eventPublisher.publishLitigeAssigned(new LitigeAssignedEvent(litigeId, request.agentId()));
        return new com.serviloc.litiges.application.dto.response.AssignResponse(
                litigeId, request.agentId(), litige.getStatus().toContractLabel());
    }

    @Override
    @Transactional
    public com.serviloc.litiges.application.dto.response.AssignResponse reassignLitige(String litigeId, ReassignLitigeRequest request) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new LitigeNotFoundException(litigeId));

        // La réassignation ne concerne qu'un litige déjà pris en charge
        if (litige.getStatus() != LitigeStatus.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de réassigner le litige " + litigeId +
                            " — statut actuel : " + litige.getStatus());
        }

        litige.setAgentId(request.agentId());
        litige.setUpdatedAt(Instant.now());
        litigeRepository.save(litige);

        log.info("[LITIGE] Réassigné — litigeId={} nouvelAgentId={}", litigeId, request.agentId());
        eventPublisher.publishLitigeAssigned(new LitigeAssignedEvent(litigeId, request.agentId()));
        return new com.serviloc.litiges.application.dto.response.AssignResponse(
                litigeId, request.agentId(), litige.getStatus().toContractLabel());
    }

    @Override
    public StatsResponse getStats() {
        long open = litigeRepository.countByStatus(LitigeStatus.OUVERT);
        long inProgress = litigeRepository.countByStatus(LitigeStatus.EN_COURS);
        Instant startOfMonth = Instant.now().truncatedTo(ChronoUnit.DAYS)
                .minus((Instant.now().atZone(java.time.ZoneOffset.UTC).getDayOfMonth() - 1L), ChronoUnit.DAYS);
        long resolvedThisMonth = litigeRepository.countByStatusAndUpdatedAtAfter(LitigeStatus.RESOLU, startOfMonth)
                + litigeRepository.countByStatusAndUpdatedAtAfter(LitigeStatus.FERME, startOfMonth);
        var totalBlocked = litigeRepository.sumAmountByStatusIn(List.of(LitigeStatus.OUVERT, LitigeStatus.EN_COURS));
        return new StatsResponse(open, inProgress, resolvedThisMonth, totalBlocked);
    }

    private LitigeMetrics buildMetrics() {
        StatsResponse stats = getStats();
        return new LitigeMetrics(stats.open(), stats.inProgress(), stats.resolvedThisMonth(), stats.totalBlockedAmount());
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
