package org.example.databasetesting.services.mongodb;

import org.example.databasetesting.entities.mongodb.KeyValueModel;
import org.example.databasetesting.repositories.mongodb.KeyValueMongoRepository;
import org.example.databasetesting.services.DatabaseActionsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoDBServiceImpl implements DatabaseActionsService<KeyValueModel> {
    private final KeyValueMongoRepository keyValueMongoRepository;

    public MongoDBServiceImpl(KeyValueMongoRepository keyValueMongoRepository) {
        this.keyValueMongoRepository = keyValueMongoRepository;
    }

    @Override
    public List<KeyValueModel> saveAll(List<KeyValueModel> keyValueRequests) {
        return keyValueMongoRepository.saveAll(keyValueRequests);
    }

    @Override
    public List<KeyValueModel> getAll() {
        return keyValueMongoRepository.findAll();
    }

    @Override
    public KeyValueModel getByKey(String key) {
        return keyValueMongoRepository.findByKey(key);
    }

    @Override
    public void deleteByKey(String key) {
        keyValueMongoRepository.deleteByKey(key);
    }
}
