package com.example.projetandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetandroid.databinding.ActivityMainBinding;
import com.example.projetandroid.util.ProductManager;
import com.example.projetandroid.view.LoginActivity;
import com.example.projetandroid.view.ProductManagementActivity;
import com.example.projetandroid.view.SalesAnalyticsActivity;
import com.example.projetandroid.view.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference productRef;
    private DatabaseReference saleRef;
    private ValueEventListener productListener;
    private ValueEventListener saleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Initialize view binding
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            // Check if user is admin
            checkAdminPermission();
            checkAndPopulateProducts(); // Add this line
            
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            
            // Check if user is signed in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // Not signed in, launch the Login activity
                Log.d(TAG, "No user logged in, redirecting to LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            
            Toast.makeText(this, "Bienvenue " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            
            // Set up buttons only if we're not redirecting
            setupButtons();
            
            if (binding.btnManageProducts != null) {
                binding.btnManageProducts.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ProductManagementActivity.class);
                    startActivity(intent);
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupButtons() {
        // Check if binding is initialized and buttons exist
        if (binding != null) {
            if (binding.btnNewSale != null) {
                binding.btnNewSale.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, 
                                Class.forName("com.example.projetandroid.view.NewSaleActivity"));
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching NewSaleActivity", e);
                        Toast.makeText(MainActivity.this, 
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            if (binding.btnHistory != null) {
                binding.btnHistory.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, 
                                Class.forName("com.example.projetandroid.view.HistoryActivity"));
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching HistoryActivity", e);
                        Toast.makeText(MainActivity.this, 
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            if (binding.btnLogout != null) {
                binding.btnLogout.setOnClickListener(v -> {
                    try {
                        signOut();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during logout", e);
                        Toast.makeText(MainActivity.this, 
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            // Add this block to setupButtons()
            if (binding.btnAnalytics != null) {
                binding.btnAnalytics.setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, SalesAnalyticsActivity.class));
                });
            }
            
            // Add settings button functionality
            binding.btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        } else {
            Log.e(TAG, "Binding is null in setupButtons");
        }
    }
    
    private void checkAdminPermission() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Simplified approach - store admin status in Realtime Database
            DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference("admin_users")
                .child(user.getUid());
                
            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean isAdmin = dataSnapshot.getValue(Boolean.class);
                    boolean hasAdminAccess = isAdmin != null && isAdmin;
                    
                    // Show/hide admin features
                    if (binding.btnManageProducts != null) {
                        binding.btnManageProducts.setVisibility(hasAdminAccess ? 
                            View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AdminCheck", "Failed to check admin status", error.toException());
                }
            });
        }
    }
    
    private void signOut() {
        // First detach any listeners
        if (productListener != null) {
            productRef.removeEventListener(productListener);
        }
        if (saleListener != null) {
            saleRef.removeEventListener(saleListener);
        }
        
        // Then sign out
        FirebaseAuth.getInstance().signOut();
        
        // Redirect to login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void checkAndPopulateProducts() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("produits");
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // If no products exist, add some
                if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                    ProductManager productManager = new ProductManager();
                    productManager.populateInitialProducts();
                    Toast.makeText(MainActivity.this, "Produits initiaux ajoutés", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Error checking products", databaseError.toException());
            }
        });
    }
}