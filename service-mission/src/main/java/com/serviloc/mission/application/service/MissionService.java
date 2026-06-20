// application/service/MissionService.java
package com.serviloc.mission.application.service;

import com.serviloc.mission.application.dto.request.CreateLitigeRequest;
import com.serviloc.mission.application.dto.request.LocationDto;
import com.serviloc.mission.application.dto.request.RateMissionRequest;
import com.serviloc.mission.application.dto.response.MissionResponse;
import com.serviloc.mission.application.port.in.MissionUseCase;
import com.serviloc.mission.domain.event.*;
import com.serviloc.mission.domain.exception.DoubleValidationAlreadyDoneException;
import com.serviloc.mission.domain.exception.MissionNotFoundException;
import com.serviloc.mission.domain.exception.UnauthorizedMissionAccessException;
import com.serviloc.mission.domain.model.*;
import com.serviloc.mission.domain.repository.EvaluationRepository;
import com.serviloc.mission.domain.repository.MissionRepository;
import com.serviloc.mission.infrastructure.external.UpdateRatingRequest;
import com.serviloc.mission.infrastructure.external.UtilisateurClient;
import com.serviloc.mission.infrastructure.messaging.MissionEventPublisher;
import com.serviloc.mission.infrastructure.persistence.entity.MissionValidationJpaEntity;
import com.serviloc.mission.infrastructure.persistence.repository.MissionValidationJpaRepository;
import com.serviloc.mission.infrastructure.persistence.repository.MissionStepJpaRepository;
import com.serviloc.mission.application.port.out.PaymentPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MissionService implements MissionUseCase {

    private final MissionRepository missionRepository;
    private final EvaluationRepository evaluationRepository;
    private final MissionEventPublisher eventPublisher;
    private final UtilisateurClient utilisateurClient;
    private final PaymentPort paymentPort;
    private final MissionValidationJpaRepository validationRepository;
    private final MissionStepJpaRepository stepRepository;

    public MissionService(
            MissionRepository missionRepository,
            EvaluationRepository evaluationRepository,
            MissionEventPublisher eventPublisher,
            UtilisateurClient utilisateurClient,
            PaymentPort paymentPort,
            MissionValidationJpaRepository validationRepository,
            MissionStepJpaRepository stepRepository) {
        this.missionRepository = missionRepository;
        this.evaluationRepository = evaluationRepository;
        this.eventPublisher = eventPublisher;
        this.utilisateurClient = utilisateurClient;
        this.paymentPort = paymentPort;
        this.validationRepository = validationRepository;
        this.stepRepository = stepRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionResponse getMissionById(String id, String userId, String role) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException(id));

        if (role.equals("client") && !mission.getClientId().equals(userId)) {
            throw new UnauthorizedMissionAccessException(userId, id, "mission");
        }
        if (role.equals("provider") && !mission.getProviderId().equals(userId)) {
            throw new UnauthorizedMissionAccessException(userId, id, "mission");
        }

        return toResponse(mission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getMissionsByProvider(String providerId) {
        return missionRepository.findByProviderId(providerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getMissionsByClient(String clientId) {
        return missionRepository.findByClientId(clientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Tâche 3 — POST /provider/missions/:id/start
    @Override
    public void startMission(String missionId, String providerId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        // Contrainte métier section 18.1 : démarrage uniquement si EN_ATTENTE
        if (mission.getStatus() != MissionStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                    "La mission " + missionId + " ne peut pas démarrer depuis le status " + mission.getStatus());
        }

        mission.setStatus(MissionStatus.EN_COURS);
        mission.setStartedAt(Instant.now());
        missionRepository.save(mission);

        eventPublisher.publishMissionStarted(
                new MissionStartedEvent(mission.getId(), mission.getProviderId(), mission.getClientId()));
    }

    // Tâche 4 — POST /provider/missions/:id/complete
    @Override
    public void completeMission(String missionId, String providerId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_COURS) {
            throw new IllegalStateException(
                    "La mission " + missionId + " doit être EN_COURS pour être complétée");
        }

        // Idempotence : section 18.2 — un même rôle ne valide qu'une fois
        boolean alreadyValidated = validationRepository
                .existsByMissionIdAndRole(missionId, "PROVIDER");
        if (alreadyValidated) {
            throw new DoubleValidationAlreadyDoneException(missionId, "PROVIDER");
        }

        MissionValidationJpaEntity validation = new MissionValidationJpaEntity();
        validation.setId("val_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        validation.setMissionId(missionId);
        validation.setValidatedBy(providerId);
        validation.setRole("PROVIDER");
        validation.setValidatedAt(Instant.now());
        validationRepository.save(validation);

        eventPublisher.publishMissionValidated(
                new MissionValidatedEvent(missionId, providerId, "PROVIDER"));
    }

    // Tâche 5 — POST /client/missions/:id/validate
    @Override
    public void validateMission(String missionId, String clientId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_COURS) {
            throw new IllegalStateException(
                    "La mission " + missionId + " doit être EN_COURS pour être validée");
        }

        // Idempotence : section 18.2
        boolean alreadyValidated = validationRepository
                .existsByMissionIdAndRole(missionId, "CLIENT");
        if (alreadyValidated) {
            throw new DoubleValidationAlreadyDoneException(missionId, "CLIENT");
        }

        MissionValidationJpaEntity validation = new MissionValidationJpaEntity();
        validation.setId("val_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        validation.setMissionId(missionId);
        validation.setValidatedBy(clientId);
        validation.setRole("CLIENT");
        validation.setValidatedAt(Instant.now());
        validationRepository.save(validation);

        eventPublisher.publishMissionValidated(
                new MissionValidatedEvent(missionId, clientId, "CLIENT"));

        // Double validation atteinte → libération des fonds (section 11, Saga 2)
        boolean providerAlsoValidated = validationRepository
                .existsByMissionIdAndRole(missionId, "PROVIDER");

        if (providerAlsoValidated) {
            paymentPort.releaseTransaction(mission.getQuoteId());

            mission.setStatus(MissionStatus.TERMINEE);
            mission.setCompletedAt(Instant.now());
            missionRepository.save(mission);

            eventPublisher.publishMissionCompleted(
                    new MissionCompletedEvent(
                            missionId,
                            mission.getClientId(),
                            mission.getProviderId(),
                            mission.getTotalAmount()));
        }
    }

    // Tâche 6 — PATCH /provider/missions/:id/steps/:stepId
    @Override
    public void updateStep(String missionId, String stepId, String providerId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_COURS) {
            throw new IllegalStateException(
                    "Les étapes ne peuvent être mises à jour que si la mission est EN_COURS");
        }

        stepRepository.findById(stepId).ifPresentOrElse(
                step -> {
                    step.setCompleted(true);
                    stepRepository.save(step);
                },
                () -> { throw new IllegalArgumentException("Étape introuvable : " + stepId); }
        );
    }

    // Tâche 7a — POST /client/missions/:id/rate
    @Override
    public void rateAsClient(String missionId, String clientId, RateMissionRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, missionId, "mission");
        }

        // Contrainte métier section 18.3
        if (mission.getStatus() != MissionStatus.TERMINEE) {
            throw new IllegalStateException(
                    "Impossible de noter une mission qui n'est pas TERMINEE");
        }

        Evaluation evaluation = buildEvaluation(
                missionId, clientId, mission.getProviderId(), "PROVIDER", request);
        evaluationRepository.save(evaluation);

        utilisateurClient.updateRating(
                mission.getProviderId(),
                new UpdateRatingRequest(request.getRating().doubleValue(), 0));

        eventPublisher.publishEvaluationCreated(
                new EvaluationCreatedEvent(missionId, mission.getProviderId(), "PROVIDER", request.getRating()));
    }

    // Tâche 7b — POST /provider/missions/:id/rate
    @Override
    public void rateAsProvider(String missionId, String providerId, RateMissionRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.TERMINEE) {
            throw new IllegalStateException(
                    "Impossible de noter une mission qui n'est pas TERMINEE");
        }

        Evaluation evaluation = buildEvaluation(
                missionId, providerId, mission.getClientId(), "CLIENT", request);
        evaluationRepository.save(evaluation);

        utilisateurClient.updateRating(
                mission.getClientId(),
                new UpdateRatingRequest(request.getRating().doubleValue(), 0));

        eventPublisher.publishEvaluationCreated(
                new EvaluationCreatedEvent(missionId, mission.getClientId(), "CLIENT", request.getRating()));
    }

    // Tâche 8a — POST /client/missions/:id/litige
    @Override
    public void declareLitigeAsClient(String missionId, String clientId, CreateLitigeRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, missionId, "mission");
        }

        // Contrainte métier section 18.4
        if (mission.getStatus() == MissionStatus.TERMINEE) {
            throw new IllegalStateException(
                    "Impossible de déclarer un litige sur une mission déjà TERMINEE");
        }

        mission.setStatus(MissionStatus.LITIGE);
        missionRepository.save(mission);
    }

    // Tâche 8b — POST /provider/missions/:id/litige
    @Override
    public void declareLitigeAsProvider(String missionId, String providerId, CreateLitigeRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() == MissionStatus.TERMINEE) {
            throw new IllegalStateException(
                    "Impossible de déclarer un litige sur une mission déjà TERMINEE");
        }

        mission.setStatus(MissionStatus.LITIGE);
        missionRepository.save(mission);
    }

    private Evaluation buildEvaluation(
            String missionId, String evaluatorId, String targetId,
            String targetRole, RateMissionRequest request) {
        Evaluation evaluation = new Evaluation();
        evaluation.setId("eva_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        evaluation.setMissionId(missionId);
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setTargetId(targetId);
        evaluation.setTargetRole(targetRole);
        evaluation.setRating(request.getRating());
        evaluation.setComment(request.getComment());
        evaluation.setCreatedAt(Instant.now());
        return evaluation;
    }

    private MissionResponse toResponse(Mission mission) {
        MissionResponse response = new MissionResponse();
        response.setId(mission.getId());
        response.setDemandId(mission.getDemandId());
        response.setQuoteId(mission.getQuoteId());
        response.setClientId(mission.getClientId());
        response.setProviderId(mission.getProviderId());
        response.setCategory(mission.getCategory());
        response.setStatus(mission.getStatus().name());
        response.setTotalAmount(mission.getTotalAmount());
        response.setSequesteredAmount(mission.getSequesteredAmount());
        response.setPaymentStatus(mission.getPaymentStatus());
        response.setStartedAt(mission.getStartedAt());
        response.setEstimatedDurationHours(mission.getEstimatedDurationHours());
        response.setCompletedAt(mission.getCompletedAt());

        if (mission.getLocation() != null) {
            LocationDto loc = new LocationDto();
            loc.setLat(mission.getLocation().lat());
            loc.setLng(mission.getLocation().lng());
            loc.setAddress(mission.getLocation().address());
            response.setLocation(loc);
        }

        return response;
    }
}