package com.serviloc.notifications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Binding des propriétés {@code serviloc.messaging.*} (cf. application.yml).
 */
@ConfigurationProperties(prefix = "serviloc.messaging")
public class RabbitMqProperties {

    /** Exchange topic partagé par toute la plateforme ServiLoc. */
    private String exchange = "serviloc.events";

    /** Queue principale consommée par ce service. */
    private String queue = "notifications.queue";

    /** Dead-letter queue, alimentée après épuisement des tentatives de retry. */
    private String dlq = "notifications.dlq";

    /** Dead-letter exchange associé à la DLQ. */
    private String dlx = "notifications.dlx";

    /** Routing keys (topic patterns) à binder sur la queue principale. */
    private List<String> routingKeys = List.of();

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getDlq() {
        return dlq;
    }

    public void setDlq(String dlq) {
        this.dlq = dlq;
    }

    public String getDlx() {
        return dlx;
    }

    public void setDlx(String dlx) {
        this.dlx = dlx;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(List<String> routingKeys) {
        this.routingKeys = routingKeys;
    }
}
