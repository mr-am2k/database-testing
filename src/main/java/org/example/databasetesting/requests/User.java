package org.example.databasetesting.requests;

import org.example.databasetesting.entities.mongodb.AddressDocument;
import org.example.databasetesting.entities.mongodb.CreditCardDocument;
import org.example.databasetesting.entities.mongodb.UserDocument;
import org.example.databasetesting.entities.postgresql.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


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

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public UserEntity toPostgresEntity() {
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
        userEntity.setCreditCard(creditCardEntity);
        userEntity.setAddress(addressEntity);

        return userEntity;
    }

    public Map<String, Object> toMongoDocument() {
        AddressDocument addressDocument = new AddressDocument();
        CreditCardDocument creditCardDocument = new CreditCardDocument();
        UserDocument userDocument = new UserDocument();

        //addressDocument.setId(new ObjectId());
        addressDocument.setAddress(this.address);
        addressDocument.setCity(this.city);
        addressDocument.setCountry(this.country);
        addressDocument.setZipCode(this.zipCode);

        //creditCardDocument.setId(new ObjectId());
        creditCardDocument.setCardNumber(this.cardNumber);
        creditCardDocument.setCvv(this.cvv);
        creditCardDocument.setExpirationDate(this.expirationDate);
        creditCardDocument.setName(this.nameOnTheCard);

        //userDocument.setId(UUID.randomUUID());
        userDocument.setFirstName(this.firstName);
        userDocument.setLastName(this.lastName);
        userDocument.setEmail(this.email);
        userDocument.setPassword(this.password);
        userDocument.setStatus(this.userStatus);
        userDocument.setAddressDocument(addressDocument);
        userDocument.setCreditCardDocument(creditCardDocument);

        Map<String, Object> map = new HashMap<>();
        map.put("address", addressDocument);
        map.put("creditCard", creditCardDocument);
        map.put("user", userDocument);

        return map;
    }
}