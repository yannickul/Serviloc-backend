package com.serviloc.utilisateurs.infrastructure.persistence;

import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider_profiles",
        indexes = {
                @Index(name = "idx_provider_user_id",   columnList = "user_id", unique = true),
                @Index(name = "idx_provider_specialty",  columnList = "specialty"),
                @Index(name = "idx_provider_available",  columnList = "is_available"),
                @Index(name = "idx_provider_zone_city",  columnList = "service_zone_city")
        })
@EntityListeners(AuditingEntityListener.class)
public class ProviderProfileJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID userId;

    // Coordonnées géographiques de la zone de service
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "service_zone_city", length = 100)
    private String serviceZoneCity;

    @Column(name = "radius_km")
    private double radiusKm = 10.0;

    @Column(length = 100)
    private String specialty;

    @Column(nullable = false)
    private double rating = 0.0;

    @Column(name = "completed_missions", nullable = false)
    private int completedMissions = 0;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = false;

    @Column(name = "hourly_rate", nullable = false)
    private double hourlyRate = 0.0;

    @Column(name = "est_certifie", nullable = false)
    private boolean estCertifie = false;

    // Listes sérialisées en JSON (PostgreSQL TEXT)
    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certificationsJson;

    @Column(name = "document_ids", columnDefinition = "TEXT")
    private String documentIdsJson;

    @Column(name = "weekly_schedule", columnDefinition = "TEXT")
    private String weeklyScheduleJson;

    @Column(name = "monthly_earnings", nullable = false)
    private double monthlyEarnings = 0.0;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected ProviderProfileJpaEntity() {}

    public ProviderProfileJpaEntity(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    // Getters & Setters
    public UUID getId()                          { return id; }
    public UUID getUserId()                      { return userId; }
    public Double getLatitude()                  { return latitude; }
    public void setLatitude(Double lat)          { this.latitude = lat; }
    public Double getLongitude()                 { return longitude; }
    public void setLongitude(Double lng)         { this.longitude = lng; }
    public String getServiceZoneCity()           { return serviceZoneCity; }
    public void setServiceZoneCity(String city)  { this.serviceZoneCity = city; }
    public double getRadiusKm()                  { return radiusKm; }
    public void setRadiusKm(double r)            { this.radiusKm = r; }
    public String getSpecialty()                 { return specialty; }
    public void setSpecialty(String s)           { this.specialty = s; }
    public double getRating()                    { return rating; }
    public void setRating(double r)              { this.rating = r; }
    public int getCompletedMissions()            { return completedMissions; }
    public void setCompletedMissions(int c)      { this.completedMissions = c; }
    public boolean isAvailable()                 { return isAvailable; }
    public void setAvailable(boolean a)          { this.isAvailable = a; }
    public double getHourlyRate()                { return hourlyRate; }
    public void setHourlyRate(double h)          { this.hourlyRate = h; }
    public boolean isEstCertifie()               { return estCertifie; }
    public void setEstCertifie(boolean e)        { this.estCertifie = e; }
    public String getCertificationsJson()        { return certificationsJson; }
    public void setCertificationsJson(String j)  { this.certificationsJson = j; }
    public String getDocumentIdsJson()           { return documentIdsJson; }
    public void setDocumentIdsJson(String j)     { this.documentIdsJson = j; }
    public String getWeeklyScheduleJson()        { return weeklyScheduleJson; }
    public void setWeeklyScheduleJson(String j)  { this.weeklyScheduleJson = j; }
    public double getMonthlyEarnings()           { return monthlyEarnings; }
    public void setMonthlyEarnings(double m)     { this.monthlyEarnings = m; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
}