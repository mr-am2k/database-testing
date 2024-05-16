package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.KeyValueModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeyValueMongoRepository extends MongoRepository<KeyValueModel, String> {
    KeyValueModel findByKey(final String key);

    void deleteByKey(final String key);
}
