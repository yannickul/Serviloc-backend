package com.serviloc.files.repository;

import com.serviloc.files.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<StoredFile, Long> {
}
