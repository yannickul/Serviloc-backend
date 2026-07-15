// infrastructure/external/NegociationClient.java
package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-negociations", fallback = NegociationClientFallback.class)
public interface NegociationClient {

    @GetMapping("/internal/quotes/{quoteId}")
    QuoteDto getQuoteById(@PathVariable String quoteId);
}