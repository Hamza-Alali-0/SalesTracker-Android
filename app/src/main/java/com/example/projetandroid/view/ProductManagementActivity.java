package com.example.projetandroid.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetandroid.R;
import com.example.projetandroid.adapter.ProductManagementAdapter;
import com.example.projetandroid.databinding.ActivityProductManagementBinding;
import com.example.projetandroid.model.ProductModel;
import com.example.projetandroid.util.ProductManager;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity implements ProductManagementAdapter.OnProductActionListener {

    private static final String TAG = "ProductManagement";
    private ActivityProductManagementBinding binding;
    private ProductManagementAdapter adapter;
    private List<ProductModel> productList;
    private ProductManager productManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize product list and adapter
        productList = new ArrayList<>();
        adapter = new ProductManagementAdapter(this, productList);
        adapter.setOnProductActionListener(this);

        // Set up RecyclerView
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProducts.setAdapter(adapter);

        // Set up FAB for adding new product
        binding.fabAddProduct.setOnClickListener(v -> showProductDialog(null, -1));

        // Initialize product manager
        productManager = new ProductManager();

        // Load products
        loadProducts();
    }

    private void loadProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);

        productManager.loadProducts(new ProductManager.ProductsLoadListener() {
            @Override
            public void onProductsLoaded(List<ProductModel> products) {
                binding.progressBar.setVisibility(View.GONE);
                productList.clear();
                productList.addAll(products);
                adapter.notifyDataSetChanged();

                // Show empty view if no products
                if (productList.isEmpty()) {
                    binding.tvEmptyProductList.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyProductList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductManagementActivity.this, 
                        "Erreur: " + errorMessage, Toast.LENGTH_SHORT).show();
                binding.tvEmptyProductList.setVisibility(View.VISIBLE);
                binding.tvEmptyProductList.setText("Erreur de chargement");
            }
        });
    }

    private void showProductDialog(ProductModel product, int position) {
        boolean isEdit = product != null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product_form, null);

        // Set dialog title
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(isEdit ? "Modifier le produit" : "Ajouter un produit");

        // Find input fields
        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etStock = dialogView.findViewById(R.id.etProductStock);

        // Pre-fill fields if editing
        if (isEdit) {
            etName.setText(product.getNom());
            etPrice.setText(String.valueOf(product.getPrix_unitaire()));
            etStock.setText(String.valueOf(product.getStock()));
        }

        // Build dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Modifier" : "Ajouter", null) // Set listeners later
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Override positive button click to prevent dialog from dismissing if validation fails
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(name)) {
                etName.setError("Le nom est obligatoire");
                return;
            }
            if (TextUtils.isEmpty(priceStr)) {
                etPrice.setError("Le prix est obligatoire");
                return;
            }
            if (TextUtils.isEmpty(stockStr)) {
                etStock.setError("Le stock est obligatoire");
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    etPrice.setError("Le prix doit être positif");
                    return;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Prix invalide");
                return;
            }

            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    etStock.setError("Le stock ne peut pas être négatif");
                    return;
                }
            } catch (NumberFormatException e) {
                etStock.setError("Stock invalide");
                return;
            }

            // Proceed with saving
            binding.progressBar.setVisibility(View.VISIBLE);

            ProductModel updatedProduct;
            if (isEdit) {
                updatedProduct = new ProductModel(product.getId(), name, price, stock);
            } else {
                updatedProduct = new ProductModel(null, name, price, stock);
            }

            saveProduct(updatedProduct, position);
            dialog.dismiss();
        });
    }

    private void saveProduct(ProductModel product, int position) {
        boolean isNewProduct = position == -1;

        DatabaseReference.CompletionListener completionListener = (error, ref) -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (error != null) {
                Toast.makeText(ProductManagementActivity.this, 
                        "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (isNewProduct) {
                Toast.makeText(ProductManagementActivity.this, 
                        "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
                // Refresh list to get new product
                loadProducts();
            } else {
                Toast.makeText(ProductManagementActivity.this, 
                        "Produit mis à jour avec succès", Toast.LENGTH_SHORT).show();
                // Update list item without reloading
                productList.set(position, product);
                adapter.notifyItemChanged(position);
            }
        };

        if (isNewProduct) {
            productManager.addProduct(product, completionListener);
        } else {
            productManager.updateProduct(product, completionListener);
        }
    }

    @Override
    public void onEditProduct(ProductModel product, int position) {
        showProductDialog(product, position);
    }

    @Override
    public void onDeleteProduct(ProductModel product, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce produit ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    
                    productManager.deleteProduct(product.getId(), (error, ref) -> {
                        binding.progressBar.setVisibility(View.GONE);
                        
                        if (error != null) {
                            Toast.makeText(ProductManagementActivity.this, 
                                    "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(ProductManagementActivity.this, 
                                "Produit supprimé avec succès", Toast.LENGTH_SHORT).show();
                        adapter.removeItem(position);
                        
                        // Show empty view if no products left
                        if (productList.isEmpty()) {
                            binding.tvEmptyProductList.setVisibility(View.VISIBLE);
                        }
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}