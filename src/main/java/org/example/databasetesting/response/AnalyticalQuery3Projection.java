package org.example.databasetesting.response;

import java.math.BigDecimal;

public interface AnalyticalQuery3Projection {
    String getCategoryName();
    Long getTotalSold();
    BigDecimal getTotalRevenue();
}

