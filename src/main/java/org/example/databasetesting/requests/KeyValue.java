package org.example.databasetesting.requests;

import org.example.databasetesting.entities.mongodb.KeyValueModel;
import org.example.databasetesting.entities.postgresql.KeyValueEntity;

public class KeyValue {
    private String key;
    private String value;

    public KeyValue(String key, String value) {
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

    public KeyValueEntity toPostgreEntity() {
        return new KeyValueEntity(this.key, this.value);
    }

    public KeyValueModel toMongoDBDocument() {
        return new KeyValueModel(this.key, this.value);
    }
}
