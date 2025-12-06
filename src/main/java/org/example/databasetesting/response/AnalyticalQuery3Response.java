package org.example.databasetesting.response;

public class AnalyticalQuery3Response {
    private String categoryName;
    private Long totalSold;
    private Double totalRevenue;

    public AnalyticalQuery3Response() {
    }

    public AnalyticalQuery3Response(String categoryName, Long totalSold, Double totalRevenue) {
        this.categoryName = categoryName;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(Long totalSold) {
        this.totalSold = totalSold;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}

