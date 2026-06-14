package com.serviloc.paiement.domain.model;

public enum TransactionStatus {
    PENDING,      // en attente de paiement Mobile Money
    SEQUESTRE,    // paiement confirmé, fonds en séquestre
    LIBERE,       // fonds libérés au prestataire
    REMBOURSE,    // fonds remboursés au client
    LITIGE,       // gelé suite à un litige
    ECHEC         // paiement Mobile Money échoué
}