package org.example.databasetesting.services.analyticalQueries;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.repositories.mongodb.MongoBidRepository;
import org.example.databasetesting.repositories.mongodb.MongoOrderDetailsRepository;
import org.example.databasetesting.response.AnalyticalQuery1Response;
import org.example.databasetesting.response.AnalyticalQuery2Response;
import org.example.databasetesting.response.AnalyticalQuery3Response;
import org.example.databasetesting.response.AnalyticalQuery4Response;
import org.example.databasetesting.response.AnalyticalQuery5Response;
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
    private final MongoOrderDetailsRepository mongoOrderDetailsRepository;
    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public MongoDBServiceAnalyticalQueriesImpl(MeterRegistry meterRegistry,
                                                MongoBidRepository mongoBidRepository,
                                                MongoOrderDetailsRepository mongoOrderDetailsRepository) {
        this.meterRegistry = meterRegistry;
        this.mongoBidRepository = mongoBidRepository;
        this.mongoOrderDetailsRepository = mongoOrderDetailsRepository;
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

    @Override
    public DatabaseActionResponse analyticalQuery2() {
        AnalyticalQuery2Response result = executeQuery(
                () -> mongoBidRepository.getUserBiddingStatistics(),
                "mongodb.analytical.query2",
                (stats) -> {
                    log.info("=== MongoDB Analytical Query 2 Results ===");
                    log.info("User Bidding Statistics:");
                    log.info("  Median bids per user: {}", stats.getMedianBidsPerUser());
                    log.info("  P90 bids per user: {}", stats.getP90BidsPerUser());
                    log.info("  Max bids by a user: {}", stats.getMaxBidsByAUser());
                    log.info("==========================================");
                }
        );
        return createDatabaseActionResponse();
    }

    @Override
    public DatabaseActionResponse analyticalQuery3() {
        List<AnalyticalQuery3Response> results = executeQuery(
                () -> mongoOrderDetailsRepository.getTopCategoriesBySales(),
                "mongodb.analytical.query3",
                (categoryStats) -> {
                    log.info("=== MongoDB Analytical Query 3 Results ===");
                    log.info("Top 10 Categories by Sales - {} categories:", categoryStats.size());
                    categoryStats.forEach(stat -> {
                        log.info("  Category: {}, Total Sold: {}, Revenue: ${}", 
                                stat.getCategoryName(), stat.getTotalSold(), stat.getTotalRevenue());
                    });
                    log.info("==========================================");
                }
        );
        return createDatabaseActionResponse();
    }

    @Override
    public DatabaseActionResponse analyticalQuery4() {
        List<AnalyticalQuery4Response> results = executeQuery(
                () -> mongoBidRepository.getTopBiddersByActivity(),
                "mongodb.analytical.query4",
                (bidderStats) -> {
                    log.info("=== MongoDB Analytical Query 4 Results ===");
                    log.info("Top 20 Users by Total Bids - {} users:", bidderStats.size());
                    bidderStats.stream().limit(5).forEach(stat -> {
                        log.info("  User ID: {}, Email: {}, Total Bids: {}", 
                                stat.getUserId(), stat.getEmail(), stat.getTotalBids());
                    });
                    log.info("==========================================");
                }
        );
        return createDatabaseActionResponse();
    }

    @Override
    public DatabaseActionResponse analyticalQuery5() {
        List<AnalyticalQuery5Response> results = executeQuery(
                () -> mongoOrderDetailsRepository.getOrdersByCountry(),
                "mongodb.analytical.query5",
                (countryData) -> {
                    log.info("=== MongoDB Analytical Query 5 Results ===");
                    log.info("Orders by Country - {} countries:", countryData.size());
                    countryData.stream().limit(10).forEach(data -> {
                        log.info("  Country: {}, Orders: {}", 
                                data.getCountry(), data.getOrders());
                    });
                    if (countryData.size() > 10) {
                        log.info("  ... and {} more", countryData.size() - 10);
                    }
                    log.info("==========================================");
                }
        );
        return createDatabaseActionResponse();
    }
}
