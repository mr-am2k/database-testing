package org.example.databasetesting.controllers;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.address.GenericServiceAddress;
import org.example.databasetesting.services.analyticalQueries.GenericServiceAnalyticalQueries;
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
    private final GenericServiceAnalyticalQueries genericServiceAnalyticalQueries;

    public MongoDBController(GenericServiceAddress genericServiceAddress, GenericServiceUser genericServiceUser, GenericServiceAnalyticalQueries genericServiceAnalyticalQueries) {
        this.genericServiceAddress = genericServiceAddress;
        this.genericServiceUser = genericServiceUser;
        this.genericServiceAnalyticalQueries = genericServiceAnalyticalQueries;
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

        CSVUtil.saveInsertResultToCSV(
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

        CSVUtil.saveInsertResultToCSV(
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

    @GetMapping(path = "/simple-count")
    public DatabaseActionResponse getSimpleCount(
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching,
            @RequestParam("indexing") String indexing) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAddress.getCount(DatabaseType.MONGODB);

        CSVUtil.saveReadResultsToCSV(
                DatabaseType.MONGODB.toString(),
                numberOfRecords,
                caching,
                "COUNT",
                "SIMPLE",
                indexing,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/simple-aggregation")
    public DatabaseActionResponse getSimpleAggregation(
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching,
            @RequestParam("indexing") String indexing) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAddress.getAggregation(DatabaseType.MONGODB);

        CSVUtil.saveReadResultsToCSV(
                DatabaseType.MONGODB.toString(),
                numberOfRecords,
                caching,
                "AGGREGATION",
                "SIMPLE",
                indexing,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/complex-count")
    public DatabaseActionResponse getComplexCount(
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching,
            @RequestParam("indexing") String indexing) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceUser.getCount(DatabaseType.MONGODB);

        CSVUtil.saveReadResultsToCSV(
                DatabaseType.MONGODB.toString(),
                numberOfRecords,
                caching,
                "COUNT",
                "COMPLEX",
                indexing,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/complex-aggregation")
    public DatabaseActionResponse getComplexAggregation(
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching,
            @RequestParam("indexing") String indexing) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceUser.getAggregation(DatabaseType.MONGODB);

        CSVUtil.saveReadResultsToCSV(
                DatabaseType.MONGODB.toString(),
                numberOfRecords,
                caching,
                "AGGREGATION",
                "COMPLEX",
                indexing,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @PutMapping(path = "/simple-update")
    public DatabaseActionResponse simpleUpdate(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("recordsInDatabase") String recordsInDatabase,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAddress.simpleUpdate(DatabaseType.MONGODB);

        CSVUtil.saveUpdateResultsToCSV(
                databaseType,
                recordsInDatabase,
                numberOfRecords,
                caching,
                "SIMPLE",
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @PutMapping(path = "/complex-update")
    public DatabaseActionResponse complexUpdate(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("recordsInDatabase") String recordsInDatabase,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceUser.complexUpdate(DatabaseType.MONGODB);

        CSVUtil.saveUpdateResultsToCSV(
                databaseType,
                recordsInDatabase,
                numberOfRecords,
                caching,
                "COMPLEX",
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @DeleteMapping(path = "/simple-delete")
    public DatabaseActionResponse simpleDelete(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("recordsInDatabase") String recordsInDatabase,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAddress.simpleDelete(DatabaseType.MONGODB);

        CSVUtil.saveDeleteResultsToCSV(
                databaseType,
                recordsInDatabase,
                numberOfRecords,
                caching,
                "SIMPLE",
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @DeleteMapping(path = "/complex-delete")
    public DatabaseActionResponse complexDelete(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("recordsInDatabase") String recordsInDatabase,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceUser.complexDelete(DatabaseType.MONGODB);

        CSVUtil.saveDeleteResultsToCSV(
                databaseType,
                recordsInDatabase,
                numberOfRecords,
                caching,
                "COMPLEX",
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/analytical-query-1")
    public DatabaseActionResponse analyticalQuery1(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching
    ) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAnalyticalQueries.analyticalQuery1(DatabaseType.MONGODB);

        CSVUtil.saveAnalyticalQueryResultsToCSV(
                databaseType,
                numberOfRecords,
                "ANALYTICAL QUERY 1",
                caching,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/analytical-query-2")
    public DatabaseActionResponse analyticalQuery2(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching
    ) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAnalyticalQueries.analyticalQuery2(DatabaseType.MONGODB);

        CSVUtil.saveAnalyticalQueryResultsToCSV(
                databaseType,
                numberOfRecords,
                "ANALYTICAL QUERY 2",
                caching,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/analytical-query-3")
    public DatabaseActionResponse analyticalQuery3(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching
    ) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAnalyticalQueries.analyticalQuery3(DatabaseType.MONGODB);

        CSVUtil.saveAnalyticalQueryResultsToCSV(
                databaseType,
                numberOfRecords,
                "ANALYTICAL QUERY 3",
                caching,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/analytical-query-4")
    public DatabaseActionResponse analyticalQuery4(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching
    ) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAnalyticalQueries.analyticalQuery4(DatabaseType.MONGODB);

        CSVUtil.saveAnalyticalQueryResultsToCSV(
                databaseType,
                numberOfRecords,
                "ANALYTICAL QUERY 4",
                caching,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }

    @GetMapping(path = "/analytical-query-5")
    public DatabaseActionResponse analyticalQuery5(
            @RequestParam("databaseType") String databaseType,
            @RequestParam("numberOfRecords") String numberOfRecords,
            @RequestParam("caching") String caching
    ) {
        final DatabaseActionResponse databaseActionResponse = this.genericServiceAnalyticalQueries.analyticalQuery5(DatabaseType.MONGODB);

        CSVUtil.saveAnalyticalQueryResultsToCSV(
                databaseType,
                numberOfRecords,
                "ANALYTICAL QUERY 5",
                caching,
                databaseActionResponse.getTime(),
                databaseActionResponse.getRamUsage(),
                databaseActionResponse.getCpuUsage()
        );

        return databaseActionResponse;
    }
}
