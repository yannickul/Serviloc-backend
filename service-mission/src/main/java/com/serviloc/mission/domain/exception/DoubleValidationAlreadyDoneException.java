package com.serviloc.mission.domain.exception;

public class DoubleValidationAlreadyDoneException extends RuntimeException {

    public DoubleValidationAlreadyDoneException(String missionId, String role) {
        super("La mission " + missionId + " a déjà été validée par " + role);
    }
}
