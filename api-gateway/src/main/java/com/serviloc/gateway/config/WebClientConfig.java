package com.serviloc.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient.Builder résolu dynamiquement via Eureka (schéma lb://nom-du-service).
 * Utilisé uniquement par les appels d'agrégation du Gateway (DashboardAggregatorController) —
 * distinct du routage proxy classique (StripPrefix/RouteLocator) déclaré en YAML.
 */
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
