package org.example.databasetesting.services;

import java.util.List;

public interface ActionsService<T> {
    void saveAll(List<List<T>> batches, int batchSize);
}