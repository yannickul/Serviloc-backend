package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.ProviderReview;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider_reviews",
        indexes = {
                @Index(name = "idx_review_agent_id",    columnList = "agent_id"),
                @Index(name = "idx_review_provider_id", columnList = "provider_id")
        })
@EntityListeners(AuditingEntityListener.class)
public class ProviderReviewJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "agent_id", nullable = false, columnDefinition = "uuid")
    private UUID agentId;

    @Column(name = "provider_id", nullable = false, columnDefinition = "uuid")
    private UUID providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderReview.Verdict verdict;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private LocalDateTime reviewedAt;

    protected ProviderReviewJpaEntity() {}

    public ProviderReviewJpaEntity(UUID id, UUID agentId, UUID providerId,
                                   ProviderReview.Verdict verdict, String comment) {
        this.id = id;
        this.agentId = agentId;
        this.providerId = providerId;
        this.verdict = verdict;
        this.comment = comment;
    }

    public UUID getId()                      { return id; }
    public UUID getAgentId()                 { return agentId; }
    public UUID getProviderId()              { return providerId; }
    public ProviderReview.Verdict getVerdict() { return verdict; }
    public String getComment()               { return comment; }
    public LocalDateTime getReviewedAt()     { return reviewedAt; }
}