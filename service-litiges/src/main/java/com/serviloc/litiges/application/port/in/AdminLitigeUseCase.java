// application/port/in/AdminLitigeUseCase.java
package com.serviloc.litiges.application.port.in;

import com.serviloc.litiges.application.dto.request.AssignLitigeRequest;
import com.serviloc.litiges.application.dto.request.ReassignLitigeRequest;
import com.serviloc.litiges.application.dto.response.LitigeDetailResponse;
import com.serviloc.litiges.application.dto.response.LitigeListResponse;
import com.serviloc.litiges.application.dto.response.StatsResponse;
import com.serviloc.litiges.domain.model.LitigeStatus;

public interface AdminLitigeUseCase {
    LitigeListResponse getLitiges(int page, int limit, LitigeStatus status, String agentId);
    LitigeDetailResponse getLitigeById(String id);
    com.serviloc.litiges.application.dto.response.AssignResponse assignLitige(String litigeId, AssignLitigeRequest request);
    com.serviloc.litiges.application.dto.response.AssignResponse reassignLitige(String litigeId, ReassignLitigeRequest request);
    StatsResponse getStats();
}
