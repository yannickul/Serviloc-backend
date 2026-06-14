package com.serviloc.paiement.infrastructure.persistence;

import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commission_config")
@EntityListeners(AuditingEntityListener.class)
public class CommissionConfigJpaEntity {

    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "standard_rate", nullable = false) private double standardRate;
    @Column(name = "urgency_rate",  nullable = false) private double urgencyRate;
    @Column(name = "created_at")                      private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")                      private LocalDateTime updatedAt;

    protected CommissionConfigJpaEntity() {}

    public CommissionConfigJpaEntity(UUID id, double standardRate,
                                     double urgencyRate, LocalDateTime createdAt) {
        this.id = id; this.standardRate = standardRate;
        this.urgencyRate = urgencyRate; this.createdAt = createdAt;
    }

    public UUID getId()               { return id; }
    public double getStandardRate()   { return standardRate; }
    public void setStandardRate(double r) { this.standardRate = r; }
    public double getUrgencyRate()    { return urgencyRate; }
    public void setUrgencyRate(double r)  { this.urgencyRate = r; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}