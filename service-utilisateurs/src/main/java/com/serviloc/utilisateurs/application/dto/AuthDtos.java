package com.serviloc.utilisateurs.application.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "Mot de passe minimum 8 caractères") String password,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numéro invalide") String phone,
            @NotBlank @Pattern(regexp = "CLIENT|PROVIDER", message = "Rôle doit être CLIENT ou PROVIDER") String role
    ) {}

    public record VerifyOtpRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 6) String code
    ) {}

    public record ResendOtpRequest(
            @NotBlank @Email String email
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

    public record LogoutRequest(
            @NotBlank String refreshToken
    ) {}


    public record RegisterResponse(
            String userId,
            String email,
            String message
    ) {}

    public record VerifyOtpResponse(
            String message
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            String role
    ) {}

    public record MessageResponse(
            String message
    ) {}
}