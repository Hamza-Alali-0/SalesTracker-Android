package com.example.projetandroid.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.projetandroid.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductManager {
    
    private static final String TAG = "ProductManager";
    private final DatabaseReference productsRef;
    
    public interface ProductsLoadListener {
        void onProductsLoaded(List<ProductModel> products);
        void onError(String errorMessage);
    }
    
    public ProductManager() {
        productsRef = FirebaseDatabase.getInstance().getReference("produits");
    }
    
    public void loadProducts(final ProductsLoadListener listener) {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ProductModel> products = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ProductModel product = snapshot.getValue(ProductModel.class);
                    if (product != null) {
                        // Make sure the ID from the key is set
                        product.setId(snapshot.getKey());
                        products.add(product);
                    }
                }
                
                listener.onProductsLoaded(products);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading products", databaseError.toException());
                listener.onError(databaseError.getMessage());
            }
        });
    }
    
    public void addProduct(ProductModel product, final DatabaseReference.CompletionListener listener) {
        // Generate a new ID if one doesn't exist
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(productsRef.push().getKey());
        }
        
        productsRef.child(product.getId()).setValue(product, listener);
    }
    
    public void updateProduct(ProductModel product, final DatabaseReference.CompletionListener listener) {
        if (product.getId() == null || product.getId().isEmpty()) {
            // Handle error case without using direct DatabaseError creation
            Log.e(TAG, "Product ID cannot be null");
            if (listener != null) {
                // Just report an operation failure but don't create custom error object
                // The first parameter (error) is null because we didn't have an error from Firebase
                // The second parameter is the reference that failed
                listener.onComplete(null, productsRef);
            }
            return;
        }
        
        productsRef.child(product.getId()).setValue(product, listener);
    }
    
    public void deleteProduct(String productId, final DatabaseReference.CompletionListener listener) {
        if (productId == null || productId.isEmpty()) {
            // Handle error case without using direct DatabaseError creation
            Log.e(TAG, "Product ID cannot be null");
            if (listener != null) {
                // Just report an operation failure but don't create custom error object
                listener.onComplete(null, productsRef);
            }
            return;
        }
        
        productsRef.child(productId).removeValue(listener);
    }
    
    // Helper method to populate initial products if needed
    public void populateInitialProducts() {
        List<ProductModel> initialProducts = new ArrayList<>();
        initialProducts.add(new ProductModel("p001", "Tomate", 50, 100));
        initialProducts.add(new ProductModel("p002", "Pomme", 70, 80));
        initialProducts.add(new ProductModel("p003", "Orange", 85, 60));
        initialProducts.add(new ProductModel("p004", "Carotte", 40, 120));
        initialProducts.add(new ProductModel("p005", "Concombre", 35, 75));
        
        for (ProductModel product : initialProducts) {
            addProduct(product, null);
        }
    }
}