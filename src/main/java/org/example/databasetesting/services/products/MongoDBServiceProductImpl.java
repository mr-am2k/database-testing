package org.example.databasetesting.services.products;

import org.example.databasetesting.entities.mongodb.ProductDocument;
import org.example.databasetesting.repositories.mongodb.MongoProductRepository;
import org.example.databasetesting.services.ActionsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MongoDBServiceProductImpl implements ActionsService<ProductDocument> {
    private final MongoProductRepository mongoProductRepository;

    public MongoDBServiceProductImpl(MongoProductRepository mongoProductRepository) {
        this.mongoProductRepository = mongoProductRepository;
    }

    @Override
    public void saveAll(List<List<ProductDocument>> request, int batchSize) {
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (List<ProductDocument> batch : request) {
                futures.add(executorService.submit(() -> processBatch(batch)));
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

    private void processBatch(List<ProductDocument> batch) {
        mongoProductRepository.saveAll(batch);
    }
}
