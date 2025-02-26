package org.example.databasetesting.controllers;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.address.GenericServiceAddress;
import org.example.databasetesting.services.user.GenericServiceUser;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/mongodb")
public class MongoDBController {
    private final GenericServiceAddress genericServiceAddress;
    private final GenericServiceUser genericServiceUser;

    public MongoDBController(GenericServiceAddress genericServiceAddress, GenericServiceUser genericServiceUser) {
        this.genericServiceAddress = genericServiceAddress;
        this.genericServiceUser = genericServiceUser;
    }

    @PostMapping(path = "/batch-insert-simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatabaseActionResponse simpleBatchInsert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchSize") int batchSize) throws IOException {
        return genericServiceAddress.saveAllSimple(file, DatabaseType.MONGODB, batchSize);
    }

    @PostMapping(path = "/batch-insert-complex", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatabaseActionResponse complexBatchInsert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchSize") int batchSize) throws IOException {
        return genericServiceUser.saveAllComplex(file, DatabaseType.MONGODB, batchSize);
    }
}
