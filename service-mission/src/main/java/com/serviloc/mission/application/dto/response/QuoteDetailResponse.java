// application/dto/response/QuoteDetailResponse.java
package com.serviloc.mission.application.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class QuoteDetailResponse {

    @JsonUnwrapped
    private QuoteResponse quote;
    private ProviderInfo provider;
    private DemandInfo demand;

    public QuoteDetailResponse(QuoteResponse quote, ProviderInfo provider, DemandInfo demand) {
        this.quote = quote;
        this.provider = provider;
        this.demand = demand;
    }

    public QuoteResponse getQuote() { return quote; }
    public ProviderInfo getProvider() { return provider; }
    public DemandInfo getDemand() { return demand; }

    public static class ProviderInfo {
        private String id;
        private String fullName;
        private String avatarInitial;
        private double rating;
        private int missionsCount;

        public ProviderInfo(String id, String fullName, String avatarInitial, double rating, int missionsCount) {
            this.id = id;
            this.fullName = fullName;
            this.avatarInitial = avatarInitial;
            this.rating = rating;
            this.missionsCount = missionsCount;
        }

        public String getId() { return id; }
        public String getFullName() { return fullName; }
        public String getAvatarInitial() { return avatarInitial; }
        public double getRating() { return rating; }
        public int getMissionsCount() { return missionsCount; }
    }

    public static class DemandInfo {
        private String category;
        private String description;

        public DemandInfo(String category, String description) {
            this.category = category;
            this.description = description;
        }

        public String getCategory() { return category; }
        public String getDescription() { return description; }
    }
}
