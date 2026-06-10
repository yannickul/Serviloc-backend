package com.serviloc.categories.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "service_category") // nom de table plus cohérent
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // interne, clé primaire

    @Column(nullable = false, unique = true)
    private String slug;   // identifiant public exposé comme "id" dans l’API

    @Column(nullable = false)
    private String label;

    private String iconKey;
    private String color;

    private Integer demandCount;
    private Double percentageShare;

    // Constructeurs
    public CategoryJpaEntity() {}

    public CategoryJpaEntity(String slug, String label, String iconKey,
                             String color, Integer demandCount, Double percentageShare) {
        this.slug = slug;
        this.label = label;
        this.iconKey = iconKey;
        this.color = color;
        this.demandCount = demandCount;
        this.percentageShare = percentageShare;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

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
