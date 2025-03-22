package org.example.databasetesting.response;

import org.springframework.beans.factory.annotation.Value;

public interface CityUserCountProjectionMongo {

    @Value("#{target._id}")
    String getCity();

    @Value("#{target.userCount}")
    Long getCount();
}
