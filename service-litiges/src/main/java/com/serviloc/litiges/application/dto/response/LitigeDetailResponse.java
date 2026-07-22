// application/dto/response/LitigeDetailResponse.java
package com.serviloc.litiges.application.dto.response;

import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import com.serviloc.litiges.infrastructure.external.dto.UserProfileDto;

public record LitigeDetailResponse(
        LitigeResponse litige,
        ConversationDto conversation,
        UserProfileDto client,
        UserProfileDto provider,
        UserProfileDto agent
) {}
