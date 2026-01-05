package com.example.projetandroid.model;

import java.io.Serializable;

public class ClientModel implements Serializable {
    private String nom;
    private String telephone;
    private double latitude;
    private double longitude;
    
    // Empty constructor needed for Firebase
    public ClientModel() {
    }
    
    public ClientModel(String nom, String telephone, double latitude, double longitude) {
        this.nom = nom;
        this.telephone = telephone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}