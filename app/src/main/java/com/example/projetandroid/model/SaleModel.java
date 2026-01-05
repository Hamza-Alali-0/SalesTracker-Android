// filepath: c:\Users\HP\Desktop\projetandroid\app\src\main\java\com\example\projetandroid\model\SaleModel.java
package com.example.projetandroid.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleModel implements Serializable {
    private String id;
    private String commercant_id;
    private ClientModel client;
    private List<SaleItemModel> produits;
    private double total;
    private String date;
    
    // Empty constructor needed for Firebase
    public SaleModel() {
        this.produits = new ArrayList<>();
    }
    
    public void calculateTotal() {
        total = 0;
        if (produits != null) {
            for (SaleItemModel item : produits) {
                total += item.getSubtotal();
            }
        }
    }
    
    public void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        this.date = sdf.format(new Date());
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCommercant_id() {
        return commercant_id;
    }
    
    public void setCommercant_id(String commercant_id) {
        this.commercant_id = commercant_id;
    }
    
    public ClientModel getClient() {
        return client;
    }
    
    public void setClient(ClientModel client) {
        this.client = client;
    }
    
    public List<SaleItemModel> getProduits() {
        return produits;
    }
    
    public void setProduits(List<SaleItemModel> produits) {
        this.produits = produits;
        calculateTotal();
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
}
