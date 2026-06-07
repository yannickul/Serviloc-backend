package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.OtpCode;
import com.serviloc.utilisateurs.domain.repository.OtpRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class OtpRepositoryAdapter implements OtpRepository {

    private final OtpCodeJpaRepository jpa;

    public OtpRepositoryAdapter(OtpCodeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OtpCode save(OtpCode otp) {
        OtpCodeJpaEntity entity = new OtpCodeJpaEntity(
                otp.getId(), otp.getUserId(), otp.getCode(), otp.getExpiresAt()
        );
        entity.setAttempts(otp.getAttempts());
        entity.setUsed(otp.isUsed());
        OtpCodeJpaEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OtpCode> findLatestByUserId(UUID userId) {
        return jpa.findLatestByUserId(userId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        jpa.deleteByUserId(userId);
    }

    private OtpCode toDomain(OtpCodeJpaEntity e) {
        try {
            var ctor = OtpCode.class.getDeclaredConstructor(
                    UUID.class, UUID.class, String.class,
                    java.time.LocalDateTime.class, int.class, boolean.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getUserId(), e.getCode(),
                    e.getExpiresAt(), e.getAttempts(), e.isUsed()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution OtpCode", ex);
        }
    }
}