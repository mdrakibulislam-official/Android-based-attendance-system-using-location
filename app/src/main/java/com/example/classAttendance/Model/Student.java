package com.example.classAttendance.Model;

public class Student {
    private String name;
    private String examRoll;
    private String email;
    private String password;

    public Student() {
    }

    public Student(String name, String examRoll, String email, String password) {
        this.name = name;
        this.examRoll = examRoll;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExamRoll() {
        return examRoll;
    }

    public void setExamRoll(String examRoll) {
        this.examRoll = examRoll;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
