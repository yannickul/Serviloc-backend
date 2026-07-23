package com.serviloc.notifications.application.service;

import com.serviloc.notifications.application.port.out.EmailSender;
import com.serviloc.notifications.application.port.out.PushNotificationSender;
import com.serviloc.notifications.domain.model.DeviceToken;
import com.serviloc.notifications.domain.model.NotificationChannel;
import com.serviloc.notifications.domain.model.NotificationLog;
import com.serviloc.notifications.domain.repository.DeviceTokenRepository;
import com.serviloc.notifications.domain.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Point d'entrée unique pour l'envoi effectif de notifications (push FCM, email),
 * utilisé par {@code NotificationEventListener} pour chaque événement métier consommé.
 *
 * Chaque appel écrit systématiquement un {@link NotificationLog} (SENT ou FAILED)
 * pour l'audit — cf. {@code GET /internal/notification-logs/:userId}.
 *
 * Note : l'envoi SMS a été remplacé par l'envoi email (Resend) pour la phase de démo.
 */
@Service
@Transactional
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    /**
     * Pseudo-userId sous lequel les sessions admin enregistrent leur device token
     * pour recevoir les push "diffusion admin" (provider.reviewed, litige.opened).
     */
    public static final String ADMIN_BROADCAST_USER_ID = "admin_broadcast";

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationSender pushNotificationSender;
    private final EmailSender emailSender;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationDispatchService(DeviceTokenRepository deviceTokenRepository,
                                        PushNotificationSender pushNotificationSender,
                                        EmailSender emailSender,
                                        NotificationLogRepository notificationLogRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.pushNotificationSender = pushNotificationSender;
        this.emailSender = emailSender;
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Envoie un push à tous les appareils enregistrés pour {@code userId}.
     * Si aucun device token n'est enregistré, trace FAILED écrite mais pas d'exception.
     */
    public void sendPush(String userId, String eventType, String title, String body, Map<String, String> data) {
        List<DeviceToken> tokens = deviceTokenRepository.findAllByUserId(userId);
        String content = title + " — " + body;

        if (tokens.isEmpty()) {
            log.warn("[PUSH] Aucun device token enregistré pour userId={} (event={})", userId, eventType);
            notificationLogRepository.save(NotificationLog.failed(userId, NotificationChannel.PUSH, eventType, content));
            return;
        }

        boolean atLeastOneSuccess = false;
        for (DeviceToken deviceToken : tokens) {
            boolean sent = pushNotificationSender.send(deviceToken.getToken(), title, body, data);
            atLeastOneSuccess = atLeastOneSuccess || sent;
        }

        notificationLogRepository.save(atLeastOneSuccess
                ? NotificationLog.sent(userId, NotificationChannel.PUSH, eventType, content)
                : NotificationLog.failed(userId, NotificationChannel.PUSH, eventType, content));
    }

    /**
     * Envoie un email transactionnel.
     * Si {@code to} est absent du payload, trace FAILED et log warning.
     */
    public void sendEmail(String userId, String eventType, String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("[EMAIL] Adresse email absente du payload pour event={} userId={} — "
                    + "email non envoyé (champ 'email' à ajouter dans le payload RabbitMQ)", eventType, userId);
            notificationLogRepository.save(NotificationLog.failed(userId, NotificationChannel.EMAIL, eventType,
                    subject + " [non envoyé : email manquant dans le payload]"));
            return;
        }

        boolean sent = emailSender.send(to, subject, body);
        notificationLogRepository.save(sent
                ? NotificationLog.sent(userId, NotificationChannel.EMAIL, eventType, subject)
                : NotificationLog.failed(userId, NotificationChannel.EMAIL, eventType, subject));
    }
}
