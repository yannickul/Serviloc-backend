package com.serviloc.notifications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binding des propriétés {@code serviloc.email.*} (cf. application.yml).
 *
 * En mode sandbox (RESEND_API_KEY vide), aucun appel réseau réel n'est effectué :
 * l'email est uniquement loggé — permet de développer sans clé API valide.
 *
 * Pour la démo : crée un compte sur https://resend.com, récupère ta clé API,
 * et définis RESEND_API_KEY dans ton .env.
 *
 * Note expéditeur : sans domaine vérifié, utilise "onboarding@resend.dev" comme FROM
 * (Resend autorise l'envoi vers TON adresse inscrite avec cet expéditeur en sandbox).
 */
@ConfigurationProperties(prefix = "serviloc.email")
public class EmailProperties {

    private String provider = "resend";
    private String apiKey = "";
    private String from = "onboarding@resend.dev";
    private String fromName = "ServiLoc";

    public boolean isSandbox() {
        return apiKey == null || apiKey.isBlank();
    }

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
