package com.serviloc.paiement.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommissionConfig {

    private final UUID id;
    private double standardRate;   // % commission standard
    private double urgencyRate;    // % commission urgence
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommissionConfig createDefault() {
        return new CommissionConfig(
                UUID.randomUUID(), 10.0, 15.0,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private CommissionConfig(UUID id, double standardRate, double urgencyRate,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.standardRate = standardRate;
        this.urgencyRate = urgencyRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(double standardRate, double urgencyRate) {
        if (standardRate < 0 || standardRate > 30)
            throw new IllegalArgumentException("Taux standard entre 0 et 30%");
        if (urgencyRate < 0 || urgencyRate > 30)
            throw new IllegalArgumentException("Taux urgence entre 0 et 30%");
        this.standardRate = standardRate;
        this.urgencyRate = urgencyRate;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId()              { return id; }
    public double getStandardRate()  { return standardRate; }
    public double getUrgencyRate()   { return urgencyRate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}