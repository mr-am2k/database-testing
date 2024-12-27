package org.example.databasetesting.requests;

import org.example.databasetesting.entities.mongodb.UserDocument;
import org.example.databasetesting.entities.mongodb.models.AddressModel;
import org.example.databasetesting.entities.mongodb.models.CreditCardModel;
import org.example.databasetesting.entities.postgresql.*;

import java.time.LocalDate;


public class User {
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

    public UserEntity toUserEntity() {
        CreditCardEntity creditCardEntity = new CreditCardEntity();
        AddressEntity addressEntity = new AddressEntity();
        UserEntity userEntity = new UserEntity();

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

        return userEntity;
    }

    public UserDocument toUserDocument() {
        AddressModel addressModel = new AddressModel();
        CreditCardModel creditCardModel = new CreditCardModel();
        UserDocument userDocument = new UserDocument();

        addressModel.setAddress(this.address);
        addressModel.setCity(this.city);
        addressModel.setCountry(this.country);
        addressModel.setZipCode(this.zipCode);

        creditCardModel.setCardNumber(this.cardNumber);
        creditCardModel.setCvv(this.cvv);
        creditCardModel.setExpirationDate(this.expirationDate);
        creditCardModel.setName(this.nameOnTheCard);

        userDocument.setFirstName(this.firstName);
        userDocument.setLastName(this.lastName);
        userDocument.setEmail(this.email);
        userDocument.setPassword(this.password);
        userDocument.setStatus(this.userStatus);
        userDocument.setAddress(addressModel);
        userDocument.setCreditCard(creditCardModel);

        return userDocument;
    }
}