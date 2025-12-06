package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.CategoryEntity;
import org.example.databasetesting.response.AnalyticalQuery3Projection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostgreSQLCategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    @Query(
            value = """
            SELECT
              c.name AS category_name,
              COUNT(*) AS total_sold,
              SUM(p.start_price) AS total_revenue
            FROM order_details od
            JOIN products p ON p.id = od.product_id
            JOIN categories c ON c.id = p.category_id
            GROUP BY c.name
            ORDER BY total_sold DESC, c.name ASC
            LIMIT 10
            """,
            nativeQuery = true
    )
    List<AnalyticalQuery3Projection> getTopCategoriesBySales();
}

