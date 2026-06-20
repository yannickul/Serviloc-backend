// application/port/in/EvaluationUseCase.java
package com.serviloc.mission.application.port.in;

import com.serviloc.mission.application.dto.request.RateMissionRequest;
import com.serviloc.mission.application.dto.response.ApiResponse;

public interface EvaluationUseCase {
    void rateAsClient(String missionId, String clientId, RateMissionRequest request);
    void rateAsProvider(String missionId, String providerId, RateMissionRequest request);
}