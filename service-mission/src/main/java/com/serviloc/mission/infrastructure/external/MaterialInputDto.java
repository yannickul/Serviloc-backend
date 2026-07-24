// MaterialInputDto.java
package com.serviloc.mission.infrastructure.external;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
// Dans MaterialInputDto (ou le DTO utilisé pour l'appel Feign vers service-negociations)
public record MaterialInputDto(
        @JsonProperty("name") String name, // Mapper explicitement vers "name"
        Integer quantity,
        BigDecimal unitPrice
) {}
