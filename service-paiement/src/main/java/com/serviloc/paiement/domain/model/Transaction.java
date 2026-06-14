package com.serviloc.paiement.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final UUID demandId;
    private final UUID clientId;
    private final UUID providerId;
    private final UUID quoteId;
    private double amount;
    private double commissionRate;
    private double commissionAmount;
    private double netAmount;          // amount - commissionAmount
    private TransactionStatus status;
    private String paymentMethod;      // orange_money | mtn_momo
    private String phoneNumber;
    private String externalRef;        // référence Mobile Money
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Transaction create(UUID demandId, UUID clientId, UUID providerId,
                                     UUID quoteId, double amount, String paymentMethod,
                                     String phoneNumber, double commissionRate) {
        if (amount <= 0)
            throw new IllegalArgumentException("Montant doit être positif");

        double commissionAmount = amount * commissionRate / 100;
        double netAmount = amount - commissionAmount;

        return new Transaction(
                UUID.randomUUID(), demandId, clientId, providerId, quoteId,
                amount, commissionRate, commissionAmount, netAmount,
                TransactionStatus.PENDING, paymentMethod, phoneNumber, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Transaction(UUID id, UUID demandId, UUID clientId, UUID providerId,
                        UUID quoteId, double amount, double commissionRate,
                        double commissionAmount, double netAmount,
                        TransactionStatus status, String paymentMethod,
                        String phoneNumber, String externalRef,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.demandId = demandId; this.clientId = clientId;
        this.providerId = providerId; this.quoteId = quoteId;
        this.amount = amount; this.commissionRate = commissionRate;
        this.commissionAmount = commissionAmount; this.netAmount = netAmount;
        this.status = status; this.paymentMethod = paymentMethod;
        this.phoneNumber = phoneNumber; this.externalRef = externalRef;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    // ─── Business methods ─────────────────────────────────────────

    public void confirm(String externalRef) {
        this.status = TransactionStatus.SEQUESTRE;
        this.externalRef = externalRef;
        this.updatedAt = LocalDateTime.now();
    }

    public void release() {
        if (status != TransactionStatus.SEQUESTRE && status != TransactionStatus.LITIGE)
            throw new IllegalStateException("Transaction non libérable");
        this.status = TransactionStatus.LIBERE;
        this.updatedAt = LocalDateTime.now();
    }

    public void refund() {
        this.status = TransactionStatus.REMBOURSE;
        this.updatedAt = LocalDateTime.now();
    }

    public void freeze() {
        if (status != TransactionStatus.SEQUESTRE)
            throw new IllegalStateException("Seule une transaction en séquestre peut être gelée");
        this.status = TransactionStatus.LITIGE;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = TransactionStatus.ECHEC;
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Getters ──────────────────────────────────────────────────
    public UUID getId()                  { return id; }
    public UUID getDemandId()            { return demandId; }
    public UUID getClientId()            { return clientId; }
    public UUID getProviderId()          { return providerId; }
    public UUID getQuoteId()             { return quoteId; }
    public double getAmount()            { return amount; }
    public double getCommissionRate()    { return commissionRate; }
    public double getCommissionAmount()  { return commissionAmount; }
    public double getNetAmount()         { return netAmount; }
    public TransactionStatus getStatus() { return status; }
    public String getPaymentMethod()     { return paymentMethod; }
    public String getPhoneNumber()       { return phoneNumber; }
    public String getExternalRef()       { return externalRef; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
}