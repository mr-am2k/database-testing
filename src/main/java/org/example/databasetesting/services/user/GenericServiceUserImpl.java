package org.example.databasetesting.services.user;

import org.example.databasetesting.requests.User;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionServiceComplex;
import org.example.databasetesting.utils.CSVUtil;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GenericServiceUserImpl implements GenericServiceUser {
    private static final int PROCESSING_THREADS = 4;
    private static final int CHUNK_SIZE = 2500000;
    private final EnumMap<DatabaseType, ActionServiceComplex> strategies = new EnumMap<>(DatabaseType.class);

    public GenericServiceUserImpl(
            PostgreSQLServiceUserImpl postgreSQLService,
            MongoDBServiceUserImpl mongoDBService
    ) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    @Override
    public DatabaseActionResponse saveAllComplex(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final long startTime = System.nanoTime();
        AtomicReference<String> maxCpuUsage = new AtomicReference<>("0%");
        AtomicReference<String> maxRamUsage = new AtomicReference<>("0MB");
        long duration;

        ExecutorService executorService = Executors.newFixedThreadPool(PROCESSING_THREADS);

        try {
            // Process each chunk sequentially
            CSVUtil.parseCSVInChunks(file, User.class, CHUNK_SIZE, chunk -> {
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

    private void processChunk(List<User> chunk,
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
            List<User> batch = chunk.subList(start, end);

            Future<DatabaseActionResponse> future = executorService.submit(() -> {
                Map<String, List<?>> entities = convertToEntities(batch, databaseType);
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

    private Map<String, List<?>> convertToEntities(List<User> users, DatabaseType databaseType) {
        if (databaseType == DatabaseType.MONGODB) {
            List<Object> addresses = new ArrayList<>();
            List<Object> creditCards = new ArrayList<>();
            List<Object> usersList = new ArrayList<>();

            users.forEach(user -> {
                Map<String, Object> mongoDocument = user.toMongoDocument();
                addresses.add(mongoDocument.get("address"));
                creditCards.add(mongoDocument.get("creditCard"));
                usersList.add(mongoDocument.get("user"));
            });

            Map<String, List<?>> resultMap = new HashMap<>();
            resultMap.put("address", addresses);
            resultMap.put("creditCard", creditCards);
            resultMap.put("user", usersList);

            return resultMap;
        } else if (databaseType == DatabaseType.POSTGRESQL) {
            return Map.of("users", users.stream().map(User::toPostgresEntity).toList());
        }
        return Collections.emptyMap();
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