package org.example.databasetesting.services;

import org.example.databasetesting.response.DatabaseActionResponse;

import java.util.List;

public interface ActionServiceComplex<T> {
    DatabaseActionResponse saveAll(List<?> entities);

    DatabaseActionResponse getCount();

    DatabaseActionResponse getAggregation();
}