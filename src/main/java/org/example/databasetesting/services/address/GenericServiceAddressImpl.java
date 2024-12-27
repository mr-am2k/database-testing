package org.example.databasetesting.services.address;

import org.example.databasetesting.requests.Address;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.ActionsService;
import org.example.databasetesting.utils.CSVUtil;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Service
public class GenericServiceAddressImpl implements GenericServiceAddress {

    private final EnumMap<DatabaseType, ActionsService> strategies = new EnumMap<>(DatabaseType.class);

    public GenericServiceAddressImpl(
            PostgreSQLServiceAddressImpl postgreSQLService,
            MongoDBServiceAddressImpl mongoDBService
    ) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    @Override
    public DatabaseActionResponse saveAllSimple(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final List<Address> requestValues = CSVUtil.parseCSV(file, Address.class);

        List<?> entityValues = switch (databaseType) {
            case MONGODB -> requestValues.stream().map(Address::toMongoEntity).toList();
            case POSTGRESQL -> requestValues.stream().map(Address::toPostgresEntity).toList();
        };

        List<? extends List<?>> batches = splitIntoBatches(entityValues, batchSize);

        final long startTime = System.nanoTime();

        DatabaseActionResponse databaseActionResponse = strategies.get(databaseType).saveAll(batches, batchSize);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration, databaseActionResponse.getCpuUsage(), databaseActionResponse.getRamUsage());
    }

    public <T> List<List<T>> splitIntoBatches(List<T> request, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < request.size(); i += batchSize) {
            int end = Math.min(i + batchSize, request.size());
            batches.add(request.subList(i, end));
        }
        return batches;
    }
}
