package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.UserEntity;
import org.example.databasetesting.response.CityUserCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
