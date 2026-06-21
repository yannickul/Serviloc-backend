// domain/model/LitigeStatus.java
package com.serviloc.litiges.domain.model;

public enum LitigeStatus {
    OUVERT,     // Créé, pas encore assigné
    EN_COURS,   // Assigné à un agent
    RESOLU,     // Résolution enregistrée et remboursement déclenché
    FERME       // Fermé automatiquement (payment.released) ou manuellement
}