package org.example.databasetesting.controllers;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.address.GenericAddressService;
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
    private final GenericAddressService genericAddressService;

    public PostgreSQLController(GenericAddressService genericAddressService) {
        this.genericAddressService = genericAddressService;
    }

    @PutMapping(path = "/batch-insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatabaseActionResponse simpleBatchInsert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchSize") int batchSize) throws IOException {
        return genericAddressService.saveAll(file, DatabaseType.POSTGRESQL, batchSize);
    }
}
