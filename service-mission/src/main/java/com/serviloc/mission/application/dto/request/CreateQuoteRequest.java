// CreateQuoteRequest.java (public)
package com.serviloc.mission.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CreateQuoteRequest {
    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal amount;
    @NotBlank(message = "La description est obligatoire")
    private String description;
    @Valid
    private List<QuoteMaterialInput> materials;
    @NotNull(message = "La durée estimée est obligatoire")
    @Min(value = 1, message = "La durée estimée doit être positive")
    private Integer estimatedDurationHours;
    @NotNull(message = "validityDays est obligatoire")
    @Min(value = 1, message = "validityDays doit être >= 1")
    private Integer validityDays;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<QuoteMaterialInput> getMaterials() { return materials; }
    public void setMaterials(List<QuoteMaterialInput> materials) { this.materials = materials; }
    public Integer getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(Integer estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }
}