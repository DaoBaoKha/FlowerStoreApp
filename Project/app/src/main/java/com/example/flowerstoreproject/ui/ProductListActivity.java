package com.example.flowerstoreproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerstoreproject.R;
import com.example.flowerstoreproject.adapters.ProductAdapter;
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
    private ProductAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private String categoryId;
    private Button btnBack; // Thêm biến nút quay về

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        productListView = findViewById(R.id.productListView);
        btnBack = findViewById(R.id.btnBack); // Gán ID cho nút quay về

        // Xử lý sự kiện bấm nút "Quay về"
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay về MainActivity
                Intent intent = new Intent(ProductListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xoá các activity trên MainActivity nếu có
                startActivity(intent);
                finish(); // Đóng ProductListActivity
            }
        });

        // Nhận categoryId từ Intent
        categoryId = getIntent().getStringExtra("categoryId");
        if (categoryId == null) {
            Toast.makeText(this, "Category ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new ProductAdapter(this, R.layout.list_item_product, products);
        productListView.setAdapter(adapter);

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
        adapter.clear();
        adapter.addAll(products);
        adapter.notifyDataSetChanged();
    }
}
