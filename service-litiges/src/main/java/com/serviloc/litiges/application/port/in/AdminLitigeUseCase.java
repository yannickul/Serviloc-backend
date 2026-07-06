// application/port/in/AdminLitigeUseCase.java
package com.serviloc.litiges.application.port.in;

import com.serviloc.litiges.application.dto.request.AssignLitigeRequest;
import com.serviloc.litiges.application.dto.request.ResolveRequest;
import com.serviloc.litiges.application.dto.response.LitigeDetailResponse;
import com.serviloc.litiges.application.dto.response.LitigeListResponse;
import com.serviloc.litiges.domain.model.LitigeStatus;

public interface AdminLitigeUseCase {
    LitigeListResponse getLitiges(int page, int limit, LitigeStatus status, String agentId);
    LitigeDetailResponse getLitigeById(String id);
    void assignLitige(String litigeId, AssignLitigeRequest request);
    void resolveLitige(String litigeId, ResolveRequest request, String agentId);
}