package org.example.databasetesting.services.product;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.web.multipart.MultipartFile;

public interface GenericServiceProduct {
    DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize);
}
