package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.BidEntity;
import org.example.databasetesting.response.AnalyticalQuery1Projection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostgreSQLBidRepository extends JpaRepository<BidEntity, Integer> {

    /**
     * Computes distribution stats of bid counts per product:
     *  - average bids per product (rounded to 2 decimals via numeric(10,2))
     *  - median (p50) and p90 using percentile_cont
     *
     * Notes:
     *  - Uses table "bids" (as in your @Table(name = "bids")).
     *  - If there are no bids, PostgreSQL percentile_cont will return NULL.
     */
    @Query(
            value = """
            WITH bid_counts AS (
                SELECT b.product_id, COUNT(*)::int AS bid_cnt
                FROM bids b
                GROUP BY b.product_id
            )
            SELECT
                AVG(bid_cnt)::numeric(10,2)                       AS avg_bids_per_product,
                percentile_cont(0.5) WITHIN GROUP (ORDER BY bid_cnt) AS median_bids_per_product,
                percentile_cont(0.9) WITHIN GROUP (ORDER BY bid_cnt) AS p90_bids_per_product
            FROM bid_counts
            """,
            nativeQuery = true
    )
    AnalyticalQuery1Projection getBidCountDistribution();
}
