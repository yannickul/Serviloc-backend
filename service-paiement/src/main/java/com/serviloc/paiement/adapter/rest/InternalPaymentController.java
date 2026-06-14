package com.serviloc.paiement.adapter.rest;

import com.serviloc.paiement.application.service.PaymentService;
import com.serviloc.paiement.domain.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal — Paiement", description = "Endpoints inter-services")
public class InternalPaymentController {

    private final PaymentService paymentService;

    public InternalPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ─── POST /internal/transactions/create ───────────────────────

    @PostMapping("/transactions/create")
    @Operation(summary = "Créer une transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        Transaction t = paymentService.createTransaction(
                UUID.fromString(request.demandId()),
                UUID.fromString(request.clientId()),
                UUID.fromString(request.providerId()),
                UUID.fromString(request.quoteId()),
                request.amount(),
                request.paymentMethod(),
                request.phoneNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(t));
    }

    // ─── POST /internal/transactions/:id/release ──────────────────

    @PostMapping("/transactions/{id}/release")
    @Operation(summary = "Libérer les fonds d'une transaction")
    public ResponseEntity<TransactionResponse> releaseTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(paymentService.releaseTransaction(id)));
    }

    // ─── POST /internal/transactions/:id/refund ───────────────────

    @PostMapping("/transactions/{id}/refund")
    @Operation(summary = "Rembourser une transaction")
    public ResponseEntity<TransactionResponse> refundTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(
                toResponse(paymentService.refundTransaction(id, request.amount())));
    }

    // ─── POST /internal/transactions/:id/freeze ───────────────────

    @PostMapping("/transactions/{id}/freeze")
    @Operation(summary = "Geler une transaction (ouverture litige)")
    public ResponseEntity<TransactionResponse> freezeTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(paymentService.freezeTransaction(id)));
    }

    // ─── GET /internal/stats/financials ───────────────────────────

    @GetMapping("/stats/financials")
    @Operation(summary = "Statistiques financières (inter-services)")
    public ResponseEntity<PaymentService.FinancialStats> getFinancialStats(
            @RequestParam(defaultValue = "2026-01-01T00:00:00") String from,
            @RequestParam(defaultValue = "2099-12-31T23:59:59") String to) {
        LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime toDate   = LocalDateTime.parse(to,   DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return ResponseEntity.ok(paymentService.getFinancialStats(fromDate, toDate));
    }

    // ─── DTOs ─────────────────────────────────────────────────────

    public record CreateTransactionRequest(
            @NotBlank String demandId,
            @NotBlank String clientId,
            @NotBlank String providerId,
            @NotBlank String quoteId,
            @Positive double amount,
            @NotBlank String paymentMethod,
            @NotBlank String phoneNumber
    ) {}

    public record RefundRequest(
            @Positive double amount
    ) {}

    public record TransactionResponse(
            String id,
            String demandId,
            String clientId,
            String providerId,
            double amount,
            double commissionAmount,
            double netAmount,
            String status,
            String paymentMethod,
            String externalRef,
            String createdAt
    ) {}

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                "txn_" + t.getId().toString().replace("-", "").substring(0, 8),
                t.getDemandId().toString(),
                t.getClientId().toString(),
                t.getProviderId().toString(),
                t.getAmount(),
                t.getCommissionAmount(),
                t.getNetAmount(),
                t.getStatus().name().toLowerCase(),
                t.getPaymentMethod(),
                t.getExternalRef(),
                t.getCreatedAt() != null
                        ? t.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'"))
                        : null
        );
    }
}