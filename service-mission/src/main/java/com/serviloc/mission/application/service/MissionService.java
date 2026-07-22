// application/service/MissionService.java
package com.serviloc.mission.application.service;

import com.serviloc.mission.application.dto.request.CreateLitigeRequest;
import com.serviloc.mission.application.dto.request.CreateStepsRequest;
import com.serviloc.mission.application.dto.request.LocationDto;
import com.serviloc.mission.application.dto.request.RateMissionRequest;
import com.serviloc.mission.application.dto.response.*;
import com.serviloc.mission.application.port.in.MissionUseCase;
import com.serviloc.mission.domain.event.*;
import com.serviloc.mission.domain.exception.*;
import com.serviloc.mission.domain.model.*;
import com.serviloc.mission.domain.repository.DemandRepository;
import com.serviloc.mission.domain.repository.EvaluationRepository;
import com.serviloc.mission.domain.repository.MissionRepository;
import com.serviloc.mission.infrastructure.external.UpdateRatingRequest;
import com.serviloc.mission.infrastructure.external.UtilisateurClient;
import com.serviloc.mission.infrastructure.messaging.MissionEventPublisher;
import com.serviloc.mission.infrastructure.persistence.entity.MissionJpaEntity;
import com.serviloc.mission.infrastructure.persistence.entity.MissionStepJpaEntity;
import com.serviloc.mission.infrastructure.persistence.entity.MissionValidationJpaEntity;
import com.serviloc.mission.infrastructure.persistence.repository.MissionJpaRepository;
import com.serviloc.mission.infrastructure.persistence.repository.MissionValidationJpaRepository;
import com.serviloc.mission.infrastructure.persistence.repository.MissionStepJpaRepository;
import com.serviloc.mission.application.port.out.PaymentPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
    private final MissionJpaRepository missionJpaRepository;
    private final DemandRepository demandRepository;

    public MissionService(
            MissionRepository missionRepository,
            EvaluationRepository evaluationRepository,
            MissionEventPublisher eventPublisher,
            UtilisateurClient utilisateurClient,
            PaymentPort paymentPort,
            MissionValidationJpaRepository validationRepository,
            MissionStepJpaRepository stepRepository,
            MissionJpaRepository missionJpaRepository, DemandRepository demandRepository) {
        this.missionRepository = missionRepository;
        this.evaluationRepository = evaluationRepository;
        this.eventPublisher = eventPublisher;
        this.utilisateurClient = utilisateurClient;
        this.paymentPort = paymentPort;
        this.validationRepository = validationRepository;
        this.stepRepository = stepRepository;
        this.missionJpaRepository = missionJpaRepository;
        this.demandRepository = demandRepository;
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
    public List<MissionResponse> getMissionsByProvider(String providerId, String status) {
        List<Mission> missions = missionRepository.findByProviderId(providerId);
        MissionStatus filterStatus = status != null ? MissionStatus.valueOf(status.toUpperCase()) : null;
        return missions.stream()
                .filter(m -> filterStatus == null || m.getStatus() == filterStatus)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getMissionsByClient(String clientId) {
        return missionRepository.findByClientId(clientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Tâche 3 — POST /provider/missions/:id/start
    @Override
    public StartMissionResponse startMission(String missionId, String providerId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                    "La mission " + missionId + " ne peut pas démarrer depuis le status " + mission.getStatus());
        }

        if (mission.getEstimatedDurationHours() <= 0) {
            throw new MissionSetupIncompleteException(missionId, "durée estimée non définie");
        }

        List<MissionStepJpaEntity> steps = stepRepository.findByMissionId(missionId);
        if (steps.isEmpty()) {
            throw new MissionSetupIncompleteException(missionId, "aucune étape définie");
        }

        mission.setStatus(MissionStatus.EN_COURS);
        mission.setStartedAt(Instant.now());
        missionRepository.save(mission);

        eventPublisher.publishMissionStarted(
                new MissionStartedEvent(mission.getId(), mission.getProviderId(), mission.getClientId()));

        return new StartMissionResponse(
                mission.getId(), mission.getStatus().name().toLowerCase(), mission.getStartedAt());
    }
    // Tâche 4 — POST /provider/missions/:id/complete
    @Override
    public CompleteMissionResponse completeMission(String missionId, String providerId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_COURS) {
            throw new IllegalStateException(
                    "La mission " + missionId + " doit être EN_COURS pour être complétée");
        }

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

        boolean clientAlsoValidated = validationRepository
                .existsByMissionIdAndRole(missionId, "CLIENT");

        if (clientAlsoValidated) {
            finalizeMissionIfBothValidated(mission);
            return new CompleteMissionResponse(
                    missionId, "provider", true, "terminee",
                    "Mission terminée — le client avait déjà validé, paiement libéré.");
        }

        return new CompleteMissionResponse(
                missionId, "provider", false, mission.getStatus().name().toLowerCase(),
                "En attente de la validation client.");
    }

    // Tâche 5 — POST /client/missions/:id/validate
    @Override
    public ValidateMissionResponse validateMission(String missionId, String clientId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_COURS) {
            throw new IllegalStateException(
                    "La mission " + missionId + " doit être EN_COURS pour être validée");
        }

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

        boolean providerAlsoValidated = validationRepository
                .existsByMissionIdAndRole(missionId, "PROVIDER");

        if (providerAlsoValidated) {
            finalizeMissionIfBothValidated(mission);
            return new ValidateMissionResponse(
                    missionId, "client", true, "libere", mission.getTotalAmount());
        }

        return new ValidateMissionResponse(
                missionId, "client", false, mission.getPaymentStatus(), null);
    }

    private void finalizeMissionIfBothValidated(Mission mission) {
        paymentPort.releaseTransaction(mission.getTransactionId());
        mission.setStatus(MissionStatus.TERMINEE);
        mission.setCompletedAt(Instant.now());
        missionRepository.save(mission);

        eventPublisher.publishMissionCompleted(
                new MissionCompletedEvent(
                        mission.getId(),
                        mission.getClientId(),
                        mission.getProviderId(),
                        mission.getTotalAmount(),
                        mission.getTransactionId()));
    }

    @Override
    @Transactional
    public DefineStepsResponse defineSteps(String missionId, String providerId, CreateStepsRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getProviderId().equals(providerId)) {
            throw new UnauthorizedMissionAccessException(providerId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Les étapes ne peuvent être définies que pour une mission EN_ATTENTE");
        }

        boolean alreadyDefined = !stepRepository.findByMissionId(missionId).isEmpty();
        if (alreadyDefined) {
            throw new StepsAlreadyDefinedException(missionId);
        }

        mission.setEstimatedDurationHours(request.getEstimatedDurationHours());
        missionRepository.save(mission);

        MissionJpaEntity missionRef = missionJpaRepository.getReferenceById(missionId);

        List<StepResponse> responses = new ArrayList<>();
        int autoOrder = 1;
        for (CreateStepsRequest.StepInput input : request.getSteps()) {
            MissionStepJpaEntity entity = new MissionStepJpaEntity();
            entity.setId("step_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            entity.setMission(missionRef);
            entity.setLabel(input.getLabel());
            entity.setCompleted(false);
            entity.setOrder(input.getOrder() != null ? input.getOrder() : autoOrder);
            autoOrder++;

            MissionStepJpaEntity saved = stepRepository.save(entity);
            responses.add(new StepResponse(saved.getId(), saved.getLabel(), saved.isCompleted(), saved.getOrder()));
        }

        return new DefineStepsResponse(missionId, mission.getEstimatedDurationHours(), responses);
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


    @Override
    public RatingResponse rateAsClient(String missionId, String clientId, RateMissionRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));

        if (!mission.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, missionId, "mission");
        }

        if (mission.getStatus() != MissionStatus.TERMINEE) {
            throw new IllegalStateException(
                    "Impossible de noter une mission qui n'est pas TERMINEE");
        }

        Evaluation evaluation = buildEvaluation(
                missionId, clientId, mission.getProviderId(), "PROVIDER", request);
        Evaluation saved = evaluationRepository.save(evaluation);

        List<Evaluation> evaluations = evaluationRepository.findByTargetId(mission.getProviderId());
        double average = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .average()
                .orElse(request.getRating().doubleValue());

        try {
            utilisateurClient.updateRating(
                    mission.getProviderId(),
                    new UpdateRatingRequest(average, evaluations.size()));
        } catch (Exception e) {
            log.warn("updateRating indisponible pour {} — bascule sur outbox RabbitMQ", mission.getProviderId());
            eventPublisher.publishRatingUpdatePending(
                    new RatingUpdatePendingEvent(
                            mission.getProviderId(), average, evaluations.size()));
        }
        eventPublisher.publishEvaluationCreated(
                new EvaluationCreatedEvent(missionId, mission.getProviderId(), "PROVIDER", request.getRating()));

        return new RatingResponse(saved.getId(), mission.getProviderId(), "provider", request.getRating());
    }

    @Override
    public RatingResponse rateAsProvider(String missionId, String providerId, RateMissionRequest request) {
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
        Evaluation saved = evaluationRepository.save(evaluation);

        List<Evaluation> evaluations = evaluationRepository.findByTargetId(mission.getClientId());
        double average = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .average()
                .orElse(request.getRating().doubleValue());

        try {
            utilisateurClient.updateRating(
                    mission.getClientId(),
                    new UpdateRatingRequest(average, evaluations.size()));
        } catch (Exception e) {
            log.warn("updateRating indisponible pour {} — bascule sur outbox RabbitMQ", mission.getClientId());
            eventPublisher.publishRatingUpdatePending(
                    new RatingUpdatePendingEvent(
                            mission.getClientId(), average, evaluations.size()));
        }

        eventPublisher.publishEvaluationCreated(
                new EvaluationCreatedEvent(missionId, mission.getClientId(), "CLIENT", request.getRating()));

        return new RatingResponse(saved.getId(), mission.getClientId(), "client", request.getRating());
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

    @Transactional(readOnly = true)
    public UserMissionStatsResponse getMissionStatsForUser(String userId) {
        long total = missionRepository.countTotalByUserId(userId);
        long completed = missionRepository.countCompletedByUserId(userId);
        return new UserMissionStatsResponse(userId, completed, total);
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
        evaluation.setCriteria(request.getCriteria());
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
        response.setStatus(mission.getStatus().name().toLowerCase());
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

        if (mission.getSteps() != null) {
            List<StepResponse> steps = mission.getSteps().stream()
                    .sorted(Comparator.comparingInt(MissionStep::getOrder))
                    .map(s -> new StepResponse(s.getId(), s.getLabel(), s.isCompleted(), s.getOrder()))
                    .collect(Collectors.toList());
            response.setSteps(steps);
        }

        return response;
    }
}