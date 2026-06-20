package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.OtpCode;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpCode.Purpose purpose = OtpCode.Purpose.REGISTRATION;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean used = false;

    protected OtpCodeJpaEntity() {}

    public OtpCodeJpaEntity(UUID id, UUID userId, String code,
                            OtpCode.Purpose purpose, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
    }

    public UUID getId()                       { return id; }
    public UUID getUserId()                   { return userId; }
    public String getCode()                   { return code; }
    public OtpCode.Purpose getPurpose()       { return purpose; }
    public LocalDateTime getExpiresAt()       { return expiresAt; }
    public int getAttempts()                  { return attempts; }
    public void setAttempts(int a)            { this.attempts = a; }
    public boolean isUsed()                   { return used; }
    public void setUsed(boolean u)            { this.used = u; }
}