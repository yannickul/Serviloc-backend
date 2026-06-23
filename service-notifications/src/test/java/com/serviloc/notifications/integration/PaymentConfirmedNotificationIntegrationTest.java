package com.serviloc.notifications.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.notifications.domain.model.NotificationChannel;
import com.serviloc.notifications.domain.model.NotificationStatus;
import com.serviloc.notifications.domain.repository.NotificationLogRepository;
import com.serviloc.notifications.domain.repository.PageQuery;
import com.serviloc.notifications.domain.repository.PageResult;
import com.serviloc.notifications.presentation.dto.ApiResponse;
import com.serviloc.notifications.presentation.dto.NotificationLogResponse;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Test d'intégration de bout en bout :
 * 1. Publie un événement {@code payment.confirmed} sur l'exchange {@code serviloc.events} (RabbitMQ réel, Testcontainers)
 * 2. Vérifie qu'un {@link com.serviloc.notifications.domain.model.NotificationLog} est bien créé en base (PostgreSQL réel)
 * 3. Vérifie que l'audit est consultable via {@code GET /internal/notification-logs/:userId}
 *
 * Cas demandé : "publier payment.confirmed → vérifier NotificationLog créé".
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentConfirmedNotificationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notifications_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);

        // Pas d'Eureka réel disponible pendant les tests.
        registry.add("eureka.client.enabled", () -> "false");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNotificationLogWhenPaymentConfirmedEventIsConsumed() {
        String clientId = "usr_client_test_1";
        String providerId = "usr_provider_test_1";

        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", "tx_test_1");
        payload.put("missionId", "mission_test_1");
        payload.put("clientId", clientId);
        payload.put("providerId", providerId);
        payload.put("amount", 15000);

        // 1. Publication réelle sur l'exchange topic, comme le ferait Service Paiement.
        rabbitTemplate.convertAndSend("serviloc.events", "payment.confirmed", payload);

        // 2. Le traitement est asynchrone (listener RabbitMQ) : on attend la création du NotificationLog.
        await().atMost(10, SECONDS).untilAsserted(() -> {
            PageResult<com.serviloc.notifications.domain.model.NotificationLog> result =
                    notificationLogRepository.findByUserId(clientId, PageQuery.of(0, 10));

            assertThat(result.content()).isNotEmpty();
            assertThat(result.content().get(0).getChannel()).isEqualTo(NotificationChannel.PUSH);
            assertThat(result.content().get(0).getType()).isEqualTo("payment.confirmed");
            assertThat(result.content().get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
        });

        // 3. Vérifie aussi que l'audit est exposé correctement via l'endpoint REST.
        ResponseEntity<ApiResponse<java.util.List<NotificationLogResponse>>> response = restTemplate.exchange(
                "http://localhost:" + port + "/internal/notification-logs/" + clientId,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data())
                .anyMatch(log -> "payment.confirmed".equals(log.type()) && log.userId().equals(clientId));
    }
}
