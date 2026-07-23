package com.serviloc.notifications.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Configuration Firebase Admin SDK pour l'envoi de notifications push (Firebase Cloud Messaging — FCM).
 *
 * Mode sandbox : si {@code serviloc.firebase.enabled=false} (profil local par défaut, tant que le
 * fichier de compte de service sandbox n'est pas fourni), aucun {@link FirebaseApp} n'est initialisé
 * et {@link com.serviloc.notifications.infrastructure.client.FirebasePushSender} bascule en mode
 * "no-op" (log uniquement, cf. cette classe).
 *
 * Le fichier {@code firebase-service-account.json} (clé de compte de service, sandbox FCM) ne doit
 * jamais être commité — il est ignoré via .gitignore et doit être fourni par variable d'environnement
 * ou monté en volume en déploiement.
 */
@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private final FirebaseProperties firebaseProperties;
    private final ResourceLoader resourceLoader;

    public FirebaseConfig(FirebaseProperties firebaseProperties, ResourceLoader resourceLoader) {
        this.firebaseProperties = firebaseProperties;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Initialise FirebaseApp puis FirebaseMessaging si activé et si le fichier de credentials est
     * trouvable. Retourne null sinon — {@code FirebasePushSender} consomme ce bean via un
     * {@code ObjectProvider} (cf. cette classe) pour gérer correctement ce cas "non disponible",
     * ce qu'une dépendance @Bean -> @Bean classique ne permet pas (Spring ne propage pas un bean
     * null entre deux méthodes @Bean liées par injection de paramètre).
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (!firebaseProperties.isEnabled()) {
            log.warn("[Firebase] Push FCM désactivé (serviloc.firebase.enabled=false) — mode sandbox/no-op");
            return null;
        }

        try {
            Resource resource = resourceLoader.getResource(toResourceLocation(firebaseProperties.getCredentialsPath()));
            if (!resource.exists()) {
                log.warn("[Firebase] Fichier de credentials introuvable ({}). Push FCM désactivé en mode no-op.",
                        firebaseProperties.getCredentialsPath());
                return null;
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp app = FirebaseApp.getApps().isEmpty()
                        ? FirebaseApp.initializeApp(options)
                        : FirebaseApp.getApps().get(0);

                log.info("[Firebase] FirebaseApp initialisé avec succès (sandbox FCM)");
                return FirebaseMessaging.getInstance(app);
            }
        } catch (IOException e) {
            log.error("[Firebase] Échec d'initialisation de FirebaseApp : {}", e.getMessage());
            return null;
        }
    }

    private String toResourceLocation(String path) {
        // Accepte un chemin classpath ("firebase-service-account.json") ou un chemin fichier absolu/relatif.
        boolean alreadyPrefixed = List.of("classpath:", "file:").stream().anyMatch(path::startsWith);
        if (alreadyPrefixed) {
            return path;
        }
        return path.startsWith("/") ? "file:" + path : "classpath:" + path;
    }
}
