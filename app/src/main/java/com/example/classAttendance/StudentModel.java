package com.example.classAttendance;

import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentModel {
    private String name;
    private String examRoll;
    private String email;
    private String ImageURI;
    private String password;
    private CircleImageView imageView;

    public StudentModel() {
    }

    public StudentModel(String name, String examRoll, String email, String password) {
        this.name = name;
        this.examRoll = examRoll;
        this.email = email;
        this.password = password;
    }

    public static Comparator<StudentModel> sortByName = new Comparator<StudentModel>() {
        @Override
        public int compare(StudentModel t1, StudentModel t2) {
            return t1.getName().compareTo(t2.getName());
        }
    };
    public static Comparator<StudentModel> sortByRoll = new Comparator<StudentModel>() {
        @Override
        public int compare(StudentModel t1, StudentModel t2) {
            return t1.getExamRoll().compareTo(t2.getExamRoll());
        }
    };

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

    public String getImageURI() {
        return ImageURI;
    }

    public void setImageURI(String imageURI) {
        ImageURI = imageURI;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CircleImageView getImageView() {
        return imageView;
    }

    public void setImageView(CircleImageView imageView) {
        this.imageView = imageView;
    }
}
