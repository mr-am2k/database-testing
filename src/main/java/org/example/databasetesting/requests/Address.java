package org.example.databasetesting.requests;

import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.entities.postgresql.AddressEntity;

public class Address {
    private String address;
    private String city;
    private String country;
    private String zipCode;

    public AddressEntity toPostgreSQLEntity() {
        return new AddressEntity(address, city, country, zipCode);
    }

    public AddressDocument toMongoEntity() {
        return new AddressDocument(address, city, country, zipCode);
    }
}
