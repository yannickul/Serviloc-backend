package com.serviloc.utilisateurs.domain.repository;

import com.serviloc.utilisateurs.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserId(UUID userId);
    void deleteExpired();
}