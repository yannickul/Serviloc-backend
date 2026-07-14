package com.serviloc.categories.domain.model;

import java.util.Locale;

/**
 * Icônes autorisées pour une catégorie de service.
 * Le contrat API v2.0 documente wrench|bolt|broom|key|brush|plus.
 * "leaf" est ajouté pour couvrir le cas Jardinage (voir exemples API_CONTRACT §8).
 */
public enum IconKey {
    WRENCH,
    BOLT,
    BROOM,
    KEY,
    BRUSH,
    PLUS,
    LEAF;

    public String toWireFormat() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static IconKey fromWireFormat(String wireValue) {
        if (wireValue == null) {
            throw new IllegalArgumentException("iconKey ne peut pas être null");
        }
        try {
            return IconKey.valueOf(wireValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("iconKey invalide : " + wireValue
                    + " (valeurs autorisées : wrench, bolt, broom, key, brush, plus, leaf)");
        }
    }
}
