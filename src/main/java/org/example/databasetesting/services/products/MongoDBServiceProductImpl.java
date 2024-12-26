package org.example.databasetesting.services.products;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.ProductDocument;
import org.example.databasetesting.repositories.mongodb.MongoProductRepository;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MongoDBServiceProductImpl implements ActionsService<ProductDocument> {
    private final MongoProductRepository mongoProductRepository;
    private final MeterRegistry meterRegistry;

    public MongoDBServiceProductImpl(MongoProductRepository mongoProductRepository, MeterRegistry meterRegistry) {
        this.mongoProductRepository = mongoProductRepository;
        this.meterRegistry = meterRegistry;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public DatabaseActionResponse saveAll(List<List<ProductDocument>> request, int batchSize) {
        int numberOfThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        AtomicLong maxCpuUsage = new AtomicLong(0);
        AtomicLong maxMemoryUsage = new AtomicLong(0);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (List<ProductDocument> batch : request) {
                futures.add(executorService.submit(() -> processBatch(batch, maxCpuUsage, maxMemoryUsage)));
            }

            for (Future<?> future : futures) {
                future.get();
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

    private void processBatch(List<ProductDocument> batch, AtomicLong maxCpuUsage, AtomicLong maxMemoryUsage) {
        long startCpu = getCpuUsage();
        long startMemory = getMemoryUsage();

        mongoProductRepository.saveAll(batch);

        long endCpu = getCpuUsage();
        long endMemory = getMemoryUsage();

        long cpuDiff = endCpu - startCpu;
        long memoryDiff = endMemory - startMemory;

        updateMaxUsage(maxCpuUsage, cpuDiff);
        updateMaxUsage(maxMemoryUsage, memoryDiff);

        meterRegistry.gauge("mongodb.operation.cpuUsage", cpuDiff);
        meterRegistry.gauge("mongodb.operation.memoryUsage", memoryDiff);
    }

    private void updateMaxUsage(AtomicLong currentMax, long newValue) {
        long oldValue;
        do {
            oldValue = currentMax.get();
            if (newValue <= oldValue) break;
        } while (!currentMax.compareAndSet(oldValue, newValue));
    }
}