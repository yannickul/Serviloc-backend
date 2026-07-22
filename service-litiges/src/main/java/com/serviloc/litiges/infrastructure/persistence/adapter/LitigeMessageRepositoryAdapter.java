// infrastructure/persistence/adapter/LitigeMessageRepositoryAdapter.java
package com.serviloc.litiges.infrastructure.persistence.adapter;

import com.serviloc.litiges.domain.model.LitigeMessage;
import com.serviloc.litiges.domain.repository.LitigeMessageRepository;
import com.serviloc.litiges.infrastructure.persistence.entity.LitigeMessageJpaEntity;
import com.serviloc.litiges.infrastructure.persistence.repository.LitigeMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LitigeMessageRepositoryAdapter implements LitigeMessageRepository {

    private final LitigeMessageJpaRepository jpaRepository;

    @Override
    public LitigeMessage save(LitigeMessage message) {
        LitigeMessageJpaEntity entity = new LitigeMessageJpaEntity();
        entity.setId(message.getId());
        entity.setLitigeId(message.getLitigeId());
        entity.setSenderId(message.getSenderId());
        entity.setSenderRole(message.getSenderRole());
        entity.setContent(message.getContent());
        entity.setSentAt(message.getSentAt());
        LitigeMessageJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<LitigeMessage> findByLitigeId(String litigeId) {
        return jpaRepository.findByLitigeIdOrderBySentAtAsc(litigeId).stream()
                .map(this::toDomain)
                .toList();
    }

    private LitigeMessage toDomain(LitigeMessageJpaEntity e) {
        LitigeMessage m = new LitigeMessage();
        m.setId(e.getId());
        m.setLitigeId(e.getLitigeId());
        m.setSenderId(e.getSenderId());
        m.setSenderRole(e.getSenderRole());
        m.setContent(e.getContent());
        m.setSentAt(e.getSentAt());
        return m;
    }
}
