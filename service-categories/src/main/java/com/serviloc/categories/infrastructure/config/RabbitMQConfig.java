package com.serviloc.categories.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ pour service-categories (voir ARCHITECTURE §4.3) :
 * exchange topic partagé "serviloc.events", consommation de "demand.published"
 * pour incrémenter demandCount (voir §3.7).
 */
@Configuration
public class RabbitMQConfig {

    public static final String SERVILOC_EVENTS_EXCHANGE = "serviloc.events";
    public static final String DEMAND_PUBLISHED_ROUTING_KEY = "demand.published";
    public static final String CATEGORIES_DEMAND_QUEUE = "categories.demand.queue";
    public static final String CATEGORIES_DEMAND_DLQ = "categories.demand.queue.dlq";

    @Bean
    public TopicExchange servilocEventsExchange() {
        return new TopicExchange(SERVILOC_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue categoriesDemandDeadLetterQueue() {
        return QueueBuilder.durable(CATEGORIES_DEMAND_DLQ).build();
    }

    @Bean
    public Queue categoriesDemandQueue() {
        return QueueBuilder.durable(CATEGORIES_DEMAND_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CATEGORIES_DEMAND_DLQ)
                .build();
    }

    @Bean
    public Binding categoriesDemandBinding(Queue categoriesDemandQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(categoriesDemandQueue)
                .to(servilocEventsExchange)
                .with(DEMAND_PUBLISHED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // Les producteurs (autres microservices) ne partagent pas nos classes Java : on ignore
        // le header __TypeId__ et on déduit toujours le type cible depuis le paramètre du
        // @RabbitListener, plutôt que de risquer une ClassNotFoundException.
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
