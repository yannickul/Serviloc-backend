package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeJpaRepository extends JpaRepository<OtpCodeJpaEntity, UUID> {

    @Query("SELECT o FROM OtpCodeJpaEntity o WHERE o.userId = :userId ORDER BY o.expiresAt DESC LIMIT 1")
    Optional<OtpCodeJpaEntity> findLatestByUserId(UUID userId);

    @Query("""
        SELECT o FROM OtpCodeJpaEntity o
        WHERE o.userId = :userId AND o.purpose = :purpose
        ORDER BY o.expiresAt DESC LIMIT 1
        """)
    Optional<OtpCodeJpaEntity> findLatestByUserIdAndPurpose(UUID userId, OtpCode.Purpose purpose);

    @Modifying
    @Query("DELETE FROM OtpCodeJpaEntity o WHERE o.userId = :userId")
    void deleteByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM OtpCodeJpaEntity o WHERE o.userId = :userId AND o.purpose = :purpose")
    void deleteByUserIdAndPurpose(UUID userId, OtpCode.Purpose purpose);
}