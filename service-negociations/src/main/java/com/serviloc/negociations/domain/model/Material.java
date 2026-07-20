package com.serviloc.negociations.domain.model;

/**
 * Value Object — matériau inclus dans un devis.
 * Immuable, pas d'identité propre.
 */
public record Material(
        java.util.UUID id,
        String name,
        int quantity,
        double unitPrice
) {
    public Material {
        if (id == null)
            id = java.util.UUID.randomUUID();
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nom du matériau obligatoire");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantité doit être positive");
        if (unitPrice < 0)
            throw new IllegalArgumentException("Prix unitaire ne peut pas être négatif");
    }

    /** Convenience constructor — génère un id si non fourni (création). */
    public Material(String name, int quantity, double unitPrice) {
        this(java.util.UUID.randomUUID(), name, quantity, unitPrice);
    }

    public double totalPrice() {
        return quantity * unitPrice;
    }

    public double subtotal() {
        return totalPrice();
    }
}