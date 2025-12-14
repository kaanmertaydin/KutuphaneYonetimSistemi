package com.yourname.library.model;

public class Student extends AbstractUser {


    public Student(int id, String firstName, String lastName, String tcNo, String email, String password, String studentNumber, String phone) {
        super(id, firstName, lastName, tcNo, email, password, studentNumber, phone);
    }

    public String getStudentNumber() { return getNumber(); }
    public void setStudentNumber(String studentNumber) { setNumber(studentNumber); }

    @Override
    public String getUserType() { return "Student"; }
}