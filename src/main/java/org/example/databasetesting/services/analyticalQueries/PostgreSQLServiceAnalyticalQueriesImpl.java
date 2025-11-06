package org.example.databasetesting.services.analyticalQueries;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.repositories.postgresql.PostgreSQLBidRepository;
import org.example.databasetesting.response.AnalyticalQuery1Projection;
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
public class PostgreSQLServiceAnalyticalQueriesImpl implements ActionServiceAnalyticalQueries {
    private static final Logger log = LoggerFactory.getLogger(PostgreSQLServiceAnalyticalQueriesImpl.class);
    private final MeterRegistry meterRegistry;
    private final PostgreSQLBidRepository postgreSQLBidRepository;
    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public PostgreSQLServiceAnalyticalQueriesImpl(MeterRegistry meterRegistry, PostgreSQLBidRepository postgreSQLBidRepository) {
        this.meterRegistry = meterRegistry;
        this.postgreSQLBidRepository = postgreSQLBidRepository;
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
        AnalyticalQuery1Projection result = executeQuery(
                () -> postgreSQLBidRepository.getBidCountDistribution(),
                "postgres.analytical.query1",
                (projection) -> {
                    log.info("=== PostgreSQL Analytical Query 1 Results ===");
                    log.info("Average Bids per Product: {}", projection.getAvgBidsPerProduct());
                    log.info("Median Bids per Product: {}", projection.getMedianBidsPerProduct());
                    log.info("P90 Bids per Product: {}", projection.getP90BidsPerProduct());
                    log.info("=============================================");
                }
        );
        return createDatabaseActionResponse();
    }

    /**
     * Helper method to execute a query, track metrics, and log results.
     * Use this for analytical query methods that need result logging.
     *
     * @param query The query to execute (returns result of type T)
     * @param metricPrefix Prefix for metric names (e.g., "postgres.analytical.query1")
     * @param logger Consumer function to log the query result
     * @return The query result of type T
     */
    private <T> T executeQuery(Supplier<T> query, String metricPrefix, java.util.function.Consumer<T> logger) {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        T result = query.get();
        recordMetrics();

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
    //             () -> {
    //                 // Your query here - can return any projection type
    //                 SomeOtherProjection result = repository.someOtherMethod();
    //             },
    //             "postgres.analytical.query2"
    //     );
    // }
}
