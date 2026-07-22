// application/dto/response/ValidateMissionResponse.java
package com.serviloc.mission.application.dto.response;

import java.math.BigDecimal;

public class ValidateMissionResponse {
    private String missionId;
    private String validatedBy;
    private boolean bothValidated;
    private String paymentStatus;
    private BigDecimal releasedAmount;

    public ValidateMissionResponse(String missionId, String validatedBy, boolean bothValidated,
                                   String paymentStatus, BigDecimal releasedAmount) {
        this.missionId = missionId;
        this.validatedBy = validatedBy;
        this.bothValidated = bothValidated;
        this.paymentStatus = paymentStatus;
        this.releasedAmount = releasedAmount;
    }

    public String getMissionId() { return missionId; }
    public String getValidatedBy() { return validatedBy; }
    public boolean isBothValidated() { return bothValidated; }
    public String getPaymentStatus() { return paymentStatus; }
    public BigDecimal getReleasedAmount() { return releasedAmount; }
}