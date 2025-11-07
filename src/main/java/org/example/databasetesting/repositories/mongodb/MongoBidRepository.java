package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.BidDocument;
import org.example.databasetesting.response.AnalyticalQuery1Response;
import org.example.databasetesting.response.AnalyticalQuery2Response;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.bson.types.ObjectId;

public interface MongoBidRepository extends MongoRepository<BidDocument, ObjectId> {

    /**
     * Computes distribution stats of bid counts per product.
     * 
     * Pipeline stages:
     *  1. Match bids with non-null products
     *  2. Group by product._id and count bids per product
     *  3. Calculate average and collect all counts for percentile calculation
     *  4. Compute percentiles (median and p90) using $percentile operator
     *  5. Project final results with rounded average and extracted percentiles
     *
     * Important Notes:
     *  - Uses collection "bids" (as in @Document(collection = "bids"))
     *  - Product is embedded in bid document as: { product: { _id, name, ... } }
     *  - The $percentile operator requires MongoDB 7.0+ with 'method' field
     *  - $percentile returns an array, so we use $arrayElemAt to extract values
     *  - Field names in final projection use camelCase to match AnalyticalQuery1Response properties
     */
    @Aggregation(pipeline = {
            // Stage 1: Match bids where product exists and is not null
            "{ $match: { product: { $exists: true, $ne: null } } }",
            
            // Stage 2: Group by product._id and count bids per product
            "{ $group: { _id: '$product._id', bid_cnt: { $sum: 1 } } }",
            
            // Stage 3: Calculate average and collect all counts
            "{ $group: { " +
                    "_id: null, " +
                    "avg: { $avg: '$bid_cnt' }, " +
                    "all_counts: { $push: '$bid_cnt' } " +
                    "} }",
            
            // Stage 4: Add percentile calculations (single operation for both percentiles)
            "{ $set: { " +
                    "percentiles: { $percentile: { input: '$all_counts', p: [0.5, 0.9], method: 'approximate' } } " +
                    "} }",
            
            // Stage 5: Project final results with camelCase field names
            "{ $project: { " +
                    "_id: 0, " +
                    "avgBidsPerProduct: { $round: ['$avg', 2] }, " +
                    "medianBidsPerProduct: { $arrayElemAt: ['$percentiles', 0] }, " +
                    "p90BidsPerProduct: { $arrayElemAt: ['$percentiles', 1] } " +
                    "} }"
    })
    AnalyticalQuery1Response getBidCountDistribution();

    /**
     * Analytical Query 2: User bidding behavior statistics for MongoDB
     *
     * Computes statistics about bids per user:
     *  - Median bids per user (p50)
     *  - P90 bids per user
     *  - Maximum bids by any single user
     *
     * Pipeline stages (3 total):
     *  1. Group by user._id and count bids per user
     *  2. Group all results and calculate percentiles using $percentile operator
     *  3. Project results with proper field names
     *
     * MongoDB Schema Notes:
     *  - Operates on 'bids' collection (this repository's entity)
     *  - bids have embedded 'user' object with _id
     *  - Uses $percentile operator (MongoDB 7.0+) with approximate method
     *  - Returns single document with aggregate statistics
     */
    @Aggregation(pipeline = {
            "{ $group: { " +
                    "_id: '$user._id', " +
                    "cnt: { $sum: 1 } " +
                    "} }",

            // Stage 2: Calculate statistics across all users
            "{ $group: { " +
                    "_id: null, " +
                    "all_counts: { $push: '$cnt' }, " +
                    "max: { $max: '$cnt' } " +
                    "} }",

            // Stage 3: Calculate percentiles and project with camelCase field names
            "{ $project: { " +
                    "_id: 0, " +
                    "medianBidsPerUser: { " +
                    "$arrayElemAt: [ " +
                    "{ $percentile: { input: '$all_counts', p: [0.5], method: 'approximate' } }, " +
                    "0 " +
                    "] " +
                    "}, " +
                    "p90BidsPerUser: { " +
                    "$arrayElemAt: [ " +
                    "{ $percentile: { input: '$all_counts', p: [0.9], method: 'approximate' } }, " +
                    "0 " +
                    "] " +
                    "}, " +
                    "maxBidsByAUser: { $toLong: '$max' } " +
                    "} }"
    })
    AnalyticalQuery2Response getUserBiddingStatistics();
}

