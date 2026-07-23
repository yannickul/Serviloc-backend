package com.serviloc.paiement.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payout {

    private final UUID id;
    private final String reference;
    private final UUID transactionId;
    private final UUID missionId;
    private final UUID providerId;
    private final double amount;          // montant brut de la transaction
    private final double commissionAmount;
    private final double netAmount;       // amount - commissionAmount, versé au prestataire
    private PayoutStatus status;
    private String externalRef;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PayoutStatus { PENDING, COMPLETED, FAILED }

    public static Payout create(UUID transactionId, UUID missionId, UUID providerId,
                                double amount, double commissionAmount, double netAmount) {
        UUID id = UUID.randomUUID();
        return new Payout(id, generateReference(id), transactionId, missionId, providerId,
                amount, commissionAmount, netAmount, PayoutStatus.PENDING,
                null, LocalDateTime.now(), LocalDateTime.now());
    }

    public static String generateReference(UUID id) {
        return "PAY-" + id.toString().substring(0, 8).toUpperCase();
    }

    public Payout(UUID id, String reference, UUID transactionId, UUID missionId, UUID providerId,
                  double amount, double commissionAmount, double netAmount, PayoutStatus status,
                  String externalRef, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.reference = reference != null ? reference : generateReference(id);
        this.transactionId = transactionId; this.missionId = missionId;
        this.providerId = providerId; this.amount = amount;
        this.commissionAmount = commissionAmount; this.netAmount = netAmount;
        this.status = status;
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
    public String getReference()         { return reference; }
    public UUID getTransactionId()       { return transactionId; }
    public UUID getMissionId()           { return missionId; }
    public UUID getProviderId()          { return providerId; }
    public double getAmount()            { return amount; }
    public double getCommissionAmount()  { return commissionAmount; }
    public double getNetAmount()         { return netAmount; }
    public PayoutStatus getStatus()      { return status; }
    public String getExternalRef()       { return externalRef; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
}
