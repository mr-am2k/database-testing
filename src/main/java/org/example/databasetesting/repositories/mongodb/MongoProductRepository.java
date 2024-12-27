package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.ProductDocument;
import org.example.databasetesting.entities.mongodb.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoProductRepository extends MongoRepository<UserDocument, String> {
}
