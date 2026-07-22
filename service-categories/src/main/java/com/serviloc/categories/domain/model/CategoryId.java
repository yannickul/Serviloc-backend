package com.serviloc.categories.domain.model;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Identifiant fort d'une catégorie de service.
 * Format attaché au contrat API : "cat_" + slug du label (ex: "cat_plomberie").
 */
public record CategoryId(String value) {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");

    public CategoryId {
        Objects.requireNonNull(value, "CategoryId ne peut pas être null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("CategoryId ne peut pas être vide");
        }
    }

    /**
     * Génère un CategoryId à partir du label métier (ex: "Plomberie" -> "cat_plomberie").
     */
    public static CategoryId fromLabel(String label) {
        Objects.requireNonNull(label, "label ne peut pas être null");
        String normalized = Normalizer.normalize(label.trim().toLowerCase(Locale.FRENCH), Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        String slug = NON_ALNUM.matcher(normalized).replaceAll("_").replaceAll("^_+|_+$", "");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Impossible de générer un identifiant à partir du label fourni");
        }
        return new CategoryId("cat_" + slug);
    }

    public static CategoryId of(String rawId) {
        return new CategoryId(rawId);
    }

    @Override
    public String toString() {
        return value;
    }
}
