package org.example.databasetesting.services;

import org.example.databasetesting.response.DatabaseActionResponse;

import java.util.List;

public interface ActionsService<T> {
    DatabaseActionResponse saveAll(List<T> entities);

    DatabaseActionResponse getCount();

    DatabaseActionResponse getAggregation();
}