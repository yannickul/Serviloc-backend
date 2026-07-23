// application/dto/request/AcceptQuoteRequest.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AcceptQuoteRequest {

    @NotBlank(message = "Le quoteId est obligatoire")
    private String quoteId;

    @NotBlank(message = "Le paymentMethod est obligatoire")
    private String paymentMethod;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}