package com.serviloc.negociations.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UtilisateursClientFallback implements UtilisateursClient {

    private static final Logger log = LoggerFactory.getLogger(UtilisateursClientFallback.class);

    @Override
    public UserSummaryResponse getUserById(String id, String internalToken) {
        log.warn("[FEIGN FALLBACK] Service Utilisateurs indisponible pour id={}", id);
        return new UserSummaryResponse(
                id, null, null, null, "Utilisateur", null, null, "?",
                null, null, null, null
        );
    }
}
