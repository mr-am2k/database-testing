package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.response.CountryCountProjection;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface MongoAddressRepository extends MongoRepository<AddressDocument, UUID> {
    long countByCountry(String country);

    @Aggregation(pipeline = {
            "{ $match: { city: { $regex: ?0, $options: 'i' } } }",
            "{ $group: { _id: '$country', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }",
            "{ $limit: 100 }"
    })
    List<CountryCountProjection> findTopCountriesByRecordCount(String cityKeyword);
}
