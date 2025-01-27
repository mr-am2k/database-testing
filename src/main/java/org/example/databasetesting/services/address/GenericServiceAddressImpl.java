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
    private static final int PROCESSING_THREADS = 4;
    private static final int CHUNK_SIZE = 1000000;
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

        ExecutorService executorService = Executors.newFixedThreadPool(PROCESSING_THREADS);

        try {
            // Process each chunk sequentially
            CSVUtil.parseCSVInChunks(file, Address.class, CHUNK_SIZE, chunk -> {
                processChunk(chunk, databaseType, batchSize, executorService, maxCpuUsage, maxRamUsage);
            });

        } catch (Exception e) {
            throw new RuntimeException("An error occurred during batch processing", e);
        } finally {
            duration = (System.nanoTime() - startTime) / 1_000_000;
            shutdownExecutor(executorService);
        }

        return new DatabaseActionResponse(duration, maxCpuUsage.get(), maxRamUsage.get());
    }

    private void processChunk(List<Address> chunk,
                              DatabaseType databaseType,
                              int batchSize,
                              ExecutorService executorService,
                              AtomicReference<String> maxCpuUsage,
                              AtomicReference<String> maxRamUsage) {

        List<Future<DatabaseActionResponse>> futures = new ArrayList<>();

        // Process the chunk in batches
        for (int i = 0; i < chunk.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, chunk.size());
            List<Address> batch = chunk.subList(start, end);

            // Convert and submit each batch for processing
            Future<DatabaseActionResponse> future = executorService.submit(() -> {
                List<?> entities = convertToEntities(batch, databaseType);
                return strategies.get(databaseType).saveAll(entities);
            });

            futures.add(future);
        }

        // Wait for all batches in this chunk to complete
        for (Future<DatabaseActionResponse> future : futures) {
            try {
                DatabaseActionResponse response = future.get();
                updateMaxMetrics(response, maxCpuUsage, maxRamUsage);
            } catch (Exception e) {
                throw new RuntimeException("Error processing batch", e);
            }
        }
    }

    private List<?> convertToEntities(List<Address> entities, DatabaseType databaseType) {
        return switch (databaseType) {
            case MONGODB -> entities.stream().map(Address::toMongoEntity).toList();
            case POSTGRESQL -> entities.stream().map(Address::toPostgresEntity).toList();
        };
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

    private void shutdownExecutor(ExecutorService executorService) {
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
}