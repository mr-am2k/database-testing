package org.example.databasetesting.services.address;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.requests.Address;
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
public class GenericServiceAddressImpl implements GenericServiceAddress {

    private final EnumMap<DatabaseType, ActionsService> strategies = new EnumMap<>(DatabaseType.class);
    private final MeterRegistry meterRegistry;

    public GenericServiceAddressImpl(
            PostgreSQLServiceAddressImpl postgreSQLService,
            MongoDBServiceAddressImpl mongoDBService,
            MeterRegistry meterRegistry
    ) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
        this.meterRegistry = meterRegistry;
    }

    @Override
    public DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final List<Address> requestValues = CSVUtil.parseCSV(file, Address.class);

        List<?> entityValues = switch (databaseType) {
            case MONGODB -> requestValues.stream().map(Address::toMongoEntity).toList();
            case POSTGRESQL -> requestValues.stream().map(Address::toPostgresEntity).toList();
        };

        List<? extends List<?>> batches = splitIntoBatches(entityValues, batchSize);

        long initialCpuUsage = getCpuUsage();
        long initialMemoryUsage = getMemoryUsage();

        final long startTime = System.nanoTime();

        strategies.get(databaseType).saveAll(batches, batchSize);

        final long endTime = System.nanoTime();

        long finalCpuUsage = getCpuUsage();
        long finalMemoryUsage = getMemoryUsage();

        meterRegistry.gauge("database.save.cpuUsage", finalCpuUsage - initialCpuUsage);
        meterRegistry.gauge("database.save.memoryUsage", finalMemoryUsage - initialMemoryUsage);

        final long duration = (endTime - startTime) / 1_000_000;

        String cpuUsageFormatted = (float) ((finalCpuUsage - initialCpuUsage) / 100) + "%";

        float ramUsageMB = (float) (finalMemoryUsage - initialMemoryUsage) / 1_048_576;

        String ramUsageFormatted = ramUsageMB + "MB";

        return new DatabaseActionResponse(duration, cpuUsageFormatted, ramUsageFormatted);
    }

    @Override
    public DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final List<Product> requestValues = CSVUtil.parseCSV(file, Product.class);

        List<?> entityValues = switch (databaseType) {
            case MONGODB -> requestValues.stream().map(Product::toProductDocument).toList();
            case POSTGRESQL -> requestValues.stream().map(Product::toProductEntity).toList();
        };

        List<? extends List<?>> batches = splitIntoBatches(entityValues, batchSize);

        long initialCpuUsage = getCpuUsage();
        long initialMemoryUsage = getMemoryUsage();

        final long startTime = System.nanoTime();

        strategies.get(databaseType).saveAll(batches, batchSize);

        final long endTime = System.nanoTime();

        long finalCpuUsage = getCpuUsage();
        long finalMemoryUsage = getMemoryUsage();

        meterRegistry.gauge("database.save.cpuUsage", finalCpuUsage - initialCpuUsage);
        meterRegistry.gauge("database.save.memoryUsage", finalMemoryUsage - initialMemoryUsage);

        final long duration = (endTime - startTime) / 1_000_000;

        String cpuUsageFormatted = (float) ((finalCpuUsage - initialCpuUsage) / 100) + "%";

        float ramUsageMB = (float) (finalMemoryUsage - initialMemoryUsage) / 1_048_576;

        String ramUsageFormatted = ramUsageMB + "MB";

        return new DatabaseActionResponse(duration, cpuUsageFormatted, ramUsageFormatted);
    }

    public <T> List<List<T>> splitIntoBatches(List<T> request, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < request.size(); i += batchSize) {
            int end = Math.min(i + batchSize, request.size());
            batches.add(request.subList(i, end));
        }
        return batches;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
