package com.serviloc.negociations.infrastructure.persistence;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_msg_conv_id", columnList = "conversation_id"),
                @Index(name = "idx_msg_sent_at", columnList = "sent_at")
        })
@EntityListeners(AuditingEntityListener.class)
public class MessageJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "sender_id", nullable = false, columnDefinition = "uuid")
    private UUID senderId;

    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_id", length = 100)
    private String imageId;

    @Column(nullable = false)
    private boolean read = false;

    @CreatedDate
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    protected MessageJpaEntity() {}

    public MessageJpaEntity(UUID id, UUID conversationId, UUID senderId,
                            String senderRole, String content, String imageId) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.content = content;
        this.imageId = imageId;
    }

    public UUID getId()                 { return id; }
    public UUID getConversationId()     { return conversationId; }
    public UUID getSenderId()           { return senderId; }
    public String getSenderRole()       { return senderRole; }
    public String getContent()          { return content; }
    public String getImageId()          { return imageId; }
    public boolean isRead()             { return read; }
    public void setRead(boolean r)      { this.read = r; }
    public LocalDateTime getSentAt()    { return sentAt; }
}