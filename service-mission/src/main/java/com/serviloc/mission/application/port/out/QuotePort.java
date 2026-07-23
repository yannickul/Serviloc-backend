package com.serviloc.mission.application.port.out;

import com.serviloc.mission.infrastructure.external.NegociationCreateQuoteRequest;
import com.serviloc.mission.infrastructure.external.NegociationUpdateQuoteRequest;
import com.serviloc.mission.infrastructure.external.NegociationUpdateQuoteStatusRequest;
import com.serviloc.mission.infrastructure.external.QuoteDto;

import java.util.List;

public interface QuotePort {
    QuoteDto getQuoteById(String quoteId);
    QuoteDto createQuote(NegociationCreateQuoteRequest request);
    QuoteDto updateQuote(String quoteId, NegociationUpdateQuoteRequest request);
    QuoteDto updateQuoteStatus(String quoteId, NegociationUpdateQuoteStatusRequest request);

    /** Tous les devis soumis sur une demande (multi-devis : plusieurs prestataires en concurrence). */
    List<QuoteDto> getQuotesByDemand(String demandId);

    /** Le devis d'un prestataire précis sur une demande donnée, s'il existe. */
    QuoteDto getQuoteByDemandAndProvider(String demandId, String providerId);
}
