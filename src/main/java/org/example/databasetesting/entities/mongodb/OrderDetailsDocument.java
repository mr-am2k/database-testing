package org.example.databasetesting.entities.mongodb;

import jakarta.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "order_details")
public class OrderDetailsDocument {
    @Id
    private ObjectId id;
    @DBRef
    private ProductDocument product;
    @DBRef
    private CategoryDocument category;
    @DBRef
    private UserDocument winner;
    @DBRef
    private AddressDocument shippingAddress;
    @DBRef
    private CreditCardDocument creditCard;
    private LocalDate orderDate;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ProductDocument getProduct() {
        return product;
    }

    public void setProduct(ProductDocument product) {
        this.product = product;
    }

    public CategoryDocument getCategory() {
        return category;
    }

    public void setCategory(CategoryDocument category) {
        this.category = category;
    }

    public UserDocument getWinner() {
        return winner;
    }

    public void setWinner(UserDocument winner) {
        this.winner = winner;
    }

    public AddressDocument getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressDocument shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public CreditCardDocument getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCardDocument creditCard) {
        this.creditCard = creditCard;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
}
