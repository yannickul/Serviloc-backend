package com.serviloc.paiement.adapter.rest;

import com.serviloc.paiement.application.service.PaymentService;
import com.serviloc.paiement.domain.model.CommissionConfig;
import com.serviloc.paiement.domain.model.Transaction;
import com.serviloc.paiement.domain.model.TransactionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Tag(name = "Admin — Paiement", description = "Gestion des transactions et commissions")
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ─── GET /admin/transactions ──────────────────────────────────

    @GetMapping("/admin/transactions")
    @Operation(summary = "Liste paginée des transactions")
    public ResponseEntity<ApiResponse<TransactionListResponse>> getTransactions(
            @RequestParam(defaultValue = "SEQUESTRE") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        TransactionStatus txnStatus = TransactionStatus.valueOf(status.toUpperCase());
        Page<Transaction> result = paymentService.getTransactions(txnStatus, page, limit);

        List<InternalPaymentController.TransactionResponse> transactions = result.getContent()
                .stream().map(this::toResponse).toList();

        return ResponseEntity.ok(ApiResponse.ok(new TransactionListResponse(
                transactions,
                new PageMeta(page, limit, result.getTotalElements(), result.getTotalPages())
        )));
    }

    // ─── PATCH /admin/settings/commission ────────────────────────

    @PatchMapping("/admin/settings/commission")
    @Operation(summary = "Mise à jour des taux de commission")
    public ResponseEntity<ApiResponse<CommissionResponse>> updateCommission(
            @Valid @RequestBody UpdateCommissionRequest request) {
        CommissionConfig config = paymentService.updateCommission(
                request.standardRate(), request.urgencyRate());
        return ResponseEntity.ok(ApiResponse.ok(new CommissionResponse(
                config.getStandardRate(), config.getUrgencyRate(),
                "Taux de commission mis à jour"
        )));
    }

    // ─── DTOs ─────────────────────────────────────────────────────

    public record UpdateCommissionRequest(
            @DecimalMin("0") @DecimalMax("30") double standardRate,
            @DecimalMin("0") @DecimalMax("30") double urgencyRate
    ) {}

    public record CommissionResponse(
            double standardRate,
            double urgencyRate,
            String message
    ) {}

    public record TransactionListResponse(
            List<InternalPaymentController.TransactionResponse> transactions,
            PageMeta meta
    ) {}

    public record PageMeta(int page, int limit, long total, int totalPages) {}

    private InternalPaymentController.TransactionResponse toResponse(Transaction t) {
        return new InternalPaymentController.TransactionResponse(
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
                        ? t.getCreatedAt().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'"))
                        : null
        );
    }
}