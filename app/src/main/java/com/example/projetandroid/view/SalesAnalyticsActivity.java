package com.example.projetandroid.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetandroid.databinding.ActivitySalesAnalyticsBinding;
import com.example.projetandroid.model.SaleItemModel;
import com.example.projetandroid.model.SaleModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesAnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "SalesAnalytics";
    private ActivitySalesAnalyticsBinding binding;
    private List<SaleModel> allSales = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySalesAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Load sales data
        loadSalesData();
        
        // Setup period selection
        binding.btnToday.setOnClickListener(v -> calculateStatsForPeriod("today"));
        binding.btnThisWeek.setOnClickListener(v -> calculateStatsForPeriod("week"));
        binding.btnThisMonth.setOnClickListener(v -> calculateStatsForPeriod("month"));
        binding.btnAllTime.setOnClickListener(v -> calculateStatsForPeriod("all"));
    }
    
    private void loadSalesData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference("ventes");
        
        // Query sales for current user
        Query query = salesRef.orderByChild("commercant_id").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allSales.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SaleModel sale = snapshot.getValue(SaleModel.class);
                    if (sale != null) {
                        allSales.add(sale);
                    }
                }
                
                binding.progressBar.setVisibility(View.GONE);
                
                // By default, show stats for all time
                calculateStatsForPeriod("all");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading sales data", databaseError.toException());
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    private void calculateStatsForPeriod(String period) {
        List<SaleModel> filteredSales = filterSalesByPeriod(period);
        
        // Calculate total revenue
        double totalRevenue = 0;
        for (SaleModel sale : filteredSales) {
            totalRevenue += sale.getTotal();
        }
        
        // Count total sales
        int saleCount = filteredSales.size();
        
        // Find best-selling products
        Map<String, Integer> productSales = new HashMap<>();
        
        for (SaleModel sale : filteredSales) {
            for (SaleItemModel item : sale.getProduits()) {
                String productName = item.getNom();
                int quantity = item.getQuantite();
                
                productSales.put(productName, productSales.getOrDefault(productName, 0) + quantity);
            }
        }
        
        // Find the best-selling product
        String bestProduct = "";
        int maxQuantity = 0;
        
        for (Map.Entry<String, Integer> entry : productSales.entrySet()) {
            if (entry.getValue() > maxQuantity) {
                maxQuantity = entry.getValue();
                bestProduct = entry.getKey();
            }
        }
        
        // Display stats
        binding.tvTotalRevenue.setText(String.format("%.2f", totalRevenue));
        binding.tvSaleCount.setText(String.valueOf(saleCount));
        
        if (!bestProduct.isEmpty()) {
            binding.tvBestSellingProduct.setText(bestProduct + " (" + maxQuantity + " vendus)");
        } else {
            binding.tvBestSellingProduct.setText("Aucun produit vendu");
        }
        
        // Set selected period button
        highlightSelectedPeriod(period);
    }
    
    private List<SaleModel> filterSalesByPeriod(String period) {
        if ("all".equals(period)) {
            return new ArrayList<>(allSales);
        }
        
        List<SaleModel> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        
        Calendar calendarNow = Calendar.getInstance();
        Date now = calendarNow.getTime();
        
        Calendar calendarStart = Calendar.getInstance();
        
        switch (period) {
            case "today":
                // Set to start of today
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                break;
                
            case "week":
                // Set to start of this week
                calendarStart.set(Calendar.DAY_OF_WEEK, calendarStart.getFirstDayOfWeek());
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                break;
                
            case "month":
                // Set to start of this month
                calendarStart.set(Calendar.DAY_OF_MONTH, 1);
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                break;
        }
        
        Date startDate = calendarStart.getTime();
        
        for (SaleModel sale : allSales) {
            try {
                Date saleDate = sdf.parse(sale.getDate());
                if (saleDate != null && saleDate.after(startDate) && saleDate.before(now)) {
                    filteredList.add(sale);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + sale.getDate(), e);
            }
        }
        
        return filteredList;
    }
    
    private void highlightSelectedPeriod(String period) {
        // Reset all buttons
        binding.btnToday.setAlpha(0.6f);
        binding.btnThisWeek.setAlpha(0.6f);
        binding.btnThisMonth.setAlpha(0.6f);
        binding.btnAllTime.setAlpha(0.6f);
        
        // Highlight selected button
        switch (period) {
            case "today":
                binding.btnToday.setAlpha(1.0f);
                break;
            case "week":
                binding.btnThisWeek.setAlpha(1.0f);
                break;
            case "month":
                binding.btnThisMonth.setAlpha(1.0f);
                break;
            case "all":
                binding.btnAllTime.setAlpha(1.0f);
                break;
        }
    }
}