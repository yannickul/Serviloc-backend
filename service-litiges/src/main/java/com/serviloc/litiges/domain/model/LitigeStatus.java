// domain/model/LitigeStatus.java
package com.serviloc.litiges.domain.model;

public enum LitigeStatus {
    OUVERT,     // Créé, pas encore assigné
    EN_COURS,   // Assigné à un agent
    RESOLU,     // Résolution acceptée et remboursement déclenché
    FERME;      // Fermé automatiquement (payment.released) ou manuellement

    /**
     * Libellé exposé dans l'API, conforme à la section 4 de l'API_CONTRACT
     * (ouvert | assigne | resolu | cloture). L'enum interne garde ses noms
     * historiques (EN_COURS, FERME) — seule la sérialisation change.
     */
    public String toContractLabel() {
        return switch (this) {
            case OUVERT -> "ouvert";
            case EN_COURS -> "assigne";
            case RESOLU -> "resolu";
            case FERME -> "cloture";
        };
    }

    public static LitigeStatus fromContractLabel(String label) {
        if (label == null) return null;
        return switch (label.toLowerCase()) {
            case "ouvert" -> OUVERT;
            case "assigne" -> EN_COURS;
            case "resolu" -> RESOLU;
            case "cloture" -> FERME;
            default -> LitigeStatus.valueOf(label.toUpperCase());
        };
    }
}
