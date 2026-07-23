// infrastructure/external/UtilisateurClientFallback.java
package com.serviloc.litiges.infrastructure.external;

import com.serviloc.litiges.domain.exception.PaymentServiceUnavailableException;
import com.serviloc.litiges.infrastructure.external.dto.SuspendUserRequest;
import com.serviloc.litiges.infrastructure.external.dto.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UtilisateurClientFallback implements UtilisateurClient {

    @Override
    public UserProfileDto getUserProfile(String userId) {
        log.warn("[FALLBACK] Profil utilisateur indisponible pour userId={}", userId);
        return UserProfileDto.unknown(userId);
    }

    @Override
    public void suspendUser(String userId, SuspendUserRequest request) {
        log.error("[FALLBACK] Suspension impossible pour userId={} — Service Utilisateurs indisponible", userId);
        throw new PaymentServiceUnavailableException(
                "Service Utilisateurs indisponible — suspension différée");
    }
}
