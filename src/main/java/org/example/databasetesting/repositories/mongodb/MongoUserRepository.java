package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoUserRepository extends MongoRepository<UserDocument, UUID> {
}
