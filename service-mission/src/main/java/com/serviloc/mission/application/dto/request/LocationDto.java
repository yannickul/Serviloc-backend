// application/dto/request/LocationDto.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.NotNull;

public class LocationDto {

    @NotNull(message = "La latitude est obligatoire")
    private Double lat;

    @NotNull(message = "La longitude est obligatoire")
    private Double lng;

    private String address;

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}