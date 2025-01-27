package org.example.databasetesting.entities.mongodb;

import jakarta.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;


@Document(collection = "bids")
public class BidDocument {
    @Id
    private ObjectId id;
    private double amount;
    private LocalDate bidTime;
    @DBRef
    private ProductDocument product;
    @DBRef
    private UserDocument user;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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

    public ProductDocument getProduct() {
        return product;
    }

    public void setProduct(ProductDocument product) {
        this.product = product;
    }

    public UserDocument getUser() {
        return user;
    }

    public void setUser(UserDocument user) {
        this.user = user;
    }
}
