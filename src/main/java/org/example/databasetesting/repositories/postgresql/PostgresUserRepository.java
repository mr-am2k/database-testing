package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostgresUserRepository extends JpaRepository<UserEntity, UUID> {
}
