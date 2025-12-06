package org.example.databasetesting.response;

import java.math.BigDecimal;

public interface AnalyticalQuery1Projection {
    BigDecimal getAvgBidsPerProduct();
    Double getMedianBidsPerProduct();
    Double getP90BidsPerProduct();
}
