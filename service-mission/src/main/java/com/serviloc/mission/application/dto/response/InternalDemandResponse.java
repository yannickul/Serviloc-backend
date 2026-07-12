// application/dto/response/InternalDemandResponse.java
package com.serviloc.mission.application.dto.response;

public class InternalDemandResponse {
    private String id;
    private String description;
    private String categoryLabel;
    private String status;

    public InternalDemandResponse(String id, String description, String categoryLabel, String status) {
        this.id = id;
        this.description = description;
        this.categoryLabel = categoryLabel;
        this.status = status;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getCategoryLabel() { return categoryLabel; }
    public String getStatus() { return status; }
}