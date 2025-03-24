package org.example.databasetesting.services.user;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.UserDocument;
import org.example.databasetesting.repositories.mongodb.MongoUserRepository;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.response.CityUserCountProjectionMongo;
import org.example.databasetesting.services.ActionServiceComplex;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MongoDBServiceUserImpl implements ActionServiceComplex<UserDocument> {
    private final MongoUserRepository mongoUserRepository;
    private final MeterRegistry meterRegistry;

    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public MongoDBServiceUserImpl(MongoUserRepository mongoUserRepository, MeterRegistry meterRegistry) {
        this.mongoUserRepository = mongoUserRepository;
        this.meterRegistry = meterRegistry;
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

    @Override
    public DatabaseActionResponse saveAll(List<?> entities) {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        List<UserDocument> users = (List<UserDocument>) entities;

        recordMetrics();
        mongoUserRepository.saveAll(users);
        recordMetrics();

        return calculateAverageResponse();
    }

    @Override
    public DatabaseActionResponse getCount() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = mongoUserRepository.countByStatusAndCreditCardExpirationDateAfterAndAddressCountryRegex("UNVERIFIED", LocalDate.of(2022,1,1), "Germany");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("mongodb.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("mongodb.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    @Override
    public DatabaseActionResponse getAggregation() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        List<CityUserCountProjectionMongo> result = mongoUserRepository.countUsersByCity("UNVERIFIED", LocalDate.of(2022,1,1), "new");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("mongodb.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("mongodb.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    private DatabaseActionResponse calculateAverageResponse() {
        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("mongodb.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("mongodb.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
