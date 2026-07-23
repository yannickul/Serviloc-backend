// adapter/rest/ProviderQuoteController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.request.UpdateQuoteRequest;
import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.QuoteResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/provider/quotes")
public class ProviderQuoteController {

    private final DemandUseCase demandUseCase;

    public ProviderQuoteController(DemandUseCase demandUseCase) {
        this.demandUseCase = demandUseCase;
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-User-Id") String providerId) {

        return ResponseEntity.ok(
                ApiResponse.success(demandUseCase.getQuoteByIdForProvider(quoteId, providerId)));
    }

    @PatchMapping("/{quoteId}")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-User-Id") String providerId,
            @Valid @RequestBody UpdateQuoteRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(demandUseCase.updateQuoteByIdForProvider(quoteId, providerId, request)));
    }
}
