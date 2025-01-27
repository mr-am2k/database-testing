package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostgresUserRepository extends JpaRepository<UserEntity, UUID> {
    //Page<UserEntity> findAllPage(Pageable pageable);
}
