// infrastructure/external/UtilisateurClient.java
package com.serviloc.litiges.infrastructure.external;

import com.serviloc.litiges.infrastructure.external.dto.SuspendUserRequest;
import com.serviloc.litiges.infrastructure.external.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ⚠️ Routes provisoires (/internal/users/**) — non spécifiées dans l'API_CONTRACT fourni.
 * À aligner avec le vrai contrat interne de service-utilisateurs avant mise en prod.
 */
@FeignClient(name = "service-utilisateurs", fallback = UtilisateurClientFallback.class)
public interface UtilisateurClient {

    @GetMapping("/internal/users/{userId}")
    UserProfileDto getUserProfile(@PathVariable String userId);

    @PatchMapping("/internal/users/{userId}/suspend")
    void suspendUser(@PathVariable String userId, @RequestBody SuspendUserRequest request);
}
