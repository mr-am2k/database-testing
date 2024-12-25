package org.example.databasetesting.services.products;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.web.multipart.MultipartFile;

public interface GenericServiceProduct {
    DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize);
    DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize);
}
