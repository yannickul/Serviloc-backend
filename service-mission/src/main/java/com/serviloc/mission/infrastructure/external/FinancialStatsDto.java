// FinancialStatsDto.java
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;

public record FinancialStatsDto(
        BigDecimal totalRevenue,
        BigDecimal commissionEarned,
        BigDecimal sequesteredAmount
) {}