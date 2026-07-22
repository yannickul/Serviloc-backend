// application/dto/request/ReassignLitigeRequest.java
package com.serviloc.litiges.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Body de PUT /admin/litiges/:id/assign — réassignation d'un litige déjà EN_COURS. */
public record ReassignLitigeRequest(@NotBlank String agentId) {}
