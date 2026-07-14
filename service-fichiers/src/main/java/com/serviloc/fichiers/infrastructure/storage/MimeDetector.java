package com.serviloc.fichiers.infrastructure.storage;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Détecte le type MIME réel d'un fichier en inspectant ses premiers octets
 * (magic bytes) via Apache Tika, indépendamment de l'extension du nom de
 * fichier ou du Content-Type déclaré par le client — exigence explicite du
 * contrat : "Validation MIME côté serveur (pas uniquement l'extension)".
 *
 * Un fichier .exe renommé en "photo.jpg" avec un Content-Type "image/jpeg"
 * forgé par le client sera donc détecté comme "application/x-msdownload"
 * (ou équivalent) et rejeté.
 */
@Component
public class MimeDetector {

    private final Tika tika = new Tika();

    /**
     * @param stream doit supporter mark/reset (utiliser un BufferedInputStream
     *               en amont) — Tika ne consomme que les premiers octets nécessaires.
     */
    public String detect(InputStream stream, String fallbackFileName) {
        try {
            return tika.detect(stream, fallbackFileName);
        } catch (IOException e) {
            throw new StorageException("Impossible de détecter le type du fichier : " + e.getMessage(), e);
        }
    }
}
