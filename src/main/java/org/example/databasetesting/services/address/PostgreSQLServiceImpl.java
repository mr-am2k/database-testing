package org.example.databasetesting.services.address;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.repositories.postgresql.PostgresAddressRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostgreSQLServiceImpl implements AddressService<AddressEntity> {
    private final PostgresAddressRepository postgresAddressRepository;
    private final BatchProcessingService batchProcessingService;

    public PostgreSQLServiceImpl(PostgresAddressRepository postgresAddressRepository,
                                 BatchProcessingService batchProcessingService) {
        this.postgresAddressRepository = postgresAddressRepository;
        this.batchProcessingService = batchProcessingService;
    }

    @Override
    public List<AddressEntity> saveAll(List<AddressEntity> request, int batchSize) {
        if (request.isEmpty()) {
            return request;
        }

        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<List<AddressEntity>> batches = splitIntoBatches(request, batchSize);

            List<Future<?>> futures = new ArrayList<>();
            for (List<AddressEntity> batch : batches) {
                futures.add(executorService.submit(() ->
                        batchProcessingService.processBatch(batch, batchSize)
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

        return request;
    }

    private List<List<AddressEntity>> splitIntoBatches(List<AddressEntity> request, int batchSize) {
        List<List<AddressEntity>> batches = new ArrayList<>();
        for (int i = 0; i < request.size(); i += batchSize) {
            int end = Math.min(i + batchSize, request.size());
            batches.add(request.subList(i, end));
        }
        return batches;
    }
}