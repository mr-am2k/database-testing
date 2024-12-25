package org.example.databasetesting.entities.mongodb;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "bids")
public class BidDocument {
    @Id
    private String id;
    private double amount;
    private LocalDate bidTime;
    private UUID productId;
    private UUID userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDate bidTime) {
        this.bidTime = bidTime;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
