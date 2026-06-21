// infrastructure/persistence/LitigeMessageJpaEntity.java
package com.serviloc.litiges.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "litige_messages")
public class LitigeMessageJpaEntity {

    @Id
    private String id;
    private String litigeId;
    private String senderId;
    private String senderRole;
    private String content;
    private Instant sentAt;

    public LitigeMessageJpaEntity() {}

    public String getId()         { return id; }
    public String getLitigeId()   { return litigeId; }
    public String getSenderId()   { return senderId; }
    public String getSenderRole() { return senderRole; }
    public String getContent()    { return content; }
    public Instant getSentAt()    { return sentAt; }

    public void setId(String id)               { this.id = id; }
    public void setLitigeId(String litigeId)   { this.litigeId = litigeId; }
    public void setSenderId(String senderId)   { this.senderId = senderId; }
    public void setSenderRole(String role)     { this.senderRole = role; }
    public void setContent(String content)     { this.content = content; }
    public void setSentAt(Instant sentAt)      { this.sentAt = sentAt; }
}