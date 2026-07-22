// application/port/out/UserProfilePort.java
package com.serviloc.litiges.application.port.out;

import com.serviloc.litiges.infrastructure.external.dto.UserProfileDto;

public interface UserProfilePort {
    UserProfileDto getProfile(String userId);
    void suspend(String userId, String reason);
}
