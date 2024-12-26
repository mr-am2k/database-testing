package org.example.databasetesting.services;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.List;

@Service
public class PostgresBatchProcessingService<T> {
    @PersistenceContext
    private EntityManager entityManager;

    private final MeterRegistry meterRegistry;

    public PostgresBatchProcessingService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Transactional
    public ResourceMetrics processBatch(List<T> batch, int batchSize) {
        long maxCpuUsage = 0;
        long maxMemoryUsage = 0;

        for (int i = 0; i < batch.size(); i++) {
            long startCpu = getCpuUsage();
            long startMemory = getMemoryUsage();

            entityManager.persist(batch.get(i));

            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }

            long endCpu = getCpuUsage();
            long endMemory = getMemoryUsage();

            long cpuDiff = endCpu - startCpu;
            long memoryDiff = endMemory - startMemory;

            maxCpuUsage = Math.max(maxCpuUsage, cpuDiff);
            maxMemoryUsage = Math.max(maxMemoryUsage, memoryDiff);

            meterRegistry.gauge("database.operation.cpuUsage", cpuDiff);
            meterRegistry.gauge("database.operation.memoryUsage", memoryDiff);
        }

        long flushStartCpu = getCpuUsage();
        long flushStartMemory = getMemoryUsage();

        entityManager.flush();
        entityManager.clear();

        long flushEndCpu = getCpuUsage();
        long flushEndMemory = getMemoryUsage();

        maxCpuUsage = Math.max(maxCpuUsage, flushEndCpu - flushStartCpu);
        maxMemoryUsage = Math.max(maxMemoryUsage, flushEndMemory - flushStartMemory);

        return new ResourceMetrics(maxCpuUsage, maxMemoryUsage);
    }

    public record ResourceMetrics(long cpuUsage, long memoryUsage) {}
}