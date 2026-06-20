package com.serviloc.utilisateurs.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OtpCode {

    public enum Purpose {
        REGISTRATION,
        PASSWORD_RESET
    }

    private final UUID id;
    private final UUID userId;
    private final String code;
    private final Purpose purpose;
    private int attempts;
    private boolean used;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public static OtpCode create(UUID userId, String code, int validityMinutes) {
        return create(userId, code, validityMinutes, Purpose.REGISTRATION);
    }

    public static OtpCode create(UUID userId, String code, int validityMinutes, Purpose purpose) {
        return new OtpCode(UUID.randomUUID(), userId, code, purpose, 0, false,
                LocalDateTime.now().plusMinutes(validityMinutes),
                LocalDateTime.now());
    }

    private OtpCode(UUID id, UUID userId, String code, Purpose purpose,
                    int attempts, boolean used, LocalDateTime expiresAt,
                    LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.purpose = purpose;
        this.attempts = attempts;
        this.used = used;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public boolean isValid(String inputCode) {
        return !used
                && attempts < 5
                && LocalDateTime.now().isBefore(expiresAt)
                && code.equals(inputCode);
    }

    public void incrementAttempts() { this.attempts++; }
    public void markUsed()          { this.used = true; }

    public UUID getId()              { return id; }
    public UUID getUserId()          { return userId; }
    public String getCode()          { return code; }
    public Purpose getPurpose()      { return purpose; }
    public int getAttempts()         { return attempts; }
    public boolean isUsed()          { return used; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}