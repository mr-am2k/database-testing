package org.example.databasetesting.services.analyticalQueries;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.repositories.mongodb.MongoBidRepository;
import org.example.databasetesting.response.AnalyticalQuery1Response;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionServiceAnalyticalQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Service
public class MongoDBServiceAnalyticalQueriesImpl implements ActionServiceAnalyticalQueries {
    private static final Logger log = LoggerFactory.getLogger(MongoDBServiceAnalyticalQueriesImpl.class);
    private final MeterRegistry meterRegistry;
    private final MongoBidRepository mongoBidRepository;
    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public MongoDBServiceAnalyticalQueriesImpl(MeterRegistry meterRegistry, MongoBidRepository mongoBidRepository) {
        this.meterRegistry = meterRegistry;
        this.mongoBidRepository = mongoBidRepository;
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
    public DatabaseActionResponse analyticalQuery1() {
        AnalyticalQuery1Response result = executeQuery(
                () -> mongoBidRepository.getBidCountDistribution(),
                "mongodb.analytical.query1",
                (projection) -> {
                    log.info("=== MongoDB Analytical Query 1 Results ===");
                    log.info("Average Bids per Product: {}", projection.getAvgBidsPerProduct());
                    log.info("Median Bids per Product: {}", projection.getMedianBidsPerProduct());
                    log.info("P90 Bids per Product: {}", projection.getP90BidsPerProduct());
                    log.info("==========================================");
                }
        );
        return createDatabaseActionResponse();
    }

    /**
     * Helper method to execute a query, track metrics, and log results.
     * Use this for analytical query methods that need result logging.
     * 
     * @param query The query to execute (returns result of type T)
     * @param metricPrefix Prefix for metric names (e.g., "mongodb.analytical.query1")
     * @param logger Consumer function to log the query result
     * @return The query result of type T
     */
    private <T> T executeQuery(Supplier<T> query, String metricPrefix, java.util.function.Consumer<T> logger) {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        T result = query.get();
        recordMetrics();

        // Log the query result
        if (result != null) {
            logger.accept(result);
        } else {
            log.warn("Query returned null result for {}", metricPrefix);
        }

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge(metricPrefix + ".avgCpuUsage", avgCpu);
        meterRegistry.gauge(metricPrefix + ".avgMemoryUsage", avgMemory);

        return result;
    }

    private DatabaseActionResponse createDatabaseActionResponse() {
        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());
        return new DatabaseActionResponse(0,
                String.format("%.2f%%", avgCpu / 100),
                String.format("%.2fMB", avgMemory / 1_048_576));
    }

    // Example: Add more query methods here
    // @Override
    // public DatabaseActionResponse analyticalQuery2() {
    //     return executeQuery(
    //             () -> mongoBidRepository.someOtherAnalyticalMethod(),
    //             "mongodb.analytical.query2",
    //             (result) -> {
    //                 // Log the result
    //             }
    //     );
    // }
}
