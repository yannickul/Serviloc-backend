// application/dto/response/StepResponse.java
package com.serviloc.mission.application.dto.response;

public class StepResponse {
    private String id;
    private String label;
    private boolean completed;
    private int order;

    public StepResponse(String id, String label, boolean completed, int order) {
        this.id = id;
        this.label = label;
        this.completed = completed;
        this.order = order;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public boolean isCompleted() { return completed; }
    public int getOrder() { return order; }
}