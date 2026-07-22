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

import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
 * Si un service downstream est indisponible, le champ correspondant revient à
 * `null` / `[]` plutôt que de faire échouer tout le dashboard (voir DownstreamGateway).
 *
 * ── Endpoints downstream utilisés ────────────────────────────────────────────
 * Vérifiés sur le code réel (branches developer/newtests/tests) — détail complet et
 * justification de chaque choix dans /AUDIT_AGREGATEUR_GATEWAY.md à la racine du repo.
 * Gaps encore réels côté downstream (non résolus par le Gateway, cf. audit §3-4) :
 *   - provider/dashboard.metrics.trends : aucune donnée historique nulle part → valeurs neutres.
 *   - admin/dashboard.pendingValidations : filtre `status` non implémenté côté service-utilisateurs
 *     (transmis à l'équipe concernée) → renvoie actuellement tous les prestataires, pas seulement
 *     ceux en attente de validation.
 *   - admin/stats.topProviders : endpoint /internal/providers/top toujours absent côté
 *     service-utilisateurs → tableau vide en attendant.
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

        // GET /client/demands n'est pas trié côté service-missions → on demande une page plus
        // large et on trie nous-mêmes par createdAt desc avant de tronquer à 5 (audit §1.2).
        Mono<JsonNode> recentDemandsMono = gateway.getJson(
                        "lb://service-missions/client/demands?page=1&limit=20",
                        h -> h.set("X-User-Id", userId))
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createArrayNode());

        Mono<JsonNode> conversationsMono = gateway.getJson(
                        "lb://service-negociations/client/conversations?page=1&limit=100",
                        h -> {
                            h.set("X-User-Id", userId);
                            h.set("X-User-Role", "CLIENT");
                        })
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        return Mono.zip(profileMono, recentDemandsMono, conversationsMono)
                .map(t -> {
                    JsonNode profile = t.getT1();
                    JsonNode recentDemands = sortDescAndTruncate(t.getT2(), "createdAt", 5);
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
                    data.set("recentDemands", recentDemands);
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

        // GET /provider/missions renvoie TOUTE la liste, non triée (audit §2.2/§5.3) : on
        // réutilise cette même liste pour recentMissions (triée, tronquée à 5) ET pour
        // missionsThisMonth (filtrée sur le mois courant), faute d'endpoint dédié.
        Mono<JsonNode> allMissionsMono = gateway.getJson(
                        "lb://service-missions/provider/missions",
                        h -> h.set("X-User-Id", userId))
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createArrayNode());

        // GET /provider/demands (categoryId optionnel) : pas de compteur dédié, .size() de la liste.
        Mono<JsonNode> availableDemandsMono = gateway.getJson(
                        "lb://service-missions/provider/demands",
                        h -> h.set("X-User-Id", userId))
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createArrayNode());

        Mono<JsonNode> earningsMono = gateway.getJson(
                        "lb://service-paiement/provider/earnings?limit=1",
                        h -> {
                            h.set("X-User-Id", userId);
                            h.set("X-User-Role", "PROVIDER");
                        })
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        return Mono.zip(profileMono, allMissionsMono, availableDemandsMono, earningsMono)
                .map(t -> {
                    JsonNode profile = t.getT1();
                    JsonNode allMissions = t.getT2();
                    JsonNode availableDemands = t.getT3();
                    JsonNode earnings = t.getT4();

                    ArrayNode recentMissions = sortDescAndTruncate(allMissions, "startedAt", 5);
                    int missionsThisMonth = countThisMonth(allMissions, "startedAt");
                    int availableDemandsCount = availableDemands.isArray() ? availableDemands.size() : 0;

                    ObjectNode metrics = mapper.createObjectNode();
                    metrics.put("missionsThisMonth", missionsThisMonth);
                    JsonNode netEarnings = earnings.has("monthlyTotal")
                            ? earnings.path("monthlyTotal")
                            : profile.path("monthlyEarnings");
                    metrics.set("netEarnings", numberOr(netEarnings, 0));
                    metrics.set("averageRating", numberOr(profile.path("rating"), 0));
                    metrics.put("availableDemandsCount", availableDemandsCount);
                    // Aucune donnée historique disponible nulle part pour calculer une vraie
                    // variation (audit §2, ligne "trends") → valeurs neutres documentées.
                    metrics.set("trends", defaultTrends());

                    ObjectNode data = mapper.createObjectNode();
                    data.set("profile", profile);
                    data.set("metrics", metrics);
                    data.set("recentMissions", recentMissions);
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

        // Source unique demands+missions pour admin/dashboard ET admin/stats (audit §3.1/§4.1-2).
        Mono<JsonNode> missionsDashboardMono = gateway.getInternalJson(
                        "lb://service-missions/internal/dashboard/missions")
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        Mono<JsonNode> financialsMono = gateway.getInternalJson(
                        "lb://service-paiement/internal/stats/financials")
                .defaultIfEmpty(mapper.createObjectNode());

        // ⚠️ Le paramètre status est ignoré côté service-utilisateurs (audit §3.3) : renvoie
        // actuellement TOUS les prestataires, pas seulement ceux en attente de validation.
        Mono<JsonNode> pendingValidationsMono = gateway.getJson(
                        "lb://service-utilisateurs/admin/providers?status=pending_verification&limit=5",
                        adminHeaders)
                .map(gateway::unwrapData)
                .map(d -> d.path("providers"))
                .defaultIfEmpty(mapper.createArrayNode());

        // Pas d'endpoint "liste des litiges actifs" dédié : /admin/litiges (pagination 0-indexée,
        // un seul statut par appel) → 2 appels parallèles OUVERT + EN_COURS, fusionnés (audit §3.4).
        Mono<JsonNode> litigesOuvertMono = gateway.getJson(
                        "lb://service-litiges/admin/litiges?status=OUVERT&page=0&limit=5",
                        adminHeaders)
                .map(gateway::unwrapData)
                .map(d -> d.path("data"))
                .defaultIfEmpty(mapper.createArrayNode());
        Mono<JsonNode> litigesEnCoursMono = gateway.getJson(
                        "lb://service-litiges/admin/litiges?status=EN_COURS&page=0&limit=5",
                        adminHeaders)
                .map(gateway::unwrapData)
                .map(d -> d.path("data"))
                .defaultIfEmpty(mapper.createArrayNode());

        // service-missions ne consomme pas encore /internal/categories/stats (audit §3.5) :
        // on appelle directement service-categories, seul endroit où demandCount existe vraiment.
        Mono<JsonNode> popularCategoriesMono = gateway.getInternalJson(
                        "lb://service-categories/internal/categories/stats")
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createArrayNode());

        Mono<JsonNode> recentTransactionsMono = gateway.getJson(
                        "lb://service-paiement/admin/transactions?limit=5",
                        adminHeaders)
                .map(gateway::unwrapData)
                .map(d -> d.path("transactions"))
                .defaultIfEmpty(mapper.createArrayNode());

        return Mono.zip(missionsDashboardMono, financialsMono, pendingValidationsMono,
                        Mono.zip(litigesOuvertMono, litigesEnCoursMono),
                        popularCategoriesMono, recentTransactionsMono)
                .map(t -> {
                    JsonNode missionsDashboard = t.getT1();
                    JsonNode financials = t.getT2();
                    JsonNode pendingValidations = t.getT3();
                    JsonNode activeLitiges = mergeArrays(t.getT4().getT1(), t.getT4().getT2());
                    JsonNode popularCategories = sortDescAndTruncate(t.getT5(), "demandCount", 6);
                    JsonNode recentTransactions = t.getT6();

                    long open = missionsDashboard.path("openDemands").asLong(0);
                    long inProgress = missionsDashboard.path("inProgressDemands").asLong(0);
                    long totalMissions = missionsDashboard.path("totalMissions").asLong(0);
                    long completedMissions = missionsDashboard.path("completedMissions").asLong(0);
                    long disputedMissions = missionsDashboard.path("disputedMissions").asLong(0);

                    ObjectNode metrics = mapper.createObjectNode();
                    metrics.set("activeDemands", metricValueLong(open + inProgress));
                    metrics.set("ongoingMissions",
                            metricValueLong(totalMissions - completedMissions - disputedMissions));
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
            @RequestParam(required = false, defaultValue = "2026-01-01T00:00:00") String from,
            @RequestParam(required = false, defaultValue = "2099-12-31T23:59:59") String to) {

        // Même endpoint que admin/dashboard : couvre demands ET missions en un seul appel
        // (audit §4.1-2 — remplace les 2 endpoints /internal/stats/summary + /demands-missions
        // qui n'existent pas sous cette forme).
        Mono<JsonNode> missionsDashboardMono = gateway.getInternalJson(
                        "lb://service-missions/internal/dashboard/missions")
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createObjectNode());

        Mono<JsonNode> financialsMono = gateway.getInternalJson(
                        "lb://service-paiement/internal/stats/financials?from=" + from + "&to=" + to)
                .defaultIfEmpty(mapper.createObjectNode());

        // ⚠️ Endpoint toujours absent côté service-utilisateurs (audit §4.4) → transmis à l'équipe.
        Mono<JsonNode> topProvidersMono = gateway.getInternalJson(
                        "lb://service-utilisateurs/internal/providers/top?limit=4")
                .defaultIfEmpty(mapper.createArrayNode());

        Mono<JsonNode> popularCategoriesMono = gateway.getInternalJson(
                        "lb://service-categories/internal/categories/stats")
                .map(gateway::unwrapData)
                .defaultIfEmpty(mapper.createArrayNode());

        return Mono.zip(missionsDashboardMono, financialsMono, topProvidersMono, popularCategoriesMono)
                .map(t -> {
                    JsonNode missionsDashboard = t.getT1();
                    JsonNode financials = t.getT2();
                    JsonNode topProviders = t.getT3();
                    JsonNode popularCategories = sortDescAndTruncate(t.getT4(), "demandCount", 6);

                    ObjectNode demands = mapper.createObjectNode();
                    demands.set("total", numberOr(missionsDashboard.path("totalDemands"), 0));
                    demands.set("open", numberOr(missionsDashboard.path("openDemands"), 0));
                    demands.set("inProgress", numberOr(missionsDashboard.path("inProgressDemands"), 0));
                    demands.set("completed", numberOr(missionsDashboard.path("completedDemands"), 0));
                    demands.set("cancelled", numberOr(missionsDashboard.path("cancelledDemands"), 0));

                    ObjectNode missions = mapper.createObjectNode();
                    missions.set("total", numberOr(missionsDashboard.path("totalMissions"), 0));
                    missions.set("completed", numberOr(missionsDashboard.path("completedMissions"), 0));
                    missions.set("inDispute", numberOr(missionsDashboard.path("disputedMissions"), 0));
                    missions.set("completionRate", numberOr(missionsDashboard.path("completionRate"), 0));

                    ObjectNode financialsOut = mapper.createObjectNode();
                    financialsOut.set("totalRevenue", numberOr(financials.path("totalRevenue"), 0));
                    financialsOut.set("commissionEarned", numberOr(financials.path("commissionEarned"), 0));
                    financialsOut.set("sequesteredAmount", numberOr(financials.path("sequesteredAmount"), 0));
                    // periodBreakdown : aucun service ne fournit de ventilation mensuelle (audit §4.3).
                    financialsOut.set("periodBreakdown", mapper.createArrayNode());

                    ObjectNode data = mapper.createObjectNode();
                    data.set("demands", demands);
                    data.set("missions", missions);
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

    private ObjectNode metricValueLong(long value) {
        ObjectNode node = mapper.createObjectNode();
        node.put("value", value);
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

    /**
     * Trie un tableau JSON par un champ (texte ISO-8601 ou numérique) en ordre décroissant et
     * tronque aux `limit` premiers éléments. Nécessaire car plusieurs endpoints downstream ne
     * trient pas leurs résultats eux-mêmes (audit §5.3).
     */
    private ArrayNode sortDescAndTruncate(JsonNode arrayNode, String field, int limit) {
        ArrayNode result = mapper.createArrayNode();
        if (arrayNode == null || !arrayNode.isArray()) {
            return result;
        }
        List<JsonNode> items = new ArrayList<>();
        arrayNode.forEach(items::add);
        items.sort(Comparator.comparing((JsonNode n) -> sortKey(n, field)).reversed());
        items.stream().limit(limit).forEach(result::add);
        return result;
    }

    private double sortKey(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNumber()) {
            return value.asDouble();
        }
        try {
            return Instant.parse(value.asText()).toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }

    /** Compte les éléments dont le champ date (ISO-8601) tombe dans le mois courant. */
    private int countThisMonth(JsonNode arrayNode, String dateField) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return 0;
        }
        YearMonth currentMonth = YearMonth.now();
        int count = 0;
        for (JsonNode item : arrayNode) {
            try {
                Instant instant = Instant.parse(item.path(dateField).asText());
                if (YearMonth.from(instant.atZone(java.time.ZoneId.systemDefault())).equals(currentMonth)) {
                    count++;
                }
            } catch (Exception ignored) {
                // champ absent/invalide → ignoré
            }
        }
        return count;
    }

    private ArrayNode mergeArrays(JsonNode a, JsonNode b) {
        ArrayNode merged = mapper.createArrayNode();
        if (a != null && a.isArray()) merged.addAll((ArrayNode) a);
        if (b != null && b.isArray()) merged.addAll((ArrayNode) b);
        return merged;
    }
}
