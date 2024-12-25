package org.example.databasetesting.services.products;

import org.example.databasetesting.entities.postgresql.ProductEntity;
import org.example.databasetesting.services.ActionsService;
import org.example.databasetesting.services.PostgresBatchProcessingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class PostgreSQLServiceProductImpl implements ActionsService<ProductEntity> {
    private final PostgresBatchProcessingService<ProductEntity> postgresBatchProcessingService;

    public PostgreSQLServiceProductImpl(PostgresBatchProcessingService<ProductEntity> postgresBatchProcessingService) {
        this.postgresBatchProcessingService = postgresBatchProcessingService;
    }

    @Override
    public void saveAll(List<List<ProductEntity>> request, int batchSize) {
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (List<ProductEntity> batch : request) {
                futures.add(executorService.submit(() ->
                        postgresBatchProcessingService.processBatch(batch, batchSize)
                ));
            }

            for (Future<?> future : futures) {
                future.get();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error processing batches in parallel", e);
        } finally {
            executorService.shutdown();
        }
    }

}