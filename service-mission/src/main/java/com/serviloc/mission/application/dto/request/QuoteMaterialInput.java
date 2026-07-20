// QuoteMaterialInput.java — partagé entre Create et Update
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class QuoteMaterialInput {
    @NotBlank(message = "Le nom du matériau est obligatoire")
    private String name;
    @NotNull @Min(1)
    private Integer quantity;
    @NotNull
    private BigDecimal unitPrice;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}