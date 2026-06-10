package com.serviloc.categories.domain.model;

import java.util.Objects;

public class ServiceCategory {

    private final Long id;          // interne, jamais exposé
    private final String slug;      // identifiant public (exposé comme "id" dans l’API)
    private String label;
    private String iconKey;
    private static String color;

    private Integer demandCount;    // stats (admin)
    private Double percentageShare; // stats (admin)

    // Constructeur privé
    private ServiceCategory(Long id, String slug, String label,
                            String iconKey, String color,
                            Integer demandCount, Double percentageShare) {
        this.id = id;
        this.slug = slug;
        this.label = label;
        this.iconKey = iconKey;
        this.color = color;
        this.demandCount = demandCount;
        this.percentageShare = percentageShare;
    }

    /** Création d’une nouvelle catégorie (id interne null, stats null) */
    public static ServiceCategory create(String slug, String label,
                                         String iconKey) {
        return new ServiceCategory(null, slug, label, iconKey, color, null, null);
    }

    /** Reconstitution depuis la base (id connu, stats éventuellement présentes) */
    public static ServiceCategory reconstitute(Long id, String slug, String label,
                                               String iconKey, String color,
                                               Integer demandCount, Double percentageShare) {
        return new ServiceCategory(id, slug, label, iconKey, color, demandCount, percentageShare);
    }

    // Getters
    public Long getId() { return id; }              // interne
    public String getSlug() { return slug; }        // exposé comme "id" dans l’API
    public String getLabel() { return label; }
    public String getIconKey() { return iconKey; }
    public String getColor() { return color; }
    public Integer getDemandCount() { return demandCount; }
    public Double getPercentageShare() { return percentageShare; }

    // Mutation métier
    public void updateDetails(String label, String iconKey, String color) {
        this.label = label;
        this.iconKey = iconKey;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceCategory c)) return false;
        return Objects.equals(slug, c.slug);
    }

    @Override
    public int hashCode() { return Objects.hash(slug); }
}
