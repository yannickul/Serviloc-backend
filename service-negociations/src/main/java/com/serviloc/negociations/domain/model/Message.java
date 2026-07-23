package com.serviloc.negociations.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {

    private final UUID id;
    private final UUID conversationId;
    private final UUID senderId;
    private final String senderRole;   // client | provider | agent
    private final String content;
    private final String imageId;      // null si pas de photo
    private boolean read;
    private boolean deleted;
    private final LocalDateTime sentAt;

    public static Message create(UUID conversationId, UUID senderId,
                                 String senderRole, String content, String imageId) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Contenu du message obligatoire");
        return new Message(UUID.randomUUID(), conversationId, senderId,
                senderRole, content, imageId, false, false, LocalDateTime.now());
    }

    private Message(UUID id, UUID conversationId, UUID senderId,
                    String senderRole, String content, String imageId,
                    boolean read, boolean deleted, LocalDateTime sentAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.content = content;
        this.imageId = imageId;
        this.read = read;
        this.deleted = deleted;
        this.sentAt = sentAt;
    }

    public void markRead() { this.read = true; }

    /** Soft-delete — le message reste en base, mais masqué côté affichage. */
    public void softDelete() { this.deleted = true; }

    public UUID getId()                 { return id; }
    public UUID getConversationId()     { return conversationId; }
    public UUID getSenderId()           { return senderId; }
    public String getSenderRole()       { return senderRole; }
    public String getContent()          { return content; }
    public String getImageId()          { return imageId; }
    public boolean isRead()             { return read; }
    public boolean isDeleted()          { return deleted; }
    public LocalDateTime getSentAt()    { return sentAt; }
}