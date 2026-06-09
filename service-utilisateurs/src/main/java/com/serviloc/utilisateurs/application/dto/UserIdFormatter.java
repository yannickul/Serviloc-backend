package com.serviloc.utilisateurs.application.dto;

import java.util.UUID;

/**
 * Formate les UUIDs internes en IDs lisibles selon le contrat API v2.0.
 * Exemples : usr_abc123f, dem_45f2a1bc, mis_9e3d7a2c
 */
public final class UserIdFormatter {

    private UserIdFormatter() {}

    public static String formatUserId(UUID id) {
        return "usr_" + shortId(id);
    }

    public static String formatDemandId(UUID id) {
        return "dem_" + shortId(id);
    }

    public static String formatMissionId(UUID id) {
        return "mis_" + shortId(id);
    }

    public static String formatLitigeId(UUID id) {
        return "lit_" + shortId(id);
    }

    public static String formatTransactionId(UUID id) {
        return "txn_" + shortId(id);
    }

    /**
     * Retourne les 8 premiers caractères de l'UUID sans tirets.
     * UUID "0b937d16-d145-4f1b-..." → "0b937d16"
     */
    private static String shortId(UUID id) {
        return id.toString().replace("-", "").substring(0, 8);
    }
}