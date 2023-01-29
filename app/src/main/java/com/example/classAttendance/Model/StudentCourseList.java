package com.example.classAttendance.Model;

public class StudentCourseList {
    private String course;
    private String teacherName;
    private String examRoll;
    private String courseCode;

    public StudentCourseList() {
    }

    public StudentCourseList(String course, String teacherName, String examRoll) {
        this.course = course;
        this.teacherName = teacherName;
        this.examRoll = examRoll;

    }

    public StudentCourseList(String course, String teacherName, String examRoll, String courseCode) {
        this.course = course;
        this.teacherName = teacherName;
        this.examRoll = examRoll;
        this.courseCode = courseCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getExamRoll() {
        return examRoll;
    }

    public void setExamRoll(String examRoll) {
        this.examRoll = examRoll;
    }
}
