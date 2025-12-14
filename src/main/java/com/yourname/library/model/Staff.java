package com.yourname.library.model;

public class Staff extends AbstractUser {


    public Staff(int id, String firstName, String lastName, String tcNo, String email, String password, String staffNumber, String phone) {
        super(id, firstName, lastName, tcNo, email, password, staffNumber, phone);
    }

    public String getStaffNumber() { return getNumber(); }
    public void setStaffNumber(String staffNumber) { setNumber(staffNumber); }

    @Override
    public String getUserType() { return "Staff"; }
}