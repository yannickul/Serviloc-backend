// infrastructure/external/adapter/UserProfilePortAdapter.java
package com.serviloc.litiges.infrastructure.external.adapter;

import com.serviloc.litiges.application.port.out.UserProfilePort;
import com.serviloc.litiges.infrastructure.external.UtilisateurClient;
import com.serviloc.litiges.infrastructure.external.dto.SuspendUserRequest;
import com.serviloc.litiges.infrastructure.external.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfilePortAdapter implements UserProfilePort {

    private final UtilisateurClient utilisateurClient;

    @Override
    public UserProfileDto getProfile(String userId) {
        if (userId == null) return null;
        try {
            return utilisateurClient.getUserProfile(userId);
        } catch (Exception e) {
            log.warn("[FEIGN] Profil indisponible pour userId={}", userId);
            return UserProfileDto.unknown(userId);
        }
    }

    @Override
    public void suspend(String userId, String reason) {
        utilisateurClient.suspendUser(userId, new SuspendUserRequest(reason));
    }
}
