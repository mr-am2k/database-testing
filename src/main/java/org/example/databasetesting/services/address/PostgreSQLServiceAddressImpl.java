package org.example.databasetesting.services.address;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.repositories.postgresql.PostgresAddressRepository;
import org.example.databasetesting.response.CountryCountProjection;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PostgreSQLServiceAddressImpl implements ActionsService<AddressEntity> {
    private final MeterRegistry meterRegistry;
    private final PostgresAddressRepository postgresAddressRepository;
    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);


    public PostgreSQLServiceAddressImpl(MeterRegistry meterRegistry, PostgresAddressRepository postgresAddressRepository) {
        this.meterRegistry = meterRegistry;
        this.postgresAddressRepository = postgresAddressRepository;
    }

    private synchronized void recordMetrics() {
        cpuMeasurements.get().add(getCpuUsage());
        memoryMeasurements.get().add(getMemoryUsage());
    }

    private double calculateAverage(List<Long> measurements) {
        synchronized (measurements) {
            return measurements.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
        }
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
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();

        this.postgresAddressRepository.saveAll(entities);

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.address.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.address.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    @Override
    public DatabaseActionResponse getCount() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = this.postgresAddressRepository.countByCountry("Bosnia and Herzegovina");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.address.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.address.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    @Override
    public DatabaseActionResponse getAggregation() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        List<CountryCountProjection> result = this.postgresAddressRepository.findTopCountriesByRecordCount("new", PageRequest.of(0, 100));
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.address.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.address.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }
}
