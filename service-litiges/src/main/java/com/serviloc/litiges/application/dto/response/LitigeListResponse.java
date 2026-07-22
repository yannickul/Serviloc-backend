// application/dto/response/LitigeListResponse.java
package com.serviloc.litiges.application.dto.response;

import java.util.List;

/**
 * data de GET /admin/litiges — conforme au contrat : { metrics: {...}, litiges: [...] }.
 * La pagination (page/limit/total/totalPages) part dans le `meta` de l'ApiResponse, pas ici.
 */
public record LitigeListResponse(
        LitigeMetrics metrics,
        List<LitigeResponse> litiges
) {}
