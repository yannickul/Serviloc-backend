package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "service-negociations", fallback = NegociationClientFallback.class)
public interface NegociationClient {

    @GetMapping("/internal/quotes/{quoteId}")
    QuoteDto getQuoteById(@PathVariable String quoteId);

    @PostMapping("/internal/quotes")
    QuoteDto createQuote(@RequestBody NegociationCreateQuoteRequest request);

    @PutMapping("/internal/quotes/{id}")
    QuoteDto updateQuote(@PathVariable String id, @RequestBody NegociationUpdateQuoteRequest request);

    @PatchMapping("/internal/quotes/{id}/status")
    QuoteDto updateQuoteStatus(@PathVariable String id, @RequestBody NegociationUpdateQuoteStatusRequest request);

    // ===== Multi-devis (plusieurs prestataires en concurrence sur une demande) =====

    /** GET /internal/quotes?demandId=xxx — liste de tous les devis de la demande */
    @GetMapping("/internal/quotes")
    List<QuoteDto> getQuotesByDemand(@RequestParam("demandId") String demandId);

    /** GET /internal/quotes?demandId=xxx&providerId=yyy — devis d'un prestataire précis (404 si absent) */
    @GetMapping("/internal/quotes")
    QuoteDto getQuoteByDemandAndProvider(
            @RequestParam("demandId") String demandId,
            @RequestParam("providerId") String providerId);
}
