// domain/repository/LitigeMotifRepository.java
package com.serviloc.litiges.domain.repository;

import com.serviloc.litiges.domain.model.LitigeMotif;

import java.util.Optional;

public interface LitigeMotifRepository {
    Optional<LitigeMotif> findById(String id);
}