package org.example.databasetesting.services;

import org.example.databasetesting.response.DatabaseActionResponse;

import java.util.List;
import java.util.Map;

public interface ActionServiceComplex<T> {
    DatabaseActionResponse saveAll(Map<String, List<?>> entities);
}