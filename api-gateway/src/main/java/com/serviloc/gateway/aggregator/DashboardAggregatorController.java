package com.serviloc.gateway.aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Agrégateur de dashboards — le Gateway appelle plusieurs microservices en parallèle
 * et fusionne les réponses dans le format documenté par API_CONTRACT.md (v2.1),
 * pour éviter que le frontend ait à faire plusieurs requêtes.
 *
 * ── Endpoints locaux, PAS des Routes proxy ──────────────────────────────────
 * Ces 4 chemins ont été retirés des routes `missions-*` de application.yml pour
 * éviter tout conflit avec le RouteLocator. La sécurité (JWT + rôle) est assurée
 * par {@link com.serviloc.gateway.filter.DashboardAuthWebFilter}, pas par
 * JwtAuthFilter/RoleAuthFilter (qui ne s'appliquent qu'aux Routes déclarées en YAML).
 *
 * ── Dégradation gracieuse ────────────────────────────────────────────────────
 * Si un service downstream est indisponible (ou pas encore mergé — cf. service-missions
 * et service-litiges au moment de l'écriture), le champ correspondant revient à `null`
 * ou `[]` plutôt que de faire échouer tout le dashboard (voir DownstreamGateway).
 *
 * ── ⚠️ Endpoints internes assumés (à confirmer avec les équipes concernées) ────
 * Documentés en détail dans /INTERNAL_CONTRACT_AGGREGATOR.md à la racine du repo :
 *   - service-missions : /internal/demands/recent, /internal/missions/recent,
 *                         /internal/missions/stats/provider/{id}, /internal/stats/summary,
 *                         /internal/stats/demands-missions, /internal/stats/popular-categories
 *   - service-litiges   : /internal/litiges/active
 *   - service-utilisateurs : /internal/providers/top (à ajouter — n'existe pas encore)
 * Les endpoints déjà réels (service-utilisateurs /client|provider/me, service-paiement
 * /internal/stats/financials, /provider/earnings, /admin/transactions, service-utilisateurs
 * /admin/providers, service-negociations /client/conversations) sont appelés tels quels.
 */
@RestController
public class DashboardAggregatorController {

    private final DownstreamGateway gateway;
    private final ObjectMapper mapper;

    public DashboardAggregatorController(DownstreamGateway gateway, ObjectMapper mapper) {
        this.gateway = gateway;
        this.mapper = mapper;
    }

    // ════════════════════════════════════════════════════════════════════
    // GET /v1/client/dashboard
    // ════════════════════════════════════════════════════════════════════

    @GetMapping("/v1/client/dashboard")
    public Mono<ResponseEntity<ObjectNode>> clientDashboard(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String userId) {

        Mono<JsonNode> profileMono = gateway.getJson(
                        "lb://service-utilisateurs/client/me",
                        h -> h.set(HttpHeaders.AUTHORIZATION, authHeader))
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        // ⚠️ Assumé — service-missions pas encore mergé (cf. INTERNAL_CONTRACT_AGGREGATOR.md)
        Mono<JsonNode> recentDemandsMono = gateway.getJson(
                        "lb://service-missions/internal/demands/recent?clientId=" + userId + "&limit=5",
                        h -> {})
                .defaultIfEmpty(mapper.createArrayNode());

        Mono<JsonNode> conversationsMono = gateway.getJson(
                        "lb://service-negociations/client/conversations?limit=100",
                        h -> {
                            h.set("X-User-Id", userId);
                            h.set("X-User-Role", "CLIENT");
                        })
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        return Mono.zip(profileMono, recentDemandsMono, conversationsMono)
                .map(t -> {
                    JsonNode profile = t.getT1();
                    JsonNode recentDemands = t.getT2();
                    JsonNode conversations = t.getT3();

                    int unreadMessages = 0;
                    JsonNode convList = conversations.path("conversations");
                    if (convList.isArray()) {
                        for (JsonNode c : convList) {
                            unreadMessages += c.path("unreadCount").asInt(0);
                        }
                    }

                    ObjectNode financialSummary = mapper.createObjectNode();
                    financialSummary.set("totalSpent", numberOr(profile.path("totalSpent"), 0));
                    financialSummary.set("completedMissions", numberOr(profile.path("completedMissions"), 0));
                    JsonNode pendingPayments = profile.path("pendingPayments");
                    if (pendingPayments.isArray() && pendingPayments.size() > 0) {
                        financialSummary.set("pendingPayment", pendingPayments.get(0));
                    } else {
                        financialSummary.putNull("pendingPayment");
                    }

                    ObjectNode data = mapper.createObjectNode();
                    data.set("profile", profile);
                    data.set("recentDemands", recentDemands.isArray() ? recentDemands : mapper.createArrayNode());
                    data.set("financialSummary", financialSummary);
                    data.put("unreadMessages", unreadMessages);

                    return ResponseEntity.ok(envelope(data));
                });
    }

    // ════════════════════════════════════════════════════════════════════
    // GET /v1/provider/dashboard
    // ════════════════════════════════════════════════════════════════════

    @GetMapping("/v1/provider/dashboard")
    public Mono<ResponseEntity<ObjectNode>> providerDashboard(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String userId) {

        Mono<JsonNode> profileMono = gateway.getJson(
                        "lb://service-utilisateurs/provider/me",
                        h -> h.set(HttpHeaders.AUTHORIZATION, authHeader))
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        // ⚠️ Assumé — service-missions
        Mono<JsonNode> recentMissionsMono = gateway.getJson(
                        "lb://service-missions/internal/missions/recent?providerId=" + userId + "&limit=5",
                        h -> {})
                .defaultIfEmpty(mapper.createArrayNode());

        // ⚠️ Assumé — service-missions : { missionsThisMonth, availableDemandsCount,
        //             trends: { missions:{value,direction,subtext}, earnings:{...}, rating:{...} } }
        Mono<JsonNode> missionsStatsMono = gateway.getJson(
                        "lb://service-missions/internal/missions/stats/provider/" + userId,
                        h -> {})
                .defaultIfEmpty(mapper.createObjectNode());

        Mono<JsonNode> earningsMono = gateway.getJson(
                        "lb://service-paiement/provider/earnings?limit=1",
                        h -> {
                            h.set("X-User-Id", userId);
                            h.set("X-User-Role", "PROVIDER");
                        })
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        return Mono.zip(profileMono, recentMissionsMono, missionsStatsMono, earningsMono)
                .map(t -> {
                    JsonNode profile = t.getT1();
                    JsonNode recentMissions = t.getT2();
                    JsonNode missionsStats = t.getT3();
                    JsonNode earnings = t.getT4();

                    ObjectNode metrics = mapper.createObjectNode();
                    metrics.set("missionsThisMonth", numberOr(missionsStats.path("missionsThisMonth"), 0));
                    JsonNode netEarnings = earnings.has("monthlyTotal")
                            ? earnings.path("monthlyTotal")
                            : profile.path("monthlyEarnings");
                    metrics.set("netEarnings", numberOr(netEarnings, 0));
                    metrics.set("averageRating", numberOr(profile.path("rating"), 0));
                    metrics.set("availableDemandsCount", numberOr(missionsStats.path("availableDemandsCount"), 0));
                    metrics.set("trends", missionsStats.has("trends")
                            ? missionsStats.get("trends")
                            : defaultTrends());

                    ObjectNode data = mapper.createObjectNode();
                    data.set("profile", profile);
                    data.set("metrics", metrics);
                    data.set("recentMissions", recentMissions.isArray() ? recentMissions : mapper.createArrayNode());
                    data.set("availability", profile.path("availability"));

                    return ResponseEntity.ok(envelope(data));
                });
    }

    // ════════════════════════════════════════════════════════════════════
    // GET /v1/admin/dashboard
    // ════════════════════════════════════════════════════════════════════

    @GetMapping("/v1/admin/dashboard")
    public Mono<ResponseEntity<ObjectNode>> adminDashboard(
            @RequestHeader("X-User-Id") String userId) {

        Consumer<HttpHeaders> adminHeaders = h -> {
            h.set("X-User-Id", userId);
            h.set("X-User-Role", "ADMIN");
        };

        // ⚠️ Assumé — service-missions : { activeDemands:{value,trend}, ongoingMissions:{value,trend} }
        Mono<JsonNode> missionsSummaryMono = gateway.getJson(
                        "lb://service-missions/internal/stats/summary", h -> {})
                .defaultIfEmpty(mapper.createObjectNode());

        Mono<JsonNode> financialsMono = gateway.getJson(
                        "lb://service-paiement/internal/stats/financials", h -> {})
                .defaultIfEmpty(mapper.createObjectNode());

        Mono<JsonNode> pendingValidationsMono = gateway.getJson(
                        "lb://service-utilisateurs/admin/providers?status=pending_verification&limit=5",
                        adminHeaders)
                .map(gateway::unwrapData)
                .map(d -> d.path("providers"))
                .defaultIfEmpty(mapper.createArrayNode());

        // ⚠️ Assumé — service-litiges
        Mono<JsonNode> activeLitigesMono = gateway.getJson(
                        "lb://service-litiges/internal/litiges/active?limit=5", h -> {})
                .defaultIfEmpty(mapper.createArrayNode());

        // ⚠️ Assumé — service-missions
        Mono<JsonNode> popularCategoriesMono = gateway.getJson(
                        "lb://service-missions/internal/stats/popular-categories?limit=6", h -> {})
                .defaultIfEmpty(mapper.createArrayNode());

        Mono<JsonNode> recentTransactionsMono = gateway.getJson(
                        "lb://service-paiement/admin/transactions?limit=5",
                        adminHeaders)
                .map(gateway::unwrapData)
                .map(d -> d.path("transactions"))
                .defaultIfEmpty(mapper.createArrayNode());

        return Mono.zip(missionsSummaryMono, financialsMono, pendingValidationsMono,
                        activeLitigesMono, popularCategoriesMono, recentTransactionsMono)
                .map(t -> {
                    JsonNode missionsSummary = t.getT1();
                    JsonNode financials = t.getT2();
                    JsonNode pendingValidations = t.getT3();
                    JsonNode activeLitiges = t.getT4();
                    JsonNode popularCategories = t.getT5();
                    JsonNode recentTransactions = t.getT6();

                    ObjectNode metrics = mapper.createObjectNode();
                    metrics.set("activeDemands", missionsSummary.has("activeDemands")
                            ? missionsSummary.get("activeDemands") : metricPlaceholder());
                    metrics.set("ongoingMissions", missionsSummary.has("ongoingMissions")
                            ? missionsSummary.get("ongoingMissions") : metricPlaceholder());
                    metrics.set("monthlyRevenue", metricValue(financials.path("totalRevenue")));
                    metrics.set("commissionEarned", metricValue(financials.path("commissionEarned")));

                    ObjectNode data = mapper.createObjectNode();
                    data.set("metrics", metrics);
                    data.set("pendingValidations", asArray(pendingValidations));
                    data.set("activeLitiges", asArray(activeLitiges));
                    data.set("popularCategories", asArray(popularCategories));
                    data.set("recentTransactions", asArray(recentTransactions));

                    return ResponseEntity.ok(envelope(data));
                });
    }

    // ════════════════════════════════════════════════════════════════════
    // GET /v1/admin/stats
    // ════════════════════════════════════════════════════════════════════

    @GetMapping("/v1/admin/stats")
    public Mono<ResponseEntity<ObjectNode>> adminStats(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false, defaultValue = "2026-01-01T00:00:00") String from,
            @RequestParam(required = false, defaultValue = "2099-12-31T23:59:59") String to) {

        // ⚠️ Assumé — service-missions : { demands:{total,open,inProgress,completed,cancelled},
        //             missions:{total,completed,inDispute,completionRate} }
        Mono<JsonNode> demandsMissionsMono = gateway.getJson(
                        "lb://service-missions/internal/stats/demands-missions", h -> {})
                .defaultIfEmpty(mapper.createObjectNode());

        Mono<JsonNode> financialsMono = gateway.getJson(
                        "lb://service-paiement/internal/stats/financials?from=" + from + "&to=" + to, h -> {})
                .defaultIfEmpty(mapper.createObjectNode());

        // ⚠️ Assumé — service-utilisateurs : endpoint /internal/providers/top à ajouter
        Mono<JsonNode> topProvidersMono = gateway.getJson(
                        "lb://service-utilisateurs/internal/providers/top?limit=4", h -> {})
                .defaultIfEmpty(mapper.createArrayNode());

        // ⚠️ Assumé — service-missions
        Mono<JsonNode> popularCategoriesMono = gateway.getJson(
                        "lb://service-missions/internal/stats/popular-categories?limit=6", h -> {})
                .defaultIfEmpty(mapper.createArrayNode());

        return Mono.zip(demandsMissionsMono, financialsMono, topProvidersMono, popularCategoriesMono)
                .map(t -> {
                    JsonNode demandsMissions = t.getT1();
                    JsonNode financials = t.getT2();
                    JsonNode topProviders = t.getT3();
                    JsonNode popularCategories = t.getT4();

                    ObjectNode financialsOut = mapper.createObjectNode();
                    financialsOut.set("totalRevenue", numberOr(financials.path("totalRevenue"), 0));
                    financialsOut.set("commissionEarned", numberOr(financials.path("commissionEarned"), 0));
                    financialsOut.set("sequesteredAmount", numberOr(financials.path("sequesteredAmount"), 0));
                    financialsOut.set("periodBreakdown", financials.has("periodBreakdown")
                            ? financials.get("periodBreakdown") : mapper.createArrayNode());

                    ObjectNode data = mapper.createObjectNode();
                    data.set("demands", demandsMissions.has("demands")
                            ? demandsMissions.get("demands") : mapper.createObjectNode());
                    data.set("missions", demandsMissions.has("missions")
                            ? demandsMissions.get("missions") : mapper.createObjectNode());
                    data.set("financials", financialsOut);
                    data.set("topProviders", asArray(topProviders));
                    data.set("popularCategories", asArray(popularCategories));

                    return ResponseEntity.ok(envelope(data));
                });
    }

    // ════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════

    private ObjectNode envelope(ObjectNode data) {
        ObjectNode root = mapper.createObjectNode();
        root.put("success", true);
        root.set("data", data);
        root.putNull("meta");
        return root;
    }

    private ArrayNode asArray(JsonNode node) {
        return node != null && node.isArray() ? (ArrayNode) node : mapper.createArrayNode();
    }

    private JsonNode numberOr(JsonNode node, double fallback) {
        return node != null && node.isNumber() ? node : mapper.getNodeFactory().numberNode(fallback);
    }

    private ObjectNode metricValue(JsonNode value) {
        ObjectNode node = mapper.createObjectNode();
        node.set("value", numberOr(value, 0));
        node.putNull("trend");
        return node;
    }

    private ObjectNode metricPlaceholder() {
        ObjectNode node = mapper.createObjectNode();
        node.put("value", 0);
        node.putNull("trend");
        return node;
    }

    private ObjectNode defaultTrends() {
        ObjectNode trends = mapper.createObjectNode();
        for (String key : new String[]{"missions", "earnings", "rating"}) {
            ObjectNode t = mapper.createObjectNode();
            t.put("value", "+0");
            t.put("direction", "stable");
            t.put("subtext", "Données indisponibles");
            trends.set(key, t);
        }
        return trends;
    }
}
