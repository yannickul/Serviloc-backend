package com.serviloc.categories.domain.model;

/**
 * Fourchette budgétaire indicative associée à une catégorie (en FCFA).
 */
public record BudgetRange(int min, int max) {

    public BudgetRange {
        if (min < 0) {
            throw new IllegalArgumentException("Le budget minimum ne peut pas être négatif");
        }
        if (max < min) {
            throw new IllegalArgumentException("Le budget maximum doit être supérieur ou égal au minimum");
        }
    }
}
