package com.serviloc.negociations.infrastructure.external;

import com.serviloc.negociations.adapter.rest.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Client interne vers service-fichiers — résolution des imageId en imageUrl
 * pour l'affichage des images dans le chat (audit front, point 2).
 * Contrat : service-fichiers-API_CONTRACT.md section 6. Réponses enveloppées
 * {success,data,meta} — voir section 3 du contrat.
 */
@FeignClient(name = "service-fichiers", fallback = FichiersClientFallback.class)
public interface FichiersClient {

    @GetMapping("/internal/files/{id}/url")
    ApiResponse<FileUrlResponse> getUrl(
            @PathVariable("id") String id,
            @RequestHeader("X-Internal-Token") String internalToken
    );

    @PostMapping("/internal/files/batch/urls")
    ApiResponse<BatchUrlsResponse> batchUrls(
            @RequestBody BatchUrlsRequest request,
            @RequestHeader("X-Internal-Token") String internalToken
    );

    record FileUrlResponse(String id, String url) {}

    record BatchUrlsRequest(List<String> ids) {}

    /** urls: map id -> url. Les ids inconnus sont silencieusement absents (voir contrat). */
    record BatchUrlsResponse(Map<String, String> urls) {}
}
