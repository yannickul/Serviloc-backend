package com.serviloc.notifications.infrastructure.client;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.serviloc.notifications.application.port.out.EmailSender;
import com.serviloc.notifications.infrastructure.config.EmailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Adapter d'envoi d'emails via l'API Resend.
 *
 * Mode sandbox (RESEND_API_KEY vide) : l'email est uniquement loggé — aucun appel réseau.
 * Mode réel : appel HTTP vers https://api.resend.com/emails avec la clé API.
 *
 * Setup rapide pour la démo :
 *   1. Créer un compte sur https://resend.com (gratuit, 3000 emails/mois)
 *   2. Récupérer la clé API dans Settings → API Keys
 *   3. Définir RESEND_API_KEY dans ton .env
 *   4. Définir EMAIL_FROM=onboarding@resend.dev si pas de domaine vérifié
 *      (Resend autorise l'envoi vers TON adresse de compte avec cet expéditeur en sandbox)
 */
@Component
@EnableConfigurationProperties(EmailProperties.class)
public class ResendEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    private final EmailProperties emailProperties;
    private final Resend resendClient;

    public ResendEmailSender(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
        this.resendClient = emailProperties.isSandbox()
                ? null
                : new Resend(emailProperties.getApiKey());
    }

    @Override
    public boolean send(String to, String subject, String body) {
        if (emailProperties.isSandbox()) {
            log.info("[SANDBOX][EMAIL] de='{}' vers='{}' sujet='{}' corps='{}'",
                    emailProperties.getFrom(), to, subject, body);
            return true;
        }

        try {
            String from = emailProperties.getFromName() + " <" + emailProperties.getFrom() + ">";
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(toHtml(subject, body))
                    .build();

            CreateEmailResponse response = resendClient.emails().send(params);
            log.info("[EMAIL] Envoyé avec succès (id={}) à '{}'", response.getId(), to);
            return true;

        } catch (ResendException e) {
            log.error("[EMAIL] Échec d'envoi vers '{}' : {}", to, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[EMAIL] Erreur inattendue lors de l'envoi vers '{}' : {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Génère un HTML minimal mais propre pour les emails transactionnels ServiLoc.
     * Remplacé plus tard par un vrai template (Thymeleaf ou Freemarker) si besoin.
     */
    private String toHtml(String subject, String body) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"/></head>
                <body style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:24px">
                  <div style="background:#1a56db;padding:16px 24px;border-radius:8px 8px 0 0">
                    <h2 style="color:#fff;margin:0">ServiLoc</h2>
                  </div>
                  <div style="border:1px solid #e5e7eb;border-top:none;padding:24px;border-radius:0 0 8px 8px">
                    <h3 style="color:#111827">%s</h3>
                    <p style="color:#374151;line-height:1.6">%s</p>
                    <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0"/>
                    <p style="font-size:12px;color:#9ca3af">
                      Cet email a été envoyé automatiquement par ServiLoc. Ne pas répondre.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(subject, body.replace("\n", "<br/>"));
    }
}
