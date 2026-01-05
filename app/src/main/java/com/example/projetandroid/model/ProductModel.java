package com.example.projetandroid.model;

import java.io.Serializable;

public class ProductModel implements Serializable {
    private String id;
    private String nom;
    private double prix_unitaire;
    private int stock;
    
    // Empty constructor needed for Firebase
    public ProductModel() {
    }
    
    public ProductModel(String id, String nom, double prix_unitaire, int stock) {
        this.id = id;
        this.nom = nom;
        this.prix_unitaire = prix_unitaire;
        this.stock = stock;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public double getPrix_unitaire() {
        return prix_unitaire;
    }
    
    public void setPrix_unitaire(double prix_unitaire) {
        this.prix_unitaire = prix_unitaire;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
}