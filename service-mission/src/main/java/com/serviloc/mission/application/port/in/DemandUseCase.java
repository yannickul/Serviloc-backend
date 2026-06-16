package com.serviloc.mission.application.port.in;

import com.serviloc.mission.application.dto.request.CreateDemandRequest;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.domain.model.DemandStatus;
import java.util.List;


public interface DemandUseCase {
    DemandResponse createDemand(CreateDemandRequest request, String clientId);
    PagedResponse<DemandResponse> getDemands(String clientId, DemandStatus status, int page, int limit);
    DemandResponse getDemandById(String id, String clientId);
    void cancelDemand(String id, String clientId);
    List<DemandResponse> getOpenDemands(String categoryId);
    PagedResponse<DemandResponse> getAllDemands(DemandStatus status, int page, int limit);
}
