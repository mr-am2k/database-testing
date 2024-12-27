package org.example.databasetesting.services.users;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.web.multipart.MultipartFile;

public interface GenericServiceUser {
    DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize);
}
