package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.CreditCardDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoCreditCardRepository extends MongoRepository<CreditCardDocument, UUID> {
}
