package com.serviloc.utilisateurs.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_codes",
        indexes = {
                @Index(name = "idx_otp_user_id", columnList = "user_id")
        })
public class OtpCodeJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean used = false;

    protected OtpCodeJpaEntity() {}

    public OtpCodeJpaEntity(UUID id, UUID userId, String code, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public UUID getId()                 { return id; }
    public UUID getUserId()             { return userId; }
    public String getCode()             { return code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getAttempts()            { return attempts; }
    public void setAttempts(int a)      { this.attempts = a; }
    public boolean isUsed()             { return used; }
    public void setUsed(boolean u)      { this.used = u; }
}