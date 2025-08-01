package org.example.databasetesting.repositories.postgresql;

import jakarta.transaction.Transactional;
import org.example.databasetesting.entities.postgresql.UserEntity;
import org.example.databasetesting.response.CityUserCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PostgresUserRepository extends JpaRepository<UserEntity, UUID> {
    long countByStatusAndCreditCard_ExpirationDateAfterAndAddress_Country(
            String status, LocalDate expirationDate, String country);

    @Query("SELECT u.address.city AS city, COUNT(u) AS userCount " +
            "FROM UserEntity u " +
            "WHERE u.status = :status " +
            "AND u.creditCard.expirationDate > :expirationDate " +
            "AND LOWER(u.address.city) LIKE LOWER(CONCAT('%', :cityKeyword, '%')) " +
            "GROUP BY u.address.city " +
            "ORDER BY userCount DESC")
    List<CityUserCountProjection> countUsersByCity(
            @Param("status") String status,
            @Param("expirationDate") LocalDate expirationDate,
            @Param("cityKeyword") String cityKeyword,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE users u
    SET status = :newStatus
    WHERE u.status = :oldStatus
      AND EXISTS (
          SELECT 1 FROM addresses a
          WHERE a.id = u.address_id AND a.city = :city
      )
      AND EXISTS (
          SELECT 1 FROM credit_cards c
          WHERE c.id = u.credit_card_id AND c.name = :name
      )
    """, nativeQuery = true)
    int updateUserStatusByCityAndCVV(
            String city,
            String oldStatus,
            String name,
            String newStatus
    );

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM UserEntity u
        WHERE u.status = :status
          AND u.address.city = :city
          AND u.creditCard.name = :cardName
    """)
    int deleteUsersByStatusAndCityAndCardName(String status, String city, String cardName);
}
