// application/port/in/LitigeUseCase.java
package com.serviloc.litiges.application.port.in;

import com.serviloc.litiges.application.dto.request.CreateLitigeRequest;
import com.serviloc.litiges.application.dto.response.LitigeResponse;

public interface LitigeUseCase {
    LitigeResponse createLitige(CreateLitigeRequest request);
    LitigeResponse getLitigeById(String id);
}