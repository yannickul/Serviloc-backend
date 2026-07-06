package com.serviloc.notifications.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration du client SMS.
 *
 * En sandbox, aucun appel HTTP réel n'est effectué (cf. {@code SandboxSmsSender}).
 * Le {@link RestClient} ci-dessous est néanmoins exposé pour permettre le branchement futur
 * d'un vrai provider SMS Cameroun (Vonage, Orange SMS API) sans changer la configuration —
 * cf. note architecture : "tâches à faire à la fin".
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsClientConfig {

    @Bean
    public RestClient smsRestClient(SmsProperties smsProperties) {
        RestClient.Builder builder = RestClient.builder();
        if (smsProperties.getBaseUrl() != null && !smsProperties.getBaseUrl().isBlank()) {
            builder.baseUrl(smsProperties.getBaseUrl());
        }
        return builder.build();
    }
}
