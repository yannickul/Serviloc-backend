// domain/model/ResolutionType.java
package com.serviloc.litiges.domain.model;

public enum ResolutionType {
    REMBOURSEMENT_TOTAL,
    REMBOURSEMENT_PARTIEL,
    AUCUN_REMBOURSEMENT,
    REJET    // Litige non fondé, clôturé sans action financière
}