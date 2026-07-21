package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
}