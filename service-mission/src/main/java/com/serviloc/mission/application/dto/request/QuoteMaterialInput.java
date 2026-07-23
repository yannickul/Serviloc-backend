// QuoteMaterialInput.java — partagé entre Create et Update
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class QuoteMaterialInput {
    @NotBlank(message = "La désignation du matériau est obligatoire")
    private String designation;
    @NotNull @Min(1)
    private Integer quantity;
    @NotNull
    private BigDecimal unitPrice;

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
