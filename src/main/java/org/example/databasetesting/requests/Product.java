package org.example.databasetesting.requests;

import org.example.databasetesting.entities.mongodb.ProductDocument;
import org.example.databasetesting.entities.mongodb.models.AddressModel;
import org.example.databasetesting.entities.mongodb.models.CreditCardModel;
import org.example.databasetesting.entities.mongodb.models.UserModel;
import org.example.databasetesting.entities.postgresql.*;

import java.time.LocalDate;


public class Product {
    private String name;
    private String description;
    private Double startPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String categoryName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String userStatus;
    private String address;
    private String city;
    private String country;
    private String zipCode;
    private String nameOnTheCard;
    private String cardNumber;
    private String cvv;
    private LocalDate expirationDate;

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

    public Double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(Double startPrice) {
        this.startPrice = startPrice;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartLocalDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndLocalDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getNameOnTheCard() {
        return nameOnTheCard;
    }

    public void setNameOnTheCard(String nameOnTheCard) {
        this.nameOnTheCard = nameOnTheCard;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationLocalDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public ProductEntity toProductEntity() {
        CategoryEntity categoryEntity = new CategoryEntity();
        CreditCardEntity creditCardEntity = new CreditCardEntity();
        AddressEntity addressEntity = new AddressEntity();
        UserEntity userEntity = new UserEntity();
        ProductEntity productEntity = new ProductEntity();

        categoryEntity.setName(this.categoryName);

        creditCardEntity.setName(this.nameOnTheCard);
        creditCardEntity.setCardNumber(this.cardNumber);
        creditCardEntity.setCvv(this.cvv);
        creditCardEntity.setExpirationDate(this.expirationDate);

        addressEntity.setAddress(this.address);
        addressEntity.setCity(this.city);
        addressEntity.setCountry(this.country);
        addressEntity.setZipCode(this.zipCode);

        userEntity.setFirstName(this.firstName);
        userEntity.setLastName(this.lastName);
        userEntity.setEmail(this.email);
        userEntity.setPassword(this.password);
        userEntity.setStatus(this.userStatus);
        userEntity.setStatus(this.userStatus);
        userEntity.setCreditCard(creditCardEntity);
        userEntity.setAddress(addressEntity);

        productEntity.setName(this.name);
        productEntity.setDescription(this.description);
        productEntity.setStartPrice(this.startPrice);
        productEntity.setStartDate(this.startDate);
        productEntity.setEndDate(this.startDate);
        productEntity.setStatus(this.status);
        productEntity.setCategory(categoryEntity);
        productEntity.setSeller(userEntity);

        return productEntity;
    }

    public ProductDocument toProductDocument() {
        AddressModel addressModel = new AddressModel();
        CreditCardModel creditCardModel = new CreditCardModel();
        UserModel userModel = new UserModel();
        ProductDocument productDocument = new ProductDocument();

        addressModel.setAddress(this.address);
        addressModel.setCity(this.city);
        addressModel.setCountry(this.country);
        addressModel.setZipCode(this.zipCode);

        creditCardModel.setCardNumber(this.cardNumber);
        creditCardModel.setCvv(this.cvv);
        creditCardModel.setExpirationDate(this.expirationDate);
        creditCardModel.setName(this.nameOnTheCard);

        userModel.setFirstName(this.firstName);
        userModel.setLastName(this.lastName);
        userModel.setEmail(this.email);
        userModel.setPassword(this.password);
        userModel.setStatus(this.userStatus);
        userModel.setAddress(addressModel);
        userModel.setCreditCard(creditCardModel);

        productDocument.setName(this.name);
        productDocument.setDescription(this.description);
        productDocument.setStatus(this.status);
        productDocument.setStartDate(this.startDate);
        productDocument.setEndDate(this.endDate);
        productDocument.setCategoryName(this.categoryName);
        productDocument.setStartPrice(this.startPrice);
        productDocument.setUserModel(userModel);

        return productDocument;
    }
}