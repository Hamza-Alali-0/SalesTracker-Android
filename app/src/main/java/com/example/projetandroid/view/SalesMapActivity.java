package com.example.projetandroid.view;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetandroid.databinding.ActivitySalesMapBinding;
import com.example.projetandroid.model.SaleModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class SalesMapActivity extends AppCompatActivity {

    private static final String TAG = "SalesMapActivity";
    private ActivitySalesMapBinding binding;
    private MapView map;
    private List<SaleModel> salesWithLocation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Basic OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        binding = ActivitySalesMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Set up map with basic settings
        map = binding.mapView;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        
     
        
        // Set initial zoom and position (Algeria)
        IMapController mapController = map.getController();
        mapController.setZoom(5.5);
        GeoPoint startPoint = new GeoPoint(28.0339, 1.6596); // Center of Algeria
        mapController.setCenter(startPoint);
        
        // Load sale locations
        loadSaleLocations();
    }

    private void loadSaleLocations() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference("ventes");
        
        // Query sales for current user
        Query query = salesRef.orderByChild("commercant_id").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing markers
                map.getOverlays().clear();
                
                // List to store sales with valid locations
                List<SaleModel> salesWithLocation = new ArrayList<>();
                int validLocationCount = 0;
                
                // Add markers for each sale with valid location
                for (DataSnapshot saleSnapshot : dataSnapshot.getChildren()) {
                    SaleModel sale = saleSnapshot.getValue(SaleModel.class);
                    if (sale != null && sale.getClient() != null) {
                        double lat = sale.getClient().getLatitude();
                        double lon = sale.getClient().getLongitude();
                        
                        // Check for valid coordinates
                        if (lat != 0 && lon != 0 && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                            // Add marker to map
                            GeoPoint point = new GeoPoint(lat, lon);
                            Marker marker = new Marker(map);
                            marker.setPosition(point);
                            
                            // Set marker title using available client information
                            // Use client ID or other available property instead of name
                            String title = "Sale ID: " + sale.getId(); 
                            // Alternatively, if client has other accessible properties:
                            // String title = "Client: " + sale.getClient().getClientName(); // if getClientName() exists
                            // String title = "Client ID: " + sale.getClient().getId(); // if getId() exists
                            
                            marker.setTitle(title);
                            map.getOverlays().add(marker);
                            validLocationCount++;
                            salesWithLocation.add(sale);
                        }
                    }
                }
                
                binding.progressBar.setVisibility(View.GONE);
                
                if (validLocationCount == 0) {
                    binding.tvNoLocations.setVisibility(View.VISIBLE);
                    binding.mapView.setVisibility(View.GONE);
                } else {
                    binding.tvNoLocations.setVisibility(View.GONE);
                    binding.mapView.setVisibility(View.VISIBLE);
                }
                
                // Zoom to show all markers
                if (validLocationCount > 0) {
                    // Track bounds (min/max lat/lon)
                    double minLat = 90.0;
                    double maxLat = -90.0;
                    double minLon = 180.0;
                    double maxLon = -180.0;
                    
                    // Find the boundaries of all markers
                    for (SaleModel sale : salesWithLocation) {
                        double lat = sale.getClient().getLatitude();
                        double lon = sale.getClient().getLongitude();
                        
                        minLat = Math.min(minLat, lat);
                        maxLat = Math.max(maxLat, lat);
                        minLon = Math.min(minLon, lon);
                        maxLon = Math.max(maxLon, lon);
                    }
                    
                    // Create bounding box from min/max coordinates
                    BoundingBox boundingBox = new BoundingBox(maxLat, maxLon, minLat, minLon);
                    
                    // Add padding (in pixels)
                    int padding = 100;
                    
                    // Zoom to show all markers
                    map.zoomToBoundingBox(boundingBox, true, padding);
                } else {
                    // If no valid locations, center on Morocco
                    map.getController().setZoom(6.0);
                    map.getController().setCenter(new GeoPoint(31.7917, -7.0926)); // Morocco center
                }
                
                map.invalidate(); // Refresh the map
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading sales data", databaseError.toException());
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoLocations.setVisibility(View.VISIBLE);
                binding.mapView.setVisibility(View.GONE);
            }
        });
    }

    private void addMarker(SaleModel sale) {
        Marker marker = new Marker(map);
        GeoPoint position = new GeoPoint(sale.getClient().getLatitude(), sale.getClient().getLongitude());
        marker.setPosition(position);
        
        // Set marker title and description
        String clientName = sale.getClient().getNom();
        String date = sale.getDate();
        double total = sale.getTotal();
        
        marker.setTitle(clientName);
        marker.setSnippet("Date: " + date + "\nTotal: " + total + " DA");
        
        // Add marker to map
        map.getOverlays().add(marker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}