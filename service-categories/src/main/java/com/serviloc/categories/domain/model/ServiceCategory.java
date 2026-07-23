package com.serviloc.categories.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Aggregate root du référentiel des catégories de services.
 * Aucune dépendance Spring / JPA : logique métier pure.
 */
public class ServiceCategory {

    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final CategoryId id;
    private String label;
    private IconKey iconKey;
    private String description;
    private String color;
    private BudgetRange budgetRange;
    private long demandCount;
    private final Instant createdAt;
    private Instant updatedAt;

    private ServiceCategory(CategoryId id, String label, IconKey iconKey, String description, String color,
                             BudgetRange budgetRange, long demandCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.label = label;
        this.iconKey = iconKey;
        this.description = description;
        this.color = color;
        this.budgetRange = budgetRange;
        this.demandCount = demandCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Factory method utilisée à la création (POST /admin/categories). */
    public static ServiceCategory create(String label, IconKey iconKey, String description, String color,
                                          BudgetRange budgetRange) {
        validateLabel(label);
        validateColor(color);
        validateDescription(description);
        Objects.requireNonNull(iconKey, "iconKey est obligatoire");
        Objects.requireNonNull(budgetRange, "budgetRange est obligatoire");
        Instant now = Instant.now();
        return new ServiceCategory(CategoryId.fromLabel(label), label.trim(), iconKey,
                description.trim(), color, budgetRange, 0L, now, now);
    }

    /** Reconstruction depuis la persistance : n'applique pas les invariants de création. */
    public static ServiceCategory reconstitute(CategoryId id, String label, IconKey iconKey, String description,
                                                String color, BudgetRange budgetRange, long demandCount,
                                                Instant createdAt, Instant updatedAt) {
        return new ServiceCategory(id, label, iconKey, description, color, budgetRange, demandCount, createdAt, updatedAt);
    }

    public void rename(String newLabel, IconKey newIconKey, String newDescription, String newColor,
                        BudgetRange newBudgetRange) {
        validateLabel(newLabel);
        validateColor(newColor);
        validateDescription(newDescription);
        Objects.requireNonNull(newIconKey, "iconKey est obligatoire");
        Objects.requireNonNull(newBudgetRange, "budgetRange est obligatoire");
        this.label = newLabel.trim();
        this.iconKey = newIconKey;
        this.description = newDescription.trim();
        this.color = newColor;
        this.budgetRange = newBudgetRange;
        this.updatedAt = Instant.now();
    }

    public void incrementDemandCount() {
        this.demandCount += 1;
        this.updatedAt = Instant.now();
    }

    public double percentageShareOver(long totalDemandAcrossCategories) {
        if (totalDemandAcrossCategories <= 0) {
            return 0.0;
        }
        return (this.demandCount * 100.0) / totalDemandAcrossCategories;
    }

    private static void validateLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Le label de la catégorie est obligatoire");
        }
        if (label.trim().length() > 100) {
            throw new IllegalArgumentException("Le label de la catégorie ne peut pas dépasser 100 caractères");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description de la catégorie est obligatoire");
        }
        if (description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères");
        }
    }

    private static void validateColor(String color) {
        if (color == null || !HEX_COLOR.matcher(color).matches()) {
            throw new IllegalArgumentException("La couleur doit être un code hexadécimal valide (ex: #dbeafe)");
        }
    }

    public CategoryId getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public IconKey getIconKey() {
        return iconKey;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public BudgetRange getBudgetRange() {
        return budgetRange;
    }

    public long getDemandCount() {
        return demandCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceCategory that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
