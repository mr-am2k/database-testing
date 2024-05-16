package org.example.databasetesting.entities.mongodb;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class KeyValueModel {
    private String key;
    private String value;

    public KeyValueModel() {
        // Nothing to do here
    }

    public KeyValueModel(String key, String value) {
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
