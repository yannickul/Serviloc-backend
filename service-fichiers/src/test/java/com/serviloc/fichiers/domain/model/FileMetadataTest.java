package com.serviloc.fichiers.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileMetadataTest {

    @Test
    void shouldCreateFileMetadataWithValidData() {
        FileMetadata file = FileMetadata.create(
                "photo.jpg", "image/jpeg", 245_000L,
                "https://cdn.serviloc.cm/uploads/photo_001.jpg", "demande", "usr_abc123");

        assertThat(file.getId()).isNotNull();
        assertThat(file.getOriginalName()).isEqualTo("photo.jpg");
        assertThat(file.getMimeType()).isEqualTo("image/jpeg");
        assertThat(file.getSize()).isEqualTo(245_000L);
        assertThat(file.getUploadedAt()).isNotNull();
    }

    @Test
    void shouldRejectBlankOriginalName() {
        assertThatThrownBy(() -> FileMetadata.create(
                " ", "image/jpeg", 1000L, "https://cdn.serviloc.cm/x.jpg", "demande", "usr_abc123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalName");
    }

    @Test
    void shouldRejectNonPositiveSize() {
        assertThatThrownBy(() -> FileMetadata.create(
                "photo.jpg", "image/jpeg", 0L, "https://cdn.serviloc.cm/x.jpg", "demande", "usr_abc123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }

    @Test
    void shouldRejectBlankUploadedBy() {
        assertThatThrownBy(() -> FileMetadata.create(
                "photo.jpg", "image/jpeg", 1000L, "https://cdn.serviloc.cm/x.jpg", "demande", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uploadedBy");
    }
}
