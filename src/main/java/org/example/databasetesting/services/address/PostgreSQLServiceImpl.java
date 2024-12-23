package org.example.databasetesting.services.address;

import jakarta.persistence.EntityManager;
import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.repositories.postgresql.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostgreSQLServiceImpl implements AddressService<AddressEntity> {
    private final AddressRepository addressRepository;
    private final EntityManager entityManager;

    public PostgreSQLServiceImpl(AddressRepository addressRepository, EntityManager entityManager) {
        this.addressRepository = addressRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<AddressEntity> saveAll(List<AddressEntity> request, int batchSize) {
        if (request.isEmpty()) {
            return request;
        }

        int numberOfThreads = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            List<List<AddressEntity>> batches = splitIntoBatches(request, batchSize);

            List<Future<?>> futures = new ArrayList<>();
            for (List<AddressEntity> batch : batches) {
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

    private void processBatch(List<AddressEntity> batch) {
        for (AddressEntity entity : batch) {
            addressRepository.save(entity);
        }
        addressRepository.flush();
        entityManager.clear();
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
