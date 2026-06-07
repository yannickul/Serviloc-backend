package com.serviloc.utilisateurs.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OtpCode {

    private final UUID id;
    private final UUID userId;
    private final String code;
    private final LocalDateTime expiresAt;
    private int attempts;
    private boolean used;

    public static OtpCode create(UUID userId, String code, int validityMinutes) {
        return new OtpCode(UUID.randomUUID(), userId, code,
                LocalDateTime.now().plusMinutes(validityMinutes), 0, false);
    }

    private OtpCode(UUID id, UUID userId, String code,
                    LocalDateTime expiresAt, int attempts, boolean used) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.used = used;
    }


    public boolean isValid(String inputCode) {
        if (used)                                    return false;
        if (LocalDateTime.now().isAfter(expiresAt))  return false;
        if (attempts >= 5)                           return false;
        return this.code.equals(inputCode);
    }

    public void incrementAttempts() { this.attempts++; }
    public void markUsed()          { this.used = true; }

    // ─── Getters ──────────────────────────────────────────────────
    public UUID getId()                 { return id; }
    public UUID getUserId()             { return userId; }
    public String getCode()             { return code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getAttempts()            { return attempts; }
    public boolean isUsed()             { return used; }
}