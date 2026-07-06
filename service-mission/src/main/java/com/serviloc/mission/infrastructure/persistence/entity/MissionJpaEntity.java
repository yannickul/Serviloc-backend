// MissionJpaEntity.java
package com.serviloc.mission.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "missions")
public class MissionJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "demand_id", nullable = false, length = 36)
    private String demandId;

    @Column(name = "quote_id", nullable = false, length = 36)
    private String quoteId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "provider_id", nullable = false, length = 36)
    private String providerId;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "sequestered_amount", precision = 15, scale = 2)
    private BigDecimal sequesteredAmount;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "estimated_duration_hours")
    private int estimatedDurationHours;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "location_lat")
    private double locationLat;

    @Column(name = "location_lng")
    private double locationLng;

    @Column(name = "location_address", length = 255)
    private String locationAddress;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MissionStepJpaEntity> steps;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDemandId() { return demandId; }
    public void setDemandId(String demandId) { this.demandId = demandId; }
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getSequesteredAmount() { return sequesteredAmount; }
    public void setSequesteredAmount(BigDecimal sequesteredAmount) { this.sequesteredAmount = sequesteredAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public int getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(int h) { this.estimatedDurationHours = h; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public double getLocationLat() { return locationLat; }
    public void setLocationLat(double locationLat) { this.locationLat = locationLat; }
    public double getLocationLng() { return locationLng; }
    public void setLocationLng(double locationLng) { this.locationLng = locationLng; }
    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
    public List<MissionStepJpaEntity> getSteps() { return steps; }
    public void setSteps(List<MissionStepJpaEntity> steps) { this.steps = steps; }
}