package org.example.databasetesting.services.address;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchProcessingService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void processBatch(List<AddressEntity> batch, int batchSize) {
        for (int i = 0; i < batch.size(); i++) {
            entityManager.persist(batch.get(i));
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }
}

