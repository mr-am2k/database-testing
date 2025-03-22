package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.AddressEntity;
import org.example.databasetesting.response.CountryCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostgresAddressRepository extends JpaRepository<AddressEntity, UUID> {
    long countByCountry(String country);

    @Query("SELECT a.country AS country, COUNT(a) AS count " +
            "FROM AddressEntity a " +
            "WHERE LOWER(a.city) LIKE LOWER(CONCAT('%', :cityKeyword, '%')) " +
            "GROUP BY a.country " +
            "ORDER BY COUNT(a) DESC")
    List<CountryCountProjection> findTopCountriesByRecordCount(@Param("cityKeyword") String cityKeyword, Pageable pageable);
}
