package com.serviloc.categories.domain.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String categoryId) {
        super("Catégorie introuvable : " + categoryId);
    }

    public static CategoryNotFoundException byLabel(String label) {
        return new CategoryNotFoundException("(label=" + label + ")");
    }
}
