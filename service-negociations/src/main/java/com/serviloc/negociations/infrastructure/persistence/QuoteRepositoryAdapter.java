package com.serviloc.negociations.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.negociations.domain.model.Material;
import com.serviloc.negociations.domain.model.Quote;
import com.serviloc.negociations.domain.model.QuoteStatus;
import com.serviloc.negociations.domain.repository.QuoteRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class QuoteRepositoryAdapter implements QuoteRepository {

    private final QuoteJpaRepository jpa;
    private final ObjectMapper objectMapper;

    public QuoteRepositoryAdapter(QuoteJpaRepository jpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.objectMapper = objectMapper;
    }

    @Override
    public Quote save(Quote q) {
        QuoteJpaEntity entity = jpa.findById(q.getId())
                .orElse(new QuoteJpaEntity(
                        q.getId(), q.getConversationId(), q.getDemandId(),
                        q.getProviderId(), q.getAmount(), q.getDescription(),
                        q.getEstimatedDurationHours(), q.getStatus(), q.getExpiresAt()
                ));
        entity.setStatus(q.getStatus());
        entity.setMaterialsJson(toJson(q.getMaterials()));
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Quote> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Quote> findByDemandId(UUID demandId) {
        return jpa.findByDemandId(demandId).map(this::toDomain);
    }

    @Override
    public List<Quote> findExpiredPending() {
        return jpa.findExpiredByStatus(QuoteStatus.EN_ATTENTE, LocalDateTime.now())
                .stream().map(this::toDomain).toList();
    }

    private Quote toDomain(QuoteJpaEntity e) {
        try {
            var ctor = Quote.class.getDeclaredConstructor(
                    UUID.class, UUID.class, UUID.class, UUID.class,
                    double.class, String.class, List.class, int.class,
                    QuoteStatus.class, LocalDateTime.class,
                    LocalDateTime.class, LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getConversationId(), e.getDemandId(),
                    e.getProviderId(), e.getAmount(), e.getDescription(),
                    fromJson(e.getMaterialsJson()), e.getEstimatedDurationHours(),
                    e.getStatus(), e.getCreatedAt(), e.getExpiresAt(), e.getUpdatedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution Quote", ex);
        }
    }

    private String toJson(List<Material> materials) {
        if (materials == null || materials.isEmpty()) return "[]";
        try { return objectMapper.writeValueAsString(materials); }
        catch (Exception e) { return "[]"; }
    }

    private List<Material> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<List<Material>>() {}); }
        catch (Exception e) { return List.of(); }
    }
}