package org.example.databasetesting.requests;

import org.example.databasetesting.utils.DatabaseType;

import java.util.List;

public class DatabaseActionRequest {
    private List<KeyValue> keyValueRequestList;
    private DatabaseType databaseType;

    public DatabaseActionRequest(List<KeyValue> keyValueRequestList, DatabaseType databaseType) {
        this.keyValueRequestList = keyValueRequestList;
        this.databaseType = databaseType;
    }

    public List<KeyValue> getKeyValueRequestList() {
        return keyValueRequestList;
    }

    public void setKeyValueRequestList(List<KeyValue> keyValueRequestList) {
        this.keyValueRequestList = keyValueRequestList;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }
}
