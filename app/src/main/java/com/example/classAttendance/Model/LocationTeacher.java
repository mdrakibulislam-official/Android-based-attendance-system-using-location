package com.example.classAttendance.Model;

public class LocationTeacher {
    private String latitude;
    private String longitude;

    public LocationTeacher() {
    }

    public LocationTeacher(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
