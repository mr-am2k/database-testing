package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.KeyValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyValuePostgreRepository extends JpaRepository<KeyValueEntity, String> {
    KeyValueEntity findByKey(final String key);

    void deleteByKey(final String key);
}
