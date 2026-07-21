// application/port/in/DemandUseCase.java — version complète Sprint 2
package com.serviloc.mission.application.port.in;

import com.serviloc.mission.application.dto.request.AcceptQuoteRequest;
import com.serviloc.mission.application.dto.request.CreateQuoteRequest;
import com.serviloc.mission.application.dto.request.UpdateQuoteRequest;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.dto.response.QuoteResponse;
import com.serviloc.mission.domain.model.DemandStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DemandUseCase {
    DemandResponse createDemand(com.serviloc.mission.application.dto.request.CreateDemandRequest request, String clientId);
    PagedResponse<DemandResponse> getDemands(String clientId, DemandStatus status, int page, int limit);
    DemandResponse getDemandById(String id, String clientId);
    void cancelDemand(String id, String clientId);
    List<DemandResponse> getOpenDemands(String categoryId);
    PagedResponse<DemandResponse> getAllDemands(DemandStatus status, int page, int limit);
    void acceptQuote(String demandId, String clientId, AcceptQuoteRequest request);
    void rejectQuote(String demandId, String clientId);

    QuoteResponse createQuoteForDemand(String demandId, String providerId, CreateQuoteRequest request);

    @Transactional(readOnly = true)
    QuoteResponse getQuoteForDemand(String demandId);

    QuoteResponse updateQuoteForDemand(String demandId, String providerId, UpdateQuoteRequest request);
}