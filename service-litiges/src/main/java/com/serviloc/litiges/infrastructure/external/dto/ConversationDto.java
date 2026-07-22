// infrastructure/external/dto/ConversationDto.java
package com.serviloc.litiges.infrastructure.external.dto;

import java.time.Instant;

/** Reflète exactement la réponse de GET /internal/conversations/{demandId} (service-negociations). */
public record ConversationDto(
        String id,
        String demandId,
        String clientId,
        String providerId,
        String status,
        Instant createdAt
) {
    public static ConversationDto empty(String demandId) {
        return new ConversationDto(null, demandId, null, null, null, null);
    }
}
