package com.serviloc.utilisateurs.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Entité domaine ProviderProfile — zéro import Spring/JPA.
 * Contient toutes les informations métier du prestataire.
 */
public class ProviderProfile {

    private final UUID id;
    private final UUID userId;
    private String specialty;
    private double rating;
    private int completedMissions;
    private boolean isAvailable;
    private double hourlyRate;
    private String serviceZoneCity;
    private double radiusKm;
    private boolean estCertifie;
    private List<String> certifications;
    private List<String> documentIds;
    private double monthlyEarnings;

    // Horaires hebdomadaires (JSON sérialisé en base)
    private String weeklyScheduleJson;

    public static ProviderProfile create(UUID userId) {
        return new ProviderProfile(
                UUID.randomUUID(), userId,
                null, 0.0, 0, false,
                0.0, null, 10.0, false,
                List.of(), List.of(), 0.0, null
        );
    }

    private ProviderProfile(UUID id, UUID userId, String specialty,
                            double rating, int completedMissions,
                            boolean isAvailable, double hourlyRate,
                            String serviceZoneCity, double radiusKm,
                            boolean estCertifie, List<String> certifications,
                            List<String> documentIds, double monthlyEarnings,
                            String weeklyScheduleJson) {
        this.id = id;
        this.userId = userId;
        this.specialty = specialty;
        this.rating = rating;
        this.completedMissions = completedMissions;
        this.isAvailable = isAvailable;
        this.hourlyRate = hourlyRate;
        this.serviceZoneCity = serviceZoneCity;
        this.radiusKm = radiusKm;
        this.estCertifie = estCertifie;
        this.certifications = certifications;
        this.documentIds = documentIds;
        this.monthlyEarnings = monthlyEarnings;
        this.weeklyScheduleJson = weeklyScheduleJson;
    }

    // ─── Business methods ─────────────────────────────────────────

    public void updateProfile(String specialty, double hourlyRate,
                              String serviceZoneCity, double radiusKm,
                              boolean estCertifie, List<String> certifications,
                              List<String> documentIds) {
        if (specialty != null && !specialty.isBlank()) this.specialty = specialty;
        if (hourlyRate > 0)    this.hourlyRate = hourlyRate;
        if (serviceZoneCity != null && !serviceZoneCity.isBlank()) this.serviceZoneCity = serviceZoneCity;
        if (radiusKm > 0)      this.radiusKm = radiusKm;
        this.estCertifie = estCertifie;
        if (certifications != null) this.certifications = certifications;
        if (documentIds != null)    this.documentIds = documentIds;
    }

    public void updateAvailability(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void updateSchedule(String weeklyScheduleJson) {
        this.weeklyScheduleJson = weeklyScheduleJson;
    }

    public void updateRating(double newRating) {
        if (newRating < 0 || newRating > 5)
            throw new IllegalArgumentException("Note entre 0 et 5");
        this.rating = newRating;
    }

    public void incrementCompletedMissions() { this.completedMissions++; }

    // ─── Getters ──────────────────────────────────────────────────
    public UUID getId()                   { return id; }
    public UUID getUserId()               { return userId; }
    public String getSpecialty()          { return specialty; }
    public double getRating()             { return rating; }
    public int getCompletedMissions()     { return completedMissions; }
    public boolean isAvailable()          { return isAvailable; }
    public double getHourlyRate()         { return hourlyRate; }
    public String getServiceZoneCity()    { return serviceZoneCity; }
    public double getRadiusKm()           { return radiusKm; }
    public boolean isEstCertifie()        { return estCertifie; }
    public List<String> getCertifications()  { return certifications; }
    public List<String> getDocumentIds()     { return documentIds; }
    public double getMonthlyEarnings()    { return monthlyEarnings; }
    public String getWeeklyScheduleJson() { return weeklyScheduleJson; }
}