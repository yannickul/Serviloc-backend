package com.serviloc.notifications.application.port.out;

import java.util.Map;

/**
 * Port sortant pour l'envoi de notifications push mobile (Firebase FCM).
 */
public interface PushNotificationSender {

    /**
     * Envoie une notification push à un token d'appareil donné.
     *
     * @param deviceToken token FCM cible
     * @param title       titre de la notification
     * @param body        corps du message
     * @param data        données additionnelles (payload applicatif, optionnel)
     * @return true si l'envoi a réussi, false sinon (le détail de l'erreur est loggé par l'adapter)
     */
    boolean send(String deviceToken, String title, String body, Map<String, String> data);
}
