// application/port/in/ClientProviderLitigeUseCase.java
package com.serviloc.litiges.application.port.in;

public interface ClientProviderLitigeUseCase {
    void acceptResolution(String litigeId, String userId, String role);
    void rejectResolution(String litigeId, String userId, String role, String reason);
}
