// domain/repository/ResolutionRepository.java
package com.serviloc.litiges.domain.repository;

import com.serviloc.litiges.domain.model.Resolution;

import java.util.Optional;

public interface ResolutionRepository {
    Resolution save(Resolution resolution);
    Optional<Resolution> findByLitigeId(String litigeId);
}