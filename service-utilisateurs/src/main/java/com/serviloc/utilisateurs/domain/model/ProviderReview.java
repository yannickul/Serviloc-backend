package com.serviloc.utilisateurs.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité domaine ProviderReview — instruction dossier prestataire par un agent.
 * Zéro import Spring/JPA.
 */
public class ProviderReview {

    public enum Verdict {
        NEEDS_REVISION,  // agent demande des corrections
        APPROVED,        // agent recommande validation
        REJECTED         // agent recommande rejet
    }

    private final UUID id;
    private final UUID agentId;
    private final UUID providerId;
    private final Verdict verdict;
    private final String comment;
    private final LocalDateTime reviewedAt;

    public static ProviderReview create(UUID agentId, UUID providerId,
                                        Verdict verdict, String comment) {
        if (verdict == null)
            throw new IllegalArgumentException("Verdict obligatoire");
        if (comment == null || comment.isBlank())
            throw new IllegalArgumentException("Commentaire obligatoire");

        return new ProviderReview(UUID.randomUUID(), agentId, providerId,
                verdict, comment, LocalDateTime.now());
    }

    private ProviderReview(UUID id, UUID agentId, UUID providerId,
                           Verdict verdict, String comment,
                           LocalDateTime reviewedAt) {
        this.id = id;
        this.agentId = agentId;
        this.providerId = providerId;
        this.verdict = verdict;
        this.comment = comment;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId()                  { return id; }
    public UUID getAgentId()             { return agentId; }
    public UUID getProviderId()          { return providerId; }
    public Verdict getVerdict()          { return verdict; }
    public String getComment()           { return comment; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}