// application/dto/response/LitigeListResponse.java
package com.serviloc.litiges.application.dto.response;

import java.util.List;

public record LitigeListResponse(
        List<LitigeResponse> data,
        int page,
        int limit,
        long total,
        int totalPages
) {}