package com.serviloc.files.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stored_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom unique du fichier stocké dans MinIO
    @Column(nullable = false, unique = true)
    private String filename;

    // Type MIME (ex: image/png, text/plain)
    @Column(nullable = false)
    private String contentType;

    // URL ou chemin MinIO (bucket + object)
    @Column(nullable = false)
    private String url;

    // Taille en octets
    private Long sizeBytes;

    // Date de création (utile pour lister/ordonner)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Callback JPA pour initialiser automatiquement createdAt
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
