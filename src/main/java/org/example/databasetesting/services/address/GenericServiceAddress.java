package org.example.databasetesting.services.address;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.web.multipart.MultipartFile;

public interface GenericServiceAddress {
    DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize);
    DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize);
}
