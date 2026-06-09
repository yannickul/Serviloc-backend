package com.serviloc.utilisateurs.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_user_id", columnList = "user_id"),
                @Index(name = "idx_rt_token",   columnList = "token", unique = true)
        })
public class RefreshTokenJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    protected RefreshTokenJpaEntity() {}

    public RefreshTokenJpaEntity(UUID id, UUID userId, String token, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public UUID getId()                 { return id; }
    public UUID getUserId()             { return userId; }
    public String getToken()            { return token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked()          { return revoked; }
    public void setRevoked(boolean r)   { this.revoked = r; }
}