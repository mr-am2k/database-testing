package org.example.databasetesting.services.address;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GenericServiceAddress {
    DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize);
}
