package org.example.databasetesting.entities.mongodb;

import jakarta.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserDocument {
    @Id
    private ObjectId id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String status;
    @DBRef
    private AddressDocument addressDocument;
    @DBRef
    private CreditCardDocument creditCardDocument;

    public UserDocument() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AddressDocument getAddressDocument() {
        return addressDocument;
    }

    public void setAddressDocument(AddressDocument addressDocument) {
        this.addressDocument = addressDocument;
    }

    public CreditCardDocument getCreditCardDocument() {
        return creditCardDocument;
    }

    public void setCreditCardDocument(CreditCardDocument creditCardDocument) {
        this.creditCardDocument = creditCardDocument;
    }
}
