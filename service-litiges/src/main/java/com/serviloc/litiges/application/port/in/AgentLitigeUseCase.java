// application/port/in/AgentLitigeUseCase.java
package com.serviloc.litiges.application.port.in;

import com.serviloc.litiges.application.dto.request.AgentSuspendUserRequest;
import com.serviloc.litiges.application.dto.request.ProposeResolutionRequest;
import com.serviloc.litiges.application.dto.request.SendMessageRequest;
import com.serviloc.litiges.application.dto.response.*;
import com.serviloc.litiges.domain.model.LitigeStatus;

import java.util.List;

public interface AgentLitigeUseCase {
    LitigeListResponse getMyLitiges(String agentId, int page, int limit, LitigeStatus status);
    LitigeDetailResponse getLitigeById(String agentId, String litigeId);
    LitigeHistoryResponse getHistory(String agentId, String litigeId);
    List<LitigeMessageResponse> getMessages(String agentId, String litigeId);
    LitigeMessageResponse sendMessage(String agentId, String litigeId, SendMessageRequest request);
    void proposeResolution(String agentId, String litigeId, ProposeResolutionRequest request);
    void updateResolution(String agentId, String litigeId, ProposeResolutionRequest request);
    void closeLitige(String agentId, String litigeId);
    void suspendUser(String agentId, String litigeId, AgentSuspendUserRequest request);
}
