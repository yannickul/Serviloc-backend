package com.serviloc.mission.domain.model;


import java.time.Instant;
import java.util.Map;

public class Evaluation {
    private String id;
    private String missionId;
    private String evaluatorId;
    private String targetId;
    private String targetRole;
    private int rating;
    private Map<String, Integer> criteria;
    private String comment;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }
    public String getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(String evaluatorId) { this.evaluatorId = evaluatorId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public Map<String, Integer> getCriteria() { return criteria; }
    public void setCriteria(Map<String, Integer> criteria) { this.criteria = criteria; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}