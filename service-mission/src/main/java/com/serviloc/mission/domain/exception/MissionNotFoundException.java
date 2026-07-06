package com.serviloc.mission.domain.exception;

public class MissionNotFoundException extends RuntimeException {
    public MissionNotFoundException(String missionId) {

        super("la mission " + missionId +" n' existe pas");
    }
}
