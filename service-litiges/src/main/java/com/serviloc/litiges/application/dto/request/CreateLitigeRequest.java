// application/dto/request/CreateLitigeRequest.java
package com.serviloc.litiges.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateLitigeRequest(
        @NotBlank String demandId,
        @NotBlank String missionId,
        @NotBlank String clientId,
        @NotBlank String providerId,
        @NotBlank String motifId,
        @NotBlank String description,
        List<String> evidenceIds,
        @NotNull BigDecimal amount,
        @NotBlank String transactionId
) {}