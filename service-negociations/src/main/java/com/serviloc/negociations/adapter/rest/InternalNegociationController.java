package com.serviloc.negociations.adapter.rest;

import com.serviloc.negociations.application.dto.NegociationDtos.*;
import com.serviloc.negociations.application.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services")
public class InternalNegociationController {

    private final ConversationService conversationService;

    public InternalNegociationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // ─── GET /internal/quotes/:quoteId ────────────────────────────

    @GetMapping("/quotes/{quoteId}")
    @Operation(summary = "Récupère un devis par son ID (inter-services)")
    public ResponseEntity<QuoteResponse> getQuoteById(@PathVariable UUID quoteId) {
        return ResponseEntity.ok(conversationService.getQuoteById(quoteId));
    }

    // ─── GET /internal/conversations/:demandId ────────────────────

    @GetMapping("/conversations/{demandId}")
    @Operation(summary = "Récupère une conversation par demandId (inter-services)")
    public ResponseEntity<ConversationInternalResponse> getConversationByDemandId(
            @PathVariable UUID demandId) {
        return ResponseEntity.ok(
                conversationService.getConversationByDemandId(demandId));
    }
}