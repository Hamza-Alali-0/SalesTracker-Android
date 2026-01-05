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

import java.util.List;

public class ProductManagementAdapter extends RecyclerView.Adapter<ProductManagementAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(ProductModel product, int position);
        void onDeleteProduct(ProductModel product, int position);
    }

    public ProductManagementAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_management, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);
        
        holder.tvProductName.setText(product.getNom());
        holder.tvProductPrice.setText(String.format("Prix: %.2f", product.getPrix_unitaire()));
        holder.tvProductStock.setText(String.format("Stock: %d", product.getStock()));
        
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product, position);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteProduct(product, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<ProductModel> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        productList.remove(position);
        notifyItemRemoved(position);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductStock;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvManageProductName);
            tvProductPrice = itemView.findViewById(R.id.tvManageProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvManageProductStock);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}