package com.serviloc.notifications.domain.model;

/**
 * Statut d'envoi d'une notification, pour l'audit dans {@link NotificationLog}.
 */
public enum NotificationStatus {
    SENT,
    FAILED
}
