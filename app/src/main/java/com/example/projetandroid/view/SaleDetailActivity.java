package com.example.projetandroid.view;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetandroid.adapter.SaleItemAdapter;
import com.example.projetandroid.databinding.ActivitySaleDetailBinding;
import com.example.projetandroid.model.ClientModel;
import com.example.projetandroid.model.SaleModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SALE = "extra_sale";
    private ActivitySaleDetailBinding binding;
    private SaleModel sale;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Get sale from intent
        if (getIntent().hasExtra(EXTRA_SALE)) {
            sale = (SaleModel) getIntent().getSerializableExtra(EXTRA_SALE);
            displaySaleDetails();
        } else {
            finish(); // Close if no sale data
        }
    }
    
    private void displaySaleDetails() {
        if (sale == null) return;
        
        // Format and display date
        binding.tvSaleDate.setText("Date: " + formatDate(sale.getDate()));
        
        // Display client info
        ClientModel client = sale.getClient();
        if (client != null) {
            binding.tvClientName.setText("Nom: " + client.getNom());
            
            String phone = client.getTelephone();
            binding.tvClientPhone.setText("Téléphone: " + 
                    (TextUtils.isEmpty(phone) ? "Non renseigné" : phone));
            
            binding.tvClientLocation.setText(String.format("Position: %.6f, %.6f", 
                    client.getLatitude(), client.getLongitude()));
        }
        
        // Display products in recycler view
        SaleItemAdapter adapter = new SaleItemAdapter(this, sale.getProduits());
        binding.recyclerViewSaleItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSaleItems.setAdapter(adapter);
        
        // Display total
        binding.tvTotal.setText(String.format("Total: %.2f", sale.getTotal()));
    }
    
    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDate; // Return as is if parsing fails
        }
    }
}