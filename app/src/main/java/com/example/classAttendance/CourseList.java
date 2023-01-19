package com.example.classAttendance;

public class CourseList {

    private String courseName;
    private String courseCode;
    private String userEmail;

    public CourseList() {
    }

    public CourseList(String courseName, String courseCode, String userEmail) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.userEmail = userEmail;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
