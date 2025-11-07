package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.CategoryEntity;
import org.example.databasetesting.response.AnalyticalQuery2Projection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostgreSQLCategoryRepository extends JpaRepository<CategoryEntity, Integer> {

    /**
     * Analytical Query 2: User bidding behavior statistics
     * 
     * Computes statistics about bids per user:
     *  - Median bids per user (p50)
     *  - P90 bids per user
     *  - Maximum bids by any single user
     *  
     * Uses CTE to first count bids per user, then calculates percentiles
     */
    @Query(
            value = """
            SELECT
              percentile_cont(0.5) WITHIN GROUP (ORDER BY cnt) AS median_bids_per_user,
              percentile_cont(0.9) WITHIN GROUP (ORDER BY cnt) AS p90_bids_per_user,
              max(cnt) AS max_bids_by_a_user
            FROM (
              SELECT user_id, COUNT(*) AS cnt
              FROM bids
              GROUP BY user_id
            ) s
            """,
            nativeQuery = true
    )
    AnalyticalQuery2Projection getUserBiddingStatistics();
}

