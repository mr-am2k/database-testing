package org.example.databasetesting.services.address;

import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.requests.Address;
import org.example.databasetesting.requests.Product;
import org.example.databasetesting.services.ActionsService;
import org.example.databasetesting.services.PostgresBatchProcessingService;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostgreSQLServiceAddressImpl implements ActionsService<AddressEntity> {
    private final PostgresBatchProcessingService<AddressEntity> postgresBatchProcessingService;

    public PostgreSQLServiceAddressImpl(PostgresBatchProcessingService<AddressEntity> postgresBatchProcessingService) {
        this.postgresBatchProcessingService = postgresBatchProcessingService;
    }

    @Override
    public void saveAll(List<List<AddressEntity>> batches, int batchSize) {
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (List<AddressEntity> batch : batches) {
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