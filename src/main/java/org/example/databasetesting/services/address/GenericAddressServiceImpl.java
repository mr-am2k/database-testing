package org.example.databasetesting.services.address;

import org.example.databasetesting.requests.Address;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.CSVUtil;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumMap;
import java.util.List;

@Service
public class GenericAddressServiceImpl implements GenericAddressService {

    private final EnumMap<DatabaseType, AddressService> strategies = new EnumMap<>(DatabaseType.class);

    @Autowired
    public GenericAddressServiceImpl(PostgreSQLServiceImpl postgreSQLService, MongoDBServiceImpl mongoDBService) {
        strategies.put(DatabaseType.POSTGRESQL, postgreSQLService);
        strategies.put(DatabaseType.MONGODB, mongoDBService);
    }

    @Override
    public DatabaseActionResponse saveAll(MultipartFile file, DatabaseType databaseType, int batchSize) {
        final List<Address> requestValues = CSVUtil.parseCSV(file, Address.class);

        List<?> entityValues = switch (databaseType) {
            case MONGODB -> requestValues.stream().map(Address::toMongoEntity).toList();
            case POSTGRESQL -> requestValues.stream().map(Address::toPostgreSQLEntity).toList();
        };

        final long startTime = System.nanoTime();

        strategies.get(databaseType).saveAll(entityValues, batchSize);

        final long endTime = System.nanoTime();

        final long duration = (endTime - startTime) / 1_000_000;

        return new DatabaseActionResponse(duration);
    }
}
