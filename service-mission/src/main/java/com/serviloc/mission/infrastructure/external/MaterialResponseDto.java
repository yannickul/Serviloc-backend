// MaterialResponseDto.java
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;

public record MaterialResponseDto(String id, String name, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}