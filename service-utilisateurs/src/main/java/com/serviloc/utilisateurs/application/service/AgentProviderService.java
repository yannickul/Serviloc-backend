package com.serviloc.utilisateurs.application.service;

import com.serviloc.utilisateurs.application.dto.AdminDtos.*;
import com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderProfileResponse;
import com.serviloc.utilisateurs.application.dto.UserIdFormatter;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.ProviderReview;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.domain.repository.ProviderProfileRepository;
import com.serviloc.utilisateurs.domain.repository.ProviderReviewRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import com.serviloc.utilisateurs.infrastructure.messaging.UserEventPublisher;
import com.serviloc.utilisateurs.infrastructure.persistence.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AgentProviderService {

    private static final Logger log = LoggerFactory.getLogger(AgentProviderService.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'");

    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ProviderReviewRepository providerReviewRepository;
    private final UserEventPublisher eventPublisher;

    public AgentProviderService(UserRepository userRepository,
                                UserJpaRepository userJpaRepository,
                                ProviderProfileRepository providerProfileRepository,
                                ProviderReviewRepository providerReviewRepository,
                                UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userJpaRepository = userJpaRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.providerReviewRepository = providerReviewRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── GET /agent/providers ─────────────────────────────────────

    @Transactional(readOnly = true)
    public ProviderListResponse getPendingProviders(int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<?> result = userJpaRepository.findByRole(UserRole.PROVIDER, pageable);

        List<ProviderProfileResponse> providers = result.getContent().stream()
                .map(e -> {
                    if (e instanceof com.serviloc.utilisateurs.infrastructure.persistence
                            .UserJpaEntity entity) {
                        User user = toDomain(entity);
                        return providerProfileRepository.findByUserId(user.getId())
                                .map(profile -> UserResponseMapper.toProviderProfile(user, profile))
                                .orElse(UserResponseMapper.toProviderProfile(user));
                    }
                    return null;
                })
                .filter(p -> p != null)
                .toList();

        PageMeta meta = new PageMeta(
                page, limit, result.getTotalElements(), result.getTotalPages()
        );
        return new ProviderListResponse(providers, meta);
    }

    // ─── GET /agent/providers/:id ─────────────────────────────────

    @Transactional(readOnly = true)
    public ProviderProfileResponse getProvider(UUID providerId) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable"));

        if (user.getRole() != UserRole.PROVIDER) {
            throw new IllegalStateException("Cet utilisateur n'est pas un prestataire");
        }

        return providerProfileRepository.findByUserId(providerId)
                .map(profile -> UserResponseMapper.toProviderProfile(user, profile))
                .orElse(UserResponseMapper.toProviderProfile(user));
    }

    // ─── POST /agent/providers/:id/review (UC30) ──────────────────

    public ProviderReviewResponse submitReview(UUID agentId, UUID providerId,
                                               ProviderReviewRequest request) {
        // Vérifie que le prestataire existe
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable"));

        if (provider.getRole() != UserRole.PROVIDER) {
            throw new IllegalStateException("Cet utilisateur n'est pas un prestataire");
        }

        ProviderReview.Verdict verdict = ProviderReview.Verdict.valueOf(
                request.verdict().toUpperCase()
        );

        ProviderReview review = ProviderReview.create(
                agentId, providerId, verdict, request.comment()
        );
        ProviderReview saved = providerReviewRepository.save(review);

        log.info("[AGENT] Review soumise : agentId={} providerId={} verdict={}",
                agentId, providerId, verdict);

        // Émission events selon le verdict
        switch (verdict) {
            case NEEDS_REVISION -> {
                // Notifie le prestataire qu'il doit corriger son dossier
                eventPublisher.publishProviderNeedsRevision(providerId, request.comment());
                log.info("[AGENT] Prestataire notifié révision : providerId={}", providerId);
            }
            case APPROVED, REJECTED -> {
                // Notifie l'admin qu'une instruction a été soumise
                eventPublisher.publishProviderReviewSubmitted(
                        agentId, providerId, verdict.name().toLowerCase());
                log.info("[AGENT] Admin notifié review soumise : verdict={}", verdict);
            }
        }

        return new ProviderReviewResponse(
                UserIdFormatter.formatUserId(saved.getId()),
                UserIdFormatter.formatUserId(agentId),
                UserIdFormatter.formatUserId(providerId),
                saved.getVerdict().name().toLowerCase(),
                saved.getComment(),
                saved.getReviewedAt() != null
                        ? saved.getReviewedAt().format(FORMATTER) : null,
                "Instruction enregistrée. L'administrateur a été notifié."
        );
    }

    // ─── Helper ───────────────────────────────────────────────────

    private User toDomain(
            com.serviloc.utilisateurs.infrastructure.persistence.UserJpaEntity e) {
        try {
            var ctor = User.class.getDeclaredConstructor(
                    UUID.class, String.class, String.class, String.class,
                    String.class, String.class, UserRole.class,
                    User.Status.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getFirstName(), e.getLastName(),
                    e.getEmail(), e.getPassword(), e.getPhone(),
                    e.getRole(), e.getStatus(),
                    e.getCreatedAt(), e.getUpdatedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution User", ex);
        }
    }
}