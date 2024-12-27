package org.example.databasetesting.services.address;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.repositories.mongodb.MongoAddressRepository;
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
public class MongoDBServiceAddressImpl implements ActionsService<AddressDocument> {
    private final MongoAddressRepository mongoAddressRepository;
    private final MeterRegistry meterRegistry;

    public MongoDBServiceAddressImpl(MongoAddressRepository mongoAddressRepository, MeterRegistry meterRegistry) {
        this.mongoAddressRepository = mongoAddressRepository;
        this.meterRegistry = meterRegistry;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public DatabaseActionResponse saveAll(List<AddressDocument> entities) {
        long startCpu = getCpuUsage();
        long startMemory = getMemoryUsage();

        mongoAddressRepository.saveAll(entities);

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
}
