// application/dto/response/DemandResponse.java
package com.serviloc.mission.application.dto.response;

import com.serviloc.mission.application.dto.request.BudgetRangeDto;
import com.serviloc.mission.application.dto.request.LocationDto;
import java.time.Instant;
import java.util.List;

public class DemandResponse {

    private String id;
    private String clientId;
    private String categoryId;
    private String description;
    private List<String> photoIds;
    private LocationDto location;
    private String status;
    private boolean isUrgent;
    private BudgetRangeDto estimatedBudget;
    private String providerId;
    private String quoteId;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getPhotoIds() { return photoIds; }
    public void setPhotoIds(List<String> photoIds) { this.photoIds = photoIds; }
    public LocationDto getLocation() { return location; }
    public void setLocation(LocationDto location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isUrgent() { return isUrgent; }
    public void setIsUrgent(boolean urgent) { isUrgent = urgent; }
    public BudgetRangeDto getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(BudgetRangeDto estimatedBudget) { this.estimatedBudget = estimatedBudget; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}