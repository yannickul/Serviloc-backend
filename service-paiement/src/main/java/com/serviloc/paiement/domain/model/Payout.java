package com.serviloc.paiement.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payout {

    private final UUID id;
    private final UUID transactionId;
    private final UUID providerId;
    private final double amount;          // netAmount de la transaction
    private final double commissionAmount;
    private PayoutStatus status;
    private String externalRef;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PayoutStatus { PENDING, COMPLETED, FAILED }

    public static Payout create(UUID transactionId, UUID providerId,
                                double amount, double commissionAmount) {
        return new Payout(UUID.randomUUID(), transactionId, providerId,
                amount, commissionAmount, PayoutStatus.PENDING,
                null, LocalDateTime.now(), LocalDateTime.now());
    }

    private Payout(UUID id, UUID transactionId, UUID providerId,
                   double amount, double commissionAmount, PayoutStatus status,
                   String externalRef, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.transactionId = transactionId;
        this.providerId = providerId; this.amount = amount;
        this.commissionAmount = commissionAmount; this.status = status;
        this.externalRef = externalRef;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public void complete(String externalRef) {
        this.status = PayoutStatus.COMPLETED;
        this.externalRef = externalRef;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PayoutStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId()                  { return id; }
    public UUID getTransactionId()       { return transactionId; }
    public UUID getProviderId()          { return providerId; }
    public double getAmount()            { return amount; }
    public double getCommissionAmount()  { return commissionAmount; }
    public PayoutStatus getStatus()      { return status; }
    public String getExternalRef()       { return externalRef; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
}