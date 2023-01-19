package com.example.classAttendance;

public class AttendanceStatus {
    private String status;
    private String limit;

    public AttendanceStatus() {

    }

    public AttendanceStatus(String status, String limit) {
        this.status = status;
        this.limit = limit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
