// infrastructure/external/ProviderSummary.java
package com.serviloc.mission.infrastructure.external;

public class ProviderSummary {

    private String id;
    private String fullName;
    private String specialty;
    private double rating;
    private int hourlyRate;
    private double distanceKm;
    private boolean isAvailable;
    private int completedMissions;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(int hourlyRate) { this.hourlyRate = hourlyRate; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public int getCompletedMissions() { return completedMissions; }
    public void setCompletedMissions(int completedMissions) { this.completedMissions = completedMissions; }
}