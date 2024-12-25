package org.example.databasetesting.services.address;

import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.repositories.mongodb.MongoAddressRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MongoDBServiceImpl implements AddressService<AddressDocument> {
    private final MongoAddressRepository mongoAddressRepository;

    public MongoDBServiceImpl(MongoAddressRepository mongoAddressRepository) {
        this.mongoAddressRepository = mongoAddressRepository;
    }

    @Override
    public List<AddressDocument> saveAll(List<AddressDocument> request, int batchSize) {
        if (request.isEmpty()) {
            return request;
        }

        int numberOfThreads = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<List<AddressDocument>> batches = splitIntoBatches(request, batchSize);

            List<Future<?>> futures = new ArrayList<>();
            for (List<AddressDocument> batch : batches) {
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

        return request;
    }

    private void processBatch(List<AddressDocument> batch) {
        mongoAddressRepository.saveAll(batch);
    }

    private List<List<AddressDocument>> splitIntoBatches(List<AddressDocument> request, int batchSize) {
        List<List<AddressDocument>> batches = new ArrayList<>();
        for (int i = 0; i < request.size(); i += batchSize) {
            int end = Math.min(i + batchSize, request.size());
            batches.add(request.subList(i, end));
        }
        return batches;
    }
}
