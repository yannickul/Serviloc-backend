// application/dto/request/ResolveRequest.java
package com.serviloc.litiges.application.dto.request;

import com.serviloc.litiges.domain.model.ResolutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ResolveRequest(
        @NotNull ResolutionType type,
        BigDecimal refundAmount,   // null si AUCUN_REMBOURSEMENT ou REJET
        @NotBlank String note
) {}