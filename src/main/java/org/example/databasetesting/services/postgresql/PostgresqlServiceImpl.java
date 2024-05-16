package org.example.databasetesting.services.postgresql;

import org.example.databasetesting.entities.postgresql.KeyValueEntity;
import org.example.databasetesting.repositories.postgresql.KeyValuePostgreRepository;
import org.example.databasetesting.services.DatabaseActionsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostgresqlServiceImpl implements DatabaseActionsService<KeyValueEntity> {
    private final KeyValuePostgreRepository keyValuePostgreRepository;

    public PostgresqlServiceImpl(KeyValuePostgreRepository keyValuePostgreRepository) {
        this.keyValuePostgreRepository = keyValuePostgreRepository;
    }

    @Override
    public List<KeyValueEntity> saveAll(List<KeyValueEntity> keyValueRequests) {
        return keyValuePostgreRepository.saveAll(keyValueRequests);
    }

    @Override
    public List<KeyValueEntity> getAll() {
        return keyValuePostgreRepository.findAll();
    }

    @Override
    public KeyValueEntity getByKey(String key) {
        return keyValuePostgreRepository.findByKey(key);
    }

    @Override
    public void deleteByKey(String key) {
        keyValuePostgreRepository.deleteById(key);
    }
}