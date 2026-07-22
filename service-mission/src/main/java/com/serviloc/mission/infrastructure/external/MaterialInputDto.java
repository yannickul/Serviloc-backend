// MaterialInputDto.java
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;

public record MaterialInputDto(String name, Integer quantity, BigDecimal unitPrice) {}