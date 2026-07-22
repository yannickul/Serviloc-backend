// application/dto/response/ResolutionDto.java
package com.serviloc.litiges.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ResolutionDto(
        String type,
        BigDecimal refundAmount,
        String note,
        Boolean clientAccepted,
        Boolean providerAccepted,
        Instant createdAt
) {}