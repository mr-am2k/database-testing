package org.example.databasetesting.controllers;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.address.GenericServiceAddress;
import org.example.databasetesting.services.user.GenericServiceUser;
import org.example.databasetesting.utils.CSVUtil;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.example.databasetesting.services.address.GenericServiceAddressImpl.NUMBER_OF_THREADS;
import static org.example.databasetesting.services.user.GenericServiceUserImpl.PROCESSING_THREADS;

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
            @RequestParam("batchSize") int batchSize,
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") int numberOfRecords,
            @RequestParam("caching") String caching,
            @RequestParam("queryType") String queryType) throws IOException {
        DatabaseActionResponse response = genericServiceAddress.saveAllSimple(file, DatabaseType.MONGODB, batchSize);

        CSVUtil.saveResultToCSV(
                databaseType,
                numberOfRecords,
                batchSize,
                caching,
                NUMBER_OF_THREADS,
                queryType,
                response.getTime(),
                response.getRamUsage(),
                response.getCpuUsage()
        );

        return response;
    }

    @PostMapping(path = "/batch-insert-complex", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatabaseActionResponse complexBatchInsert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchSize") int batchSize,
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") int numberOfRecords,
            @RequestParam("caching") String caching,
            @RequestParam("queryType") String queryType) throws IOException {
        DatabaseActionResponse response = genericServiceUser.saveAllComplex(file, DatabaseType.MONGODB, batchSize);

        CSVUtil.saveResultToCSV(
                databaseType,
                numberOfRecords,
                batchSize,
                caching,
                PROCESSING_THREADS,
                queryType,
                response.getTime(),
                response.getRamUsage(),
                response.getCpuUsage()
        );

        return response;
    }
}
