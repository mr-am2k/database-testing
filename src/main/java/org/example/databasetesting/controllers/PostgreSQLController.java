package org.example.databasetesting.controllers;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.address.GenericServiceAddress;
import org.example.databasetesting.services.products.GenericServiceProduct;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/postgres")
public class PostgreSQLController {
    private final GenericServiceAddress genericServiceAddress;
    private final GenericServiceProduct genericServiceProduct;

    public PostgreSQLController(GenericServiceAddress genericServiceAddress, GenericServiceProduct genericServiceProduct) {
        this.genericServiceAddress = genericServiceAddress;
        this.genericServiceProduct = genericServiceProduct;
    }

    @PutMapping(path = "/batch-insert-simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatabaseActionResponse simpleBatchInsert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchSize") int batchSize) throws IOException {
        return genericServiceAddress.saveAllSimple(file, DatabaseType.POSTGRESQL, batchSize);
    }

    @PutMapping(path = "/batch-insert-complex", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatabaseActionResponse complexBatchInsert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchSize") int batchSize) throws IOException {
        return genericServiceProduct.saveAllComplex(file, DatabaseType.POSTGRESQL, batchSize);
    }
}
