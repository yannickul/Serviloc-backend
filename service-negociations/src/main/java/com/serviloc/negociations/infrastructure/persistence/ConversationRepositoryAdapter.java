package com.serviloc.negociations.infrastructure.persistence;

import com.serviloc.negociations.domain.model.Conversation;
import com.serviloc.negociations.domain.model.ConversationStatus;
import com.serviloc.negociations.domain.repository.ConversationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository jpa;

    public ConversationRepositoryAdapter(ConversationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Conversation save(Conversation c) {
        ConversationJpaEntity entity = jpa.findById(c.getId())
                .orElse(new ConversationJpaEntity(
                        c.getId(), c.getClientId(), c.getProviderId(),
                        c.getDemandId(), c.getStatus()));

        entity.setStatus(c.getStatus());
        entity.setLastMessageAt(c.getLastMessageAt());
        entity.setUnreadCountClient(c.getUnreadCountClient());
        entity.setUnreadCountProvider(c.getUnreadCountProvider());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByClientIdAndProviderIdAndDemandId(
            UUID clientId, UUID providerId, UUID demandId) {
        return jpa.findByClientIdAndProviderIdAndDemandId(
                clientId, providerId, demandId).map(this::toDomain);
    }

    @Override
    public Page<Conversation> findByClientIdOrderByLastMessageAtDesc(
            UUID clientId, Pageable pageable) {
        return jpa.findByClientIdOrderByLastMessageAtDesc(clientId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Conversation> findByProviderIdOrderByLastMessageAtDesc(
            UUID providerId, Pageable pageable) {
        return jpa.findByProviderIdOrderByLastMessageAtDesc(providerId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByDemandId(UUID demandId) {
        return jpa.findByDemandId(demandId).map(this::toDomain);
    }

    private Conversation toDomain(ConversationJpaEntity e) {
        try {
            var ctor = Conversation.class.getDeclaredConstructor(
                    UUID.class, UUID.class, UUID.class, UUID.class,
                    ConversationStatus.class, java.time.LocalDateTime.class,
                    int.class, int.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getClientId(), e.getProviderId(), e.getDemandId(),
                    e.getStatus(), e.getLastMessageAt(),
                    e.getUnreadCountClient(), e.getUnreadCountProvider(),
                    e.getCreatedAt(), e.getUpdatedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution Conversation", ex);
        }
    }
}