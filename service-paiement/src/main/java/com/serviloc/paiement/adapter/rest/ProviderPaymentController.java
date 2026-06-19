package com.serviloc.paiement.adapter.rest;

import com.serviloc.paiement.application.service.PaymentService;
import com.serviloc.paiement.domain.model.Payout;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Provider — Paiement", description = "Gains et historique paiements prestataire")
public class ProviderPaymentController {

    private final PaymentService paymentService;

    public ProviderPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ─── GET /provider/earnings ───────────────────────────────────

    @GetMapping("/provider/earnings")
    @Operation(summary = "Historique des gains du prestataire connecté")
    public ResponseEntity<ApiResponse<EarningsResponse>> getEarnings(
            @RequestHeader("X-User-Id") String providerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String month) {

        PaymentService.ProviderEarnings earnings = paymentService.getProviderEarnings(
                UUID.fromString(providerId), page, limit, month);

        List<PayoutResponse> payoutResponses = earnings.payouts().stream()
                .map(this::toPayoutResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(new EarningsResponse(
                earnings.monthlyTotal(),
                payoutResponses
        )));
    }

    // ─── DTOs ─────────────────────────────────────────────────────

    public record EarningsResponse(
            double monthlyTotal,
            List<PayoutResponse> payouts
    ) {}

    public record PayoutResponse(
            String id,
            String transactionId,
            double amount,
            double commissionAmount,
            String status,
            String externalRef,
            String createdAt
    ) {}

    private PayoutResponse toPayoutResponse(Payout p) {
        return new PayoutResponse(
                "pyt_" + p.getId().toString().replace("-", "").substring(0, 8),
                "txn_" + p.getTransactionId().toString().replace("-", "").substring(0, 8),
                p.getAmount(),
                p.getCommissionAmount(),
                p.getStatus().name().toLowerCase(),
                p.getExternalRef(),
                p.getCreatedAt() != null
                        ? p.getCreatedAt().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'"))
                        : null
        );
    }
}