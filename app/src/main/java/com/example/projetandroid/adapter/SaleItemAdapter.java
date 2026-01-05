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

import java.util.List;

public class SaleItemAdapter extends RecyclerView.Adapter<SaleItemAdapter.SaleItemViewHolder> {
    
    private Context context;
    private List<SaleItemModel> itemsList;
    
    public SaleItemAdapter(Context context, List<SaleItemModel> itemsList) {
        this.context = context;
        this.itemsList = itemsList;
    }
    
    @NonNull
    @Override
    public SaleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sale_detail, parent, false);
        return new SaleItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SaleItemViewHolder holder, int position) {
        SaleItemModel item = itemsList.get(position);
        
        holder.tvProductName.setText(item.getNom());
        holder.tvQuantity.setText(String.format("%d x", item.getQuantite()));
        holder.tvPrice.setText(String.format("%.2f", item.getPrix_unitaire()));
        holder.tvSubtotal.setText(String.format("%.2f", item.getSubtotal()));
    }
    
    @Override
    public int getItemCount() {
        return itemsList.size();
    }
    
    static class SaleItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice, tvSubtotal;
        
        public SaleItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvDetailProductName);
            tvQuantity = itemView.findViewById(R.id.tvDetailQuantity);
            tvPrice = itemView.findViewById(R.id.tvDetailPrice);
            tvSubtotal = itemView.findViewById(R.id.tvDetailSubtotal);
        }
    }
}