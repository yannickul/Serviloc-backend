// application/port/in/DemandUseCase.java — version multi-devis (plusieurs prestataires en concurrence)
package com.serviloc.mission.application.port.in;

import com.serviloc.mission.application.dto.request.AcceptQuoteRequest;
import com.serviloc.mission.application.dto.request.CreateQuoteRequest;
import com.serviloc.mission.application.dto.request.UpdateQuoteRequest;
import com.serviloc.mission.application.dto.response.ApplyDemandResponse;
import com.serviloc.mission.application.dto.response.ApplicationResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.dto.response.QuoteDetailResponse;
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

    /** Le client accepte UN devis précis (request.getQuoteId()) parmi ceux reçus sur la demande. */
    void acceptQuote(String demandId, String clientId, AcceptQuoteRequest request);

    /** Le client refuse UN devis précis. */
    void rejectQuote(String demandId, String clientId, String quoteId);

    /** Candidature d'un prestataire sur une demande, avant soumission de son devis. */
    ApplyDemandResponse applyToDemand(String demandId, String providerId);

    QuoteResponse createQuoteForDemand(String demandId, String providerId, CreateQuoteRequest request);

    /** Tous les devis soumis sur la demande (vue client — comparaison multi-prestataires). */
    @Transactional(readOnly = true)
    List<QuoteResponse> getQuotesForDemand(String demandId);

    /** Le devis du prestataire courant sur cette demande (vue provider). */
    @Transactional(readOnly = true)
    QuoteResponse getQuoteForDemandAndProvider(String demandId, String providerId);

    QuoteResponse updateQuoteForDemand(String demandId, String providerId, UpdateQuoteRequest request);

    /** Accès direct à un devis par son id (sans passer par la demande), réservé au prestataire propriétaire. */
    QuoteResponse getQuoteByIdForProvider(String quoteId, String providerId);
    QuoteResponse updateQuoteByIdForProvider(String quoteId, String providerId, UpdateQuoteRequest request);

    List<ApplicationResponse> getApplicationsForDemand(String demandId, String clientId);
    QuoteDetailResponse getQuoteDetail(String quoteId);
}
