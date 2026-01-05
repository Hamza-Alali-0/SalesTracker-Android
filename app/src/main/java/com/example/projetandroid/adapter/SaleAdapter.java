package com.example.projetandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetandroid.R;
import com.example.projetandroid.model.SaleItemModel;
import com.example.projetandroid.model.SaleModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {
    
    private Context context;
    private List<SaleModel> saleList;
    private OnSaleClickListener clickListener;
    
    public interface OnSaleClickListener {
        void onSaleClick(SaleModel sale);
    }
    
    public SaleAdapter(Context context, List<SaleModel> saleList) {
        this.context = context;
        this.saleList = saleList;
    }
    
    public void setOnSaleClickListener(OnSaleClickListener listener) {
        this.clickListener = listener;
    }
    
    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sale, parent, false);
        return new SaleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        SaleModel sale = saleList.get(position);
        
        // Format and display sale information
        holder.tvClientName.setText(sale.getClient().getNom());
        holder.tvSaleDate.setText(formatDate(sale.getDate()));
        holder.tvSaleTotal.setText(String.format("%.2f", sale.getTotal()));
        
        // Build product string
        StringBuilder productsText = new StringBuilder();
        for (SaleItemModel item : sale.getProduits()) {
            productsText.append(item.getNom())
                    .append(" (").append(item.getQuantite()).append(")")
                    .append(", ");
        }
        
        // Remove trailing comma and space
        if (productsText.length() > 2) {
            productsText.setLength(productsText.length() - 2);
        }
        
        holder.tvProducts.setText(productsText.toString());
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSaleClick(sale);
            }
        });
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
    
    @Override
    public int getItemCount() {
        return saleList.size();
    }
    
    public void updateData(List<SaleModel> newSaleList) {
        this.saleList = newSaleList;
        notifyDataSetChanged();
    }

    public void updateSales(List<SaleModel> newSales) {
        this.saleList = newSales;
        notifyDataSetChanged();
    }
    
    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvSaleDate, tvSaleTotal, tvProducts;
        
        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvSaleDate = itemView.findViewById(R.id.tvSaleDate);
            tvSaleTotal = itemView.findViewById(R.id.tvSaleTotal);
            tvProducts = itemView.findViewById(R.id.tvProducts);
        }
    }
}