package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.BidDocument;
import org.example.databasetesting.response.AnalyticalQuery1Response;
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
}

