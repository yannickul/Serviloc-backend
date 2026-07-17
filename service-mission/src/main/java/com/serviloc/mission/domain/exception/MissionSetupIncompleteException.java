// domain/exception/MissionSetupIncompleteException.java
package com.serviloc.mission.domain.exception;

public class MissionSetupIncompleteException extends RuntimeException {
    public MissionSetupIncompleteException(String missionId, String reason) {
        super("La mission " + missionId + " ne peut pas démarrer : " + reason
                + ". Complétez la durée estimée et au moins une étape via POST /provider/missions/:id/steps.");
    }
}