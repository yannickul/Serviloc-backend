// domain/repository/LitigeMessageRepository.java
package com.serviloc.litiges.domain.repository;

import com.serviloc.litiges.domain.model.LitigeMessage;

import java.util.List;

public interface LitigeMessageRepository {
    LitigeMessage save(LitigeMessage message);
    List<LitigeMessage> findByLitigeId(String litigeId);
}
