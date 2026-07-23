// domain/model/LitigeMessage.java
package com.serviloc.litiges.domain.model;

import java.time.Instant;

public class LitigeMessage {

    private String id;
    private String litigeId;
    private String senderId;
    private String senderRole;  // AGENT | CLIENT | PROVIDER
    private String content;
    private Instant sentAt;

    public LitigeMessage() {}

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