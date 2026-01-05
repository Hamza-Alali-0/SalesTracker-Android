package com.example.projetandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetandroid.R;
import com.example.projetandroid.model.ProductModel;
import com.example.projetandroid.model.SaleItemModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private Context context;
    private List<ProductModel> productList;
    private Map<String, Integer> selectedQuantities; // Map to track selected quantities by product ID
    private OnTotalChangeListener totalChangeListener;
    
    public interface OnTotalChangeListener {
        void onTotalChanged(double total);
    }
    
    public ProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
        this.selectedQuantities = new HashMap<>();
    }
    
    public void setOnTotalChangeListener(OnTotalChangeListener listener) {
        this.totalChangeListener = listener;
    }
    
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        
        // Set product details
        holder.tvProductName.setText(product.getNom());
        holder.tvProductPrice.setText(String.format("%.2f", product.getPrix_unitaire()));
        
        // Get current quantity or default to 0
        int quantity = selectedQuantities.getOrDefault(product.getId(), 0);
        holder.tvQuantity.setText(String.valueOf(quantity));
        
        // Decrease quantity
        holder.btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                selectedQuantities.put(product.getId(), quantity - 1);
                notifyItemChanged(position);
                updateTotalPrice();
            }
        });
        
        // Increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            if (quantity < product.getStock()) {
                selectedQuantities.put(product.getId(), quantity + 1);
                notifyItemChanged(position);
                updateTotalPrice();
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return productList.size();
    }
    
    private void updateTotalPrice() {
        if (totalChangeListener != null) {
            double total = 0;
            for (ProductModel product : productList) {
                int quantity = selectedQuantities.getOrDefault(product.getId(), 0);
                total += quantity * product.getPrix_unitaire();
            }
            totalChangeListener.onTotalChanged(total);
        }
    }
    
    // Get selected products as SaleItemModel objects
    public List<SaleItemModel> getSelectedItems() {
        List<SaleItemModel> selectedItems = new ArrayList<>();
        
        for (ProductModel product : productList) {
            int quantity = selectedQuantities.getOrDefault(product.getId(), 0);
            if (quantity > 0) {
                SaleItemModel saleItem = new SaleItemModel(
                        product.getId(),
                        product.getNom(),
                        quantity,
                        product.getPrix_unitaire()
                );
                selectedItems.add(saleItem);
            }
        }
        
        return selectedItems;
    }
    
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvQuantity;
        Button btnMinus, btnPlus;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnDecrease);
            btnPlus = itemView.findViewById(R.id.btnIncrease);
        }
    }
}