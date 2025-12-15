package com.myapp.rollcall.model;

public class Student {
    private String studentId;
    private String name;
    private String gender;
    private String clazz;
    private String photoPath;

    public Student() {}

    public Student(String studentId, String name, String gender, String clazz, String photoPath) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.clazz = clazz;
        this.photoPath = photoPath;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getClazz() { return clazz; }
    public void setClazz(String clazz) { this.clazz = clazz; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}

