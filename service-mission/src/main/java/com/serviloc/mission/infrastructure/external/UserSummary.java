package com.serviloc.mission.infrastructure.external;

public record UserSummary(
        String id,
        String fullName,
        String phone,
        String role
) {}