// application/service/DemandService.java — version complète avec cache
package com.serviloc.mission.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.mission.application.dto.request.*;
import com.serviloc.mission.application.dto.request.CreateQuoteRequest;
import com.serviloc.mission.application.port.out.QuotePort;
import com.serviloc.mission.domain.event.QuoteAcceptedEvent;
import com.serviloc.mission.application.dto.response.*;
import com.serviloc.mission.application.port.in.DemandUseCase;
import com.serviloc.mission.domain.event.DemandPublishedEvent;
import com.serviloc.mission.domain.exception.DemandNotFoundException;
import com.serviloc.mission.domain.exception.QuoteNotFoundException;
import com.serviloc.mission.domain.exception.UnauthorizedMissionAccessException;
import com.serviloc.mission.domain.model.*;
import com.serviloc.mission.domain.repository.DemandRepository;
import com.serviloc.mission.infrastructure.external.*;
import com.serviloc.mission.infrastructure.messaging.MissionEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DemandService implements DemandUseCase {

    private final DemandRepository demandRepository;
    private final MissionEventPublisher eventPublisher;
    private final CategorieClient categorieClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final QuotePort quotePort;

    public DemandService(
            DemandRepository demandRepository,
            MissionEventPublisher eventPublisher,
            CategorieClient categorieClient,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper, QuotePort quotePort) {
        this.demandRepository = demandRepository;
        this.eventPublisher = eventPublisher;
        this.categorieClient = categorieClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.quotePort = quotePort;
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

    @Override
    public void acceptQuote(String demandId, String clientId, AcceptQuoteRequest request) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException(demandId));

        if (!demand.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, demandId, "demande");
        }

        if (demand.getQuoteId() == null) {
            throw new QuoteNotFoundException(demandId);
        }

        if (demand.getStatus() != DemandStatus.OUVERTE) {
            throw new IllegalStateException(
                    "Impossible d'accepter un devis pour une demande au statut " + demand.getStatus());
        }

        quotePort.updateQuoteStatus(demand.getQuoteId(),
                new NegociationUpdateQuoteStatusRequest("accepte", request.getPaymentMethod(), request.getPhoneNumber()));

        eventPublisher.publishQuoteAccepted(new QuoteAcceptedEvent(
                demand.getQuoteId(),
                demandId,
                clientId,
                demand.getProviderId(),
                request.getPaymentMethod(),
                request.getPhoneNumber()
        ));
    }

    @Override
    public void rejectQuote(String demandId, String clientId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException(demandId));

        if (!demand.getClientId().equals(clientId)) {
            throw new UnauthorizedMissionAccessException(clientId, demandId, "demande");
        }

        if (demand.getQuoteId() == null) {
            throw new QuoteNotFoundException(demandId);
        }

        quotePort.updateQuoteStatus(demand.getQuoteId(),
                new NegociationUpdateQuoteStatusRequest("refuse", null, null));

        demand.setQuoteId(null);
        demandRepository.save(demand);
    }



    private CategorySummary resolveCategory(String categoryId) {
        String cacheKey = "category:" + categoryId;

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, CategorySummary.class);
            }
        } catch (Exception e) {
            log.warn("Erreur lecture cache Redis pour categoryId={} : {}", categoryId, e.getMessage());
        }

        CategorySummary category = categorieClient.getCategoryById(categoryId);

        try {
            redisTemplate.opsForValue().set(
                    cacheKey, objectMapper.writeValueAsString(category), Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("Erreur écriture cache Redis pour categoryId={} : {}", categoryId, e.getMessage());
        }

        return category;
    }

    @Override
    public QuoteResponse createQuoteForDemand(String demandId, String providerId, CreateQuoteRequest request) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException(demandId));

        if (demand.getQuoteId() != null) {
            throw new IllegalStateException("Un devis existe déjà pour la demande " + demandId);
        }

        NegociationCreateQuoteRequest negRequest = new NegociationCreateQuoteRequest(
                demandId,
                providerId,
                request.getAmount(),
                request.getDescription(),
                mapMaterialInputs(request.getMaterials()),
                request.getEstimatedDurationHours(),
                request.getValidityDays());

        QuoteDto quote = quotePort.createQuote(negRequest);

        // Option A : quoteId peuplé dès la création, pas seulement à l'acceptation
        demand.setQuoteId(quote.id());
        demand.setProviderId(providerId);
        demandRepository.save(demand);

        return toQuoteResponse(quote);
    }

    @Transactional(readOnly = true)
    @Override
    public QuoteResponse getQuoteForDemand(String demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException(demandId));

        if (demand.getQuoteId() == null) {
            throw new QuoteNotFoundException(demandId);
        }

        return toQuoteResponse(quotePort.getQuoteById(demand.getQuoteId()));
    }

    @Override
    public QuoteResponse updateQuoteForDemand(String demandId, String providerId, UpdateQuoteRequest request) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException(demandId));

        if (demand.getQuoteId() == null) {
            throw new QuoteNotFoundException(demandId);
        }

        NegociationUpdateQuoteRequest negRequest = new NegociationUpdateQuoteRequest(
                providerId,
                request.getAmount(),
                request.getDescription(),
                mapMaterialInputs(request.getMaterials()),
                request.getEstimatedDurationHours(),
                request.getValidityDays());

        QuoteDto quote = quotePort.updateQuote(demand.getQuoteId(), negRequest);
        return toQuoteResponse(quote);
    }

    private List<MaterialInputDto> mapMaterialInputs(List<QuoteMaterialInput> inputs) {
        if (inputs == null) return null;
        return inputs.stream()
                .map(m -> new MaterialInputDto(m.getName(), m.getQuantity(), m.getUnitPrice()))
                .collect(Collectors.toList());
    }


    private DemandResponse toResponse(Demand demand) {
        DemandResponse response = new DemandResponse();
        response.setId(demand.getId());
        response.setClientId(demand.getClientId());

        CategorySummary category = resolveCategory(demand.getCategoryId());
        response.setCategory(new CategoryDto(category.getId(), category.getLabel(), category.getIconKey()));

        response.setDescription(demand.getDescription());

        if (demand.getPhotoIds() != null) {
            List<PhotoDto> photos = demand.getPhotoIds().stream()
                    .map(id -> new PhotoDto(id, null, null))
                    .collect(Collectors.toList());
            response.setPhotos(photos);
        }

        response.setStatus(demand.getStatus().name().toLowerCase());
        response.setIsUrgent(demand.isUrgent());
        response.setCreatedAt(demand.getCreatedAt());
        response.setUpdatedAt(demand.getUpdatedAt());
        response.setProviderId(demand.getProviderId());
        response.setQuoteId(demand.getQuoteId());
        response.setMissionId(demand.getMissionId());

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

    @Transactional(readOnly = true)
    public InternalDemandResponse getDemandForInternal(String demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException(demandId));

        CategorySummary category = resolveCategory(demand.getCategoryId());

        return new InternalDemandResponse(
                demand.getId(),
                demand.getDescription(),
                category.getLabel(),
                demand.getStatus().name().toLowerCase());
    }

    private QuoteResponse toQuoteResponse(QuoteDto quote) {
        QuoteResponse response = new QuoteResponse();
        response.setId(quote.id());
        response.setDemandId(quote.demandId());
        response.setProviderId(quote.providerId());
        response.setClientId(quote.clientId());
        response.setLaborDescription(quote.laborDescription());
        response.setLaborAmount(quote.laborAmount());
        if (quote.materials() != null) {
            response.setMaterials(quote.materials().stream()
                    .map(m -> new QuoteResponse.MaterialResponse(m.id(), m.name(), m.quantity(), m.unitPrice(), m.subtotal()))
                    .collect(Collectors.toList()));
        }
        response.setMaterialsTotal(quote.materialsTotal());
        response.setTotalAmount(quote.totalAmount());
        response.setEstimatedDurationHours(quote.estimatedDurationHours());
        response.setValidityDays(quote.validityDays());
        response.setStatus(quote.status());
        response.setCreatedAt(quote.createdAt());
        response.setExpiresAt(quote.expiresAt());
        return response;
    }
}