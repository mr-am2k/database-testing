package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
}