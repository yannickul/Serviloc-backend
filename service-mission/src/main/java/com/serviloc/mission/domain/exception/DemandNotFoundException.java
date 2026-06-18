package com.serviloc.mission.domain.exception;

public class DemandNotFoundException extends RuntimeException {
    public DemandNotFoundException(String demandId) {

        super("La demande " + demandId + " n'existe pas");
    }
}
