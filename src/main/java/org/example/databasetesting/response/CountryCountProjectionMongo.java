package org.example.databasetesting.response;

import org.springframework.beans.factory.annotation.Value;

public interface CountryCountProjectionMongo {
    @Value("#{target._id}")
    String getCountry();

    @Value("#{target.count}")
    Long getCount();
}
