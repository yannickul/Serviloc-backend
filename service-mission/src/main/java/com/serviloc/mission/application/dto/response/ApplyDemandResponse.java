// application/dto/response/ApplyDemandResponse.java
package com.serviloc.mission.application.dto.response;

public record ApplyDemandResponse(
        String demandId,
        String status,
        String message
) {}
