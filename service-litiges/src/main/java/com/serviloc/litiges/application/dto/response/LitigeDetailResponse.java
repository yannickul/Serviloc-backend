// application/dto/response/LitigeDetailResponse.java
package com.serviloc.litiges.application.dto.response;

import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;

public record LitigeDetailResponse(
        LitigeResponse litige,
        ConversationDto conversation
) {}