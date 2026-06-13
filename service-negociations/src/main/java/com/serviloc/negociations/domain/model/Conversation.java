package com.serviloc.negociations.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Conversation {

    private final UUID id;
    private final UUID clientId;
    private final UUID providerId;
    private final UUID demandId;
    private ConversationStatus status;
    private LocalDateTime lastMessageAt;
    private int unreadCountClient;
    private int unreadCountProvider;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Conversation create(UUID clientId, UUID providerId, UUID demandId) {
        return new Conversation(
                UUID.randomUUID(), clientId, providerId, demandId,
                ConversationStatus.ACTIVE, LocalDateTime.now(),
                0, 0, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Conversation(UUID id, UUID clientId, UUID providerId, UUID demandId,
                         ConversationStatus status, LocalDateTime lastMessageAt,
                         int unreadCountClient, int unreadCountProvider,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.providerId = providerId;
        this.demandId = demandId;
        this.status = status;
        this.lastMessageAt = lastMessageAt;
        this.unreadCountClient = unreadCountClient;
        this.unreadCountProvider = unreadCountProvider;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Business methods ─────────────────────────────────────────

    public void onNewMessage(String senderRole) {
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if ("client".equalsIgnoreCase(senderRole)) {
            this.unreadCountProvider++;
        } else {
            this.unreadCountClient++;
        }
    }

    public void markReadByClient()   { this.unreadCountClient = 0; }
    public void markReadByProvider() { this.unreadCountProvider = 0; }
    public void close()              { this.status = ConversationStatus.CLOSED; }

    // ─── Getters ──────────────────────────────────────────────────
    public UUID getId()                    { return id; }
    public UUID getClientId()              { return clientId; }
    public UUID getProviderId()            { return providerId; }
    public UUID getDemandId()              { return demandId; }
    public ConversationStatus getStatus()  { return status; }
    public LocalDateTime getLastMessageAt(){ return lastMessageAt; }
    public int getUnreadCountClient()      { return unreadCountClient; }
    public int getUnreadCountProvider()    { return unreadCountProvider; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }
}