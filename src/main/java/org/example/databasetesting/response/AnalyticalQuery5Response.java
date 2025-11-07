package org.example.databasetesting.response;

public class AnalyticalQuery5Response {
    private String country;
    private Long orders;

    public AnalyticalQuery5Response() {
    }

    public AnalyticalQuery5Response(String country, Long orders) {
        this.country = country;
        this.orders = orders;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getOrders() {
        return orders;
    }

    public void setOrders(Long orders) {
        this.orders = orders;
    }
}

