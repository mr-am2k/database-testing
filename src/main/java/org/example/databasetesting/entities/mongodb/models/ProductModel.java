package org.example.databasetesting.entities.mongodb.models;

import java.util.Date;

public class ProductModel {
    private String name;
    private String description;
    private String categoryName;
    private double startPrice;
    private Date startDate;
    private Date endDate;
    private String status;
    private UserModel userModel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
