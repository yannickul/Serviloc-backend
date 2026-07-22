// application/dto/response/LitigeMetrics.java
package com.serviloc.litiges.application.dto.response;

import java.math.BigDecimal;

public record LitigeMetrics(
        long open,
        long inProgress,
        long resolvedThisMonth,
        BigDecimal totalBlockedAmount
) {}
