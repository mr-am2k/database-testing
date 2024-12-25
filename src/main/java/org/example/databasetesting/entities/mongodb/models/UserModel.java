package org.example.databasetesting.entities.mongodb.models;

public class UserModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String status;
    private AddressModel address;
    private CreditCardModel creditCard;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AddressModel getAddress() {
        return address;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public CreditCardModel getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCardModel creditCard) {
        this.creditCard = creditCard;
    }
}
