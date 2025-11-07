package org.example.databasetesting.response;

public class AnalyticalQuery4Response {
    private String userId;
    private String email;
    private Long totalBids;

    public AnalyticalQuery4Response() {
    }

    public AnalyticalQuery4Response(String userId, String email, Long totalBids) {
        this.userId = userId;
        this.email = email;
        this.totalBids = totalBids;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(Long totalBids) {
        this.totalBids = totalBids;
    }
}

