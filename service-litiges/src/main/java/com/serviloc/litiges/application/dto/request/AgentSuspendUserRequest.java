// application/dto/request/AgentSuspendUserRequest.java
package com.serviloc.litiges.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Body de POST /agent/litiges/:id/suspend-user. userId doit être le client ou le prestataire du litige. */
public record AgentSuspendUserRequest(
        @NotBlank String userId,
        @NotBlank String reason
) {}
