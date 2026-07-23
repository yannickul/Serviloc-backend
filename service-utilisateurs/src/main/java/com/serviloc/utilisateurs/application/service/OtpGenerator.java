package com.serviloc.utilisateurs.application.service;

import java.security.SecureRandom;

/**
 * Génère des codes OTP numériques aléatoires (remplace l'ancien code fixe "123456").
 */
public final class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpGenerator() {}

    /** Génère un code à 6 chiffres, ex. "042837". */
    public static String generate6Digits() {
        int value = RANDOM.nextInt(1_000_000); // 0 → 999999
        return String.format("%06d", value);
    }
}
