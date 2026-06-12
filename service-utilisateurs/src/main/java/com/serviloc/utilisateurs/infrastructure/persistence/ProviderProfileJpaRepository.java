package com.serviloc.utilisateurs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderProfileJpaRepository extends JpaRepository<ProviderProfileJpaEntity, UUID> {

    Optional<ProviderProfileJpaEntity> findByUserId(UUID userId);

    /**
     * Requête Haversine — filtre les prestataires dans un rayon donné.
     * Formule : d = 6371 * acos(cos(lat1)*cos(lat2)*cos(lng2-lng1) + sin(lat1)*sin(lat2))
     */
    @Query("""
        SELECT p FROM ProviderProfileJpaEntity p
        WHERE p.isAvailable = true
        AND p.latitude IS NOT NULL
        AND p.longitude IS NOT NULL
        AND (:specialty IS NULL OR LOWER(p.specialty) = LOWER(:specialty))
        AND p.rating >= :minRating
        AND (:maxRate = 0 OR p.hourlyRate <= :maxRate)
        AND (6371 * acos(
               LEAST(1.0, cos(radians(:lat)) * cos(radians(p.latitude))
               * cos(radians(p.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(p.latitude)))
             )) <= :radiusKm
        ORDER BY p.rating DESC
        """)
    List<ProviderProfileJpaEntity> findAvailableInZone(
            @Param("lat")       double lat,
            @Param("lng")       double lng,
            @Param("radiusKm")  double radiusKm,
            @Param("specialty") String specialty,
            @Param("minRating") double minRating,
            @Param("maxRate")   double maxRate
    );
}