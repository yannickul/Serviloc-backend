// adapter/rest/ClientQuoteController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.QuoteDetailResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/devis")
public class ClientQuoteController {

    private final DemandUseCase demandUseCase;

    public ClientQuoteController(DemandUseCase demandUseCase) {
        this.demandUseCase = demandUseCase;
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<ApiResponse<QuoteDetailResponse>> getQuoteDetail(
            @PathVariable String quoteId) {

        return ResponseEntity.ok(ApiResponse.success(demandUseCase.getQuoteDetail(quoteId)));
    }
}
