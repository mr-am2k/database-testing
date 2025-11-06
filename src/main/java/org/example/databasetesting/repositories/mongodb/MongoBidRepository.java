package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.BidDocument;
import org.example.databasetesting.response.AnalyticalQuery1Projection;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.bson.types.ObjectId;

public interface MongoBidRepository extends MongoRepository<BidDocument, ObjectId> {

    /**
     * Computes distribution stats of bid counts per product:
     *  - average bids per product (rounded to 2 decimals)
     *  - median (p50) and p90 using percentile
     *
     * Notes:
     *  - Uses collection "bids" (as in @Document(collection = "bids"))
     *  - Product is embedded in bid document as: { product: { _id, name, ... } }
     *  - Filters out bids where product is null or doesn't exist
     *  - Groups by product._id (the embedded product's ObjectId)
     *  - The $percentile operator requires a 'method' field (approximate or discrete)
     *  - $percentile returns an array, so we use $arrayElemAt to extract the value
     */
    @Aggregation(pipeline = {
            "{ $group: { _id: '$product._id', bid_cnt: { $sum: 1 } } }",
            "{ $group: { " +
                    "_id: null, " +
                    "avg: { $avg: '$bid_cnt' }, " +
                    "all_counts: { $push: '$bid_cnt' } " +
                    "} }",
            "{ $set: { " +
                    "med: { $percentile: { input: '$all_counts', p: [0.5], method: 'approximate' } }, " +
                    "p90: { $percentile: { input: '$all_counts', p: [0.9], method: 'approximate' } } " +
                    "} }",
            "{ $project: { " +
                    "_id: 0, " +
                    "avg_bids_per_product: { $round: ['$avg', 2] }, " +
                    "median_bids_per_product: { $arrayElemAt: ['$med', 0] }, " +
                    "p90_bids_per_product: { $arrayElemAt: ['$p90', 0] } " +
                    "} }"
    })
    AnalyticalQuery1Projection getBidCountDistribution();
}

