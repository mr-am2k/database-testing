package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.UserDocument;
import org.example.databasetesting.response.CityUserCountProjectionMongo;
import org.example.databasetesting.response.UserCountProjection;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MongoUserRepository extends MongoRepository<UserDocument, UUID> {
    long countByStatusAndCreditCardExpirationDateAfterAndAddressCountryRegex(
            String status, LocalDate expirationDate, String country);


    @Aggregation(pipeline = {
            "{ $match: { status: ?0, 'creditCard.expirationDate': { $gt: ?1 }, 'address.city': { $regex: ?2, $options: 'i' } } }",
            "{ $group: { _id: '$address.city', userCount: { $sum: 1 } } }",
            "{ $sort: { userCount: -1 } }",
            "{ $limit: 100 }"
    })
    List<CityUserCountProjectionMongo> countUsersByCity(String status, LocalDate expirationDate, String cityKeyword);
}
