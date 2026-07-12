package com.serviloc.mission.application.dto.response;

public class RatingResponse {
    private String ratingId;
    private String targetId;
    private String targetRole;
    private int rating;

    public RatingResponse(String ratingId, String targetId, String targetRole, int rating) {
        this.ratingId = ratingId;
        this.targetId = targetId;
        this.targetRole = targetRole;
        this.rating = rating;
    }

    public String getRatingId() { return ratingId; }
    public String getTargetId() { return targetId; }
    public String getTargetRole() { return targetRole; }
    public int getRating() { return rating; }
}