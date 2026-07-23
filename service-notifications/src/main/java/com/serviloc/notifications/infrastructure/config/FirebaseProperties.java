package com.serviloc.notifications.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binding des propriétés {@code serviloc.firebase.*} (cf. application.yml).
 */
@ConfigurationProperties(prefix = "serviloc.firebase")
public class FirebaseProperties {

    /** Active ou désactive l'envoi réel de push FCM (false en local tant que le fichier sandbox n'est pas fourni). */
    private boolean enabled = true;

    /** Chemin (classpath ou fichier) vers le JSON de compte de service Firebase (sandbox). */
    private String credentialsPath = "firebase-service-account.json";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }
}
