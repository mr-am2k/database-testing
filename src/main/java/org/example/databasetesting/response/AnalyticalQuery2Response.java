package org.example.databasetesting.response;

public class AnalyticalQuery2Response {
    private Double medianBidsPerUser;
    private Double p90BidsPerUser;
    private Long maxBidsByAUser;

    public AnalyticalQuery2Response() {
    }

    public AnalyticalQuery2Response(Double medianBidsPerUser, Double p90BidsPerUser, Long maxBidsByAUser) {
        this.medianBidsPerUser = medianBidsPerUser;
        this.p90BidsPerUser = p90BidsPerUser;
        this.maxBidsByAUser = maxBidsByAUser;
    }

    public Double getMedianBidsPerUser() {
        return medianBidsPerUser;
    }

    public void setMedianBidsPerUser(Double medianBidsPerUser) {
        this.medianBidsPerUser = medianBidsPerUser;
    }

    public Double getP90BidsPerUser() {
        return p90BidsPerUser;
    }

    public void setP90BidsPerUser(Double p90BidsPerUser) {
        this.p90BidsPerUser = p90BidsPerUser;
    }

    public Long getMaxBidsByAUser() {
        return maxBidsByAUser;
    }

    public void setMaxBidsByAUser(Long maxBidsByAUser) {
        this.maxBidsByAUser = maxBidsByAUser;
    }

    @Override
    public String toString() {
        return "AnalyticalQuery2Response{" +
                "medianBidsPerUser=" + medianBidsPerUser +
                ", p90BidsPerUser=" + p90BidsPerUser +
                ", maxBidsByAUser=" + maxBidsByAUser +
                '}';
    }
}
