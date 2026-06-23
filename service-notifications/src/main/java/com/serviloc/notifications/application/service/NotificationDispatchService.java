package com.serviloc.notifications.application.service;

import com.serviloc.notifications.application.port.out.EmailSender;
import com.serviloc.notifications.application.port.out.PushNotificationSender;
import com.serviloc.notifications.application.port.out.SmsSender;
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
 * Point d'entrée unique pour l'envoi effectif de notifications (push FCM, SMS, email),
 * utilisé par {@code NotificationEventListener} pour chaque événement métier consommé.
 *
 * Chaque appel se termine systématiquement par l'écriture d'un {@link NotificationLog}
 * (SENT ou FAILED), pour l'audit — cf. {@code GET /internal/notification-logs/:userId}.
 */
@Service
@Transactional
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    /** Pseudo-userId conventionnel sous lequel tout utilisateur "admin" enregistre son device token,
     *  en complément de son token personnel, pour recevoir les notifications de diffusion admin
     *  (provider.reviewed, litige.opened). Convention à valider avec l'équipe Service Utilisateurs /
     *  frontend admin. */
    public static final String ADMIN_BROADCAST_USER_ID = "admin_broadcast";

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationSender pushNotificationSender;
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationDispatchService(DeviceTokenRepository deviceTokenRepository,
                                        PushNotificationSender pushNotificationSender,
                                        SmsSender smsSender,
                                        EmailSender emailSender,
                                        NotificationLogRepository notificationLogRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.pushNotificationSender = pushNotificationSender;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Envoie un push à tous les appareils enregistrés pour {@code userId}.
     * Si aucun device token n'est enregistré, une trace FAILED est écrite (visible à l'audit)
     * mais aucune exception n'est levée — ce n'est pas une erreur technique récupérable par retry.
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

        NotificationLog entry = atLeastOneSuccess
                ? NotificationLog.sent(userId, NotificationChannel.PUSH, eventType, content)
                : NotificationLog.failed(userId, NotificationChannel.PUSH, eventType, content);
        notificationLogRepository.save(entry);
    }

    /**
     * Envoie un SMS. Si {@code phoneNumber} est absent (champ manquant dans le payload de
     * l'événement d'origine), l'envoi est sauté avec un warning explicite plutôt qu'un échec silencieux.
     */
    public void sendSms(String userId, String eventType, String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("[SMS] Numéro de téléphone absent du payload pour event={} userId={} — "
                    + "SMS non envoyé (champ 'phone' à ajouter dans le payload RabbitMQ)", eventType, userId);
            notificationLogRepository.save(NotificationLog.failed(userId, NotificationChannel.SMS, eventType,
                    message + " [non envoyé : téléphone manquant dans le payload]"));
            return;
        }

        boolean sent = smsSender.send(phoneNumber, message);
        NotificationLog entry = sent
                ? NotificationLog.sent(userId, NotificationChannel.SMS, eventType, message)
                : NotificationLog.failed(userId, NotificationChannel.SMS, eventType, message);
        notificationLogRepository.save(entry);
    }

    /**
     * Envoie un email transactionnel.
     */
    public void sendEmail(String userId, String eventType, String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("[EMAIL] Adresse email absente du payload pour event={} userId={} — email non envoyé",
                    eventType, userId);
            notificationLogRepository.save(NotificationLog.failed(userId, NotificationChannel.EMAIL, eventType,
                    subject + " [non envoyé : email manquant]"));
            return;
        }

        boolean sent = emailSender.send(to, subject, body);
        NotificationLog entry = sent
                ? NotificationLog.sent(userId, NotificationChannel.EMAIL, eventType, subject)
                : NotificationLog.failed(userId, NotificationChannel.EMAIL, eventType, subject);
        notificationLogRepository.save(entry);
    }
}
