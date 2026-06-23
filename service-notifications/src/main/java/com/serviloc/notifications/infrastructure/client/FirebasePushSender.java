package com.serviloc.notifications.infrastructure.client;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.serviloc.notifications.application.port.out.PushNotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter d'envoi de notifications push via Firebase Cloud Messaging (FCM).
 *
 * En mode sandbox / FirebaseApp non initialisé, l'envoi est simulé (log "[SANDBOX] push ...")
 * plutôt que de lever une exception — pratique en développement local sans credentials Firebase
 * valides. Le bean {@code FirebaseMessaging} peut être absent (cf. {@code FirebaseConfig}) ; on le
 * récupère via {@link ObjectProvider#getIfAvailable()} plutôt qu'une injection directe, car Spring
 * ne propage pas correctement un bean @Bean retournant {@code null} vers un constructeur classique.
 */
@Component
public class FirebasePushSender implements PushNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushSender.class);

    private final FirebaseMessaging firebaseMessaging;

    public FirebasePushSender(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        this.firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
    }

    @Override
    public boolean send(String deviceToken, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.info("[SANDBOX][PUSH] token={} title=\"{}\" body=\"{}\" data={}", deviceToken, title, body, data);
            return true;
        }

        try {
            Message.Builder builder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String messageId = firebaseMessaging.send(builder.build());
            log.info("[PUSH] Envoyé avec succès (messageId={}) à token={}", messageId, deviceToken);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("[PUSH] Échec d'envoi à token={} : {}", deviceToken, e.getMessage());
            return false;
        }
    }
}
