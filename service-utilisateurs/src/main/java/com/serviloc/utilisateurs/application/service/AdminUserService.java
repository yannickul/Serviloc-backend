package com.serviloc.utilisateurs.application.service;

import com.serviloc.utilisateurs.application.dto.AdminDtos.*;
import com.serviloc.utilisateurs.application.dto.AuthDtos.UserResponse;
import com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderProfileResponse;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.domain.repository.ProviderProfileRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import com.serviloc.utilisateurs.infrastructure.messaging.UserEventPublisher;
import com.serviloc.utilisateurs.infrastructure.persistence.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final UserEventPublisher eventPublisher;

    public AdminUserService(UserRepository userRepository,
                            UserJpaRepository userJpaRepository,
                            ProviderProfileRepository providerProfileRepository,
                            UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userJpaRepository = userJpaRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── GET /admin/users ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserListResponse getUsers(String role, String status,
                                     String search, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<?> result = userJpaRepository.findAll(pageable);

        List<UserResponse> users = result.getContent().stream()
                .map(e -> {
                    if (e instanceof com.serviloc.utilisateurs.infrastructure.persistence.UserJpaEntity entity) {
                        return UserResponseMapper.toUserResponse(toDomain(entity));
                    }
                    return null;
                })
                .filter(u -> u != null)
                .toList();

        PageMeta meta = new PageMeta(
                page, limit, result.getTotalElements(), result.getTotalPages()
        );
        return new UserListResponse(users, meta);
    }

    // ─── PATCH /admin/users/:id/suspend ───────────────────────────

    public SuspendResponse suspendUser(UUID userId, SuspendUserRequest request, UUID suspendedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.suspend();
        userRepository.save(user);

        eventPublisher.publishUserSuspended(
                userId, user.getEmail(), suspendedBy, "admin", null);

        log.info("[ADMIN] Utilisateur suspendu : userId={} duration={} suspendedBy={}",
                userId, request.duration(), suspendedBy);

        return new SuspendResponse(
                "usr_" + userId.toString().replace("-", "").substring(0, 8),
                "suspended",
                request.duration(),
                request.reason()
        );
    }

    // ─── PATCH /admin/users/:id/reactivate ────────────────────────

    public UserResponse reactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.reactivate();
        User saved = userRepository.save(user);

        eventPublisher.publishUserReactivated(userId, saved.getEmail());

        log.info("[ADMIN] Utilisateur réactivé : userId={}", userId);

        return UserResponseMapper.toUserResponse(saved);
    }

    // ─── GET /admin/providers ─────────────────────────────────────

    @Transactional(readOnly = true)
    public ProviderListResponse getProviders(String status, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<?> result = userJpaRepository.findByRole(UserRole.PROVIDER, pageable);

        List<ProviderProfileResponse> providers = result.getContent().stream()
                .map(e -> {
                    if (e instanceof com.serviloc.utilisateurs.infrastructure.persistence.UserJpaEntity entity) {
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

    // ─── GET /admin/providers/:id ─────────────────────────────────

    @Transactional(readOnly = true)
    public ProviderProfileResponse getProvider(UUID providerId) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable"));

        return providerProfileRepository.findByUserId(providerId)
                .map(profile -> UserResponseMapper.toProviderProfile(user, profile))
                .orElse(UserResponseMapper.toProviderProfile(user));
    }

    // ─── POST /admin/providers/:id/validate ───────────────────────

    public ProviderActionResponse validateProvider(UUID providerId, UUID decidedBy) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable"));

        eventPublisher.publishProviderValidated(providerId, user.getEmail(), decidedBy);

        log.info("[ADMIN] Prestataire validé : userId={} decidedBy={}", providerId, decidedBy);

        return new ProviderActionResponse(
                "usr_" + providerId.toString().replace("-", "").substring(0, 8),
                "validated",
                "Dossier validé. Le prestataire a été notifié."
        );
    }

    // ─── POST /admin/providers/:id/reject ─────────────────────────

    public ProviderActionResponse rejectProvider(UUID providerId,
                                                 RejectProviderRequest request,
                                                 UUID decidedBy) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable"));

        eventPublisher.publishProviderRejected(providerId, request.reason(), decidedBy);

        log.info("[ADMIN] Prestataire rejeté : userId={} decidedBy={}", providerId, decidedBy);

        return new ProviderActionResponse(
                "usr_" + providerId.toString().replace("-", "").substring(0, 8),
                "rejected",
                "Dossier rejeté. Le prestataire a été notifié."
        );
    }

    // ─── POST /admin/providers/:id/notify ─────────────────────────

    public ProviderActionResponse notifyProvider(UUID providerId,
                                                 NotifyProviderRequest request) {
        User user = userRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable"));

        eventPublisher.publishProviderNotified(providerId, user.getEmail(), request.message());

        return new ProviderActionResponse(
                "usr_" + providerId.toString().replace("-", "").substring(0, 8),
                "notified",
                "Notification envoyée au prestataire."
        );
    }

    // ─── Helper ───────────────────────────────────────────────────

    private User toDomain(com.serviloc.utilisateurs.infrastructure.persistence.UserJpaEntity e) {
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