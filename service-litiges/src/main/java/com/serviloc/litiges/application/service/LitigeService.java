// application/service/LitigeService.java
package com.serviloc.litiges.application.service;

import com.serviloc.litiges.application.dto.request.CreateLitigeRequest;
import com.serviloc.litiges.application.dto.response.LitigeResponse;
import com.serviloc.litiges.application.dto.response.ResolutionDto;
import com.serviloc.litiges.application.port.in.LitigeUseCase;
import com.serviloc.litiges.application.port.out.PaymentPort;
import com.serviloc.litiges.domain.event.LitigeOpenedEvent;
import com.serviloc.litiges.domain.exception.LitigeNotFoundException;
import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.domain.repository.LitigeRepository;
import com.serviloc.litiges.infrastructure.messaging.LitigeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class LitigeService implements LitigeUseCase {

    private final LitigeRepository litigeRepository;
    private final PaymentPort paymentPort;
    private final LitigeEventPublisher eventPublisher;

    // Compteur en mémoire pour la référence — suffisant en monorepo mono-instance
    // En multi-instances, utiliser une séquence PostgreSQL (Sprint 3)
    private static final AtomicLong referenceCounter = new AtomicLong(1);

    @Override
    @Transactional
    public LitigeResponse createLitige(CreateLitigeRequest request) {

        // Règle 6 — Idempotence : un litige OUVERT ne peut exister en double pour la même mission
        boolean alreadyOpen = litigeRepository.existsByMissionIdAndStatus(
                request.missionId(), LitigeStatus.OUVERT);
        if (alreadyOpen) {
            throw new IllegalStateException(
                    "Un litige est déjà ouvert pour la mission : " + request.missionId());
        }

        Instant now = Instant.now();
        String id = UUID.randomUUID().toString();
        String reference = generateReference();

        Litige litige = new Litige();
        litige.setId(id);
        litige.setReference(reference);
        litige.setDemandId(request.demandId());
        litige.setMissionId(request.missionId());
        litige.setClientId(request.clientId());
        litige.setProviderId(request.providerId());
        litige.setMotifId(request.motifId());
        litige.setDescription(request.description());
        litige.setEvidenceIds(request.evidenceIds());
        litige.setAmount(request.amount());
        litige.setStatus(LitigeStatus.OUVERT);
        litige.setAgentId(null);
        litige.setResolution(null);
        litige.setTransactionId(request.transactionId());
        litige.setCreatedAt(now);
        litige.setUpdatedAt(now);

        Litige saved = litigeRepository.save(litige);
        log.info("[LITIGE] Créé — id={} reference={} missionId={}", id, reference, request.missionId());

        // Gel de la transaction — non bloquant si Paiement est down (fallback log)
        try {
            paymentPort.freezeTransaction(request.transactionId());
        } catch (Exception e) {
            log.error("[LITIGE] Gel transaction échoué pour transactionId={} — litige créé quand même",
                    request.transactionId(), e);
        }

        // Publication de l'event RabbitMQ
        eventPublisher.publishLitigeOpened(new LitigeOpenedEvent(
                saved.getId(),
                saved.getReference(),
                saved.getDemandId(),
                saved.getMissionId(),
                saved.getClientId(),
                saved.getProviderId(),
                saved.getMotifId(),
                saved.getCreatedAt()
        ));

        return toResponse(saved);
    }

    @Override
    public LitigeResponse getLitigeById(String id) {
        Litige litige = litigeRepository.findById(id)
                .orElseThrow(() -> new LitigeNotFoundException(id));
        return toResponse(litige);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateReference() {
        int year = Year.now().getValue();
        long seq = referenceCounter.getAndIncrement();
        return String.format("LIT-%d-%04d", year, seq);
    }

    LitigeResponse toResponse(Litige litige) {
        ResolutionDto resolutionDto = null;
        if (litige.getResolution() != null) {
            var r = litige.getResolution();
            resolutionDto = new ResolutionDto(
                    r.getType().name(),
                    r.getRefundAmount(),
                    r.getNote(),
                    r.getClientAccepted(),
                    r.getProviderAccepted(),
                    r.getCreatedAt()
            );
        }
        return new LitigeResponse(
                litige.getId(),
                litige.getReference(),
                litige.getDemandId(),
                litige.getMissionId(),
                litige.getClientId(),
                litige.getProviderId(),
                litige.getAgentId(),
                litige.getMotifId(),
                litige.getDescription(),
                litige.getEvidenceIds(),
                litige.getAmount(),
                litige.getStatus().name(),
                resolutionDto,
                litige.getCreatedAt(),
                litige.getUpdatedAt()
        );
    }
}