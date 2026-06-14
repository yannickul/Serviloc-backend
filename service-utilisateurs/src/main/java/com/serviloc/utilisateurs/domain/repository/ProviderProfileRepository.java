package com.serviloc.utilisateurs.domain.repository;

import com.serviloc.utilisateurs.domain.model.ProviderProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderProfileRepository {
    ProviderProfile save(ProviderProfile profile);
    Optional<ProviderProfile> findByUserId(UUID userId);
    List<ProviderProfile> findAvailableInZone(double lat, double lng,
                                              double radiusKm, String specialty,
                                              double minRating, double maxRate);
}