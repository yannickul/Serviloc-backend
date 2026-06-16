// application/dto/response/MissionResponse.java
package com.serviloc.mission.application.dto.response;

import com.serviloc.mission.application.dto.request.LocationDto;
import java.math.BigDecimal;
import java.time.Instant;

public class MissionResponse {

    private String id;
    private String demandId;
    private String quoteId;
    private String clientId;
    private String providerId;
    private String category;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal sequesteredAmount;
    private String paymentStatus;
    private Instant startedAt;
    private int estimatedDurationHours;
    private Instant completedAt;
    private LocationDto location;

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
    public void setEstimatedDurationHours(int estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public LocationDto getLocation() { return location; }
    public void setLocation(LocationDto location) { this.location = location; }
}