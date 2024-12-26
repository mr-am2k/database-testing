package org.example.databasetesting.services.address;

import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.entities.postgresql.ProductEntity;
import org.example.databasetesting.requests.Address;
import org.example.databasetesting.requests.Product;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.example.databasetesting.services.PostgresBatchProcessingService;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PostgreSQLServiceAddressImpl implements ActionsService<AddressEntity> {
    private final PostgresBatchProcessingService<AddressEntity> postgresBatchProcessingService;

    public PostgreSQLServiceAddressImpl(PostgresBatchProcessingService<AddressEntity> postgresBatchProcessingService) {
        this.postgresBatchProcessingService = postgresBatchProcessingService;
    }

    @Override
    public DatabaseActionResponse saveAll(List<List<AddressEntity>> request, int batchSize) {
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        AtomicLong maxCpuUsage = new AtomicLong(0);
        AtomicLong maxMemoryUsage = new AtomicLong(0);

        try {
            List<Future<PostgresBatchProcessingService.ResourceMetrics>> futures = new ArrayList<>();

            for (List<AddressEntity> batch : request) {
                futures.add(executorService.submit(() ->
                        postgresBatchProcessingService.processBatch(batch, batchSize)
                ));
            }

            for (Future<PostgresBatchProcessingService.ResourceMetrics> future : futures) {
                PostgresBatchProcessingService.ResourceMetrics metrics = future.get();
                updateMaxUsage(maxCpuUsage, metrics.cpuUsage());
                updateMaxUsage(maxMemoryUsage, metrics.memoryUsage());
            }

            String cpuUsageFormatted = (float) (maxCpuUsage.get() / 100) + "%";
            float ramUsageMB = (float) maxMemoryUsage.get() / 1_048_576;
            String ramUsageFormatted = ramUsageMB + "MB";

            return new DatabaseActionResponse(0, cpuUsageFormatted, ramUsageFormatted);

        } catch (Exception e) {
            throw new RuntimeException("Error processing batches in parallel", e);
        } finally {
            executorService.shutdown();
        }
    }

    private void updateMaxUsage(AtomicLong currentMax, long newValue) {
        long oldValue;
        do {
            oldValue = currentMax.get();
            if (newValue <= oldValue) break;
        } while (!currentMax.compareAndSet(oldValue, newValue));
    }
}
