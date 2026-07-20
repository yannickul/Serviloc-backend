package com.serviloc.negociations.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Quote {

    private final UUID id;
    private final UUID conversationId;
    private final UUID demandId;
    private final UUID providerId;
    private double amount;
    private String description;
    private List<Material> materials;
    private int estimatedDurationHours;
    private int validityDays;
    private QuoteStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime updatedAt;

    public static Quote create(UUID conversationId, UUID demandId,
                               UUID providerId, double amount,
                               String description, List<Material> materials,
                               int estimatedDurationHours, int validityDays) {
        if (amount <= 0)
            throw new IllegalArgumentException("Montant du devis doit être positif");
        if (validityDays <= 0)
            throw new IllegalArgumentException("validityDays doit être >= 1");
        LocalDateTime now = LocalDateTime.now();
        return new Quote(
                UUID.randomUUID(), conversationId, demandId, providerId,
                amount, description, materials, estimatedDurationHours, validityDays,
                QuoteStatus.EN_ATTENTE, now,
                now.plusDays(validityDays), now
        );
    }

    private Quote(UUID id, UUID conversationId, UUID demandId, UUID providerId,
                  double amount, String description, List<Material> materials,
                  int estimatedDurationHours, int validityDays, QuoteStatus status,
                  LocalDateTime createdAt, LocalDateTime expiresAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.demandId = demandId;
        this.providerId = providerId;
        this.amount = amount;
        this.description = description;
        this.materials = materials;
        this.estimatedDurationHours = estimatedDurationHours;
        this.validityDays = validityDays;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
    }

    // ─── Business methods ─────────────────────────────────────────

    /** Modifie le contenu du devis en conservant id/conversationId/providerId/status/createdAt. */
    public void update(double amount, String description, List<Material> materials,
                       int estimatedDurationHours, int validityDays) {
        if (amount <= 0)
            throw new IllegalArgumentException("Montant du devis doit être positif");
        if (validityDays <= 0)
            throw new IllegalArgumentException("validityDays doit être >= 1");
        this.amount = amount;
        this.description = description;
        this.materials = materials;
        this.estimatedDurationHours = estimatedDurationHours;
        this.validityDays = validityDays;
        this.expiresAt = LocalDateTime.now().plusDays(validityDays);
        this.updatedAt = LocalDateTime.now();
    }

    public void accept() {
        if (status != QuoteStatus.EN_ATTENTE)
            throw new IllegalStateException("Devis non en attente");
        this.status = QuoteStatus.ACCEPTE;
        this.updatedAt = LocalDateTime.now();
    }

    public void refuse() {
        this.status = QuoteStatus.REFUSE;
        this.updatedAt = LocalDateTime.now();
    }

    public void resetToWaiting() {
        this.status = QuoteStatus.EN_ATTENTE;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = QuoteStatus.EXPIRE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // ─── Getters ──────────────────────────────────────────────────
    public UUID getId()                      { return id; }
    public UUID getConversationId()          { return conversationId; }
    public UUID getDemandId()                { return demandId; }
    public UUID getProviderId()              { return providerId; }
    public double getAmount()                { return amount; }
    public String getDescription()           { return description; }
    public List<Material> getMaterials()     { return materials; }
    public int getEstimatedDurationHours()   { return estimatedDurationHours; }
    public int getValidityDays()             { return validityDays; }
    public QuoteStatus getStatus()           { return status; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getExpiresAt()      { return expiresAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
}