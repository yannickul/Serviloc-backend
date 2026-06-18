package com.serviloc.mission.domain.exception;

public class UnauthorizedMissionAccessException extends RuntimeException {
    public UnauthorizedMissionAccessException(String userId, String missionId) {

        super("L'utilisateur " + userId + " n'a pas accès à la mission " + missionId);
    }

    public UnauthorizedMissionAccessException(String userId, String resourceId, String resourceType) {
        super(String.format("L'utilisateur %s n'a pas accès à la %s %s",
                userId, resourceType, resourceId));
    }

}
