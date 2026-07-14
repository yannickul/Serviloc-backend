package com.serviloc.mission.application.dto.response;

import com.serviloc.mission.application.dto.request.BudgetRangeDto;
import com.serviloc.mission.application.dto.request.LocationDto;
import java.time.Instant;
import java.util.List;

public class DemandResponse {

    private String id;
    private String clientId;
    private CategoryDto category;
    private String description;
    private List<PhotoDto> photos;
    private LocationDto location;
    private String status;
    private boolean isUrgent;
    private BudgetRangeDto estimatedBudget;
    private String providerId;
    private String quoteId;
    private String missionId;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public CategoryDto getCategory() { return category; }
    public void setCategory(CategoryDto category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<PhotoDto> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDto> photos) { this.photos = photos; }
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
    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}