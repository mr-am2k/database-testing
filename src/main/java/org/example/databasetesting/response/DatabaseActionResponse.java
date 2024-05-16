package org.example.databasetesting.response;

public class DatabaseActionResponse {
    private long time;

    public DatabaseActionResponse(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
