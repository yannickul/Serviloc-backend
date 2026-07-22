// application/dto/response/StatsResponse.java
package com.serviloc.litiges.application.dto.response;

import java.math.BigDecimal;

/** data de GET /admin/litiges/stats. */
public record StatsResponse(
        long open,
        long inProgress,
        long resolvedThisMonth,
        BigDecimal totalBlockedAmount
) {}
