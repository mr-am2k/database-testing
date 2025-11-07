package org.example.databasetesting.services.analyticalQueries;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionServiceAnalyticalQueries;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;

@Service
public class GenericServiceAnalyticalQueriesImpl implements GenericServiceAnalyticalQueries {
    private final EnumMap<DatabaseType, ActionServiceAnalyticalQueries> strategies = new EnumMap<>(DatabaseType.class);

    public GenericServiceAnalyticalQueriesImpl(
            PostgreSQLServiceAnalyticalQueriesImpl postgreSQLService,
            MongoDBServiceAnalyticalQueriesImpl mongoDBService) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    /**
     * Helper method to execute a query and track total execution time.
     * The individual database services already track CPU and RAM.
     */
    private DatabaseActionResponse executeWithTimeTracking(
            DatabaseType databaseType,
            java.util.function.Function<ActionServiceAnalyticalQueries, DatabaseActionResponse> queryExecutor) {
        long duration;

        final long startTime = System.nanoTime();

        final DatabaseActionResponse response = queryExecutor.apply(strategies.get(databaseType));

        final long endTime = System.nanoTime();

        duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(
                duration,
                response.getCpuUsage(),
                response.getRamUsage()
        );
    }

    @Override
    public DatabaseActionResponse analyticalQuery1(DatabaseType databaseType) {
        return executeWithTimeTracking(databaseType, ActionServiceAnalyticalQueries::analyticalQuery1);
    }

    @Override
    public DatabaseActionResponse analyticalQuery2(DatabaseType databaseType) {
        return executeWithTimeTracking(databaseType, ActionServiceAnalyticalQueries::analyticalQuery2);
    }
}
