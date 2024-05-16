package org.example.databasetesting.entities.postgresql;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "key_value")
public class KeyValueEntity {
    @Id
    private String key;
    private String value;

    public KeyValueEntity() {
        // Nothing to do here
    }

    public KeyValueEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
