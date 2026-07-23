// application/dto/request/AssignLitigeRequest.java
package com.serviloc.litiges.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AssignLitigeRequest(
        @NotBlank String agentId
) {}