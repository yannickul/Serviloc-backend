package com.serviloc.notifications.infrastructure.messaging;

import java.util.Map;
import java.util.Optional;

/**
 * Lecture défensive d'un payload d'événement (Map JSON générique) : ne lève jamais d'exception
 * sur un champ absent, retourne {@code null}/{@code Optional.empty()} à la place — les handlers
 * décident eux-mêmes comment réagir (warning + skip, plutôt que de planter le listener).
 */
final class PayloadReader {

    private PayloadReader() {
    }

    static String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    static Optional<String> getOptionalString(Map<String, Object> payload, String key) {
        return Optional.ofNullable(getString(payload, key));
    }

    static long getLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String s) {
            return Long.parseLong(s);
        }
        return 0L;
    }
}
