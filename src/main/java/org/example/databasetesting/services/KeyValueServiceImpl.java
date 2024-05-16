package org.example.databasetesting.services;

import jakarta.annotation.PostConstruct;
import org.example.databasetesting.entities.mongodb.KeyValueModel;
import org.example.databasetesting.entities.postgresql.KeyValueEntity;
import org.example.databasetesting.requests.DatabaseActionRequest;
import org.example.databasetesting.requests.KeyValue;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.mongodb.MongoDBServiceImpl;
import org.example.databasetesting.services.postgresql.PostgresqlServiceImpl;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Service
public class KeyValueServiceImpl implements KeyValueService{
    private final EnumMap<DatabaseType, DatabaseActionsService> strategies = new EnumMap<>(DatabaseType.class);

    private final PostgresqlServiceImpl postgresqlService;
    private final MongoDBServiceImpl mongoDBService;

    public KeyValueServiceImpl(PostgresqlServiceImpl postgresqlService, MongoDBServiceImpl mongoDBService) {
        this.postgresqlService = postgresqlService;
        this.mongoDBService = mongoDBService;
    }

    @PostConstruct
    public void init() {
        strategies.put(DatabaseType.POSTGRESQL, postgresqlService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    @Override
    public DatabaseActionResponse batchInsert(DatabaseActionRequest request) {
        final List<KeyValue> requestValues = request.getKeyValueRequestList();

        List<?> entityValues = switch (request.getDatabaseType()) {
            case MONGODB -> requestValues.stream().map(KeyValue::toMongoDBDocument).toList();
            case POSTGRESQL -> requestValues.stream().map(KeyValue::toPostgreEntity).toList();
        };

        final long startTime = System.nanoTime();

        strategies.get(request.getDatabaseType()).saveAll(entityValues);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration);
    }

    @Override
    public DatabaseActionResponse getAll(DatabaseType databaseType) {
        final long startTime = System.nanoTime();

        strategies.get(databaseType).getAll();

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration);
    }

    @Override
    public DatabaseActionResponse getbyKey(String key, DatabaseType databaseType) {
        final long startTime = System.nanoTime();

        strategies.get(databaseType).getByKey(key);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration);
    }

    @Override
    public DatabaseActionResponse deleteByKey(String key, DatabaseType databaseType) {
        final long startTime = System.nanoTime();

        strategies.get(databaseType).deleteByKey(key);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration);
    }
}
