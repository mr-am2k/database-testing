package org.example.databasetesting.services.address;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoDBServiceImpl implements AddressService {
    @Override
    public List saveAll(List request, int batchSize) {
        return List.of();
    }
}
