// infrastructure/external/dto/RefundRequest.java
package com.serviloc.litiges.infrastructure.external.dto;

import java.math.BigDecimal;

/** Body attendu par POST /internal/transactions/{id}/refund (service-paiement) : { amount }. */
public record RefundRequest(BigDecimal amount) {}
