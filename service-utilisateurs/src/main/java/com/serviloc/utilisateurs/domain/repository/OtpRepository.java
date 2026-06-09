package com.serviloc.utilisateurs.domain.repository;

import com.serviloc.utilisateurs.domain.model.OtpCode;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository {
    OtpCode save(OtpCode otp);
    Optional<OtpCode> findLatestByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}