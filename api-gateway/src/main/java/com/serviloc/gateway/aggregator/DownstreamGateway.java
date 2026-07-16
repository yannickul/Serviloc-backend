package com.serviloc.gateway.aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@Component
public class DownstreamGateway {

    private static final Logger log = LoggerFactory.getLogger(DownstreamGateway.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;

    public DownstreamGateway(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder.build();
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

    /** Extrait le champ "data" de l'enveloppe ApiResponse {success,data,meta}. */
    public JsonNode unwrapData(JsonNode envelope) {
        if (envelope == null || envelope.isMissingNode() || envelope.isNull()) {
            return MissingNode.getInstance();
        }
        return envelope.has("data") ? envelope.get("data") : envelope;
    }
}
