package com.serviloc.categories.domain.exception;

public class DuplicateCategoryLabelException extends RuntimeException {

    public DuplicateCategoryLabelException(String label) {
        super("Une catégorie avec le label '" + label + "' existe déjà");
    }
}
