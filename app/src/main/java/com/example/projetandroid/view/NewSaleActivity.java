package com.example.projetandroid.view;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetandroid.R;
import com.example.projetandroid.adapter.ProductAdapter;
import com.example.projetandroid.databinding.ActivityNewSaleBinding;
import com.example.projetandroid.model.ClientModel;
import com.example.projetandroid.model.ProductModel;
import com.example.projetandroid.model.SaleItemModel;
import com.example.projetandroid.model.SaleModel;
import com.example.projetandroid.util.ProductManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewSaleActivity extends AppCompatActivity implements ProductAdapter.OnTotalChangeListener {

    private static final String TAG = "NewSaleActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    private ActivityNewSaleBinding binding;
    private ProductAdapter productAdapter;
    private List<ProductModel> productList;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private double currentTotal = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewSaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Request location permissions
        requestLocationPermission();
        
        // Set up product list (Initially with dummy data, will be replaced with Firebase data)
        setupProductList();
        
        // Set up product recycler view
        productAdapter = new ProductAdapter(this, productList);
        productAdapter.setOnTotalChangeListener(this);
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProducts.setAdapter(productAdapter);
        
        // Set up save button
        binding.btnSaveSale.setOnClickListener(v -> saveSale());
    }
    
    private void setupProductList() {
        productList = new ArrayList<>();
        
        // Initially show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Load products from Firebase
        ProductManager productManager = new ProductManager();
        productManager.loadProducts(new ProductManager.ProductsLoadListener() {
            @Override
            public void onProductsLoaded(List<ProductModel> products) {
                productList.clear();
                productList.addAll(products);
                productAdapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onError(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(NewSaleActivity.this, 
                        "Erreur de chargement des produits: " + errorMessage, 
                        Toast.LENGTH_SHORT).show();
                
                // Fallback to dummy data if Firebase load fails
                loadDummyProducts();
            }
        });
    }

    private void loadDummyProducts() {
        productList.clear();
        productList.add(new ProductModel("p001", "Tomate", 50, 100));
        productList.add(new ProductModel("p002", "Pomme", 70, 80));
        productList.add(new ProductModel("p003", "Orange", 85, 60));
        productList.add(new ProductModel("p004", "Carotte", 40, 120));
        productList.add(new ProductModel("p005", "Concombre", 35, 75));
        productAdapter.notifyDataSetChanged();
    }
    
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }
    
    @SuppressLint("MissingPermission") // We check permissions before calling this
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        binding.tvLocationInfo.setText("Position: " + 
                                location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        binding.tvLocationInfo.setText("Position: Non disponible");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    binding.tvLocationInfo.setText("Erreur de localisation");
                });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void saveSale() {
        // Validate client name
        String clientName = binding.etClientName.getText().toString().trim();
        if (TextUtils.isEmpty(clientName)) {
            binding.etClientName.setError("Le nom du client est obligatoire");
            return;
        }
        
        // Check if any products were selected
        List<SaleItemModel> selectedItems = productAdapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins un produit",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Create client model
        ClientModel client = new ClientModel();
        client.setNom(clientName);
        client.setTelephone(binding.etClientPhone.getText().toString().trim());
        
        if (currentLocation != null) {
            client.setLatitude(currentLocation.getLatitude());
            client.setLongitude(currentLocation.getLongitude());
        }
        
        // Create sale model
        SaleModel sale = new SaleModel();
        sale.setId(UUID.randomUUID().toString());
        sale.setClient(client);
        sale.setProduits(selectedItems);
        sale.setCurrentDate();
        sale.setCommercant_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        // Save to Firebase
        DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference("ventes");
        salesRef.child(sale.getId()).setValue(sale)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(NewSaleActivity.this, 
                            "Vente enregistrée avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(NewSaleActivity.this, 
                            "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    @Override
    public void onTotalChanged(double total) {
        currentTotal = total;
        binding.tvTotal.setText(String.format("Total: %.2f", total));
    }
}