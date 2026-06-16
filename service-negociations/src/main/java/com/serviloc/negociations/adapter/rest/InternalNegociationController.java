package com.serviloc.negociations.adapter.rest;

import com.serviloc.negociations.application.dto.NegociationDtos.*;
import com.serviloc.negociations.application.service.ConversationService;
import com.serviloc.negociations.application.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services")
public class InternalNegociationController {

    private final ConversationService conversationService;
    private final QuoteService quoteService;

    public InternalNegociationController(ConversationService conversationService,
                                         QuoteService quoteService) {
        this.conversationService = conversationService;
        this.quoteService = quoteService;
    }

    // ─── GET /internal/quotes/:quoteId ────────────────────────────

    @GetMapping("/quotes/{quoteId}")
    @Operation(summary = "Récupère un devis par son ID")
    public ResponseEntity<QuoteResponse> getQuoteById(@PathVariable UUID quoteId) {
        return ResponseEntity.ok(conversationService.getQuoteById(quoteId));
    }

    // ─── POST /internal/quotes ────────────────────────────────────

    @PostMapping("/quotes")
    @Operation(summary = "Créer un devis (depuis Service Missions)")
    public ResponseEntity<QuoteResponse> createQuote(
            @RequestParam UUID conversationId,
            @Valid @RequestBody CreateQuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quoteService.createQuote(conversationId, request));
    }

    // ─── PUT /internal/quotes/:id ─────────────────────────────────

    @PutMapping("/quotes/{id}")
    @Operation(summary = "Mettre à jour un devis")
    public ResponseEntity<QuoteResponse> updateQuote(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuoteRequest request) {
        return ResponseEntity.ok(quoteService.updateQuote(id, request));
    }

    // ─── PATCH /internal/quotes/:id/status ───────────────────────

    @PatchMapping("/quotes/{id}/status")
    @Operation(summary = "Changer le statut d'un devis (accepte|refuse)")
    public ResponseEntity<QuoteResponse> updateQuoteStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuoteStatusRequest request) {
        return ResponseEntity.ok(quoteService.updateQuoteStatus(id, request));
    }

    // ─── GET /internal/conversations/:demandId ────────────────────

    @GetMapping("/conversations/{demandId}")
    @Operation(summary = "Récupère une conversation par demandId")
    public ResponseEntity<ConversationInternalResponse> getConversationByDemandId(
            @PathVariable UUID demandId) {
        return ResponseEntity.ok(
                conversationService.getConversationByDemandId(demandId));
    }
}