package com.serviloc.negociations.infrastructure.external;

import com.serviloc.negociations.adapter.rest.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FichiersClientFallback implements FichiersClient {

    private static final Logger log = LoggerFactory.getLogger(FichiersClientFallback.class);

    @Override
    public ApiResponse<FileUrlResponse> getUrl(String id, String internalToken) {
        log.warn("[FEIGN FALLBACK] Service Fichiers indisponible pour id={}", id);
        return ApiResponse.ok(new FileUrlResponse(id, null));
    }

    @Override
    public ApiResponse<BatchUrlsResponse> batchUrls(BatchUrlsRequest request, String internalToken) {
        log.warn("[FEIGN FALLBACK] Service Fichiers indisponible pour {} ids", request.ids().size());
        return ApiResponse.ok(new BatchUrlsResponse(Map.of()));
    }
}
