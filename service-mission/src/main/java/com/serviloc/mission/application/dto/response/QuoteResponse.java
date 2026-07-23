package com.serviloc.mission.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class QuoteResponse {
    private String id;
    private String reference;
    private String demandId;
    private String providerId;
    private String clientId;
    private String laborDescription;
    private BigDecimal laborAmount;
    private List<MaterialResponse> materials;
    private BigDecimal materialsTotal;
    private BigDecimal totalAmount;
    private Integer estimatedDurationHours;
    private Integer validityDays;
    private String status;
    private String createdAt;
    private String expiresAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getDemandId() { return demandId; }
    public void setDemandId(String demandId) { this.demandId = demandId; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getLaborDescription() { return laborDescription; }
    public void setLaborDescription(String laborDescription) { this.laborDescription = laborDescription; }
    public BigDecimal getLaborAmount() { return laborAmount; }
    public void setLaborAmount(BigDecimal laborAmount) { this.laborAmount = laborAmount; }
    public List<MaterialResponse> getMaterials() { return materials; }
    public void setMaterials(List<MaterialResponse> materials) { this.materials = materials; }
    public BigDecimal getMaterialsTotal() { return materialsTotal; }
    public void setMaterialsTotal(BigDecimal materialsTotal) { this.materialsTotal = materialsTotal; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(Integer estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public static class MaterialResponse {
        private String id;
        private String designation;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public MaterialResponse(String id, String designation, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.id = id;
            this.designation = designation;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public String getId() { return id; }
        public String getDesignation() { return designation; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}