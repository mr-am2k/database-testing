package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoProductRepository extends MongoRepository<ProductDocument, String> {
}
