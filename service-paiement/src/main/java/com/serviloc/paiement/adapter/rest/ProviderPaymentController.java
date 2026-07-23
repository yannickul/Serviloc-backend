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
            String reference,
            String missionId,
            String category,
            double amount,
            double commission,
            double netAmount,
            String status,
            String paidAt
    ) {}

    /**
     * Mapping Payout.PayoutStatus → statut attendu par le front (libere|sequestre|en_attente),
     * décidé en équipe : FAILED est temporairement affiché comme "sequestre" en
     * attendant une mise à jour du contrat (aucun équivalent "échec" côté front).
     *   PENDING   → en_attente
     *   COMPLETED → libere
     *   FAILED    → sequestre (temporaire)
     */
    private PayoutResponse toPayoutResponse(Payout p) {
        String status = switch (p.getStatus()) {
            case PENDING -> "en_attente";
            case COMPLETED -> "libere";
            case FAILED -> "sequestre";
        };
        String paidAt = p.getStatus() == Payout.PayoutStatus.COMPLETED && p.getUpdatedAt() != null
                ? p.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'"))
                : null;

        return new PayoutResponse(
                p.getId().toString(),
                p.getReference(),
                p.getMissionId() != null ? p.getMissionId().toString() : null,
                null, // category : aucune source de donnée actuellement (ni quote_accepted ni mission.completed ne la transportent) — à ajouter côté events si besoin confirmé
                p.getAmount(),
                p.getCommissionAmount(),
                p.getNetAmount(),
                status,
                paidAt
        );
    }
}
