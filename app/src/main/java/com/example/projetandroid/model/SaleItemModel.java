package com.example.projetandroid.model;

import java.io.Serializable;

public class SaleItemModel implements Serializable {
    private String id;
    private String nom;
    private int quantite;
    private double prix_unitaire;
    
    // Empty constructor needed for Firebase
    public SaleItemModel() {
    }
    
    public SaleItemModel(String id, String nom, int quantite, double prix_unitaire) {
        this.id = id;
        this.nom = nom;
        this.quantite = quantite;
        this.prix_unitaire = prix_unitaire;
    }

    // Create from a product with quantity
    public SaleItemModel(ProductModel product, int quantite) {
        this.id = product.getId();
        this.nom = product.getNom();
        this.quantite = quantite;
        this.prix_unitaire = product.getPrix_unitaire();
    }
    
    // Calculate subtotal for this item
    public double getSubtotal() {
        return quantite * prix_unitaire;
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
    
    public int getQuantite() {
        return quantite;
    }
    
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    
    public double getPrix_unitaire() {
        return prix_unitaire;
    }
    
    public void setPrix_unitaire(double prix_unitaire) {
        this.prix_unitaire = prix_unitaire;
    }
}