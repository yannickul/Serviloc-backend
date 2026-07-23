// application/dto/request/RejectQuoteRequest.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RejectQuoteRequest {

    @NotBlank(message = "Le quoteId est obligatoire")
    private String quoteId;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
}
