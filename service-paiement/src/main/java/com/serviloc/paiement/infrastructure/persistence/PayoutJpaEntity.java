package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.Payout;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payouts",
        indexes = {
                @Index(name = "idx_payout_provider_id",    columnList = "provider_id"),
                @Index(name = "idx_payout_mission_id",     columnList = "mission_id"),
                @Index(name = "idx_payout_transaction_id", columnList = "transaction_id", unique = true)
        })
@EntityListeners(AuditingEntityListener.class)
public class PayoutJpaEntity {

    @Id @Column(columnDefinition = "uuid")                              private UUID id;
    @Column(name = "reference", length = 40)                            private String reference;
    @Column(name = "transaction_id", nullable = false, columnDefinition = "uuid") private UUID transactionId;
    @Column(name = "mission_id", columnDefinition = "uuid")             private UUID missionId;
    @Column(name = "provider_id",    nullable = false, columnDefinition = "uuid") private UUID providerId;
    @Column(nullable = false)                                           private double amount;
    @Column(name = "commission_amount", nullable = false)               private double commissionAmount;
    @Column(name = "net_amount", nullable = false)                      private double netAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)                              private Payout.PayoutStatus status;
    @Column(name = "external_ref", length = 100)                        private String externalRef;
    @CreatedDate
    @Column(nullable = false, updatable = false)                        private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(nullable = false)                                           private LocalDateTime updatedAt;

    protected PayoutJpaEntity() {}

    public PayoutJpaEntity(UUID id, String reference, UUID transactionId, UUID missionId, UUID providerId,
                           double amount, double commissionAmount, double netAmount,
                           Payout.PayoutStatus status) {
        this.id = id; this.reference = reference; this.transactionId = transactionId;
        this.missionId = missionId;
        this.providerId = providerId; this.amount = amount;
        this.commissionAmount = commissionAmount; this.netAmount = netAmount;
        this.status = status;
    }

    public UUID getId()                       { return id; }
    public String getReference()              { return reference; }
    public UUID getTransactionId()            { return transactionId; }
    public UUID getMissionId()                { return missionId; }
    public UUID getProviderId()               { return providerId; }
    public double getAmount()                 { return amount; }
    public double getCommissionAmount()       { return commissionAmount; }
    public double getNetAmount()              { return netAmount; }
    public Payout.PayoutStatus getStatus()    { return status; }
    public void setStatus(Payout.PayoutStatus s) { this.status = s; }
    public String getExternalRef()            { return externalRef; }
    public void setExternalRef(String r)      { this.externalRef = r; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
}
