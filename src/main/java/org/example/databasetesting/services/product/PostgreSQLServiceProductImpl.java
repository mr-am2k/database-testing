package org.example.databasetesting.services.product;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.example.databasetesting.entities.postgresql.ProductEntity;
import org.example.databasetesting.repositories.postgresql.PostgresProductRepository;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.List;


@Service
public class PostgreSQLServiceProductImpl implements ActionsService<ProductEntity> {
    private final MeterRegistry meterRegistry;
    private final PostgresProductRepository postgresProductRepository;

    public PostgreSQLServiceProductImpl(MeterRegistry meterRegistry, PostgresProductRepository postgresProductRepository) {
        this.meterRegistry = meterRegistry;
        this.postgresProductRepository = postgresProductRepository;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    @Transactional
    public DatabaseActionResponse saveAll(List<ProductEntity> entities) {
        long maxCpuUsage = 0;
        long maxMemoryUsage = 0;

        long startCpu = getCpuUsage();
        long startMemory = getMemoryUsage();

        postgresProductRepository.saveAll(entities);

        long endCpu = getCpuUsage();
        long endMemory = getMemoryUsage();

        maxCpuUsage = Math.max(maxCpuUsage, endCpu - startCpu);
        maxMemoryUsage = Math.max(maxMemoryUsage, endMemory - startMemory);

        meterRegistry.gauge("database.operation.cpuUsage", maxCpuUsage);
        meterRegistry.gauge("database.operation.memoryUsage", maxMemoryUsage);

        String cpuUsageFormatted = (float) (maxCpuUsage / 100) + "%";
        float ramUsageMB = (float) maxMemoryUsage / 1_048_576;
        String ramUsageFormatted = ramUsageMB + "MB";

        return new DatabaseActionResponse(0, cpuUsageFormatted, ramUsageFormatted);
    }
}