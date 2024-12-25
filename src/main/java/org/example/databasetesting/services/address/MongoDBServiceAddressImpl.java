package org.example.databasetesting.services.address;

import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.repositories.mongodb.MongoAddressRepository;
import org.example.databasetesting.services.ActionsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MongoDBServiceAddressImpl implements ActionsService<AddressDocument> {
    private final MongoAddressRepository mongoAddressRepository;

    public MongoDBServiceAddressImpl(MongoAddressRepository mongoAddressRepository) {
        this.mongoAddressRepository = mongoAddressRepository;
    }

    @Override
    public void saveAll(List<List<AddressDocument>> request, int batchSize) {
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (List<AddressDocument> batch : request) {
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

    private void processBatch(List<AddressDocument> batch) {
        mongoAddressRepository.saveAll(batch);
    }
}
