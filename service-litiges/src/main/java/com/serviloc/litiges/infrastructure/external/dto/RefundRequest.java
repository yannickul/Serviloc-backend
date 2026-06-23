// infrastructure/external/dto/RefundRequest.java
package com.serviloc.litiges.infrastructure.external.dto;

import java.math.BigDecimal;

public record RefundRequest(
        BigDecimal amount,
        String reason
) {}