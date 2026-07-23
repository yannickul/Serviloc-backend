package com.serviloc.mission.application.dto.response;

public class PhotoDto {
    private String id;
    private String url;
    private String name;

    public PhotoDto(String id, String url, String name) {
        this.id = id;
        this.url = url;
        this.name = name;
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getName() { return name; }
}