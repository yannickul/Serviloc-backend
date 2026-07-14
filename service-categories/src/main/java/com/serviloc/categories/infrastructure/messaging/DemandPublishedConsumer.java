package com.serviloc.categories.infrastructure.messaging;

import com.serviloc.categories.application.service.CategoryService;
import com.serviloc.categories.domain.exception.CategoryNotFoundException;
import com.serviloc.categories.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consommateur de l'événement "demand.published" : incrémente demandCount
 * de la catégorie concernée à chaque nouvelle demande publiée par un client.
 *
 * Idempotence : un double traitement du même message ne fait qu'incrémenter le compteur
 * une fois de plus (risque accepté, cf. tâche "recalcule percentageShare" — la précision
 * exacte du compteur n'est pas critique métier, c'est un indicateur statistique).
 */
@Component
public class DemandPublishedConsumer {

    private static final Logger log = LoggerFactory.getLogger(DemandPublishedConsumer.class);

    private final CategoryService categoryService;

    public DemandPublishedConsumer(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @RabbitListener(queues = RabbitMQConfig.CATEGORIES_DEMAND_QUEUE)
    public void onDemandPublished(DemandPublishedEvent event) {
        log.info("Événement demand.published reçu pour la catégorie {} (demandId={})",
                event.categoryId(), event.demandId());
        try {
            categoryService.incrementDemandCount(event.categoryId());
        } catch (CategoryNotFoundException ex) {
            // Catégorie inconnue : on log et on laisse le message être ack (pas de retry infini
            // sur une donnée invalide), plutôt que de bloquer la queue.
            log.warn("Catégorie introuvable pour demand.published : categoryId={}, demandId={}",
                    event.categoryId(), event.demandId());
        }
    }
}
