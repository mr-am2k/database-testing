package org.example.databasetesting.services.user;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.UserDocument;
import org.example.databasetesting.repositories.mongodb.MongoUserRepository;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.response.CityUserCountProjectionMongo;
import org.example.databasetesting.services.ActionServiceComplex;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;
import org.example.databasetesting.entities.mongodb.UserDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MongoDBServiceUserImpl implements ActionServiceComplex<UserDocument> {
    private final MongoUserRepository mongoUserRepository;
    private final MeterRegistry meterRegistry;
    private final MongoTemplate mongoTemplate;

    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public MongoDBServiceUserImpl(MongoUserRepository mongoUserRepository, MeterRegistry meterRegistry, MongoTemplate mongoTemplate) {
        this.mongoUserRepository = mongoUserRepository;
        this.meterRegistry = meterRegistry;
        this.mongoTemplate = mongoTemplate;
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

    @Override
    public DatabaseActionResponse complexUpdate() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = updateStatusByCityStatusCVV("ACTIVE","Zavidovici" , "Muamer","DEACTIVATED");
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
    public DatabaseActionResponse complexDelete() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        long result = this.mongoUserRepository.deleteByStatusAndAddress_CityAndCreditCard_Name("DEACTIVATED","Zavidovici" , "Muamer");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("mongodb.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("mongodb.operation.avgMemoryUsage", avgMemory);

        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }



    private long updateStatusByCityStatusCVV(
            String oldStatus,
            String city,
            String name,
            String newStatus
    ) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(oldStatus));
        query.addCriteria(Criteria.where("address.city").is(city));
        query.addCriteria(Criteria.where("creditCard.name").is(name));

        Update update = new Update().set("status", newStatus);

        UpdateResult result = mongoTemplate.updateMulti(query, update, UserDocument.class);
        return result.getModifiedCount();
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
