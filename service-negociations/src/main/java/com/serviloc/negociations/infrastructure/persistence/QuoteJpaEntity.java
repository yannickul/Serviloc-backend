package com.serviloc.negociations.infrastructure.persistence;

import com.serviloc.negociations.domain.model.QuoteStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quotes",
        indexes = {
                @Index(name = "idx_quote_demand_id", columnList = "demand_id"),
                @Index(name = "idx_quote_status",    columnList = "status")
        })
@EntityListeners(AuditingEntityListener.class)
public class QuoteJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "demand_id", nullable = false, columnDefinition = "uuid")
    private UUID demandId;

    @Column(name = "provider_id", nullable = false, columnDefinition = "uuid")
    private UUID providerId;

    @Column(nullable = false)
    private double amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "materials_json", columnDefinition = "TEXT")
    private String materialsJson;

    @Column(name = "estimated_duration_hours")
    private int estimatedDurationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected QuoteJpaEntity() {}

    public QuoteJpaEntity(UUID id, UUID conversationId, UUID demandId,
                          UUID providerId, double amount, String description,
                          int estimatedDurationHours, QuoteStatus status,
                          LocalDateTime expiresAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.demandId = demandId;
        this.providerId = providerId;
        this.amount = amount;
        this.description = description;
        this.estimatedDurationHours = estimatedDurationHours;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public UUID getId()                      { return id; }
    public UUID getConversationId()          { return conversationId; }
    public UUID getDemandId()                { return demandId; }
    public UUID getProviderId()              { return providerId; }
    public double getAmount()                { return amount; }
    public String getDescription()           { return description; }
    public String getMaterialsJson()         { return materialsJson; }
    public void setMaterialsJson(String j)   { this.materialsJson = j; }
    public int getEstimatedDurationHours()   { return estimatedDurationHours; }
    public QuoteStatus getStatus()           { return status; }
    public void setStatus(QuoteStatus s)     { this.status = s; }
    public LocalDateTime getExpiresAt()      { return expiresAt; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
}