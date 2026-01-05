package com.example.projetandroid.model;

public class UserModel {
    private String businessName;
    private String phoneNumber;

    public UserModel() {
        // Required empty constructor for Firebase
        this.businessName = "";
        this.phoneNumber = "";
    }

    public UserModel(String businessName, String phoneNumber) {
        this.businessName = businessName != null ? businessName : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
    }

    public String getBusinessName() {
        return businessName != null ? businessName : "";
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName != null ? businessName : "";
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : "";
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
    }
}