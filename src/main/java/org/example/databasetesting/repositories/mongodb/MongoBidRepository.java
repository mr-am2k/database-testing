package org.example.databasetesting.repositories.mongodb;

import org.example.databasetesting.entities.mongodb.BidDocument;
import org.example.databasetesting.response.AnalyticalQuery1Response;
import org.example.databasetesting.response.AnalyticalQuery2Response;
import org.example.databasetesting.response.AnalyticalQuery4Response;
import org.example.databasetesting.response.AnalyticalQuery5Response;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.bson.types.ObjectId;

import java.util.List;

public interface MongoBidRepository extends MongoRepository<BidDocument, ObjectId> {
    @Aggregation(pipeline = {
            "{ $match: { product: { $exists: true, $ne: null } } }",
            "{ $group: { _id: '$product._id', bid_cnt: { $sum: 1 } } }",
            "{ $group: { " +
                    "_id: null, " +
                    "avg: { $avg: '$bid_cnt' }, " +
                    "all_counts: { $push: '$bid_cnt' } " +
                    "} }",
            "{ $set: { " +
                    "percentiles: { $percentile: { input: '$all_counts', p: [0.5, 0.9], method: 'approximate' } } " +
                    "} }",
            "{ $project: { " +
                    "_id: 0, " +
                    "avgBidsPerProduct: { $round: ['$avg', 2] }, " +
                    "medianBidsPerProduct: { $arrayElemAt: ['$percentiles', 0] }, " +
                    "p90BidsPerProduct: { $arrayElemAt: ['$percentiles', 1] } " +
                    "} }"
    })
    AnalyticalQuery1Response getBidCountDistribution();

    @Aggregation(pipeline = {
            "{ $group: { " +
                    "_id: '$user._id', " +
                    "cnt: { $sum: 1 } " +
                    "} }",

            "{ $group: { " +
                    "_id: null, " +
                    "all_counts: { $push: '$cnt' }, " +
                    "max: { $max: '$cnt' } " +
                    "} }",

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

    @Aggregation(pipeline = {
            // Stage 1: Group by user ID and count bids
            "{ $group: { " +
                    "_id: '$user._id', " +
                    "total_bids: { $sum: 1 }, " +
                    "email: { $first: '$user.email' } " +
                "} }",
            // Stage 2: Sort by total_bids descending, then _id ascending (before limit for better performance)
            "{ $sort: { total_bids: -1, _id: 1 } }",
            // Stage 3: Limit to top 20 (before projection to reduce overhead)
            "{ $limit: 20 }",
            // Stage 4: Project with camelCase field names (only projects top 20, not all documents)
            "{ $project: { " +
                    "_id: 0, " +
                    "userId: { $toString: '$_id' }, " +
                    "email: 1, " +
                    "totalBids: { $toLong: '$total_bids' } " +
                "} }"
    })
    List<AnalyticalQuery4Response> getTopBiddersByActivity();
}
