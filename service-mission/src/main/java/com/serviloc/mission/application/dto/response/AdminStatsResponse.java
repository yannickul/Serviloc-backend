// application/dto/response/AdminStatsResponse.java
package com.serviloc.mission.application.dto.response;

public class AdminStatsResponse {
    private MissionsStats missions;
    private FinancialsStats financials;
    private UsersStats users;
    private LitigesStats litiges;

    public MissionsStats getMissions() { return missions; }
    public void setMissions(MissionsStats missions) { this.missions = missions; }
    public FinancialsStats getFinancials() { return financials; }
    public void setFinancials(FinancialsStats financials) { this.financials = financials; }
    public UsersStats getUsers() { return users; }
    public void setUsers(UsersStats users) { this.users = users; }
    public LitigesStats getLitiges() { return litiges; }
    public void setLitiges(LitigesStats litiges) { this.litiges = litiges; }

    public static class MissionsStats {
        private long total;
        private long completed;
        private long inProgress;
        private long cancelled;

        public MissionsStats(long total, long completed, long inProgress, long cancelled) {
            this.total = total;
            this.completed = completed;
            this.inProgress = inProgress;
            this.cancelled = cancelled;
        }

        public long getTotal() { return total; }
        public long getCompleted() { return completed; }
        public long getInProgress() { return inProgress; }
        public long getCancelled() { return cancelled; }
    }

    public static class FinancialsStats {
        private double totalRevenue;
        private int totalSequestre;
        private int totalLibere;
        private int totalEchec;

        public FinancialsStats(double totalRevenue, int totalSequestre, int totalLibere, int totalEchec) {
            this.totalRevenue = totalRevenue;
            this.totalSequestre = totalSequestre;
            this.totalLibere = totalLibere;
            this.totalEchec = totalEchec;
        }

        public double getTotalRevenue() { return totalRevenue; }
        public int getTotalSequestre() { return totalSequestre; }
        public int getTotalLibere() { return totalLibere; }
        public int getTotalEchec() { return totalEchec; }
    }

    public static class UsersStats {
        private long totalClients;
        private long totalProviders;
        private long totalAgents;

        public UsersStats(long totalClients, long totalProviders, long totalAgents) {
            this.totalClients = totalClients;
            this.totalProviders = totalProviders;
            this.totalAgents = totalAgents;
        }

        public long getTotalClients() { return totalClients; }
        public long getTotalProviders() { return totalProviders; }
        public long getTotalAgents() { return totalAgents; }
    }

    public static class LitigesStats {
        private long total;
        private long open;
        private long resolved;

        public LitigesStats(long total, long open, long resolved) {
            this.total = total;
            this.open = open;
            this.resolved = resolved;
        }

        public long getTotal() { return total; }
        public long getOpen() { return open; }
        public long getResolved() { return resolved; }
    }
}