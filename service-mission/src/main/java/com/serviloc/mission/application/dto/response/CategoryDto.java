package com.serviloc.mission.application.dto.response;

public class CategoryDto {
    private String id;
    private String label;
    private String iconKey;

    public CategoryDto(String id, String label, String iconKey) {
        this.id = id;
        this.label = label;
        this.iconKey = iconKey;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getIconKey() { return iconKey; }
}