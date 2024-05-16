package org.example.databasetesting.services;

import java.util.List;

public interface DatabaseActionsService<T> {
    List<T> saveAll(List<T> keyValueRequests);

    List<T> getAll();

    T getByKey(final String key);

    void deleteByKey(final String key);
}
