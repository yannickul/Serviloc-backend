package com.serviloc.utilisateurs.application.service;

import com.serviloc.utilisateurs.application.dto.AdminDtos.*;
import com.serviloc.utilisateurs.application.dto.ProfileDtos.AgentProfileResponse;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.domain.exception.EmailAlreadyExistsException;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.AgentProfile;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.domain.repository.AgentProfileRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import com.serviloc.utilisateurs.infrastructure.messaging.UserEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminAgentService {

    private static final Logger log = LoggerFactory.getLogger(AdminAgentService.class);

    private final UserRepository userRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final UserEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public AdminAgentService(UserRepository userRepository,
                             AgentProfileRepository agentProfileRepository,
                             UserEventPublisher eventPublisher,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── POST /admin/agents ───────────────────────────────────────

    public AgentProfileResponse createAgent(CreateAgentRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Mot de passe provisoire — sera changé au premier login
        String tempPassword = generateTempPassword();

        User user = User.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(tempPassword),
                request.phone(),
                UserRole.AGENT
        );
        user.activate(); // Agent actif immédiatement
        User savedUser = userRepository.save(user);

        // Génération du code agent auto : AGT-XXX
        String agentCode = generateAgentCode();
        AgentProfile profile = AgentProfile.create(
                savedUser.getId(), agentCode, request.department()
        );
        AgentProfile savedProfile = agentProfileRepository.save(profile);

        // Event → Service Notifications envoie email avec mot de passe provisoire
        eventPublisher.publishAgentCreated(
                savedUser.getId(), savedUser.getEmail(),
                agentCode, tempPassword
        );

        log.info("[ADMIN] Agent créé : userId={} agentCode={}", savedUser.getId(), agentCode);

        return UserResponseMapper.toAgentProfile(
                savedUser, savedProfile.getAgentCode(),
                savedProfile.getDepartment(), 0
        );
    }

    // ─── GET /admin/agents ────────────────────────────────────────

    @Transactional(readOnly = true)
    public AgentListResponse getAgents(int page, int limit) {
        // Stub S2 — pagination complète en S3
        List<AgentProfileResponse> agents = List.of();
        PageMeta meta = new PageMeta(page, limit, 0, 0);
        return new AgentListResponse(agents, meta);
    }

    // ─── GET /admin/agents/:id ────────────────────────────────────

    @Transactional(readOnly = true)
    public AgentProfileResponse getAgent(UUID agentUserId) {
        User user = userRepository.findById(agentUserId)
                .orElseThrow(() -> new UserNotFoundException("Agent introuvable"));

        AgentProfile profile = agentProfileRepository.findByUserId(agentUserId)
                .orElseThrow(() -> new UserNotFoundException("Profil agent introuvable"));

        return UserResponseMapper.toAgentProfile(
                user, profile.getAgentCode(),
                profile.getDepartment(), profile.getAssignedLitigesCount()
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private String generateAgentCode() {
        int count = agentProfileRepository.countActiveAgents() + 1;
        return String.format("AGT-%03d", count);
    }

    private String generateTempPassword() {
        // Mot de passe provisoire : Serviloc@XXXX (4 chiffres aléatoires)
        int suffix = (int) (Math.random() * 9000) + 1000;
        return "Serviloc@" + suffix;
    }
}