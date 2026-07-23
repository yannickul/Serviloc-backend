// application/service/ClientProviderLitigeService.java
package com.serviloc.litiges.application.service;

import com.serviloc.litiges.application.port.in.ClientProviderLitigeUseCase;
import com.serviloc.litiges.domain.exception.LitigeNotFoundException;
import com.serviloc.litiges.domain.exception.UnauthorizedLitigeAccessException;
import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.Resolution;
import com.serviloc.litiges.domain.repository.LitigeRepository;
import com.serviloc.litiges.domain.repository.ResolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientProviderLitigeService implements ClientProviderLitigeUseCase {

    private final LitigeRepository litigeRepository;
    private final ResolutionRepository resolutionRepository;

    @Override
    @Transactional
    public void acceptResolution(String litigeId, String userId, String role) {
        Resolution resolution = getResolutionForParty(litigeId, userId, role);
        applyDecision(resolution, role, true);
        log.info("[LITIGE] Résolution acceptée — litigeId={} role={} userId={}", litigeId, role, userId);
    }

    @Override
    @Transactional
    public void rejectResolution(String litigeId, String userId, String role, String reason) {
        Resolution resolution = getResolutionForParty(litigeId, userId, role);
        applyDecision(resolution, role, false);
        log.info("[LITIGE] Résolution rejetée — litigeId={} role={} userId={} reason={}", litigeId, role, userId, reason);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Resolution getResolutionForParty(String litigeId, String userId, String role) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new LitigeNotFoundException(litigeId));

        boolean isClient = "CLIENT".equalsIgnoreCase(role) && userId.equals(litige.getClientId());
        boolean isProvider = "PROVIDER".equalsIgnoreCase(role) && userId.equals(litige.getProviderId());
        if (!isClient && !isProvider) {
            throw new UnauthorizedLitigeAccessException(
                    "L'utilisateur " + userId + " n'est pas partie au litige " + litigeId);
        }

        return resolutionRepository.findByLitigeId(litigeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune résolution à valider pour le litige " + litigeId));
    }

    private void applyDecision(Resolution resolution, String role, boolean accepted) {
        if ("CLIENT".equalsIgnoreCase(role)) {
            resolution.setClientAccepted(accepted);
        } else {
            resolution.setProviderAccepted(accepted);
        }
        resolutionRepository.save(resolution);
    }
}
