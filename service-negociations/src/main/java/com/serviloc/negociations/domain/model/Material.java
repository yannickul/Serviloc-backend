package com.serviloc.negociations.domain.model;

/**
 * Value Object — matériau inclus dans un devis.
 * Immuable, pas d'identité propre.
 */
public record Material(
        String name,
        int quantity,
        double unitPrice
) {
    public Material {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nom du matériau obligatoire");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantité doit être positive");
        if (unitPrice < 0)
            throw new IllegalArgumentException("Prix unitaire ne peut pas être négatif");
    }

    public double totalPrice() {
        return quantity * unitPrice;
    }
}