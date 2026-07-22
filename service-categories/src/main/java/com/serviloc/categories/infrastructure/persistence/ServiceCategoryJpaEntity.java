package com.serviloc.categories.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entité JPA pure infrastructure. Les timestamps createdAt/updatedAt sont gérés
 * explicitement par le domaine (ServiceCategory) et reportés tels quels ici par
 * le mapper — pas d'auditing Spring Data pour éviter un double contrôle.
 */
@Entity
@Table(name = "service_categories")
public class ServiceCategoryJpaEntity {

    @Id
    @Column(length = 60, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 100)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon_key", nullable = false, length = 20)
    private IconKeyJpa iconKey;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(name = "demand_count", nullable = false)
    private long demandCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ServiceCategoryJpaEntity() {
        // requis par JPA
    }

    public ServiceCategoryJpaEntity(String id, String label, IconKeyJpa iconKey, String color,
                                     long demandCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.label = label;
        this.iconKey = iconKey;
        this.color = color;
        this.demandCount = demandCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public IconKeyJpa getIconKey() {
        return iconKey;
    }

    public void setIconKey(IconKeyJpa iconKey) {
        this.iconKey = iconKey;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getDemandCount() {
        return demandCount;
    }

    public void setDemandCount(long demandCount) {
        this.demandCount = demandCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Enum de persistance, séparée du domaine (découplage infra / domaine). */
    public enum IconKeyJpa {
        WRENCH, BOLT, BROOM, KEY, BRUSH, PLUS, LEAF
    }
}
