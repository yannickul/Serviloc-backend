// application/dto/response/MissionStepResponse.java
package com.serviloc.mission.application.dto.response;

public class MissionStepResponse {

    private String id;
    private String label;
    private boolean completed;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}