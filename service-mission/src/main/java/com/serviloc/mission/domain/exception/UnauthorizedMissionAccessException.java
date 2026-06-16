package com.serviloc.mission.domain.exception;

public class UnauthorizedMissionAccessException extends RuntimeException {
    public UnauthorizedMissionAccessException(String userId, String missionId) {

        super("L'utilisateur " + userId + " n'a pas accès à la mission " + missionId);
    }
}
