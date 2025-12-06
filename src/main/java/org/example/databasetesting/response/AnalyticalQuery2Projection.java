package org.example.databasetesting.response;

import java.math.BigDecimal;

public interface AnalyticalQuery2Projection {
    Double getMedianBidsPerUser();
    Double getP90BidsPerUser();
    Long getMaxBidsByAUser();
}
