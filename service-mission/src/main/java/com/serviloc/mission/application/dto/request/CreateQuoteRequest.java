// CreateQuoteRequest.java (public) — champs laborDescription/laborAmount (alignés sur le modèle Quote frontend)
package com.serviloc.mission.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CreateQuoteRequest {
    @NotNull(message = "Le montant de main d'œuvre est obligatoire")
    private BigDecimal laborAmount;
    @NotBlank(message = "La description de la main d'œuvre est obligatoire")
    private String laborDescription;
    @Valid
    private List<QuoteMaterialInput> materials;
    @NotNull(message = "La durée estimée est obligatoire")
    @Min(value = 1, message = "La durée estimée doit être positive")
    private Integer estimatedDurationHours;
    @NotNull(message = "validityDays est obligatoire")
    @Min(value = 1, message = "validityDays doit être >= 1")
    private Integer validityDays;

    public BigDecimal getLaborAmount() { return laborAmount; }
    public void setLaborAmount(BigDecimal laborAmount) { this.laborAmount = laborAmount; }
    public String getLaborDescription() { return laborDescription; }
    public void setLaborDescription(String laborDescription) { this.laborDescription = laborDescription; }
    public List<QuoteMaterialInput> getMaterials() { return materials; }
    public void setMaterials(List<QuoteMaterialInput> materials) { this.materials = materials; }
    public Integer getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(Integer estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }
}
