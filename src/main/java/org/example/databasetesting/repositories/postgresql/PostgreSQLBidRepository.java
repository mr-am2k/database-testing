package org.example.databasetesting.repositories.postgresql;

import org.example.databasetesting.entities.postgresql.BidEntity;
import org.example.databasetesting.response.AnalyticalQuery1Projection;
import org.example.databasetesting.response.AnalyticalQuery2Projection;
import org.example.databasetesting.response.AnalyticalQuery4Projection;
import org.example.databasetesting.response.AnalyticalQuery5Projection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostgreSQLBidRepository extends JpaRepository<BidEntity, Integer> {
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

    @Query(
            value = """
            SELECT
              u.id AS user_id,
              u.email,
              COUNT(*) AS total_bids
            FROM bids b
            JOIN users u ON u.id = b.user_id
            GROUP BY u.id, u.email
            ORDER BY total_bids DESC, u.id ASC
            LIMIT 20
            """,
            nativeQuery = true
    )
    List<AnalyticalQuery4Projection> getTopBiddersByActivity();

    @Query(
            value = """
            SELECT
              a.country,
              COUNT(*) AS orders
            FROM order_details od
            JOIN addresses a ON a.id = od.address_id
            GROUP BY a.country
            ORDER BY orders DESC
            """,
            nativeQuery = true
    )
    List<AnalyticalQuery5Projection> getOrdersByCountry();
}
