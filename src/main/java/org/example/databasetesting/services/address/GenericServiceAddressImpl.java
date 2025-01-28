package org.example.databasetesting.services.address;

import org.example.databasetesting.requests.Address;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.example.databasetesting.utils.CSVUtil;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GenericServiceAddressImpl implements GenericServiceAddress {
    private static final int NUMBER_OF_THREADS = 25;
    private final EnumMap<DatabaseType, ActionsService> strategies = new EnumMap<>(DatabaseType.class);

    public GenericServiceAddressImpl(
            PostgreSQLServiceAddressImpl postgreSQLService,
            MongoDBServiceAddressImpl mongoDBService
    ) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    @Override
    public DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final long startTime = System.nanoTime();
        AtomicReference<String> maxCpuUsage = new AtomicReference<>("0%");
        AtomicReference<String> maxRamUsage = new AtomicReference<>("0MB");
        long duration;

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Future<?>> futures = new ArrayList<>();

        try {
            List<List<Address>> batches = CSVUtil.parseCSV(file, Address.class, batchSize);

            for (List<Address> batch : batches) {
                futures.add(executorService.submit(() -> {
                    var entityBatch = switch (databaseType) {
                        case MONGODB -> batch.stream().map(Address::toMongoEntity).toList();
                        case POSTGRESQL -> batch.stream().map(Address::toPostgresEntity).toList();
                    };

                    DatabaseActionResponse batchResponse = strategies.get(databaseType).saveAll(entityBatch);
                    updateMaxMetrics(batchResponse, maxCpuUsage, maxRamUsage);
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during batch processing", e);
        } finally {
            final long endTime = System.nanoTime();
            duration = (endTime - startTime) / 1_000_000;

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("Executor service did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return new DatabaseActionResponse(duration, maxCpuUsage.get(), maxRamUsage.get());
    }

    private void updateMaxMetrics(DatabaseActionResponse batchResponse,
                                  AtomicReference<String> maxCpuUsage,
                                  AtomicReference<String> maxRamUsage) {
        float currentCpu = Float.parseFloat(batchResponse.getCpuUsage().replace("%", ""));
        float maxCpu = Float.parseFloat(maxCpuUsage.get().replace("%", ""));
        if (currentCpu > maxCpu) {
            maxCpuUsage.set(batchResponse.getCpuUsage());
        }

        float currentRam = Float.parseFloat(batchResponse.getRamUsage().replace("MB", ""));
        float maxRam = Float.parseFloat(maxRamUsage.get().replace("MB", ""));
        if (currentRam > maxRam) {
            maxRamUsage.set(batchResponse.getRamUsage());
        }
    }
}