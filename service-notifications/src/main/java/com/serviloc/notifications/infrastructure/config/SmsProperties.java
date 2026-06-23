package com.serviloc.notifications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binding des propriétés {@code serviloc.sms.*} (cf. application.yml).
 *
 * En sandbox (provider = "sandbox"), aucun appel réseau réel n'est effectué :
 * le SMS est uniquement loggé, ce qui permet de développer sans clé API valide.
 * Provider final prévu en fin de projet : Vonage ou Orange SMS API Cameroun.
 */
@ConfigurationProperties(prefix = "serviloc.sms")
public class SmsProperties {

    private String provider = "sandbox";
    private String apiKey = "";
    private String senderId = "ServiLoc";
    private String baseUrl;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isSandbox() {
        return "sandbox".equalsIgnoreCase(provider);
    }
}
