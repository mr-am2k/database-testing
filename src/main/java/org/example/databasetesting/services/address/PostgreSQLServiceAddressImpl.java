package org.example.databasetesting.services.address;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.springframework.stereotype.Service;


import java.lang.management.ManagementFactory;
import java.util.List;

@Service
public class PostgreSQLServiceAddressImpl implements ActionsService<AddressEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    private final MeterRegistry meterRegistry;

    public PostgreSQLServiceAddressImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    @Transactional
    public DatabaseActionResponse saveAll(List<AddressEntity> entities) {
        long startCpu = getCpuUsage();
        long startMemory = getMemoryUsage();

        for (AddressEntity entity : entities) {
            entityManager.persist(entity);
        }

        entityManager.flush();
        entityManager.clear();

        long endCpu = getCpuUsage();
        long endMemory = getMemoryUsage();

        long cpuDiff = endCpu - startCpu;
        long memoryDiff = endMemory - startMemory;

        meterRegistry.gauge("database.operation.cpuUsage", cpuDiff);
        meterRegistry.gauge("database.operation.memoryUsage", memoryDiff);

        String cpuUsageFormatted = (float) (cpuDiff / 100) + "%";
        float ramUsageMB = (float) memoryDiff / 1_048_576;
        String ramUsageFormatted = ramUsageMB + "MB";

        return new DatabaseActionResponse(0, cpuUsageFormatted, ramUsageFormatted);
    }
}
