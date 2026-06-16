package com.serviloc.mission.domain.model;

import java.time.Instant;
import java.util.List;

public class Demand {
    private String id;
    private String clientId;
    private String categoryId;
    private String description;
    private List<String> photoIds;
    private Location location;
    private DemandStatus status;
    private boolean isUrgent;
    private BudgetRange estimatedBudget;
    private String providerId;
    private String quoteId;
    private Instant createdAt;

    // Getters / Setters
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
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public DemandStatus getStatus() { return status; }
    public void setStatus(DemandStatus status) { this.status = status; }
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    public BudgetRange getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(BudgetRange estimatedBudget) { this.estimatedBudget = estimatedBudget; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
