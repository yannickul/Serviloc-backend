package com.serviloc.fichiers.domain.model;

import java.util.UUID;

/**
 * Identifiant fortement typé d'un {@link FileMetadata}, pour éviter de confondre
 * un UUID de fichier avec un UUID d'utilisateur, de mission, etc.
 */
public record FileId(UUID value) {

    public FileId {
        if (value == null) {
            throw new IllegalArgumentException("FileId ne peut pas être null");
        }
    }

    public static FileId generate() {
        return new FileId(UUID.randomUUID());
    }

    public static FileId of(UUID value) {
        return new FileId(value);
    }

    public static FileId of(String value) {
        return new FileId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
