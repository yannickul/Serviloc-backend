package com.serviloc.gateway.aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Appels HTTP inter-services pour les endpoints d'agrégation (dashboards, stats).
 *
 * Principe de résilience (voir ARCHITECTURE_MICROSERVICES_SERVILOC.md §9 —
 * "Que faire si un service est indisponible ?") : un service downstream en panne,
 * pas encore déployé, ou pas encore enregistré dans Eureka ne doit JAMAIS faire
 * planter l'agrégation entière. On dégrade gracieusement : le champ correspondant
 * revient à `null` / `[]` côté frontend plutôt qu'un 503 sur tout le dashboard.
 *
 * Sécurité inter-services : tous les endpoints `/internal/**` de service-utilisateurs,
 * service-paiement, service-negociations, service-missions et service-categories exigent
 * le header `X-Internal-Token` (filtre appliqué au niveau servlet, avant même le controller —
 * voir AUDIT_AGREGATEUR_GATEWAY.md §0). Utiliser {@link #getInternalJson} pour tout appel
 * vers un chemin `/internal/**` ; {@link #getJson} pour les endpoints publics/admin qui
 * s'appuient sur X-User-Id / X-User-Role / Authorization.
 */
@Component
public class DownstreamGateway {

    private static final Logger log = LoggerFactory.getLogger(DownstreamGateway.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;
    private final String internalToken;

    public DownstreamGateway(WebClient.Builder loadBalancedWebClientBuilder,
                              @Value("${internal.token}") String internalToken) {
        this.webClient = loadBalancedWebClientBuilder.build();
        this.internalToken = internalToken;
    }

    /** Appel GET générique. En cas d'échec (timeout, service down, 4xx/5xx) → Mono.empty(). */
    public Mono<JsonNode> getJson(String lbUri, Consumer<HttpHeaders> headers) {
        return webClient.get()
                .uri(lbUri)
                .headers(headers)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(TIMEOUT)
                .doOnError(e -> log.warn("[Aggregator] Appel échoué → {} : {}", lbUri, e.toString()))
                .onErrorResume(e -> Mono.empty());
    }

    /** Appel GET vers un endpoint `/internal/**` : ajoute automatiquement X-Internal-Token. */
    public Mono<JsonNode> getInternalJson(String lbUri) {
        return getJson(lbUri, h -> h.set("X-Internal-Token", internalToken));
    }

    /** Appel GET vers un endpoint `/internal/**` avec headers additionnels (rare). */
    public Mono<JsonNode> getInternalJson(String lbUri, Consumer<HttpHeaders> extraHeaders) {
        return getJson(lbUri, h -> {
            h.set("X-Internal-Token", internalToken);
            extraHeaders.accept(h);
        });
    }

    /** Extrait le champ "data" de l'enveloppe ApiResponse {success,data,meta}. */
    public JsonNode unwrapData(JsonNode envelope) {
        if (envelope == null || envelope.isMissingNode() || envelope.isNull()) {
            return MissingNode.getInstance();
        }
        return envelope.has("data") ? envelope.get("data") : envelope;
    }
}
