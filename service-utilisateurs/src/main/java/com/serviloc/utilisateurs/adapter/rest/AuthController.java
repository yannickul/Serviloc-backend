package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.AuthDtos.*;
import com.serviloc.utilisateurs.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Inscription, OTP, login, refresh, logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Vérification de l'OTP et activation du compte")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyOtp(request)));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Renvoi d'un nouvel OTP")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.resendOtp(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion — retourne access token + refresh token + profil")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouvellement du token d'accès")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion — invalide le refresh token")
    public ResponseEntity<ApiResponse<MessageResponse>> logout(
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("Déconnexion réussie")));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de réinitialisation de mot de passe")
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.forgotPassword(request)));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialisation du mot de passe avec code OTP")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.resetPassword(request)));
    }
}