// application/port/in/MissionUseCase.java
package com.serviloc.mission.application.port.in;

import com.serviloc.mission.application.dto.response.MissionResponse;
import java.util.List;

public interface MissionUseCase {
    MissionResponse getMissionById(String id, String userId, String role);
    List<MissionResponse> getMissionsByProvider(String providerId);
    List<MissionResponse> getMissionsByClient(String clientId);
}