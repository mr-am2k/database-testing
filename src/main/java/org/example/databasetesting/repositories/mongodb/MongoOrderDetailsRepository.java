package org.example.databasetesting.repositories.mongodb;

import org.bson.types.ObjectId;
import org.example.databasetesting.entities.mongodb.OrderDetailsDocument;
import org.example.databasetesting.response.AnalyticalQuery3Response;
import org.example.databasetesting.response.AnalyticalQuery5Response;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoOrderDetailsRepository extends MongoRepository<OrderDetailsDocument, ObjectId> {
    @Aggregation(pipeline = {
            "{ $match: { " +
                    "category: { $exists: true, $ne: null }, " +
                    "\"category.categoryName\": { $exists: true, $ne: null }, " +
                    "product: { $exists: true, $ne: null }, " +
                    "\"product.startPrice\": { $exists: true, $ne: null } " +
                "} }",
            "{ $group: { " +
                    "_id: '$category.categoryName', " +
                    "totalSold: { $sum: 1 }, " +
                    "totalRevenue: { $sum: '$product.startPrice' } " +
                "} }",
            "{ $sort: { totalSold: -1, _id: 1 } }",
            "{ $limit: 10 }",
            "{ $project: { " +
                    "_id: 0, " +
                    "categoryName: '$_id', " +
                    "totalSold: { $toLong: '$totalSold' }, " +
                    "totalRevenue: 1 " +
                "} }"
    })
    List<AnalyticalQuery3Response> getTopCategoriesBySales();

    @Aggregation(pipeline = {
            "{ $group: { " +
                    "_id: '$shippingAddress.country', " +
                    "orders: { $sum: 1 } " +
                "} }",
            "{ $project: { " +
                    "_id: 0, " +
                    "country: '$_id', " +
                    "orders: { $toLong: '$orders' } " +
                "} }",
            
            // Stage 3: Sort by orders descending
            "{ $sort: { orders: -1 } }"
    })
    List<AnalyticalQuery5Response> getOrdersByCountry();

}
