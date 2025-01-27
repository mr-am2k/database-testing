package org.example.databasetesting.entities.mongodb;

import jakarta.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;


@Document(collection = "products")
public class ProductDocument {
    @Id
    private ObjectId id;
    private String name;
    private String description;
    @DBRef
    private CategoryDocument category;
    private double startPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    @DBRef
    private UserDocument seller;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategoryDocument getCategory() {
        return category;
    }

    public void setCategory(CategoryDocument category) {
        this.category = category;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserDocument getSeller() {
        return seller;
    }

    public void setSeller(UserDocument seller) {
        this.seller = seller;
    }
}
