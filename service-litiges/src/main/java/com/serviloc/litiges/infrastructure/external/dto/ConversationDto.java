// infrastructure/external/dto/ConversationDto.java
package com.serviloc.litiges.infrastructure.external.dto;

import java.util.List;

public record ConversationDto(
        String id,
        String demandId,
        List<MessageDto> messages
) {
    // Retourné par le fallback NegociationClient quand le service est indisponible
    public static ConversationDto empty(String demandId) {
        return new ConversationDto(null, demandId, List.of());
    }
}