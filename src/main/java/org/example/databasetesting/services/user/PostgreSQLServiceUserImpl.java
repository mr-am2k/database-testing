package org.example.databasetesting.services.user;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.example.databasetesting.entities.postgresql.UserEntity;
import org.example.databasetesting.repositories.postgresql.PostgresUserRepository;
import org.example.databasetesting.response.CityUserCountProjection;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionServiceComplex;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PostgreSQLServiceUserImpl implements ActionServiceComplex<UserEntity> {
    private final MeterRegistry meterRegistry;
    private final PostgresUserRepository postgresUserRepository;
    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public PostgreSQLServiceUserImpl(MeterRegistry meterRegistry, PostgresUserRepository postgresUserRepository) {
        this.meterRegistry = meterRegistry;
        this.postgresUserRepository = postgresUserRepository;
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
    public DatabaseActionResponse saveAll(List<?> entities) {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        List<UserEntity> users = (List<UserEntity>) entities;
        postgresUserRepository.saveAll(users);

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    @Override
    public DatabaseActionResponse getCount() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = postgresUserRepository.countByStatusAndCreditCard_ExpirationDateAfterAndAddress_Country("UNVERIFIED", LocalDate.of(2022,1,1), "Germany");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    @Override
    public DatabaseActionResponse getAggregation() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        List<CityUserCountProjection> result = postgresUserRepository.countUsersByCity("UNVERIFIED", LocalDate.of(2022,1,1), "new", PageRequest.of(0, 100));
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }
}