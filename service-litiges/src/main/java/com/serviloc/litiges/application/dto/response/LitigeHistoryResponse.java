// application/dto/response/LitigeHistoryResponse.java
package com.serviloc.litiges.application.dto.response;

import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;

import java.util.List;

/**
 * data de GET /agent/litiges/:id/history — combine la conversation de négociation
 * (avant litige) et le fil des messages échangés une fois le litige ouvert.
 */
public record LitigeHistoryResponse(
        ConversationDto negotiationConversation,
        List<LitigeMessageResponse> litigeMessages
) {}
