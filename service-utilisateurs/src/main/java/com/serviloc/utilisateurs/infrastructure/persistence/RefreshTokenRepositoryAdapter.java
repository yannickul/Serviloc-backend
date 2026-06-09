package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.RefreshToken;
import com.serviloc.utilisateurs.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpa;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RefreshToken save(RefreshToken rt) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity(
                rt.getId(), rt.getUserId(), rt.getToken(), rt.getExpiresAt()
        );
        if (rt.isRevoked()) entity.setRevoked(true);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpa.findByToken(token).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpa.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteExpired() {
        jpa.deleteExpiredOrRevoked(LocalDateTime.now());
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity e) {
        try {
            var ctor = RefreshToken.class.getDeclaredConstructor(
                    UUID.class, UUID.class, String.class,
                    java.time.LocalDateTime.class, boolean.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getUserId(), e.getToken(),
                    e.getExpiresAt(), e.isRevoked()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution RefreshToken", ex);
        }
    }
}