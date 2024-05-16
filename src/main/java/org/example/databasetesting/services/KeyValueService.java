package org.example.databasetesting.services;

import org.example.databasetesting.requests.DatabaseActionRequest;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;

public interface KeyValueService {
    DatabaseActionResponse batchInsert(DatabaseActionRequest request);

    DatabaseActionResponse getAll(DatabaseType databaseType);

    DatabaseActionResponse getbyKey(final String key, final DatabaseType databaseType);

    DatabaseActionResponse deleteByKey(final String key, final DatabaseType databaseType);
}
