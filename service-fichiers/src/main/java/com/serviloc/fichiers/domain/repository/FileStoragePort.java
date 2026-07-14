package com.serviloc.fichiers.domain.repository;

import java.io.InputStream;

/**
 * Port de stockage binaire des fichiers (implémenté par l'adapter MinIO en
 * infrastructure/storage). Le domaine ne connaît que la notion abstraite de
 * "stocker un flux d'octets sous une clé et récupérer une URL publique".
 */
public interface FileStoragePort {

    /**
     * Stocke le flux binaire sous la clé objet donnée et retourne l'URL
     * publique de lecture.
     */
    String store(String objectKey, InputStream data, long size, String contentType);

    /**
     * Supprime physiquement l'objet du stockage.
     */
    void delete(String objectKey);
}
