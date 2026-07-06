package com.serviloc.notifications.infrastructure.client;

import com.serviloc.notifications.application.port.out.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter d'envoi d'emails — implémentation "log only" en attendant un vrai provider (phase 2,
 * cf. architecture §3.6). Utilisé notamment par le consumer {@code agent.created} (mot de passe
 * provisoire de l'agent), à activer dans une étape ultérieure.
 */
@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public boolean send(String to, String subject, String body) {
        log.info("[SANDBOX][EMAIL] à={} sujet=\"{}\" corps=\"{}\"", to, subject, body);
        return true;
    }
}
