package org.example.databasetesting.services.products;

import org.example.databasetesting.requests.Product;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.example.databasetesting.utils.CSVUtil;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Service
public class GenericServiceProductImpl implements GenericServiceProduct {

    private final EnumMap<DatabaseType, ActionsService> strategies = new EnumMap<>(DatabaseType.class);

    public GenericServiceProductImpl(
            PostgreSQLServiceProductImpl postgreSQLService,
            MongoDBServiceProductImpl mongoDBService
    ) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    @Override
    public DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final List<Product> requestValues = CSVUtil.parseCSV(file, Product.class);

        List<?> entityValues = switch (databaseType) {
            case MONGODB -> requestValues.stream().map(Product::toProductDocument).toList();
            case POSTGRESQL -> requestValues.stream().map(Product::toProductEntity).toList();
        };

        List<? extends List<?>> batches = splitIntoBatches(entityValues, batchSize);

        final long startTime = System.nanoTime();

        DatabaseActionResponse databaseActionResponse = strategies.get(databaseType).saveAll(batches, batchSize);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration, databaseActionResponse.getCpuUsage(), databaseActionResponse.getRamUsage());
    }

    @Override
    public DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final List<Product> requestValues = CSVUtil.parseCSV(file, Product.class);

        List<?> entityValues = switch (databaseType) {
            case MONGODB -> requestValues.stream().map(Product::toProductDocument).toList();
            case POSTGRESQL -> requestValues.stream().map(Product::toProductEntity).toList();
        };

        List<? extends List<?>> batches = splitIntoBatches(entityValues, batchSize);

        final long startTime = System.nanoTime();

        DatabaseActionResponse databaseActionResponse = strategies.get(databaseType).saveAll(batches, batchSize);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration, databaseActionResponse.getCpuUsage(), databaseActionResponse.getRamUsage());
    }

    public <T> List<List<T>> splitIntoBatches(List<T> request, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < request.size(); i += batchSize) {
            int end = Math.min(i + batchSize, request.size());
            batches.add(request.subList(i, end));
        }
        return batches;
    }
}
