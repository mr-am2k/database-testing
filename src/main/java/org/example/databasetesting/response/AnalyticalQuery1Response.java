package org.example.databasetesting.response;

public class AnalyticalQuery1Response {
    private Double avgBidsPerProduct;
    private Double medianBidsPerProduct;
    private Double p90BidsPerProduct;

    public AnalyticalQuery1Response() {
    }

    public AnalyticalQuery1Response(Double avgBidsPerProduct, Double medianBidsPerProduct, Double p90BidsPerProduct) {
        this.avgBidsPerProduct = avgBidsPerProduct;
        this.medianBidsPerProduct = medianBidsPerProduct;
        this.p90BidsPerProduct = p90BidsPerProduct;
    }

    public Double getAvgBidsPerProduct() {
        return avgBidsPerProduct;
    }

    public void setAvgBidsPerProduct(Double avgBidsPerProduct) {
        this.avgBidsPerProduct = avgBidsPerProduct;
    }

    public Double getMedianBidsPerProduct() {
        return medianBidsPerProduct;
    }

    public void setMedianBidsPerProduct(Double medianBidsPerProduct) {
        this.medianBidsPerProduct = medianBidsPerProduct;
    }

    public Double getP90BidsPerProduct() {
        return p90BidsPerProduct;
    }

    public void setP90BidsPerProduct(Double p90BidsPerProduct) {
        this.p90BidsPerProduct = p90BidsPerProduct;
    }

    @Override
    public String toString() {
        return "BidStatistics{" +
                "avg_bids_per_product=" + avgBidsPerProduct +
                ", median_bids_per_product=" + medianBidsPerProduct +
                ", p90_bids_per_product=" + p90BidsPerProduct +
                '}';
    }
}
