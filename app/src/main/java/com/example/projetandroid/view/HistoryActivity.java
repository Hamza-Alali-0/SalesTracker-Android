package com.example.projetandroid.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetandroid.adapter.SaleAdapter;
import com.example.projetandroid.databinding.ActivityHistoryBinding;
import com.example.projetandroid.model.SaleModel;
import com.example.projetandroid.model.SaleItemModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements SaleAdapter.OnSaleClickListener {

    private static final String TAG = "HistoryActivity";
    private ActivityHistoryBinding binding;
    private SaleAdapter saleAdapter;
    private List<SaleModel> salesList;
    private List<SaleModel> allSales = new ArrayList<>();
    private List<SaleModel> filteredSales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize sales list and adapter
        salesList = new ArrayList<>();
        saleAdapter = new SaleAdapter(this, salesList);
        saleAdapter.setOnSaleClickListener(this);
        
        // Set up RecyclerView
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(saleAdapter);
        

        
        // Load sales data
        loadSalesData();

        // Setup filters
        setupFilters();
        
        // Setup export button
        setupExportButton();
        
        // Setup map button
        setupButtons();
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
                
                // Show all sales initially
                showAllSales();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading sales data", databaseError.toException());
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(HistoryActivity.this, 
                        "Erreur: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSalesToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayStart = calendar.getTimeInMillis();
        
        filterSalesByDate(todayStart, System.currentTimeMillis());
    }

    private void filterSalesThisWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long weekStart = calendar.getTimeInMillis();
        
        filterSalesByDate(weekStart, System.currentTimeMillis());
    }

    private void filterSalesByDate(long startDate, long endDate) {
        filteredSales.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        
        for (SaleModel sale : allSales) {
            try {
                Date saleDate = dateFormat.parse(sale.getDate());
                if (saleDate != null && saleDate.getTime() >= startDate && saleDate.getTime() <= endDate) {
                    filteredSales.add(sale);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + sale.getDate(), e);
            }
        }
        
        updateUI();
    }

    private void showAllSales() {
        filteredSales.clear();
        filteredSales.addAll(allSales);
        updateUI();
    }

    private void updateUI() {
        salesList.clear();
        salesList.addAll(filteredSales);
        saleAdapter.notifyDataSetChanged();
        
        // Show empty view if no sales
        if (salesList.isEmpty()) {
            binding.tvEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyHistory.setVisibility(View.GONE);
        }
    }

    private void setupFilters() {
        // Date filter
        binding.btnFilterDate.setOnClickListener(v -> showDatePickerDialog());
        
        // Client filter
        binding.etClientSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterByClientName(s.toString());
            }
        });
        
        // Reset filters
        binding.btnResetFilters.setOnClickListener(v -> {
            binding.etClientSearch.setText("");
            showAllSales();
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format the selected date
                    String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    filterByDate(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void filterByDate(String date) {
        List<SaleModel> filteredList = new ArrayList<>();
        
        for (SaleModel sale : allSales) {
            // Extract just the date part (YYYY-MM-DD) from the full timestamp
            String saleDate = sale.getDate().substring(0, 10);
            if (saleDate.equals(date)) {
                filteredList.add(sale);
            }
        }
        
        updateSalesList(filteredList);
    }

    private void filterByClientName(String clientName) {
        if (clientName.isEmpty()) {
            showAllSales();
            return;
        }
        
        List<SaleModel> filteredList = new ArrayList<>();
        String lowerCaseQuery = clientName.toLowerCase();
        
        for (SaleModel sale : allSales) {
            if (sale.getClient() != null && 
                sale.getClient().getNom().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(sale);
            }
        }
        
        updateSalesList(filteredList);
    }

    private void updateSalesList(List<SaleModel> salesList) {
        saleAdapter.updateSales(salesList);
        
        if (salesList.isEmpty()) {
            binding.tvEmptySalesList.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptySalesList.setVisibility(View.GONE);
        }
    }

    private void setupExportButton() {
        binding.btnExport.setOnClickListener(v -> exportSalesData());
    }

    private void exportSalesData() {
        // Check storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    STORAGE_PERMISSION_CODE);
            return;
        }

        if (allSales.isEmpty()) {
            Toast.makeText(this, "Aucune vente à exporter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create CSV content
        StringBuilder csvData = new StringBuilder();
        csvData.append("Date,Client,Téléphone,Latitude,Longitude,Produits,Quantités,Prix Unitaires,Total\n");

        for (SaleModel sale : allSales) {
            StringBuilder products = new StringBuilder();
            StringBuilder quantities = new StringBuilder();
            StringBuilder prices = new StringBuilder();

            for (SaleItemModel item : sale.getProduits()) {
                if (products.length() > 0) {
                    products.append("; ");
                    quantities.append("; ");
                    prices.append("; ");
                }
                products.append(item.getNom());
                quantities.append(item.getQuantite());
                prices.append(item.getPrix_unitaire());
            }

            // Get client information safely
            String clientName = "N/A";
            String clientPhone = "N/A";
            double latitude = 0.0;
            double longitude = 0.0;
            
            if (sale.getClient() != null) {
                clientName = sale.getClient().getNom();
                clientPhone = sale.getClient().getTelephone();
                
                // Access latitude and longitude directly if they exist in ClientModel
                // If not, we'll use default values of 0.0
                try {
                    latitude = sale.getClient().getLatitude();
                    longitude = sale.getClient().getLongitude();
                } catch (Exception e) {
                    Log.e("HistoryActivity", "Error accessing location data: " + e.getMessage());
                }
            }

            // Format the row
            csvData.append(String.format(Locale.getDefault(), 
                    "\"%s\",\"%s\",\"%s\",\"%f\",\"%f\",\"%s\",\"%s\",\"%s\",\"%f\"\n",
                    sale.getDate(),
                    clientName,
                    clientPhone,
                    latitude,
                    longitude,
                    products.toString(),
                    quantities.toString(),
                    prices.toString(),
                    sale.getTotal()));
        }

        try {
            // Create directory if it doesn't exist
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "VenteFacile");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file
            String fileName = "ventes_" + new SimpleDateFormat("yyyyMMdd_HHmmss", 
                    Locale.getDefault()).format(new Date()) + ".csv";
            File file = new File(directory, fileName);

            // Write to file
            FileWriter writer = new FileWriter(file);
            writer.append(csvData.toString());
            writer.flush();
            writer.close();

            Toast.makeText(this, "Données exportées vers " + file.getAbsolutePath(), 
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'exportation: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private static final int STORAGE_PERMISSION_CODE = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportSalesData();
            } else {
                Toast.makeText(this, "Permission de stockage requise pour exporter", 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaleClick(SaleModel sale) {
        Intent intent = new Intent(this, SaleDetailActivity.class);
        intent.putExtra(SaleDetailActivity.EXTRA_SALE, sale);
        startActivity(intent);
    }
    
    private void setupButtons() {
        binding.btnShowMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, SalesMapActivity.class);
            startActivity(intent);
        });
    }
}