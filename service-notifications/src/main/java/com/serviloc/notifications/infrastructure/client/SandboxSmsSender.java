package com.serviloc.notifications.infrastructure.client;

import com.serviloc.notifications.application.port.out.SmsSender;
import com.serviloc.notifications.infrastructure.config.SmsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter d'envoi de SMS.
 *
 * Mode sandbox ({@code serviloc.sms.provider=sandbox}, valeur par défaut) : le SMS n'est pas
 * réellement envoyé, seulement loggé — permet de développer/tester sans clé API valide.
 *
 * Le {@link RestClient} est déjà injecté pour brancher un vrai provider (Vonage, Orange SMS API
 * Cameroun) en fin de projet, sans changer la signature du port {@link SmsSender}.
 */
@Component
public class SandboxSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SandboxSmsSender.class);

    private final SmsProperties smsProperties;
    private final RestClient smsRestClient;

    public SandboxSmsSender(SmsProperties smsProperties, RestClient smsRestClient) {
        this.smsProperties = smsProperties;
        this.smsRestClient = smsRestClient;
    }

    @Override
    public boolean send(String phoneNumber, String message) {
        if (smsProperties.isSandbox()) {
            log.info("[SANDBOX][SMS] de={} vers={} message=\"{}\"", smsProperties.getSenderId(), phoneNumber, message);
            return true;
        }

        // TODO (fin de projet) : appel réel au provider SMS (Vonage / Orange SMS API Cameroun)
        // via `smsRestClient`, authentifié avec `smsProperties.getApiKey()`.
        log.warn("[SMS] Provider '{}' non implémenté — basculement en sandbox pour ne pas bloquer.",
                smsProperties.getProvider());
        log.info("[SANDBOX][SMS] de={} vers={} message=\"{}\"", smsProperties.getSenderId(), phoneNumber, message);
        return true;
    }
}
