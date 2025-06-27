package com.example.flowerstoreproject.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.api.RetrofitClient;
import com.example.flowerstoreproject.api.services.ProductService;
import com.example.flowerstoreproject.model.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity {

    private ListView productListView;
    private ArrayAdapter<String> adapter;
    private List<Product> products = new ArrayList<>();
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        productListView = findViewById(R.id.productListView);

        // Lấy categoryId từ Intent
        categoryId = getIntent().getStringExtra("categoryId");
        if (categoryId == null) {
            Toast.makeText(this, "Category ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ListView adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        productListView.setAdapter(adapter);

        // Load products
        loadProducts();
    }

    private void loadProducts() {
        ProductService productService = RetrofitClient.getClient().create(ProductService.class);
        Call<List<Product>> call = productService.getProductsByCategory(categoryId);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    products = response.body();
                    updateProductList();
                } else {
                    Toast.makeText(ProductListActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    Log.e("ProductListActivity", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(ProductListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProductListActivity", "Error loading products", t);
            }
        });
    }

    private void updateProductList() {
        List<String> productNames = new ArrayList<>();
        for (Product product : products) {
            productNames.add(product.getName() + " - $" + product.getPrice());
        }
        adapter.clear();
        adapter.addAll(productNames);
        adapter.notifyDataSetChanged();
    }
}