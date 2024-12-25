package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostgresProductRepository extends JpaRepository<ProductEntity, UUID> {
}
