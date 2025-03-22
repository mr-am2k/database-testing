package org.example.databasetesting.services.user;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.entities.mongodb.CreditCardDocument;
import org.example.databasetesting.entities.mongodb.UserDocument;
import org.example.databasetesting.repositories.mongodb.MongoAddressRepository;
import org.example.databasetesting.repositories.mongodb.MongoCreditCardRepository;
import org.example.databasetesting.repositories.mongodb.MongoUserRepository;
import org.example.databasetesting.response.CityUserCountProjectionMongo;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.response.UserCountProjection;
import org.example.databasetesting.services.ActionServiceComplex;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MongoDBServiceUserImpl implements ActionServiceComplex<UserDocument> {
    private final MongoUserRepository mongoUserRepository;
    private final MongoAddressRepository mongoAddressRepository;
    private final MongoCreditCardRepository mongoCreditCardRepository;
    private final MeterRegistry meterRegistry;

    private final ThreadLocal<List<Long>> cpuMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);
    private final ThreadLocal<List<Long>> memoryMeasurements = ThreadLocal.withInitial(CopyOnWriteArrayList::new);

    public MongoDBServiceUserImpl(MongoUserRepository mongoUserRepository,
                                  MongoAddressRepository mongoAddressRepository,
                                  MongoCreditCardRepository creditCardRepository,
                                  MeterRegistry meterRegistry) {
        this.mongoUserRepository = mongoUserRepository;
        this.mongoAddressRepository = mongoAddressRepository;
        this.mongoCreditCardRepository = creditCardRepository;
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
    public DatabaseActionResponse saveAll(Map<String, List<?>> entities) {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();
        recordMetrics(); // Initial measurement

        List<AddressDocument> addresses = (List<AddressDocument>) entities.get("address");
        List<CreditCardDocument> creditCards = (List<CreditCardDocument>) entities.get("creditCard");
        List<UserDocument> users = (List<UserDocument>) entities.get("user");

        validateInputLists(addresses, creditCards, users);
        recordMetrics();

        List<AddressDocument> savedAddresses = saveAddresses(addresses);
        recordMetrics();

        List<CreditCardDocument> savedCreditCards = saveCreditCards(creditCards);
        recordMetrics();

        List<UserDocument> linkedUsers = linkAndValidateUsers(users, savedAddresses, savedCreditCards);

        mongoUserRepository.saveAll(linkedUsers);

        recordMetrics();

        return calculateAverageResponse();
    }

    @Override
    public DatabaseActionResponse getCount() {
        cpuMeasurements.get().clear();
        memoryMeasurements.get().clear();

        recordMetrics();
        List<UserCountProjection> result = mongoUserRepository.countUnverifiedUsersWithValidCardAndAddress( "UNVERIFIED", LocalDate.of(2022,1,1), "Germany");
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
        List<CityUserCountProjectionMongo> result = mongoUserRepository.countUsersByCity("UNVERIFIED", LocalDate.of(2022,1,1), "new");
        recordMetrics();

        double avgCpu = calculateAverage(cpuMeasurements.get());
        double avgMemory = calculateAverage(memoryMeasurements.get());

        meterRegistry.gauge("postgres.operation.avgCpuUsage", avgCpu);
        meterRegistry.gauge("postgres.operation.avgMemoryUsage", avgMemory);

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

    private void validateInputLists(List<AddressDocument> addresses,
                                    List<CreditCardDocument> creditCards,
                                    List<UserDocument> users) {
        if (addresses == null || creditCards == null || users == null) {
            throw new IllegalArgumentException("Input lists cannot be null");
        }

        if (addresses.size() != users.size() || creditCards.size() != users.size()) {
            throw new IllegalArgumentException(
                    String.format("Size mismatch: Users=%d, Addresses=%d, CreditCards=%d",
                            users.size(), addresses.size(), creditCards.size()));
        }
    }

    private List<AddressDocument> saveAddresses(List<AddressDocument> addresses) {
        List<AddressDocument> savedAddresses = mongoAddressRepository.saveAll(addresses);
        if (savedAddresses.size() != addresses.size()) {
            throw new RuntimeException("Failed to save all addresses");
        }
        return savedAddresses;
    }

    private List<CreditCardDocument> saveCreditCards(List<CreditCardDocument> creditCards) {
        List<CreditCardDocument> savedCards = mongoCreditCardRepository.saveAll(creditCards);
        if (savedCards.size() != creditCards.size()) {
            throw new RuntimeException("Failed to save all credit cards");
        }
        return savedCards;
    }

    private List<UserDocument> linkAndValidateUsers(List<UserDocument> users,
                                                    List<AddressDocument> addresses,
                                                    List<CreditCardDocument> creditCards) {
        for (int i = 0; i < users.size(); i++) {
            UserDocument user = users.get(i);
            AddressDocument address = addresses.get(i);
            CreditCardDocument creditCard = creditCards.get(i);

            // Validate address matching
            if (!address.getZipCode().equals(user.getAddressDocument().getZipCode()) ||
                    !address.getCity().equals(user.getAddressDocument().getCity()) ||
                !address.getCountry().equals(user.getAddressDocument().getCountry()) ||
                !address.getAddress().equals(user.getAddressDocument().getAddress())
            ) {
                throw new IllegalStateException("Address mismatch for user index: " + i);
            }

            // Validate credit card matching
            if (!creditCard.getCardNumber().equals(user.getCreditCardDocument().getCardNumber()) ||
                    !creditCard.getCvv().equals(user.getCreditCardDocument().getCvv())) {
                throw new IllegalStateException("Credit card mismatch for user index: " + i);
            }

            user.setAddressDocument(address);
            user.setCreditCardDocument(creditCard);
        }
        return users;
    }

    private long getCpuUsage() {
        return (long) (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() * 100);
    }

    private long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}