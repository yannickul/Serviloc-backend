// application/port/in/MissionUseCase.java — version complète Sprint 2
package com.serviloc.mission.application.port.in;

import com.serviloc.mission.application.dto.request.AcceptQuoteRequest;
import com.serviloc.mission.application.dto.request.CreateLitigeRequest;
import com.serviloc.mission.application.dto.request.RateMissionRequest;
import com.serviloc.mission.application.dto.response.MissionResponse;

import java.util.List;

public interface MissionUseCase {
    MissionResponse getMissionById(String id, String userId, String role);
    List<MissionResponse> getMissionsByProvider(String providerId);
    List<MissionResponse> getMissionsByClient(String clientId);
    void startMission(String missionId, String providerId);
    void completeMission(String missionId, String providerId);
    void validateMission(String missionId, String clientId);
    void updateStep(String missionId, String stepId, String providerId);
    void rateAsClient(String missionId, String clientId, RateMissionRequest request);
    void rateAsProvider(String missionId, String providerId, RateMissionRequest request);
    void declareLitigeAsClient(String missionId, String clientId, CreateLitigeRequest request);
    void declareLitigeAsProvider(String missionId, String providerId, CreateLitigeRequest request);
}