package com.serviloc.notifications.application.port.out;

/**
 * Port sortant pour l'envoi de SMS (OTP, alertes critiques).
 */
public interface SmsSender {

    /**
     * Envoie un SMS au numéro donné.
     *
     * @param phoneNumber numéro au format international (ex: "+237695000000")
     * @param message     contenu du SMS (texte brut)
     * @return true si l'envoi a réussi, false sinon
     */
    boolean send(String phoneNumber, String message);
}
