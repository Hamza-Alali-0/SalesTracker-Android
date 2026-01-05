package com.example.projetandroid.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetandroid.MainActivity;
import com.example.projetandroid.databinding.ActivitySettingsBinding;
import com.example.projetandroid.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivitySettingsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
                loadUserProfile();
            } else {
                Toast.makeText(this, "Non connecté", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Save profile button
            binding.btnSaveProfile.setOnClickListener(v -> saveUserProfile());

            // Change password button
            binding.btnChangePassword.setOnClickListener(v -> changePassword());

            // Sign out button
            binding.btnSignOut.setOnClickListener(v -> signOut());
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadUserProfile() {
        try {
            binding.progressBar.setVisibility(View.VISIBLE);
            
            // Make sure the email is displayed even if profile loading fails
            if (mAuth.getCurrentUser() != null) {
                binding.etEmail.setText(mAuth.getCurrentUser().getEmail());
            }
            
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    try {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        
                        if (user != null) {
                            binding.etBusinessName.setText(user.getBusinessName());
                            binding.etPhoneNumber.setText(user.getPhoneNumber());
                        } else {
                            // Create empty user if null
                            Log.d(TAG, "No user profile data found, creating empty fields");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data", e);
                        Toast.makeText(SettingsActivity.this, 
                                "Erreur de lecture du profil", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Database error", databaseError.toException());
                    Toast.makeText(SettingsActivity.this, 
                            "Erreur d'accès à la base de données", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error loading profile", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserProfile() {
        try {
            String businessName = binding.etBusinessName.getText().toString().trim();
            String phoneNumber = binding.etPhoneNumber.getText().toString().trim();
            
            if (businessName.isEmpty()) {
                binding.etBusinessName.setError("Veuillez entrer un nom d'entreprise");
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            
            UserModel userUpdates = new UserModel(businessName, phoneNumber);
            
            mDatabase.child("users").child(userId).setValue(userUpdates)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, 
                                    "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, 
                                    "Échec de la mise à jour du profil", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Profile update failed", task.getException());
                        }
                    });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error saving profile", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String email = user.getEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "Email invalide", Toast.LENGTH_SHORT).show();
                return;
            }
            
            binding.progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, 
                                    "Email de réinitialisation du mot de passe envoyé à " + email, 
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, 
                                    "Échec de l'envoi de l'email de réinitialisation", 
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Password reset failed", task.getException());
                        }
                    });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error changing password", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut() {
        try {
            mAuth.signOut();
            finish();
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error signing out", e);
            Toast.makeText(this, "Erreur lors de la déconnexion: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
}