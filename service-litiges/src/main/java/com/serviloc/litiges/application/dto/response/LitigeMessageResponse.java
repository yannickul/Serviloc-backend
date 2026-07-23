// application/dto/response/LitigeMessageResponse.java
package com.serviloc.litiges.application.dto.response;

import java.time.Instant;

public record LitigeMessageResponse(
        String id,
        String litigeId,
        String senderId,
        String senderRole,
        String content,
        Instant sentAt
) {}
