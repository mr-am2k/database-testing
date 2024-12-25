package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoAddressRepository extends MongoRepository<AddressDocument, UUID> {
}
