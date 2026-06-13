package com.serviloc.negociations.infrastructure.persistence;

import com.serviloc.negociations.domain.model.ConversationStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations",
        indexes = {
                @Index(name = "idx_conv_client_id",   columnList = "client_id"),
                @Index(name = "idx_conv_provider_id", columnList = "provider_id"),
                @Index(name = "idx_conv_demand_id",   columnList = "demand_id"),
                @Index(name = "idx_conv_last_msg",    columnList = "last_message_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conv_client_provider_demand",
                        columnNames = {"client_id", "provider_id", "demand_id"})
        })
@EntityListeners(AuditingEntityListener.class)
public class ConversationJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "client_id", nullable = false, columnDefinition = "uuid")
    private UUID clientId;

    @Column(name = "provider_id", nullable = false, columnDefinition = "uuid")
    private UUID providerId;

    @Column(name = "demand_id", nullable = false, columnDefinition = "uuid")
    private UUID demandId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "unread_count_client", nullable = false)
    private int unreadCountClient = 0;

    @Column(name = "unread_count_provider", nullable = false)
    private int unreadCountProvider = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ConversationJpaEntity() {}

    public ConversationJpaEntity(UUID id, UUID clientId, UUID providerId,
                                 UUID demandId, ConversationStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.providerId = providerId;
        this.demandId = demandId;
        this.status = status;
        this.lastMessageAt = LocalDateTime.now();
    }

    public UUID getId()                      { return id; }
    public UUID getClientId()                { return clientId; }
    public UUID getProviderId()              { return providerId; }
    public UUID getDemandId()                { return demandId; }
    public ConversationStatus getStatus()    { return status; }
    public void setStatus(ConversationStatus s) { this.status = s; }
    public LocalDateTime getLastMessageAt()  { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime t) { this.lastMessageAt = t; }
    public int getUnreadCountClient()        { return unreadCountClient; }
    public void setUnreadCountClient(int c)  { this.unreadCountClient = c; }
    public int getUnreadCountProvider()      { return unreadCountProvider; }
    public void setUnreadCountProvider(int c){ this.unreadCountProvider = c; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
}