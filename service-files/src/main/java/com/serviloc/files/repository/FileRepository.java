package com.serviloc.files.repository;

import com.serviloc.files.entity.StoredFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<StoredFile, Long> {

    // Recherche exacte par nom de fichier
    StoredFile findByFilename(String filename);

    // Recherche exacte par URL
    StoredFile findByUrl(String url);

    // Recherche paginée par type MIME
    Page<StoredFile> findByContentType(String contentType, Pageable pageable);

    // Recherche paginée par nom partiel (insensible à la casse)
    Page<StoredFile> findByFilenameContainingIgnoreCase(String partialName, Pageable pageable);
}
