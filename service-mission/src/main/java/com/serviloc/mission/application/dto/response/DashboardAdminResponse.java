// application/dto/response/DashboardAdminResponse.java
package com.serviloc.mission.application.dto.response;

public class DashboardAdminResponse {

    private long totalDemands;
    private long openDemands;
    private long inProgressDemands;
    private long completedDemands;
    private long cancelledDemands;
    private long totalMissions;
    private long completedMissions;
    private long disputedMissions;
    private double completionRate;

    public long getTotalDemands() { return totalDemands; }
    public void setTotalDemands(long totalDemands) { this.totalDemands = totalDemands; }
    public long getOpenDemands() { return openDemands; }
    public void setOpenDemands(long openDemands) { this.openDemands = openDemands; }
    public long getInProgressDemands() { return inProgressDemands; }
    public void setInProgressDemands(long inProgressDemands) { this.inProgressDemands = inProgressDemands; }
    public long getCompletedDemands() { return completedDemands; }
    public void setCompletedDemands(long completedDemands) { this.completedDemands = completedDemands; }
    public long getCancelledDemands() { return cancelledDemands; }
    public void setCancelledDemands(long cancelledDemands) { this.cancelledDemands = cancelledDemands; }
    public long getTotalMissions() { return totalMissions; }
    public void setTotalMissions(long totalMissions) { this.totalMissions = totalMissions; }
    public long getCompletedMissions() { return completedMissions; }
    public void setCompletedMissions(long completedMissions) { this.completedMissions = completedMissions; }
    public long getDisputedMissions() { return disputedMissions; }
    public void setDisputedMissions(long disputedMissions) { this.disputedMissions = disputedMissions; }
    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
}