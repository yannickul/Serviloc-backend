// infrastructure/external/NegociationUpdateQuoteStatusRequest.java
package com.serviloc.mission.infrastructure.external;

public record NegociationUpdateQuoteStatusRequest(String status, String paymentMethod, String phoneNumber) {}