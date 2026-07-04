package com.serviloc.notifications.application.port.out;

/**
 * Port sortant pour l'envoi d'emails transactionnels (ex: mot de passe provisoire agent).
 * Phase 2 selon l'architecture — implémentation sandbox/log fournie en attendant un vrai provider.
 */
public interface EmailSender {

    boolean send(String to, String subject, String body);
}
