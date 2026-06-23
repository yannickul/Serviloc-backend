package com.serviloc.utilisateurs.application.service;

import com.serviloc.utilisateurs.application.dto.AuthDtos.*;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.domain.exception.EmailAlreadyExistsException;
import com.serviloc.utilisateurs.domain.exception.InvalidOtpException;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.OtpCode;
import com.serviloc.utilisateurs.domain.model.RefreshToken;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.domain.repository.OtpRepository;
import com.serviloc.utilisateurs.domain.repository.RefreshTokenRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import com.serviloc.utilisateurs.infrastructure.config.JwtService;
import com.serviloc.utilisateurs.infrastructure.messaging.UserEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository,
                       OtpRepository otpRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.eventPublisher = eventPublisher;
    }

    // ─── Register ─────────────────────────────────────────────────

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.phone(),
                UserRole.valueOf(request.role().toUpperCase())
        );
        User saved = userRepository.save(user);

        // OTP mock — code fixe 123456 en dev (S1)
        otpRepository.deleteByUserId(saved.getId());
        OtpCode otp = OtpCode.create(saved.getId(), "123456", 10);
        otpRepository.save(otp);

        log.info("[AUTH] Inscription : userId={} email={}", saved.getId(), saved.getEmail());

        eventPublisher.publishUserRegistered(
                saved.getId(), saved.getEmail(), saved.getRole().name(), saved.getPhone());

        return new RegisterResponse(
                saved.getId().toString(),
                saved.getEmail(),
                "Compte créé. OTP de test : 123456"
        );
    }

    // ─── Verify OTP ───────────────────────────────────────────────

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(
                        "Utilisateur introuvable : " + request.email()));

        OtpCode otp = otpRepository.findLatestByUserId(user.getId())
                .orElseThrow(() -> new InvalidOtpException(
                        "Aucun OTP trouvé pour cet utilisateur"));

        if (!otp.isValid(request.code())) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            throw new InvalidOtpException("OTP invalide ou expiré");
        }

        otp.markUsed();
        otpRepository.save(otp);
        user.activate();
        userRepository.save(user);

        log.info("[AUTH] Compte activé : userId={}", user.getId());
        return new VerifyOtpResponse("Compte activé avec succès");
    }

    // ─── Resend OTP ───────────────────────────────────────────────

    public VerifyOtpResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(
                        "Utilisateur introuvable : " + request.email()));

        otpRepository.deleteByUserId(user.getId());
        OtpCode otp = OtpCode.create(user.getId(), "123456", 10);
        otpRepository.save(otp);

        log.info("[AUTH] OTP renvoyé : userId={}", user.getId());
        return new VerifyOtpResponse("OTP renvoyé. Code de test : 123456");
    }

    // ─── Login ────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (!user.isActive()) {
            throw new IllegalStateException("Compte non activé. Vérifiez votre OTP.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtService.generateAccessToken(
                userDetails, user.getRole().name(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenRepository.revokeAllByUserId(user.getId());
        RefreshToken rt = RefreshToken.create(
                user.getId(), refreshToken, jwtService.getRefreshTokenExpiration());
        refreshTokenRepository.save(rt);

        log.info("[AUTH] Login réussi : userId={}", user.getId());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                user.getRole().name().toLowerCase(),
                UserResponseMapper.toUserResponse(user)   // ← user complet dans la réponse
        );
    }

    // ─── Refresh ──────────────────────────────────────────────────

    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token invalide"));

        if (!rt.isValid()) {
            throw new IllegalArgumentException("Refresh token expiré ou révoqué");
        }

        User user = userRepository.findById(rt.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateAccessToken(
                userDetails, user.getRole().name(), user.getId().toString());

        return new AuthResponse(
                newAccessToken,
                request.refreshToken(),
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                user.getRole().name().toLowerCase(),
                UserResponseMapper.toUserResponse(user)
        );
    }

    // ─── Logout ───────────────────────────────────────────────────

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.revoke();
            refreshTokenRepository.save(rt);
        });
    }

    // ─── POST /auth/forgot-password ───────────────────────────────

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            otpRepository.deleteByUserIdAndPurpose(user.getId(), OtpCode.Purpose.PASSWORD_RESET);
            OtpCode otp = OtpCode.create(
                    user.getId(), "123456", 10, OtpCode.Purpose.PASSWORD_RESET
            );
            otpRepository.save(otp);
            log.info("[AUTH] Code reset password généré : userId={}", user.getId());
            // TODO : event RabbitMQ pour Service Notifications (envoi email réel)
        });

        // Toujours 200, même si l'email n'existe pas (anti-énumération de comptes)
        return new MessageResponse(
                "Si un compte existe avec cet email, un code de réinitialisation a été envoyé."
        );
    }

// ─── POST /auth/reset-password ────────────────────────────────

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        OtpCode otp = otpRepository
                .findLatestByUserIdAndPurpose(user.getId(), OtpCode.Purpose.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidOtpException(
                        "Aucune demande de réinitialisation trouvée"));

        if (!otp.isValid(request.code())) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            throw new InvalidOtpException("Code invalide ou expiré");
        }

        otp.markUsed();
        otpRepository.save(otp);

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Sécurité : invalide tous les refresh tokens existants
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("[AUTH] Mot de passe réinitialisé : userId={}", user.getId());

        return new MessageResponse("Mot de passe réinitialisé avec succès");
    }
}