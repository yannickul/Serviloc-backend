package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.TransactionStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        indexes = {
                @Index(name = "idx_txn_demand_id",   columnList = "demand_id"),
                @Index(name = "idx_txn_client_id",   columnList = "client_id"),
                @Index(name = "idx_txn_provider_id", columnList = "provider_id"),
                @Index(name = "idx_txn_status",      columnList = "status"),
                @Index(name = "idx_txn_quote_id",    columnList = "quote_id", unique = true)
        })
@EntityListeners(AuditingEntityListener.class)
public class TransactionJpaEntity {

    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "demand_id",   nullable = false, columnDefinition = "uuid") private UUID demandId;
    @Column(name = "client_id",   nullable = false, columnDefinition = "uuid") private UUID clientId;
    @Column(name = "provider_id", nullable = false, columnDefinition = "uuid") private UUID providerId;
    @Column(name = "quote_id",    nullable = false, columnDefinition = "uuid") private UUID quoteId;
    @Column(nullable = false)                    private double amount;
    @Column(name = "commission_rate")            private double commissionRate;
    @Column(name = "commission_amount")          private double commissionAmount;
    @Column(name = "net_amount")                 private double netAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)       private TransactionStatus status;
    @Column(name = "payment_method", length = 30) private String paymentMethod;
    @Column(name = "phone_number",   length = 20) private String phoneNumber;
    @Column(name = "external_ref",   length = 100) private String externalRef;
    @CreatedDate
    @Column(nullable = false, updatable = false)  private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(nullable = false)                     private LocalDateTime updatedAt;

    protected TransactionJpaEntity() {}

    public TransactionJpaEntity(UUID id, UUID demandId, UUID clientId, UUID providerId,
                                UUID quoteId, double amount, double commissionRate,
                                double commissionAmount, double netAmount,
                                TransactionStatus status, String paymentMethod,
                                String phoneNumber) {
        this.id = id; this.demandId = demandId; this.clientId = clientId;
        this.providerId = providerId; this.quoteId = quoteId;
        this.amount = amount; this.commissionRate = commissionRate;
        this.commissionAmount = commissionAmount; this.netAmount = netAmount;
        this.status = status; this.paymentMethod = paymentMethod;
        this.phoneNumber = phoneNumber;
    }

    public UUID getId()                   { return id; }
    public UUID getDemandId()             { return demandId; }
    public UUID getClientId()             { return clientId; }
    public UUID getProviderId()           { return providerId; }
    public UUID getQuoteId()              { return quoteId; }
    public double getAmount()             { return amount; }
    public double getCommissionRate()     { return commissionRate; }
    public double getCommissionAmount()   { return commissionAmount; }
    public double getNetAmount()          { return netAmount; }
    public TransactionStatus getStatus()  { return status; }
    public void setStatus(TransactionStatus s) { this.status = s; }
    public String getPaymentMethod()      { return paymentMethod; }
    public String getPhoneNumber()        { return phoneNumber; }
    public String getExternalRef()        { return externalRef; }
    public void setExternalRef(String r)  { this.externalRef = r; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}