// application/service/DemandService.java
package com.serviloc.mission.application.service;

import com.serviloc.mission.application.dto.request.BudgetRangeDto;
import com.serviloc.mission.application.dto.request.CreateDemandRequest;
import com.serviloc.mission.application.dto.request.LocationDto;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import com.serviloc.mission.domain.event.DemandPublishedEvent;
import com.serviloc.mission.domain.exception.DemandNotFoundException;
import com.serviloc.mission.domain.exception.UnauthorizedMissionAccessException;
import com.serviloc.mission.domain.model.*;
import com.serviloc.mission.domain.repository.DemandRepository;
import com.serviloc.mission.infrastructure.messaging.MissionEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandService implements DemandUseCase {

    private final DemandRepository demandRepository;
    private final MissionEventPublisher eventPublisher;

    public DemandService(
            DemandRepository demandRepository,
            MissionEventPublisher eventPublisher) {
        this.demandRepository = demandRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DemandResponse createDemand(CreateDemandRequest request, String clientId) {
        Demand demand = new Demand();
        demand.setId("dem_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        demand.setClientId(clientId);
        demand.setCategoryId(request.getCategoryId());
        demand.setDescription(request.getDescription());
        demand.setPhotoIds(request.getPhotoIds() != null
                ? request.getPhotoIds() : new ArrayList<>());
        demand.setLocation(new Location(
                request.getLocation().getLat(),
                request.getLocation().getLng(),
                request.getLocation().getAddress()
        ));
        demand.setStatus(DemandStatus.OUVERTE);
        demand.setIsUrgent(request.isUrgent());
        demand.setEstimatedBudget(new BudgetRange(
                request.getEstimatedBudget().getMin(),
                request.getEstimatedBudget().getMax()
        ));
        demand.setCreatedAt(Instant.now());

        Demand saved = demandRepository.save(demand);

        eventPublisher.publishDemandPublished(new DemandPublishedEvent(
                saved.getId(),
                saved.getLocation().lat(),
                saved.getLocation().lng(),
                saved.getCategoryId(),
                saved.getClientId()
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DemandResponse> getDemands(
            String clientId, DemandStatus status, int page, int limit) {

        List<Demand> demands = demandRepository
                .findByClientId(clientId, status, page, limit);
        long total = demandRepository.countByClientId(clientId, status);

        List<DemandResponse> responses = demands.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(responses, page, limit, total);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandResponse getDemandById(String id, String clientId) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new DemandNotFoundException(id));

        if (!demand.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, id, "demand");
        }

        return toResponse(demand);
    }

    @Override
    public void cancelDemand(String id, String clientId) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new DemandNotFoundException(id));

        if (!demand.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, id, "demand");
        }

        demand.setStatus(DemandStatus.ANNULEE);
        demandRepository.save(demand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandResponse> getOpenDemands(String categoryId) {
        List<Demand> demands = demandRepository.findByStatus(DemandStatus.OUVERTE);

        return demands.stream()
                .filter(d -> categoryId == null
                        || categoryId.equals(d.getCategoryId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DemandResponse> getAllDemands(
            DemandStatus status, int page, int limit) {

        List<Demand> demands = status != null
                ? demandRepository.findByStatus(status)
                : demandRepository.findAll(page, limit);

        long total = status != null
                ? demandRepository.countByStatus(status)
                : demandRepository.countAll();

        List<DemandResponse> responses = demands.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(responses, page, limit, total);
    }

    private DemandResponse toResponse(Demand demand) {
        DemandResponse response = new DemandResponse();
        response.setId(demand.getId());
        response.setClientId(demand.getClientId());
        response.setCategoryId(demand.getCategoryId());
        response.setDescription(demand.getDescription());
        response.setPhotoIds(demand.getPhotoIds());
        response.setStatus(demand.getStatus().name());
        response.setIsUrgent(demand.isUrgent());
        response.setCreatedAt(demand.getCreatedAt());
        response.setProviderId(demand.getProviderId());
        response.setQuoteId(demand.getQuoteId());

        if (demand.getLocation() != null) {
            LocationDto loc = new LocationDto();
            loc.setLat(demand.getLocation().lat());
            loc.setLng(demand.getLocation().lng());
            loc.setAddress(demand.getLocation().address());
            response.setLocation(loc);
        }

        if (demand.getEstimatedBudget() != null) {
            BudgetRangeDto budget = new BudgetRangeDto();
            budget.setMin(demand.getEstimatedBudget().min());
            budget.setMax(demand.getEstimatedBudget().max());
            response.setEstimatedBudget(budget);
        }

        return response;
    }
}