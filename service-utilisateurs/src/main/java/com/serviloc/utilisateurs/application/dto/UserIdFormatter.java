package com.serviloc.utilisateurs.application.dto;

import java.util.UUID;


public final class UserIdFormatter {

    private UserIdFormatter() {}

    public static String formatUserId(UUID id) {
        return id.toString();
    }

    public static String formatDemandId(UUID id) {
        return id.toString();
    }

    public static String formatMissionId(UUID id) {
        return id.toString();
    }

    public static String formatLitigeId(UUID id) {
        return id.toString();
    }

    public static String formatTransactionId(UUID id) {
        return id.toString();
    }
}