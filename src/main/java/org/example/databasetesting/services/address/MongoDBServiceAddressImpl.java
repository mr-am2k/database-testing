package org.example.databasetesting.services.address;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.repositories.mongodb.MongoAddressRepository;
import org.example.databasetesting.response.CountryCountProjectionMongo;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Criteria;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class MongoDBServiceAddressImpl implements ActionsService<AddressDocument> {
    private final MongoAddressRepository mongoAddressRepository;
    private final MongoTemplate mongoTemplate;
    private final MeterRegistry meterRegistry;
    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public MongoDBServiceAddressImpl(MongoAddressRepository mongoAddressRepository, MeterRegistry meterRegistry, MongoTemplate mongoTemplate) {
        this.mongoAddressRepository = mongoAddressRepository;
        this.meterRegistry = meterRegistry;
        this.mongoTemplate = mongoTemplate;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
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
    public DatabaseActionResponse saveAll(List<AddressDocument> entities) {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();

        mongoAddressRepository.saveAll(entities);

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("mongodb.address.avgCpuUsage", avgCpu);
        meterRegistry.gauge("mongodb.address.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    @Override
    public DatabaseActionResponse getCount() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = this.mongoAddressRepository.countByCountry("Bosnia and Herzegovina");
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
        List<CountryCountProjectionMongo> result = this.mongoAddressRepository.findTopCountriesByRecordCount("new");
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
    public DatabaseActionResponse simpleUpdate() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = this.updateCityByCity("Zavidovici", "Sarajevo");
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
    public DatabaseActionResponse simpleDelete() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = this.mongoAddressRepository.deleteByCity("Sarajevo");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.address.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.address.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    private long updateCityByCity(String oldCity, String newCity) {
        Query query = new Query(Criteria.where("city").is(oldCity));
        Update update = new Update().set("city", newCity);

        UpdateResult result = this.mongoTemplate.updateMulti(query, update, AddressDocument.class);
        return result.getModifiedCount();
    }
}
