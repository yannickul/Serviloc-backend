package com.serviloc.mission.domain.model;


public class MissionStep {
    private String id;
    private String missionId;
    private String label;
    private boolean completed;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}