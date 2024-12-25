package org.example.databasetesting.response;

public class DatabaseActionResponse {
    private long time;
    private String cpuUsage;
    private String ramUsage;

    public DatabaseActionResponse(long time, String cpuUsage, String ramUsage) {
        this.time = time;
        this.cpuUsage = cpuUsage;
        this.ramUsage = ramUsage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getRamUsage() {
        return ramUsage;
    }

    public void setRamUsage(String ramUsage) {
        this.ramUsage = ramUsage;
    }
}
