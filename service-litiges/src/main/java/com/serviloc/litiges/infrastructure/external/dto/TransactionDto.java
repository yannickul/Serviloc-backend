// infrastructure/external/dto/TransactionDto.java
package com.serviloc.litiges.infrastructure.external.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Reflète la réponse de service-paiement pour freeze/refund sur /internal/transactions/{id}. */
public record TransactionDto(
        String id,
        String demandId,
        String clientId,
        String providerId,
        BigDecimal amount,
        BigDecimal commissionAmount,
        BigDecimal netAmount,
        String status,
        String paymentMethod,
        String externalRef,
        Instant createdAt
) {}
