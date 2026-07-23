// infrastructure/external/dto/MessageDto.java
package com.serviloc.litiges.infrastructure.external.dto;

import java.time.Instant;

public record MessageDto(
        String id,
        String senderId,
        String senderRole,
        String content,
        Instant sentAt
) {}