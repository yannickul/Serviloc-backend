// application/dto/request/BudgetRangeDto.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BudgetRangeDto {

    @NotNull(message = "Le budget minimum est obligatoire")
    @DecimalMin(value = "0", message = "Le budget minimum doit être positif")
    private BigDecimal min;

    @NotNull(message = "Le budget maximum est obligatoire")
    @DecimalMin(value = "0", message = "Le budget maximum doit être positif")
    private BigDecimal max;

    public BigDecimal getMin() { return min; }
    public void setMin(BigDecimal min) { this.min = min; }
    public BigDecimal getMax() { return max; }
    public void setMax(BigDecimal max) { this.max = max; }
}