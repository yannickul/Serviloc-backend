// domain/exception/StepsAlreadyDefinedException.java
package com.serviloc.mission.domain.exception;

public class StepsAlreadyDefinedException extends RuntimeException {
    public StepsAlreadyDefinedException(String missionId) {
        super("Les étapes sont déjà définies pour la mission " + missionId);
    }
}