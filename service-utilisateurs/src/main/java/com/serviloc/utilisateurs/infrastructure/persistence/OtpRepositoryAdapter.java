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
                otp.getId(), otp.getUserId(), otp.getCode(),
                otp.getPurpose(), otp.getExpiresAt()
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
    public Optional<OtpCode> findLatestByUserIdAndPurpose(UUID userId, OtpCode.Purpose purpose) {
        return jpa.findLatestByUserIdAndPurpose(userId, purpose).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        jpa.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndPurpose(UUID userId, OtpCode.Purpose purpose) {
        jpa.deleteByUserIdAndPurpose(userId, purpose);
    }

    private OtpCode toDomain(OtpCodeJpaEntity e) {
        try {
            var ctor = OtpCode.class.getDeclaredConstructor(
                    UUID.class, UUID.class, String.class, OtpCode.Purpose.class,
                    int.class, boolean.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getUserId(), e.getCode(), e.getPurpose(),
                    e.getAttempts(), e.isUsed(), e.getExpiresAt(),
                    java.time.LocalDateTime.now() // createdAt non stocké en base actuellement
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution OtpCode", ex);
        }
    }
}