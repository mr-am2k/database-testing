package org.example.databasetesting.services.address;

import java.util.List;

public interface AddressService<T> {
    List<T> saveAll(List<T> request, int batchSize);

}
