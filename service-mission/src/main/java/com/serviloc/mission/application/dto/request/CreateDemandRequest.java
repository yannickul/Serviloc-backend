// application/dto/request/CreateDemandRequest.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateDemandRequest {

    @NotBlank(message = "La catégorie est obligatoire")
    private String categoryId;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La localisation est obligatoire")
    @Valid
    private LocationDto location;

    private boolean isUrgent;

    @NotNull(message = "Le budget estimé est obligatoire")
    @Valid
    private BudgetRangeDto estimatedBudget;

    private List<String> photoIds;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocationDto getLocation() { return location; }
    public void setLocation(LocationDto location) { this.location = location; }
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    public BudgetRangeDto getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(BudgetRangeDto estimatedBudget) { this.estimatedBudget = estimatedBudget; }
    public List<String> getPhotoIds() { return photoIds; }
    public void setPhotoIds(List<String> photoIds) { this.photoIds = photoIds; }
}