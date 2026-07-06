package com.serviloc.notifications.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Topologie RabbitMQ du Service Notifications.
 *
 * Exchange topic unique {@code serviloc.events}, partagé par toute la plateforme ServiLoc.
 * Ce service ne fait que consommer : il bind sa queue {@code notifications.queue} sur tous les
 * routing keys qui le concernent (user.*, provider.*, agent.*, demand.*, negotiation.*, payment.*,
 * litige.*, mission.*) — cf. architecture §4.3.
 *
 * Dead-lettering : la queue principale est configurée avec {@code x-dead-letter-exchange} pointant
 * vers {@code serviloc.events.dlx} (exchange de dead-letter partagé, nommé par l'équipe — pas
 * spécifique à ce service). Combiné au retry Spring AMQP (3 tentatives, cf. application.yml
 * {@code spring.rabbitmq.listener.simple.retry}), un message qui échoue 3 fois est automatiquement
 * routé par le broker vers {@code notifications.dlq} après le 3ème NACK (sans requeue).
 *
 * {@code x-message-ttl} (1h) sur la queue principale : voulu par l'équipe, indépendant du
 * dead-lettering par échec de traitement — un message non consommé après 1h expire et est
 * lui aussi routé vers la DLQ (comportement RabbitMQ standard pour un TTL + DLX combinés).
 */
@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange servilocEventsExchange(RabbitMqProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public DirectExchange notificationsDeadLetterExchange(RabbitMqProperties properties) {
        return new DirectExchange(properties.getDlx(), true, false);
    }

    @Bean
    public Queue notificationsQueue(RabbitMqProperties properties) {
        return QueueBuilder.durable(properties.getQueue())
                .withArgument("x-dead-letter-exchange", properties.getDlx())
                .withArgument("x-dead-letter-routing-key", properties.getDlq())
                .withArgument("x-message-ttl", 3_600_000)
                .build();
    }

    @Bean
    public Queue notificationsDeadLetterQueue(RabbitMqProperties properties) {
        return QueueBuilder.durable(properties.getDlq()).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue notificationsDeadLetterQueue,
                                      DirectExchange notificationsDeadLetterExchange,
                                      RabbitMqProperties properties) {
        return BindingBuilder.bind(notificationsDeadLetterQueue)
                .to(notificationsDeadLetterExchange)
                .with(properties.getDlq());
    }

    /**
     * Un binding par routing key pattern déclaré dans {@code serviloc.messaging.routing-keys}
     * (ex: "user.*", "payment.*"...).
     */
    @Bean
    public Declarables notificationsQueueBindings(Queue notificationsQueue,
                                                    TopicExchange servilocEventsExchange,
                                                    RabbitMqProperties properties) {
        List<Binding> bindings = new ArrayList<>();
        for (String routingKey : properties.getRoutingKeys()) {
            bindings.add(BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with(routingKey));
        }
        return new Declarables(bindings);
    }
}
