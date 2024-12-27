package org.example.databasetesting.services.product;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.ProductDocument;
import org.example.databasetesting.repositories.mongodb.MongoProductRepository;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.List;
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
    public DatabaseActionResponse saveAll(List<ProductDocument> entities) {
        long startCpu = getCpuUsage();
        long startMemory = getMemoryUsage();

        mongoProductRepository.saveAll(entities);

        long endCpu = getCpuUsage();
        long endMemory = getMemoryUsage();

        long cpuDiff = endCpu - startCpu;
        long memoryDiff = endMemory - startMemory;

        meterRegistry.gauge("mongodb.operation.cpuUsage", cpuDiff);
        meterRegistry.gauge("mongodb.operation.memoryUsage", memoryDiff);

        String cpuUsageFormatted = (float) (cpuDiff / 100) + "%";
        float ramUsageMB = (float) memoryDiff / 1_048_576;
        String ramUsageFormatted = ramUsageMB + "MB";

        return new DatabaseActionResponse(0, cpuUsageFormatted, ramUsageFormatted);
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