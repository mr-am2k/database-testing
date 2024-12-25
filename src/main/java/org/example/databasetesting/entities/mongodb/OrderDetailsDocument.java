package org.example.databasetesting.entities.mongodb;

import jakarta.persistence.Id;
import org.example.databasetesting.entities.mongodb.models.AddressModel;
import org.example.databasetesting.entities.mongodb.models.CreditCardModel;
import org.example.databasetesting.entities.mongodb.models.ProductModel;
import org.example.databasetesting.entities.mongodb.models.UserModel;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "order_details")
public class OrderDetailsDocument {
    @Id
    private String id;
    private ProductModel product;
    private UserModel winner;
    private AddressModel shippingAddress;
    private CreditCardModel paymentMethod;
    private Date orderDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProductModel getProduct() {
        return product;
    }

    public void setProduct(ProductModel product) {
        this.product = product;
    }

    public UserModel getWinner() {
        return winner;
    }

    public void setWinner(UserModel winner) {
        this.winner = winner;
    }

    public AddressModel getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressModel shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public CreditCardModel getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(CreditCardModel paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
}
