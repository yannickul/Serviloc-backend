package com.serviloc.utilisateurs.application.service;

import com.serviloc.utilisateurs.infrastructure.external.MissionsClient;
import com.serviloc.utilisateurs.infrastructure.external.PaiementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(ProfileEnrichmentService.class);

    private final PaiementClient paiementClient;
    private final MissionsClient missionsClient;

    @Value("${internal.token}")
    private String internalToken;

    public ProfileEnrichmentService(PaiementClient paiementClient,
                                    MissionsClient missionsClient) {
        this.paiementClient = paiementClient;
        this.missionsClient = missionsClient;
    }

    // ─── Client ───────────────────────────────────────────────────

    public ClientEnrichment getClientEnrichment(UUID clientId) {
        try {
            MissionsClient.MissionStatsResponse stats =
                    missionsClient.getMissionStats(clientId.toString(), internalToken);

            PaiementClient.ClientTransactionSummary summary =
                    paiementClient.getClientTransactionSummary(clientId.toString(), internalToken);

            List<PendingPaymentInfo> pendingPayments = summary.pendingTransactions()
                    .stream()
                    .map(t -> new PendingPaymentInfo(t.amount(), "Mission en cours"))
                    .toList();

            return new ClientEnrichment(
                    summary.totalSpent(),
                    stats.completedMissions(),
                    pendingPayments
            );
        } catch (Exception e) {
            log.warn("[ENRICHMENT] Erreur enrichissement client={} : {}", clientId, e.getMessage());
            return new ClientEnrichment(0.0, 0, List.of());
        }
    }
    // ─── Provider ─────────────────────────────────────────────────

    public ProviderEnrichment getProviderEnrichment(UUID providerId) {
        try {
            // completedMissions depuis Service Missions
            MissionsClient.MissionStatsResponse stats =
                    missionsClient.getMissionStats(providerId.toString(), internalToken);

            // monthlyEarnings depuis Service Paiement
            String currentMonth = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            PaiementClient.ProviderEarningsResponse earnings =
                    paiementClient.getProviderEarnings(providerId.toString(), 1, currentMonth);

            return new ProviderEnrichment(
                    stats.completedMissions(),
                    earnings.monthlyTotal()
            );
        } catch (Exception e) {
            log.warn("[ENRICHMENT] Erreur enrichissement provider={} : {}", providerId, e.getMessage());
            return new ProviderEnrichment(0, 0.0);
        }
    }

    // ─── DTOs internes ────────────────────────────────────────────

    public record PendingPaymentInfo(
            double amount,
            String missionLabel
    ) {}

    public record ClientEnrichment(
            double totalSpent,
            int completedMissions,
            List<PendingPaymentInfo> pendingPayments
    ) {}

    public record ProviderEnrichment(
            int completedMissions,
            double monthlyEarnings
    ) {}
}