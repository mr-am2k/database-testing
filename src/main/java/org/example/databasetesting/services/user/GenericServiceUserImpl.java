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
    public static final int PROCESSING_THREADS = 4;
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

        ExecutorService executorService = Executors.newFixedThreadPool(PROCESSING_THREADS);

        try {
            // Parse all data at once and split into batches
            List<List<User>> batches = CSVUtil.parseCSV(file, User.class, batchSize);

            // Process all batches in parallel
            List<CompletableFuture<DatabaseActionResponse>> batchFutures = new ArrayList<>();

            for (List<User> batch : batches) {
                CompletableFuture<DatabaseActionResponse> batchFuture = CompletableFuture.supplyAsync(() -> {
                    Map<String, List<?>> entities = convertToEntities(batch, databaseType);
                    return strategies.get(databaseType).saveAll(entities);
                }, executorService).whenComplete((response, ex) -> {
                    if (response != null) {
                        updateMaxMetrics(response, maxCpuUsage, maxRamUsage);
                    }
                });

                batchFutures.add(batchFuture);
            }

            // Wait for all batches to complete
            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();

        } catch (Exception e) {
            throw new RuntimeException("An error occurred during batch processing", e);
        } finally {
            shutdownExecutor(executorService);
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        return new DatabaseActionResponse(duration, maxCpuUsage.get(), maxRamUsage.get());
    }

    @Override
    public DatabaseActionResponse getCount(DatabaseType databaseType) {
        long duration;

        final long startTime = System.nanoTime();

        final DatabaseActionResponse response = strategies.get(databaseType).getCount();

        final long endTime = System.nanoTime();

        duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(
                duration,
                response.getCpuUsage(),
                response.getRamUsage()
        );
    }

    @Override
    public DatabaseActionResponse getAggregation(DatabaseType databaseType) {
        long duration;

        final long startTime = System.nanoTime();

        final DatabaseActionResponse response = strategies.get(databaseType).getAggregation();

        final long endTime = System.nanoTime();

        duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(
                duration,
                response.getCpuUsage(),
                response.getRamUsage()
        );
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