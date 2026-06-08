package com.serviloc.categories.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "category_jpa_entity")
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String iconKey;
    private String color;
    private Integer demandCount;
    private Double percentageShare;

    // ✅ Constructeurs
    public CategoryJpaEntity() {}

    public CategoryJpaEntity(String label, String iconKey, String color, Integer demandCount, Double percentageShare) {
        this.label = label;
        this.iconKey = iconKey;
        this.color = color;
        this.demandCount = demandCount;
        this.percentageShare = percentageShare;
    }

    // ✅ Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getDemandCount() { return demandCount; }
    public void setDemandCount(Integer demandCount) { this.demandCount = demandCount; }

    public Double getPercentageShare() { return percentageShare; }
    public void setPercentageShare(Double percentageShare) { this.percentageShare = percentageShare; }
}

